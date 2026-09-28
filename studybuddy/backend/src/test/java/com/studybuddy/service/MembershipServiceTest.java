package com.studybuddy.service;

import com.studybuddy.dto.MembershipResponse;
import com.studybuddy.entity.Membership;
import com.studybuddy.entity.Student;
import com.studybuddy.entity.StudyGroup;
import com.studybuddy.entity.Subject;
import com.studybuddy.exception.BadRequestException;
import com.studybuddy.exception.DuplicateResourceException;
import com.studybuddy.exception.ForbiddenException;
import com.studybuddy.exception.GroupFullException;
import com.studybuddy.exception.ResourceNotFoundException;
import com.studybuddy.repository.MembershipRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private MembershipService membershipService;

    private Student creator;
    private Student student2;
    private Student student3;
    private Subject subject;
    private StudyGroup group;

    @BeforeEach
    void setUp() {
        creator = new Student(1L, "Alice Brown", "alice@example.com");
        student2 = new Student(2L, "Bob Smith", "bob@example.com");
        student3 = new Student(3L, "Charlie Green", "charlie@example.com");

        subject = new Subject(1L, "Database Systems", "CS302");
        group = new StudyGroup(1L, "DB Study Group", subject, creator, 2); // Max capacity is 2
        group.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("2. Successful Group Joining: Student can join if space is available")
    void testJoinGroup_Success() {
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        when(studyGroupRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(group));
        when(membershipRepository.existsByStudentIdAndStudyGroupId(2L, 1L)).thenReturn(false);
        when(membershipRepository.countByStudyGroupId(1L)).thenReturn(1L); // 1 member currently, max is 2

        Membership savedMembership = new Membership(2L, student2, group);
        when(membershipRepository.save(any(Membership.class))).thenReturn(savedMembership);

        MembershipResponse response = membershipService.joinGroup(1L, 2L);

        assertNotNull(response);
        assertEquals(2L, response.getStudentId());
        assertEquals(1L, response.getGroupId());
        verify(membershipRepository, times(1)).save(any(Membership.class));
    }

    @Test
    @DisplayName("6. Duplicate Join Rejection: Rejects joining if already a member")
    void testJoinGroup_Duplicate_ThrowsException() {
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        when(studyGroupRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(group));
        when(membershipRepository.existsByStudentIdAndStudyGroupId(2L, 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> membershipService.joinGroup(1L, 2L));
        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("7. Full Group Rejection: Rejects joining if group has reached capacity")
    void testJoinGroup_Full_ThrowsException() {
        when(studentRepository.findById(3L)).thenReturn(Optional.of(student3));
        when(studyGroupRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(group));
        when(membershipRepository.existsByStudentIdAndStudyGroupId(3L, 1L)).thenReturn(false);
        when(membershipRepository.countByStudyGroupId(1L)).thenReturn(2L); // 2 members, max is 2

        assertThrows(GroupFullException.class, () -> membershipService.joinGroup(1L, 3L));
        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("3. Successful Group Leaving: Student can leave group")
    void testLeaveGroup_Success() {
        Membership membership = new Membership(2L, student2, group);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(studentRepository.existsById(2L)).thenReturn(true);
        when(membershipRepository.findByStudentIdAndStudyGroupId(2L, 1L)).thenReturn(Optional.of(membership));

        membershipService.leaveGroup(1L, 2L);

        verify(membershipRepository, times(1)).delete(membership);
    }

    @Test
    @DisplayName("3b. Creator Cannot Leave Directly: Must transfer creator ownership")
    void testLeaveGroup_CreatorDeparture_ThrowsException() {
        Membership creatorMembership = new Membership(1L, creator, group);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(membershipRepository.findByStudentIdAndStudyGroupId(1L, 1L)).thenReturn(Optional.of(creatorMembership));

        assertThrows(BadRequestException.class, () -> membershipService.leaveGroup(1L, 1L));
        verify(membershipRepository, never()).delete(any());
    }

    @Test
    @DisplayName("5. Creator Removing a Member: Creator can successfully remove a member")
    void testRemoveMember_Success() {
        Membership membership = new Membership(2L, student2, group);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(membershipRepository.findByStudentIdAndStudyGroupId(2L, 1L)).thenReturn(Optional.of(membership));

        // Requesting student is creator (ID 1)
        membershipService.removeMember(1L, 2L, 1L);

        verify(membershipRepository, times(1)).delete(membership);
    }

    @Test
    @DisplayName("8. Non-Creator Removal Rejection: Non-creator cannot remove a member")
    void testRemoveMember_NonCreator_ThrowsForbiddenException() {
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(group));

        // Requesting student is student2 (ID 2), creator is ID 1
        assertThrows(ForbiddenException.class, () -> membershipService.removeMember(1L, 3L, 2L));
        verify(membershipRepository, never()).delete(any());
    }

    @Test
    @DisplayName("5b. Creator Cannot Remove Themselves: Rejected through remove member endpoint")
    void testRemoveMember_CreatorRemovesSelf_ThrowsException() {
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(BadRequestException.class, () -> membershipService.removeMember(1L, 1L, 1L));
        verify(membershipRepository, never()).delete(any());
    }
}
