package com.studybuddy.dto;

import com.studybuddy.entity.Membership;
import java.time.LocalDateTime;

public class MembershipResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long groupId;
    private String groupName;
    private boolean isCreator;
    private LocalDateTime joinedAt;

    public MembershipResponse() {
    }

    public MembershipResponse(Long id, Long studentId, String studentName, String studentEmail,
                              Long groupId, String groupName, boolean isCreator, LocalDateTime joinedAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.groupId = groupId;
        this.groupName = groupName;
        this.isCreator = isCreator;
        this.joinedAt = joinedAt;
    }

    public static MembershipResponse fromEntity(Membership membership) {
        if (membership == null) return null;
        boolean creatorFlag = false;
        String gName = null;
        Long gId = null;
        if (membership.getStudyGroup() != null) {
            gId = membership.getStudyGroup().getId();
            gName = membership.getStudyGroup().getGroupName();
            if (membership.getStudyGroup().getCreator() != null && membership.getStudent() != null) {
                creatorFlag = membership.getStudyGroup().getCreator().getId().equals(membership.getStudent().getId());
            }
        }
        return new MembershipResponse(
                membership.getId(),
                membership.getStudent() != null ? membership.getStudent().getId() : null,
                membership.getStudent() != null ? membership.getStudent().getName() : null,
                membership.getStudent() != null ? membership.getStudent().getEmail() : null,
                gId,
                gName,
                creatorFlag,
                membership.getJoinedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isCreator() {
        return isCreator;
    }

    public void setCreator(boolean creator) {
        isCreator = creator;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
