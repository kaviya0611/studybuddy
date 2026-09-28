package com.studybuddy.service;

import com.studybuddy.dto.StudentRequest;
import com.studybuddy.dto.StudentResponse;
import com.studybuddy.entity.Student;
import com.studybuddy.exception.DuplicateResourceException;
import com.studybuddy.exception.ResourceNotFoundException;
import com.studybuddy.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Student with email already exists: " + request.getEmail());
        }

        Student student = new Student(request.getName().trim(), request.getEmail().trim().toLowerCase());
        Student saved = studentRepository.save(student);
        return StudentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = getStudentEntity(id);
        return StudentResponse.fromEntity(student);
    }

    @Transactional(readOnly = true)
    public Student getStudentEntity(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
    }
}
