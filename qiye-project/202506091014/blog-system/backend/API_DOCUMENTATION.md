# Blog System Backend API Documentation

This document describes the RESTful API endpoints for the Blog System backend. It includes endpoint descriptions, request/response formats, and example test cases for frontend-backend integration.

---

## User APIs

### 1. User Registration

- **URL:** `/api/users/register`
- **Method:** POST
- **Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "nickname": "string (optional)",
  "avatarUrl": "string (optional)"
}
```
- **Response:**
  - 200 OK: `"User registered successfully"`
  - 400 Bad Request: `"Username is already taken"` or `"Email is already in use"`

- **Test Example (curl):**
```bash
curl -X POST http://localhost:8080/api/users/register \
-H "Content-Type: application/json" \
-d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

---

### 2. User Login

- **URL:** `/api/users/login`
- **Method:** POST
- **Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```
- **Response:**
  - 200 OK: `"Login successful"` (Note: JWT token implementation pending)
  - 400 Bad Request: `"Account does not exist"` or `"Incorrect password"`

- **Test Example (curl):**
```bash
curl -X POST http://localhost:8080/api/users/login \
-H "Content-Type: application/json" \
-d '{"username":"testuser","password":"password123"}'
```

---

### 3. Get User Info

- **URL:** `/api/users/{id}`
- **Method:** GET
- **Response:**
  - 200 OK: User object JSON
  - 404 Not Found

- **Test Example (curl):**
```bash
curl http://localhost:8080/api/users/1
```

---

### 4. Update User Info

- **URL:** `/api/users/{id}`
- **Method:** PUT
- **Request Body:**
```json
{
  "nickname": "string",
  "avatarUrl": "string",
  "email": "string"
}
```
- **Response:**
  - 200 OK: `"User info updated"`
  - 404 Not Found

- **Test Example (curl):**
```bash
curl -X PUT http://localhost:8080/api/users/1 \
-H "Content-Type: application/json" \
-d '{"nickname":"NewNick","avatarUrl":"http://example.com/avatar.png","email":"newemail@example.com"}'
```

---

## Blog Post APIs

### 1. Create Post

- **URL:** `/api/posts`
- **Method:** POST
- **Request Body:**
```json
{
  "title": "string",
  "content": "string",
  "author": {
    "id": number
  }
}
```
- **Response:**
  - 200 OK: `"Post created successfully"`

- **Test Example (curl):**
```bash
curl -X POST http://localhost:8080/api/posts \
-H "Content-Type: application/json" \
-d '{"title":"My First Post","content":"This is the content","author":{"id":1}}'
```

---

### 2. Get Post by ID

- **URL:** `/api/posts/{id}`
- **Method:** GET
- **Response:**
  - 200 OK: Post object JSON
  - 404 Not Found

- **Test Example (curl):**
```bash
curl http://localhost:8080/api/posts/1
```

---

### 3. Update Post

- **URL:** `/api/posts/{id}`
- **Method:** PUT
- **Request Body:**
```json
{
  "title": "string",
  "content": "string"
}
```
- **Response:**
  - 200 OK: `"Post updated successfully"`
  - 404 Not Found

- **Test Example (curl):**
```bash
curl -X PUT http://localhost:8080/api/posts/1 \
-H "Content-Type: application/json" \
-d '{"title":"Updated Title","content":"Updated content"}'
```

---

### 4. Delete Post

- **URL:** `/api/posts/{id}`
- **Method:** DELETE
- **Response:**
  - 200 OK: `"Post deleted successfully"`
  - 404 Not Found

- **Test Example (curl):**
```bash
curl -X DELETE http://localhost:8080/api/posts/1
```

---

### 5. List All Posts

- **URL:** `/api/posts`
- **Method:** GET
- **Response:**
  - 200 OK: Array of Post objects

- **Test Example (curl):**
```bash
curl http://localhost:8080/api/posts
```

---

## Comment APIs

### 1. Add Comment

- **URL:** `/api/comments`
- **Method:** POST
- **Request Body:**
```json
{
  "content": "string",
  "author": {
    "id": number
  },
  "post": {
    "id": number
  }
}
```
- **Response:**
  - 200 OK: `"Comment added successfully"`

- **Test Example (curl):**
```bash
curl -X POST http://localhost:8080/api/comments \
-H "Content-Type: application/json" \
-d '{"content":"Nice post!","author":{"id":1},"post":{"id":1}}'
```

---

### 2. Get Comments by Post ID

- **URL:** `/api/comments/post/{postId}`
- **Method:** GET
- **Response:**
  - 200 OK: Array of Comment objects

- **Test Example (curl):**
```bash
curl http://localhost:8080/api/comments/post/1
```

---

### 3. Delete Comment

- **URL:** `/api/comments/{id}`
- **Method:** DELETE
- **Response:**
  - 200 OK: `"Comment deleted successfully"`
  - 404 Not Found

- **Test Example (curl):**
```bash
curl -X DELETE http://localhost:8080/api/comments/1
```

---

## Like APIs

### 1. Add Like

- **URL:** `/api/likes`
- **Method:** POST
- **Request Body:**
```json
{
  "user": {
    "id": number
  },
  "post": {
    "id": number
  }
}
```
- **Response:**
  - 200 OK: `"Like added successfully"`

- **Test Example (curl):**
```bash
curl -X POST http://localhost:8080/api/likes \
-H "Content-Type: application/json" \
-d '{"user":{"id":1},"post":{"id":1}}'
```

---

### 2. Remove Like

- **URL:** `/api/likes/{id}`
- **Method:** DELETE
- **Response:**
  - 200 OK: `"Like removed successfully"`
  - 404 Not Found

- **Test Example (curl):**
```bash
curl -X DELETE http://localhost:8080/api/likes/1
```

---

# Notes

- Authentication and authorization (JWT) are to be implemented for secure access.
- Passwords should be hashed before storage.
- Error handling and validation should be enhanced in production.
- Frontend should include the JWT token in Authorization headers for protected endpoints.

This documentation should assist frontend and backend teams in integration and testing.
