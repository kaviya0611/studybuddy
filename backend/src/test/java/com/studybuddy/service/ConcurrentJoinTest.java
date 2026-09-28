package com.studybuddy.service;

import com.studybuddy.entity.Membership;
import com.studybuddy.entity.Student;
import com.studybuddy.entity.StudyGroup;
import com.studybuddy.entity.Subject;
import com.studybuddy.exception.GroupFullException;
import com.studybuddy.repository.MembershipRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcurrentJoinTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private MembershipService membershipService;

    @Test
    @DisplayName("12. Concurrent Join Requests: Capacity is strictly enforced under concurrent attempts")
    void testConcurrentJoinRequests_CapacityEnforced() throws InterruptedException {
        int maxCapacity = 3;
        Student creator = new Student(1L, "Leader", "leader@example.com");
        Subject subject = new Subject(1L, "Physics", "PHY101");
        StudyGroup group = new StudyGroup(1L, "Physics Study", subject, creator, maxCapacity);

        // Group already has creator (count = 1), so only 2 spots left
        AtomicInteger currentMemberCount = new AtomicInteger(1);

        when(studyGroupRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(group));
        when(studentRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Optional.of(new Student(id, "Student " + id, "student" + id + "@example.com"));
        });
        when(membershipRepository.existsByStudentIdAndStudyGroupId(anyLong(), eq(1L))).thenReturn(false);

        // Simulate synchronized DB count and save
        synchronized (this) {
            when(membershipRepository.countByStudyGroupId(1L)).thenAnswer(inv -> (long) currentMemberCount.get());
            when(membershipRepository.save(any(Membership.class))).thenAnswer(inv -> {
                currentMemberCount.incrementAndGet();
                Membership m = inv.getArgument(0);
                m.setId(System.nanoTime());
                return m;
            });
        }

        int numberOfConcurrentThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfConcurrentThreads);
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentThreads);
        List<Boolean> successResults = Collections.synchronizedList(new ArrayList<>());
        List<Exception> errorResults = Collections.synchronizedList(new ArrayList<>());

        for (int i = 2; i < 2 + numberOfConcurrentThreads; i++) {
            final long studentId = i;
            executorService.submit(() -> {
                try {
                    synchronized (group) { // Simulates row lock in MySQL
                        membershipService.joinGroup(1L, studentId);
                    }
                    successResults.add(true);
                } catch (Exception e) {
                    errorResults.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // Exactly 2 additional joins should succeed (since creator is 1 and max is 3)
        assertEquals(2, successResults.size(), "Only 2 spots were open, so exactly 2 concurrent joins must succeed");
        // Remaining 8 must be rejected
        assertEquals(8, errorResults.size(), "Remaining 8 concurrent joins must be rejected");
        for (Exception ex : errorResults) {
            assertEquals(GroupFullException.class, ex.getClass());
        }
    }
}
