# StudyBuddy – Student Study Group Formation Tool
**Full-Stack College Assessment Project**

StudyBuddy is a complete, production-ready, full-stack college assessment web application designed to help students form, discover, join, and manage subject-wise study groups with strictly enforced capacity caps and business rules.

---

## Quick Navigation Links
- [Detailed Project Documentation](file:///c:/Users/KAVYA/Downloads/studybuddy/documentation/README.md)
- [Database Schema (SQL Script)](file:///c:/Users/KAVYA/Downloads/studybuddy/database/studybuddy.sql)
- [Entity-Relationship (ER) Diagram](file:///c:/Users/KAVYA/Downloads/studybuddy/documentation/ER_Diagram.md)
- [UML Class Diagram & Architecture](file:///c:/Users/KAVYA/Downloads/studybuddy/documentation/UML_Class_Diagram.md)
- [REST API Documentation](file:///c:/Users/KAVYA/Downloads/studybuddy/documentation/API_Documentation.md)
- [Postman Collection](file:///c:/Users/KAVYA/Downloads/studybuddy/documentation/StudyBuddy.postman_collection.json)

---

## 5 Compulsory Features Overview

| Feature | Endpoint | Description |
|---|---|---|
| **1. Create Group** | `POST /api/groups` | Allows student to create a subject-wise study group with configurable capacity (`> 0`). Creator is automatically enrolled as 1st member. |
| **2. Join Group** | `POST /api/groups/{id}/join` | Allows student to join an open group. Enforces Rule 1 (Capacity) & Rule 2 (No duplicate joins) with pessimistic database locks. |
| **3. Leave Group** | `DELETE /api/groups/{id}/leave?studentId={id}` | Allows student to leave a group. Group creator cannot leave without transferring ownership, avoiding orphaned groups. |
| **4. View Groups by Subject** | `GET /api/groups?subjectId={id}` | Filters groups by curriculum subject, displaying member counts, capacity, and available spaces. |
| **5. Remove Member** | `DELETE /api/groups/{id}/members/{studentId}` | Creator-only operation using server-side verified auth token. Non-creators receive `403 Forbidden`. |

---

## Technology Stack
- **Backend**: Java 17, Spring Boot 3.3.4, Spring Data JPA, Hibernate, Jakarta Bean Validation, Maven
- **Database**: MySQL 8.0 Server
- **Frontend**: React 19, Vite, Axios, Vanilla CSS (Professional Blue & Purple Palette)
- **Testing**: JUnit 5 Jupiter, Mockito (15/15 automated tests passing)

---

## Running the Application

### 1. Database
Make sure MySQL is running on port 3306. The database is initialized via:
```bash
mysql -u root -p < database/studybuddy.sql
```
*(Default password is `6112008`, or set `DB_PASSWORD` env variable)*

### 2. Backend
```bash
cd backend
mvn clean compile
mvn test
mvn spring-boot:run
```
Backend runs on `http://localhost:8080`.

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:5173`.
