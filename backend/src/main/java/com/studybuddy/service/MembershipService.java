package com.studybuddy.service;

import com.studybuddy.dto.MembershipResponse;
import com.studybuddy.dto.StudyGroupResponse;
import com.studybuddy.entity.Membership;
import com.studybuddy.entity.Student;
import com.studybuddy.entity.StudyGroup;
import com.studybuddy.exception.BadRequestException;
import com.studybuddy.exception.DuplicateResourceException;
import com.studybuddy.exception.ForbiddenException;
import com.studybuddy.exception.GroupFullException;
import com.studybuddy.exception.ResourceNotFoundException;
import com.studybuddy.repository.MembershipRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final StudentRepository studentRepository;

    public MembershipService(MembershipRepository membershipRepository,
                             StudyGroupRepository studyGroupRepository,
                             StudentRepository studentRepository) {
        this.membershipRepository = membershipRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * COMPULSORY FEATURE 2: JOIN GROUP
     * Enforces:
     * 1. Student existence check.
     * 2. Group existence check with pessimistic locking.
     * 3. Student not already joined check (Rule 2).
     * 4. Available space check (Rule 1).
     * 5. Saves membership record.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public MembershipResponse joinGroup(Long groupId, Long studentId) {
        // 1. Verify student exists
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        // 2. Verify group exists with pessimistic write lock to handle concurrency
        StudyGroup group = studyGroupRepository.findByIdWithPessimisticLock(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found with ID: " + groupId));

        // 3. Check whether student is already a member
        if (membershipRepository.existsByStudentIdAndStudyGroupId(studentId, groupId)) {
            throw new DuplicateResourceException("Student is already a member of this study group");
        }

        // 4. Check whether group has available space
        long currentCount = membershipRepository.countByStudyGroupId(groupId);
        if (currentCount >= group.getMaxMembers()) {
            throw new GroupFullException("Cannot join: Study group is full (Capacity: " + group.getMaxMembers() + ")");
        }

        // 5. Create and save membership
        Membership membership = new Membership(student, group);
        Membership saved = membershipRepository.save(membership);

        return MembershipResponse.fromEntity(saved);
    }

    /**
     * COMPULSORY FEATURE 3: LEAVE GROUP
     * Enforces:
     * 1. Verify student and group exist.
     * 2. Verify student is actually a member.
     * 3. Handle creator departure: creator cannot abandon group without transferring ownership.
     * 4. Removes membership record.
     */
    @Transactional
    public void leaveGroup(Long groupId, Long studentId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found with ID: " + groupId));

        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }

        Membership membership = membershipRepository.findByStudentIdAndStudyGroupId(studentId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Student is not a member of this study group"));

        // Creator departure rule: Require creator to transfer ownership before leaving
        if (group.getCreator().getId().equals(studentId)) {
            throw new BadRequestException("Group creator cannot leave the group directly. Please transfer creator ownership to another member before leaving.");
        }

        membershipRepository.delete(membership);
    }

    /**
     * COMPULSORY FEATURE 5: REMOVE MEMBER
     * Enforces:
     * 1. Verify group exists.
     * 2. Verify requesting student is the creator (reject unauthorized removal attempts).
     * 3. Creator must not remove themselves through this endpoint.
     * 4. Verify target student is a member.
     * 5. Remove selected membership from MySQL.
     */
    @Transactional
    public void removeMember(Long groupId, Long memberStudentId, Long requestingStudentId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found with ID: " + groupId));

        // Verify that requesting student is the group creator
        if (!group.getCreator().getId().equals(requestingStudentId)) {
            throw new ForbiddenException("Unauthorized: Only the group creator can remove members");
        }

        // Creator must not remove themselves through this endpoint
        if (group.getCreator().getId().equals(memberStudentId)) {
            throw new BadRequestException("The group creator cannot be removed through this endpoint");
        }

        Membership membership = membershipRepository.findByStudentIdAndStudyGroupId(memberStudentId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID " + memberStudentId + " is not a member of this group"));

        membershipRepository.delete(membership);
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> getGroupMembers(Long groupId) {
        if (!studyGroupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Study group not found with ID: " + groupId);
        }

        return membershipRepository.findByStudyGroupId(groupId).stream()
                .map(MembershipResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudyGroupResponse> getJoinedGroupsByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }

        List<Membership> memberships = membershipRepository.findByStudentId(studentId);
        return memberships.stream()
                .map(m -> {
                    StudyGroup group = m.getStudyGroup();
                    long count = membershipRepository.countByStudyGroupId(group.getId());
                    return StudyGroupResponse.fromEntity(group, count);
                })
                .collect(Collectors.toList());
    }
}
