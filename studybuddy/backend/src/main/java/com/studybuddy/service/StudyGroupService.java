package com.studybuddy.service;

import com.studybuddy.dto.CreateGroupRequest;
import com.studybuddy.dto.StudyGroupResponse;
import com.studybuddy.entity.Membership;
import com.studybuddy.entity.Student;
import com.studybuddy.entity.StudyGroup;
import com.studybuddy.entity.Subject;
import com.studybuddy.exception.BadRequestException;
import com.studybuddy.exception.ForbiddenException;
import com.studybuddy.exception.ResourceNotFoundException;
import com.studybuddy.repository.MembershipRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;
import com.studybuddy.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final MembershipRepository membershipRepository;

    public StudyGroupService(StudyGroupRepository studyGroupRepository,
                             StudentRepository studentRepository,
                             SubjectRepository subjectRepository,
                             MembershipRepository membershipRepository) {
        this.studyGroupRepository = studyGroupRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.membershipRepository = membershipRepository;
    }

    /**
     * COMPULSORY FEATURE 1: CREATE GROUP
     * Validates input fields, checks student and subject existence, validates capacity > 0,
     * saves group in MySQL, and automatically adds the creator as the first member.
     */
    @Transactional
    public StudyGroupResponse createGroup(CreateGroupRequest request) {
        if (request.getMaxMembers() == null || request.getMaxMembers() <= 0) {
            throw new BadRequestException("Maximum member capacity must be a positive number greater than 0");
        }

        Student creator = studentRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator student not found with ID: " + request.getCreatorId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + request.getSubjectId()));

        StudyGroup group = new StudyGroup(
                request.getGroupName().trim(),
                subject,
                creator,
                request.getMaxMembers()
        );

        StudyGroup savedGroup = studyGroupRepository.save(group);

        // Automatically add the creator as the first member
        Membership creatorMembership = new Membership(creator, savedGroup);
        membershipRepository.save(creatorMembership);

        return StudyGroupResponse.fromEntity(savedGroup, 1);
    }

    @Transactional(readOnly = true)
    public List<StudyGroupResponse> getAllGroups() {
        return studyGroupRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudyGroupResponse getGroupById(Long id) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found with ID: " + id));
        return mapToResponse(group);
    }

    /**
     * COMPULSORY FEATURE 4: VIEW GROUPS BY SUBJECT
     * Retrieves groups by subject, calculates member count and available spaces.
     */
    @Transactional(readOnly = true)
    public List<StudyGroupResponse> getGroupsBySubject(Long subjectId) {
        // Verify subject exists
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject not found with ID: " + subjectId);
        }

        List<StudyGroup> groups = studyGroupRepository.findBySubjectId(subjectId);
        return groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper to transfer creator ownership so a creator can safely leave a group.
     */
    @Transactional
    public StudyGroupResponse transferCreator(Long groupId, Long newCreatorId, Long requestingStudentId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Study group not found with ID: " + groupId));

        if (!group.getCreator().getId().equals(requestingStudentId)) {
            throw new ForbiddenException("Only the current group creator can transfer creator ownership");
        }

        if (group.getCreator().getId().equals(newCreatorId)) {
            throw new BadRequestException("Student is already the group creator");
        }

        Student newCreator = studentRepository.findById(newCreatorId)
                .orElseThrow(() -> new ResourceNotFoundException("New creator student not found with ID: " + newCreatorId));

        // Must be a member of the group
        if (!membershipRepository.existsByStudentIdAndStudyGroupId(newCreatorId, groupId)) {
            throw new BadRequestException("New creator must already be a member of the group");
        }

        group.setCreator(newCreator);
        StudyGroup updated = studyGroupRepository.save(group);
        return mapToResponse(updated);
    }

    private StudyGroupResponse mapToResponse(StudyGroup group) {
        long memberCount = membershipRepository.countByStudyGroupId(group.getId());
        return StudyGroupResponse.fromEntity(group, memberCount);
    }
}
