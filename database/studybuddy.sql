-- ===================================================
-- StudyBuddy – Student Study Group Formation Tool
-- Database Initialization Script
-- MySQL 8.0+ Compatible
-- ===================================================

CREATE DATABASE IF NOT EXISTS `studybuddy` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `studybuddy`;

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS `memberships`;
DROP TABLE IF EXISTS `study_groups`;
DROP TABLE IF EXISTS `subjects`;
DROP TABLE IF EXISTS `students`;

-- ---------------------------------------------------
-- 1. STUDENT TABLE
-- ---------------------------------------------------
CREATE TABLE `students` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_students_email` UNIQUE (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------
-- 2. SUBJECT TABLE
-- ---------------------------------------------------
CREATE TABLE `subjects` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `subject_name` VARCHAR(100) NOT NULL,
    `subject_code` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_subjects_code` UNIQUE (`subject_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------
-- 3. STUDY GROUP TABLE
-- ---------------------------------------------------
CREATE TABLE `study_groups` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `group_name` VARCHAR(100) NOT NULL,
    `subject_id` BIGINT NOT NULL,
    `creator_id` BIGINT NOT NULL,
    `max_members` INT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_groups_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_groups_creator` FOREIGN KEY (`creator_id`) REFERENCES `students` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------
-- 4. MEMBERSHIP TABLE
-- ---------------------------------------------------
CREATE TABLE `memberships` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `student_id` BIGINT NOT NULL,
    `group_id` BIGINT NOT NULL,
    `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_memberships_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_memberships_group` FOREIGN KEY (`group_id`) REFERENCES `study_groups` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `uk_membership_student_group` UNIQUE (`student_id`, `group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------
-- SEED DATA FOR TESTING & ASSESSMENT DEMONSTRATION
-- ---------------------------------------------------

-- Sample Students
INSERT INTO `students` (`id`, `name`, `email`) VALUES
(1, 'Alex Johnson', 'alex.johnson@university.edu'),
(2, 'Beatrice Vance', 'beatrice.vance@university.edu'),
(3, 'Carlos Mendez', 'carlos.mendez@university.edu'),
(4, 'Diana Prince', 'diana.prince@university.edu'),
(5, 'Ethan Hunt', 'ethan.hunt@university.edu');

-- Sample Subjects
INSERT INTO `subjects` (`id`, `subject_name`, `subject_code`) VALUES
(1, 'Computer Networks', 'CS401'),
(2, 'Database Management Systems', 'CS402'),
(3, 'Data Structures & Algorithms', 'CS201'),
(4, 'Operating Systems', 'CS301'),
(5, 'Artificial Intelligence', 'CS501');

-- Sample Study Groups
INSERT INTO `study_groups` (`id`, `group_name`, `subject_id`, `creator_id`, `max_members`, `created_at`) VALUES
(1, 'Network Protocols Study Circle', 1, 1, 4, NOW()),
(2, 'SQL Optimization & Indexing', 2, 2, 3, NOW()),
(3, 'Algorithms & LeetCode Prep', 3, 3, 5, NOW());

-- Sample Memberships (Creator is automatically 1st member)
INSERT INTO `memberships` (`id`, `student_id`, `group_id`, `joined_at`) VALUES
(1, 1, 1, NOW()), -- Alex (Creator) in Network Protocols
(2, 2, 1, NOW()), -- Beatrice in Network Protocols
(3, 2, 2, NOW()), -- Beatrice (Creator) in SQL Optimization
(4, 3, 2, NOW()), -- Carlos in SQL Optimization
(5, 3, 3, NOW()), -- Carlos (Creator) in Algorithms
(6, 4, 3, NOW()); -- Diana in Algorithms
