# StudyBuddy – REST API Documentation

Base URL: `http://localhost:8080/api`

---

## 1. Authentication APIs

### 1.1 Login / Obtain Verified Session Token
- **Method**: `POST`
- **Path**: `/api/auth/login`
- **Request Body**:
  ```json
  {
    "email": "alex.johnson@university.edu"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "Login successful",
    "data": {
      "token": "4f9b8c2d-9861-419b-a1b9-373b9e4a8ef7",
      "student": {
        "id": 1,
        "name": "Alex Johnson",
        "email": "alex.johnson@university.edu"
      },
      "message": "Authentication successful"
    },
    "timestamp": "2026-09-28T10:45:00"
  }
  ```

---

## 2. Student APIs

### 2.1 Create Student
- **Method**: `POST`
- **Path**: `/api/students`
- **Request Body**:
  ```json
  {
    "name": "Frank Miller",
    "email": "frank.miller@university.edu"
  }
  ```
- **Response**: `201 Created`

### 2.2 Get All Students
- **Method**: `GET`
- **Path**: `/api/students`
- **Response**: `200 OK` (Array of students)

### 2.3 Get Student by ID
- **Method**: `GET`
- **Path**: `/api/students/{id}`
- **Response**: `200 OK`

### 2.4 Get Joined Groups for Student
- **Method**: `GET`
- **Path**: `/api/students/{id}/groups`
- **Response**: `200 OK` (Array of study group objects joined by student)

---

## 3. Subject APIs

### 3.1 Create Subject
- **Method**: `POST`
- **Path**: `/api/subjects`
- **Request Body**:
  ```json
  {
    "subjectName": "Software Engineering",
    "subjectCode": "CS403"
  }
  ```
- **Response**: `201 Created`

### 3.2 Get All Subjects
- **Method**: `GET`
- **Path**: `/api/subjects`
- **Response**: `200 OK`

---

## 4. Study Group APIs

### 4.1 Create Study Group (Compulsory Feature 1)
- **Method**: `POST`
- **Path**: `/api/groups`
- **Request Body**:
  ```json
  {
    "groupName": "Distributed Systems Prep",
    "subjectId": 1,
    "creatorId": 1,
    "maxMembers": 4
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "success": true,
    "message": "Study group created successfully",
    "data": {
      "id": 4,
      "groupName": "Distributed Systems Prep",
      "subjectId": 1,
      "subjectName": "Computer Networks",
      "subjectCode": "CS401",
      "creatorId": 1,
      "creatorName": "Alex Johnson",
      "creatorEmail": "alex.johnson@university.edu",
      "maxMembers": 4,
      "currentMemberCount": 1,
      "availableSpaces": 3,
      "createdAt": "2026-09-28T10:45:00"
    }
  }
  ```

### 4.2 View All Groups / Filter by Subject (Compulsory Feature 4)
- **Method**: `GET`
- **Path**: `/api/groups` or `/api/groups?subjectId={id}`
- **Query Parameter**: `subjectId` (Optional, Long)
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "Groups for subject retrieved successfully",
    "data": [
      {
        "id": 1,
        "groupName": "Network Protocols Study Circle",
        "subjectId": 1,
        "subjectName": "Computer Networks",
        "subjectCode": "CS401",
        "creatorId": 1,
        "creatorName": "Alex Johnson",
        "creatorEmail": "alex.johnson@university.edu",
        "maxMembers": 4,
        "currentMemberCount": 2,
        "availableSpaces": 2,
        "createdAt": "2026-09-28T10:35:00"
      }
    ]
  }
  ```

### 4.3 Transfer Creator Ownership
- **Method**: `PUT`
- **Path**: `/api/groups/{id}/transfer-creator`
- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
  ```json
  {
    "newCreatorId": 2
  }
  ```
- **Response**: `200 OK`

---

## 5. Membership APIs

### 5.1 Join Study Group (Compulsory Feature 2)
- **Method**: `POST`
- **Path**: `/api/groups/{id}/join`
- **Request Body**:
  ```json
  {
    "studentId": 3
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "success": true,
    "message": "Successfully joined the study group",
    "data": {
      "id": 7,
      "studentId": 3,
      "studentName": "Carlos Mendez",
      "studentEmail": "carlos.mendez@university.edu",
      "groupId": 1,
      "groupName": "Network Protocols Study Circle",
      "isCreator": false,
      "joinedAt": "2026-09-28T10:46:00"
    }
  }
  ```
- **Error Responses**:
  - `409 Conflict`: Group is full (`"Cannot join: Study group is full (Capacity: 4)"`)
  - `409 Conflict`: Student already joined (`"Student is already a member of this study group"`)
  - `404 Not Found`: Invalid student or invalid group

### 5.2 Leave Study Group (Compulsory Feature 3)
- **Method**: `DELETE`
- **Path**: `/api/groups/{id}/leave?studentId={studentId}`
- **Query Parameter**: `studentId` (Required, Long)
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "Successfully left the study group",
    "data": null
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: Creator attempts to leave without transferring ownership (`"Group creator cannot leave the group directly. Please transfer creator ownership to another member before leaving."`)
  - `404 Not Found`: Student not a member of the group

### 5.3 Get Group Members
- **Method**: `GET`
- **Path**: `/api/groups/{id}/members`
- **Response**: `200 OK` (Array of enrolled member records with `isCreator` boolean)

### 5.4 Remove Member (Compulsory Feature 5)
- **Method**: `DELETE`
- **Path**: `/api/groups/{id}/members/{studentId}`
- **Headers**: `Authorization: Bearer <token>` or `X-Auth-Token: <token>`
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "Member removed successfully from the group",
    "data": null
  }
  ```
- **Error Responses**:
  - `401 Unauthorized`: No authentication token provided
  - `403 Forbidden`: Authenticated user is not the group creator (`"Unauthorized: Only the group creator can remove members"`)
  - `400 Bad Request`: Creator attempts to remove themselves (`"The group creator cannot be removed through this endpoint"`)
  - `404 Not Found`: Target student is not enrolled in the group
