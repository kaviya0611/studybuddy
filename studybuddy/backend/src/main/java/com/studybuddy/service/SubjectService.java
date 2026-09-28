package com.studybuddy.service;

import com.studybuddy.dto.SubjectRequest;
import com.studybuddy.dto.SubjectResponse;
import com.studybuddy.entity.Subject;
import com.studybuddy.exception.DuplicateResourceException;
import com.studybuddy.exception.ResourceNotFoundException;
import com.studybuddy.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        String codeUpper = request.getSubjectCode().trim().toUpperCase();
        if (subjectRepository.existsBySubjectCode(codeUpper)) {
            throw new DuplicateResourceException("Subject code already exists: " + codeUpper);
        }

        Subject subject = new Subject(request.getSubjectName().trim(), codeUpper);
        Subject saved = subjectRepository.save(subject);
        return SubjectResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(SubjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {
        Subject subject = getSubjectEntity(id);
        return SubjectResponse.fromEntity(subject);
    }

    @Transactional(readOnly = true)
    public Subject getSubjectEntity(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + id));
    }
}
