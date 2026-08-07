# ATS Resume Analyzer — Testing Guide

## Prerequisites Checklist

Before running the project, complete these one-time setup steps:

### 1. MySQL Setup
```sql
-- Open MySQL client and run:
CREATE DATABASE IF NOT EXISTS ats_resume_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure application.properties
Open `backend/src/main/resources/application.properties` and update:
- `spring.datasource.username` → your MySQL username (usually `root`)
- `spring.datasource.password` → your MySQL password
- `gemini.api.key` → your Gemini API key from https://aistudio.google.com/app/apikey

### 3. Install Java 21 + Maven
```bash
# Check Java version
java -version    # Must show 21+

# Check Maven version  
mvn -version     # Must show 3.x+
```

### 4. Install Node.js
```bash
node -version    # Must show 18+
npm -version     # Must show 9+
```

---

## Running the Backend

```bash
# Navigate to backend folder
cd backend

# Run Spring Boot (downloads dependencies + starts server)
mvn spring-boot:run
```

You should see:
```
Started ResumeApplication in X.XXX seconds
Tomcat started on port(s): 8080
```

---

## Running the Frontend

```bash
# In a NEW terminal window, navigate to frontend
cd frontend

# Install dependencies (only needed once)
npm install

# Start the development server
npm run dev
```

Open your browser at: **http://localhost:5173**

---

## API Testing with curl (Phase 9)

> You can also use Postman. These curl commands run in any terminal.

### Test 1: Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","password":"password123"}'
```

**Expected Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john@example.com",
  "name": "John Doe"
}
```

---

### Test 2: Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john@example.com",
  "name": "John Doe"
}
```

> **Save the token!** You'll need it for the next tests.

---

### Test 3: Upload Resume (replace TOKEN with actual token)
```bash
# Windows PowerShell:
$TOKEN = "YOUR_TOKEN_HERE"
curl -X POST http://localhost:8080/api/resume/upload `
  -H "Authorization: Bearer $TOKEN" `
  -F "file=@C:\path\to\your\resume.pdf"

# Linux/Mac:
curl -X POST http://localhost:8080/api/resume/upload \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@/path/to/your/resume.pdf"
```

**Expected Response (201 Created, after ~10-20 seconds):**
```json
{
  "id": 1,
  "originalFileName": "resume.pdf",
  "atsScore": 72,
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### Test 4: Get Upload History
```bash
curl http://localhost:8080/api/resume/history \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

### Test 5: Get Analysis for Resume ID 1
```bash
curl http://localhost:8080/api/resume/1/analysis \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Expected Response:**
```json
{
  "resumeId": 1,
  "originalFileName": "resume.pdf",
  "atsScore": 72,
  "parsedData": "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"skills\":[\"Java\",\"React\"]}",
  "suggestions": "{\"topProblems\":[\"...\"],\"improvements\":[\"...\"],\"missingSkills\":[\"...\"]}"
}
```

---

### Test 6: Error Handling — Duplicate Email (should fail)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane","email":"john@example.com","password":"pass123"}'
```

**Expected Response (400 Bad Request):**
```json
{
  "status": 400,
  "error": "An account with this email already exists"
}
```

---

### Test 7: Unauthorized Access (no token, should fail)
```bash
curl http://localhost:8080/api/resume/history
```

**Expected Response (401 Unauthorized):**
```json
{
  "status": 401,
  "error": "Unauthorized"
}
```

---

## Frontend Smoke Test Checklist

Open http://localhost:5173 and verify:

- [ ] **Register page** loads at `/register`
- [ ] Fill form and submit → redirected to `/dashboard`
- [ ] **Navbar** shows your name + Logout button
- [ ] **Dashboard** shows "No uploads yet" message (first time)
- [ ] Navigate to `/upload`
- [ ] Drag and drop a PDF → file name appears with green check
- [ ] Click "Analyze Resume" → spinner shows for ~15 seconds
- [ ] Redirected to `/analysis/1`
- [ ] ATS score circle animates in
- [ ] Personal info, skills, experience visible
- [ ] Top problems, suggestions, missing skills visible
- [ ] Navigate back to `/dashboard` → history table shows the upload
- [ ] Click "Logout" → redirected to `/login`
- [ ] Try navigating to `/dashboard` without login → redirected to `/login`

---

## Common Issues & Fixes

### "Connection refused" on backend startup
- Is MySQL running? Start it: `net start MySQL` (Windows) or `brew services start mysql`
- Check credentials in `application.properties`
- Make sure the database `ats_resume_db` exists

### "CORS error" in browser console
- Make sure both servers are running (8080 + 5173)
- Check `app.cors.allowed-origins=http://localhost:5173` in application.properties

### Gemini API returns no results
- Check your API key in application.properties
- The key must be active at https://aistudio.google.com/app/apikey
- Free tier has rate limits; wait a minute and try again

### File upload returns 500 error
- Check that the `uploads/resumes` directory was created (look in the `backend` folder)
- If not, create it manually: `mkdir -p backend/uploads/resumes`

### JWT "Malformed token" errors
- Make sure jwt.secret in application.properties is at least 32 characters long
- The current value is long enough — this error only happens if you changed the secret

---

## Database Verification Queries

Run these in MySQL to verify data is being saved correctly:

```sql
USE ats_resume_db;

-- Check registered users
SELECT id, name, email, created_at FROM users;

-- Check uploaded resumes with user names
SELECT r.id, u.name AS user, r.original_file_name, r.ats_score, r.created_at
FROM resumes r JOIN users u ON r.user_id = u.id;

-- Check analysis results
SELECT ra.id, r.original_file_name, ra.ats_score,
       LEFT(ra.parsed_json, 100) AS parsed_preview
FROM resume_analysis ra JOIN resumes r ON ra.resume_id = r.id;
```

---

## Project Structure Reference

```
resume/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/ats/resume/
│       ├── ResumeApplication.java       ← Entry point
│       ├── controller/
│       │   ├── AuthController.java      ← /api/auth/**
│       │   └── ResumeController.java    ← /api/resume/**
│       ├── service/
│       │   ├── AuthService.java         ← Login/register logic
│       │   ├── ResumeService.java       ← Upload pipeline
│       │   └── GeminiService.java       ← AI integration
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── ResumeRepository.java
│       │   └── ResumeAnalysisRepository.java
│       ├── entity/
│       │   ├── User.java
│       │   ├── Resume.java
│       │   └── ResumeAnalysis.java
│       ├── dto/
│       │   ├── RegisterRequest.java
│       │   ├── LoginRequest.java
│       │   ├── AuthResponse.java
│       │   ├── ResumeResponse.java
│       │   └── AnalysisResponse.java
│       ├── security/
│       │   ├── JwtUtil.java             ← Token generation/validation
│       │   ├── JwtAuthenticationFilter.java  ← Per-request JWT check
│       │   └── UserDetailsServiceImpl.java   ← Load user from DB
│       ├── config/
│       │   ├── SecurityConfig.java      ← Security rules + CORS
│       │   └── AppConfig.java           ← RestTemplate + ObjectMapper
│       ├── exception/
│       │   └── GlobalExceptionHandler.java   ← Centralized errors
│       └── util/
│           ├── FileStorageUtil.java     ← Save PDF to disk
│           └── PdfTextExtractor.java   ← Extract text from PDF
│
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    ├── index.html
    └── src/
        ├── main.jsx                    ← React entry point
        ├── App.jsx                     ← Routes
        ├── index.css                   ← Tailwind + global styles
        ├── services/
        │   ├── api.js                  ← Axios instance + interceptors
        │   ├── authService.js          ← Auth API calls
        │   └── resumeService.js        ← Resume API calls
        ├── hooks/
        │   └── useAuth.js              ← Auth state hook
        ├── components/
        │   ├── Navbar.jsx
        │   ├── ProtectedRoute.jsx
        │   └── ScoreCircle.jsx
        └── pages/
            ├── LoginPage.jsx
            ├── RegisterPage.jsx
            ├── DashboardPage.jsx
            ├── UploadPage.jsx
            └── AnalysisPage.jsx
```
