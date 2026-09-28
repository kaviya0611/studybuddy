package com.studybuddy.repository;

import com.studybuddy.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByStudyGroupId(Long groupId);
    List<Membership> findByStudenName(String name);
    List<Membership> findByStudentId(Long studentId);
    Optional<Membership> findByStudentIdAndStudyGroupId(Long studentId, Long groupId);
    boolean existsByStudentIdAndStudyGroupId(Long studentId, Long groupId);
    long countByStudyGroupId(Long groupId);
    void deleteByStudentIdAndStudyGroupId(Long studentId, Long groupId);
}
