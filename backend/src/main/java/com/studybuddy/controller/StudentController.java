package com.studybuddy.controller;

import com.studybuddy.dto.ApiResponse;
import com.studybuddy.dto.StudentRequest;
import com.studybuddy.dto.StudentResponse;
import com.studybuddy.dto.StudyGroupResponse;
import com.studybuddy.service.MembershipService;
import com.studybuddy.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;
    private final MembershipService membershipService;

    public StudentController(StudentService studentService, MembershipService membershipService) {
        this.studentService = studentService;
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse created = studentService.createStudent(request);
        return new ResponseEntity<>(
                ApiResponse.success("Student created successfully", created),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents() {
        List<StudentResponse> list = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        StudentResponse student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success("Student retrieved successfully", student));
    }

    @GetMapping("/{id}/groups")
    public ResponseEntity<ApiResponse<List<StudyGroupResponse>>> getJoinedGroups(@PathVariable Long id) {
        List<StudyGroupResponse> groups = membershipService.getJoinedGroupsByStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Joined groups retrieved successfully", groups));
    }
}
