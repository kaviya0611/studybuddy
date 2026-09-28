# StudyBuddy – Student Study Group Formation Tool
**College Assessment Project Documentation**

---

## 1. Project Title & Overview
**Project Title**: StudyBuddy – Student Study Group Formation Tool  
**Architecture**: 4-Tier Enterprise Architecture (Controller → Service → Repository → MySQL)  
**Target Environment**: Java 17, Spring Boot 3.3.4, React 19 (Vite), MySQL 8.0  

### 1.1 Objective & Problem Statement
Students preparing for university exams often seek small, focused study groups tailored by subject. However, coordinating across informal chat channels leads to scheduling confusion, untracked group sizes, duplicate entries, and lack of clarity on who is genuinely committed. 

### 1.2 Proposed Solution
StudyBuddy provides an automated, centralized web portal allowing students to create, join, leave, filter, and moderate subject-wise study groups. Configurable maximum member capacities are strictly enforced in the service layer using database pessimistic write locks to prevent over-enrollment even under concurrent traffic spikes.

---

## 2. Technology Stack

| Layer | Technologies Used | Description |
|---|---|---|
| **Backend** | Java 17 (LTS), Spring Boot 3.3.4 | Core framework, Spring Web, Dependency Injection |
| **Persistence** | Spring Data JPA, Hibernate ORM | Relational mapping, pessimistic locks, declarative transactions |
| **Validation** | Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@Email`, `@Min`) | Declarative input sanitization & boundary checking |
| **Database** | MySQL Server 8.0 | Relational database with InnoDB engine, foreign keys & unique constraints |
| **Frontend** | React 19, Vite, Vanilla CSS, Axios | Simple, clean, and colorful UI (Blue, Purple & White) |
| **Testing** | JUnit 5 Jupiter, Mockito, Surefire | Service-layer unit tests, concurrency latch tests, Postman collection |

---

## 3. Project Directory Structure
```
StudyBuddy/
├── backend/
│   ├── src/main/java/com/studybuddy/
│   │   ├── config/              # CorsConfig, DataInitializer
│   │   ├── controller/          # StudentController, SubjectController, StudyGroupController, MembershipController, AuthController
│   │   ├── dto/                 # Request & Response Data Transfer Objects
│   │   ├── entity/              # Student, Subject, StudyGroup, Membership
│   │   ├── exception/           # Custom domain exceptions & GlobalExceptionHandler
│   │   ├── repository/          # JpaRepositories with locking queries
│   │   ├── service/             # Business rules, transactions, auth verification
│   │   └── StudyBuddyApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── src/test/java/com/studybuddy/service/
│   │   ├── ConcurrentJoinTest.java
│   │   ├── MembershipServiceTest.java
│   │   └── StudyGroupServiceTest.java
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/          # AlertMessage, Navbar
│   │   ├── pages/               # 5 Compulsory Feature pages
│   │   │   ├── CreateGroup.jsx
│   │   │   ├── JoinGroup.jsx
│   │   │   ├── LeaveGroup.jsx
│   │   │   ├── ViewGroupsBySubject.jsx
│   │   │   └── RemoveMember.jsx
│   │   ├── services/            # api.js Axios client
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   ├── index.html
│   └── package.json
├── database/
│   └── studybuddy.sql           # Schema definition & seed data
└── documentation/
    ├── ER_Diagram.md            # Detailed ER diagram & table specifications
    ├── UML_Class_Diagram.md     # UML class diagrams & component design
    ├── API_Documentation.md     # Full REST API contracts & sample payloads
    ├── StudyBuddy.postman_collection.json # Ready-to-import Postman collection
    └── README.md
```

---

## 4. Database Design & Relational Model

The database contains exactly four entities:

1. **`students`**: Stores student identity.
   - `id` (PK, Auto Increment)
   - `name` (VARCHAR 100, NOT NULL)
   - `email` (VARCHAR 150, NOT NULL, UNIQUE)
2. **`subjects`**: Academic courses.
   - `id` (PK, Auto Increment)
   - `subject_name` (VARCHAR 100, NOT NULL)
   - `subject_code` (VARCHAR 50, NOT NULL, UNIQUE)
3. **`study_groups`**: Subject-wise study circles.
   - `id` (PK, Auto Increment)
   - `group_name` (VARCHAR 100, NOT NULL)
   - `subject_id` (FK -> `subjects(id)`, NOT NULL)
   - `creator_id` (FK -> `students(id)`, NOT NULL)
   - `max_members` (INT, NOT NULL, Positive)
   - `created_at` (DATETIME, NOT NULL)
4. **`memberships`**: Student group enrollments.
   - `id` (PK, Auto Increment)
   - `student_id` (FK -> `students(id)`, NOT NULL)
   - `group_id` (FK -> `study_groups(id)`, NOT NULL)
   - `joined_at` (DATETIME, NOT NULL)
   - Unique Constraint: `(student_id, group_id)`

---

## 5. The Five Compulsory Features

### Feature 1: Create Group
- **Interface**: Clean form specifying Group Name, Subject dropdown, Student Creator dropdown, and Maximum Member Capacity.
- **Backend Flow**:
  - Validates positive capacity (`maxMembers > 0`) and required fields.
  - Verifies student and subject existence.
  - Inserts the group into MySQL.
  - **Automatically adds the creator as the first member**.
  - Returns `HTTP 201 Created`.

### Feature 2: Join Group
- **Interface**: Group card grid showing Group Name, Subject, occupancy progress bar, available slots, and a Join button.
- **Backend Flow**:
  - Employs `@Transactional` and `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`) on the StudyGroup row.
  - **Rule 1 Enforcement**: Validates that current member count < maxMembers. Rejects with `409 Conflict` (`GroupFullException`) if full.
  - **Rule 2 Enforcement**: Validates that student is not already a member. Rejects with `409 Conflict` (`DuplicateResourceException`) if already joined.
  - Persists new Membership record upon passing all checks.

### Feature 3: Leave Group
- **Interface**: Displays all groups joined by the currently active student with a Leave Group button.
- **Backend Flow**:
  - Validates student is enrolled.
  - **Creator Departure Guard**: Enforces that the creator cannot leave without transferring ownership, preventing orphaned groups.
  - Built-in Transfer Creator workflow allows designating a new creator before departure.
  - Removes membership record and updates member count automatically.

### Feature 4: View Groups by Subject
- **Interface**: Subject dropdown filter.
- **Backend Flow**:
  - Queries `GET /api/groups?subjectId={id}`.
  - Calculates current member count and available spaces (`maxMembers - currentMemberCount`).
  - Displays formatted card/table or a user-friendly empty state when no groups exist for the selected subject.

### Feature 5: Remove Member
- **Interface**: Member list for any selected group. The Remove Member action button is rendered **only** when the logged-in student is the verified creator.
- **Backend Flow**:
  - Secure server-side identity verification via `AuthService` tokens (`Authorization: Bearer <token>`).
  - Frontend creator claim is never blindly trusted.
  - Non-creators receive `403 Forbidden` (`Unauthorized: Only the group creator can remove members`).
  - Creator cannot remove themselves (`400 Bad Request`).
  - Removes target membership from MySQL.

---

## 6. Testing & Quality Verification

### 6.1 Automated Test Execution Summary
15 comprehensive automated JUnit 5 & Mockito test cases executed:
- **`ConcurrentJoinTest`**: Verified that 10 concurrent threads competing for 2 remaining slots in a group result in exactly 2 successful joins and 8 rejections with `GroupFullException`.
- **`StudyGroupServiceTest`**: Verified group creation, creator auto-enrollment, subject filtering, empty subject handling, invalid student, invalid group, and non-positive capacity rejection.
- **`MembershipServiceTest`**: Verified join success, duplicate join rejection, capacity limits, leave success, creator departure restriction, creator member removal, and non-creator removal rejection.

Result:
```
Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 7. Execution Instructions

### Prerequisites
- Java 17+ installed (`java -version`)
- Maven 3.8+ installed (`mvn -v`)
- MySQL Server 8.0+ running on port 3306
- Node.js 18+ and npm installed

### 7.1 Database Setup
Run the SQL script using MySQL CLI:
```bash
mysql -u root -p < database/studybuddy.sql
```
*(Enter password when prompted. Default password configured in application is `6112008` or can be overridden via `DB_PASSWORD` environment variable).*

### 7.2 Backend Setup & Execution
```bash
cd backend
mvn clean compile
mvn test
mvn spring-boot:run
```
The Spring Boot backend will start on `http://localhost:8080`.

### 7.3 Frontend Setup & Execution
In a separate terminal:
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.
