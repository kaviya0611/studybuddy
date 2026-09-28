package com.studybuddy.config;

import com.studybuddy.entity.Membership;
import com.studybuddy.entity.Student;
import com.studybuddy.entity.StudyGroup;
import com.studybuddy.entity.Subject;
import com.studybuddy.repository.MembershipRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyGroupRepository;
import com.studybuddy.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final MembershipRepository membershipRepository;

    public DataInitializer(StudentRepository studentRepository,
                           SubjectRepository subjectRepository,
                           StudyGroupRepository studyGroupRepository,
                           MembershipRepository membershipRepository) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() == 0) {
            logger.info("Database empty, initializing seed data...");

            Student s1 = studentRepository.save(new Student("Alex Johnson", "alex.johnson@university.edu"));
            Student s2 = studentRepository.save(new Student("Beatrice Vance", "beatrice.vance@university.edu"));
            Student s3 = studentRepository.save(new Student("Carlos Mendez", "carlos.mendez@university.edu"));
            Student s4 = studentRepository.save(new Student("Diana Prince", "diana.prince@university.edu"));
            Student s5 = studentRepository.save(new Student("Ethan Hunt", "ethan.hunt@university.edu"));

            Subject sub1 = subjectRepository.save(new Subject("Computer Networks", "CS401"));
            Subject sub2 = subjectRepository.save(new Subject("Database Management Systems", "CS402"));
            Subject sub3 = subjectRepository.save(new Subject("Data Structures & Algorithms", "CS201"));
            Subject sub4 = subjectRepository.save(new Subject("Operating Systems", "CS301"));
            Subject sub5 = subjectRepository.save(new Subject("Artificial Intelligence", "CS501"));

            // Groups
            StudyGroup g1 = studyGroupRepository.save(new StudyGroup("Network Protocols Study Circle", sub1, s1, 4));
            membershipRepository.save(new Membership(s1, g1)); // Creator is 1st member
            membershipRepository.save(new Membership(s2, g1)); // Beatrice joined

            StudyGroup g2 = studyGroupRepository.save(new StudyGroup("SQL Optimization & Indexing", sub2, s2, 3));
            membershipRepository.save(new Membership(s2, g2)); // Creator
            membershipRepository.save(new Membership(s3, g2)); // Carlos joined

            StudyGroup g3 = studyGroupRepository.save(new StudyGroup("Algorithms & LeetCode Prep", sub3, s3, 5));
            membershipRepository.save(new Membership(s3, g3)); // Creator
            membershipRepository.save(new Membership(s4, g3)); // Diana joined

            logger.info("Seed data successfully initialized in MySQL!");
        } else {
            logger.info("Database already contains data, skipping seed initialization.");
        }
    }
}
