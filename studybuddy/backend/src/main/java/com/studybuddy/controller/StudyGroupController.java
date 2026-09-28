package com.studybuddy.controller;

import com.studybuddy.dto.ApiResponse;
import com.studybuddy.dto.CreateGroupRequest;
import com.studybuddy.dto.StudyGroupResponse;
import com.studybuddy.dto.TransferCreatorRequest;
import com.studybuddy.service.AuthService;
import com.studybuddy.service.StudyGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;
    private final AuthService authService;

    public StudyGroupController(StudyGroupService studyGroupService, AuthService authService) {
        this.studyGroupService = studyGroupService;
        this.authService = authService;
    }

    /**
     * COMPULSORY FEATURE 1: CREATE GROUP
     * Returns HTTP 201 Created after successful creation.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StudyGroupResponse>> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        StudyGroupResponse created = studyGroupService.createGroup(request);
        return new ResponseEntity<>(
                ApiResponse.success("Study group created successfully", created),
                HttpStatus.CREATED
        );
    }

    /**
     * COMPULSORY FEATURE 4: VIEW GROUPS BY SUBJECT
     * If subjectId is supplied, filters groups by subject; otherwise returns all groups.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudyGroupResponse>>> getGroups(
            @RequestParam(required = false) Long subjectId) {
        List<StudyGroupResponse> list;
        if (subjectId != null) {
            list = studyGroupService.getGroupsBySubject(subjectId);
            return ResponseEntity.ok(ApiResponse.success("Groups for subject retrieved successfully", list));
        } else {
            list = studyGroupService.getAllGroups();
            return ResponseEntity.ok(ApiResponse.success("All groups retrieved successfully", list));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudyGroupResponse>> getGroupById(@PathVariable Long id) {
        StudyGroupResponse group = studyGroupService.getGroupById(id);
        return ResponseEntity.ok(ApiResponse.success("Study group retrieved successfully", group));
    }

    @PutMapping("/{id}/transfer-creator")
    public ResponseEntity<ApiResponse<StudyGroupResponse>> transferCreator(
            @PathVariable Long id,
            @Valid @RequestBody TransferCreatorRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Auth-Token", required = false) String xAuthToken) {

        String token = authHeader != null ? authHeader : xAuthToken;
        Long requestingStudentId = authService.getAuthenticatedStudentId(token);

        StudyGroupResponse updated = studyGroupService.transferCreator(id, request.getNewCreatorId(), requestingStudentId);
        return ResponseEntity.ok(ApiResponse.success("Creator ownership transferred successfully", updated));
    }
}
