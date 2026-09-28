package com.studybuddy.service;

import com.studybuddy.dto.CreateGroupRequest;
import com.studybuddy.dto.StudyGroupResponse;
import com.studybuddy.entity.Membership;
import com.studybuddy.entity.Student;
import com.studybuddy.entity.StudyGroup;
import com.studybuddy.entity.Subject;
import com.studybuddy.exception.BadRequestException;
import com.studybuddy.exception.ResourceNotFoundException;
import com.studybuddy.repository.MembershipRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;
import com.studybuddy.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @InjectMocks
    private StudyGroupService studyGroupService;

    private Student creator;
    private Subject subject;
    private StudyGroup group;

    @BeforeEach
    void setUp() {
        creator = new Student(1L, "Alice Brown", "alice@example.com");
        subject = new Subject(1L, "Mathematics", "MATH101");
        group = new StudyGroup(1L, "Calculus Study Group", subject, creator, 4);
        group.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("1. Successful Group Creation: Creator is auto-added as member")
    void testCreateGroup_Success() {
        CreateGroupRequest request = new CreateGroupRequest("Calculus Study Group", 1L, 1L, 4);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(group);
        when(membershipRepository.save(any(Membership.class))).thenReturn(new Membership(1L, creator, group));

        StudyGroupResponse response = studyGroupService.createGroup(request);

        assertNotNull(response);
        assertEquals("Calculus Study Group", response.getGroupName());
        assertEquals(4, response.getMaxMembers());
        assertEquals(1L, response.getCreatorId());
        assertEquals(1L, response.getCurrentMemberCount());

        verify(membershipRepository, times(1)).save(any(Membership.class));
    }

    @Test
    @DisplayName("4. Viewing Groups by Subject: Retrieves groups for existing subject")
    void testGetGroupsBySubject_Success() {
        when(subjectRepository.existsById(1L)).thenReturn(true);
        when(studyGroupRepository.findBySubjectId(1L)).thenReturn(List.of(group));
        when(membershipRepository.countByStudyGroupId(1L)).thenReturn(2L);

        List<StudyGroupResponse> list = studyGroupService.getGroupsBySubject(1L);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Calculus Study Group", list.get(0).getGroupName());
        assertEquals(2L, list.get(0).getCurrentMemberCount());
        assertEquals(2L, list.get(0).getAvailableSpaces()); // 4 - 2 = 2
    }

    @Test
    @DisplayName("4b. Viewing Groups by Subject: Empty list when no groups exist")
    void testGetGroupsBySubject_Empty() {
        when(subjectRepository.existsById(2L)).thenReturn(true);
        when(studyGroupRepository.findBySubjectId(2L)).thenReturn(Collections.emptyList());

        List<StudyGroupResponse> list = studyGroupService.getGroupsBySubject(2L);

        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("9. Invalid Student: Fails to create group when student does not exist")
    void testCreateGroup_InvalidStudent_ThrowsException() {
        CreateGroupRequest request = new CreateGroupRequest("Algorithms Study Group", 1L, 999L, 4);

        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studyGroupService.createGroup(request));
        verify(studyGroupRepository, never()).save(any());
    }

    @Test
    @DisplayName("10. Invalid Group: Throws exception when retrieving non-existent group")
    void testGetGroupById_InvalidGroup_ThrowsException() {
        when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studyGroupService.getGroupById(999L));
    }

    @Test
    @DisplayName("11. Invalid Maximum Capacity: Zero or negative capacity rejected")
    void testCreateGroup_InvalidCapacity_ThrowsException() {
        CreateGroupRequest zeroCapacity = new CreateGroupRequest("Zero Cap Group", 1L, 1L, 0);
        assertThrows(BadRequestException.class, () -> studyGroupService.createGroup(zeroCapacity));

        CreateGroupRequest negCapacity = new CreateGroupRequest("Negative Cap Group", 1L, 1L, -5);
        assertThrows(BadRequestException.class, () -> studyGroupService.createGroup(negCapacity));

        verify(studyGroupRepository, never()).save(any());
    }
}
