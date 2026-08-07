package com.ats.resume.controller;

import com.ats.resume.dto.AnalysisResponse;
import com.ats.resume.dto.ResumeResponse;
import com.ats.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ─────────────────────────────────────────────────────────
 * ResumeController — HTTP Endpoints for Resume Operations
 * Location: controller/ResumeController.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Exposes REST API endpoints for:
 * - Uploading a resume (POST /api/resume/upload)
 * - Getting upload history (GET /api/resume/history)
 * - Getting the latest resume (GET /api/resume/latest)
 * - Getting a full analysis (GET /api/resume/{id}/analysis)
 *
 * KEY CONCEPTS:
 *
 * @AuthenticationPrincipal UserDetails userDetails:
 *   After our JwtAuthenticationFilter validates the JWT and sets
 *   the SecurityContext, we can inject the authenticated user here.
 *   userDetails.getUsername() returns the email we put in the JWT.
 *   This is how we know WHO is making each request.
 *
 * @RequestParam("file") MultipartFile file:
 *   MultipartFile represents an uploaded file.
 *   The frontend sends the PDF as multipart/form-data.
 *   @RequestParam("file") maps the form field named "file" to this parameter.
 *
 * @PathVariable Long id:
 *   Extracts the {id} from the URL path.
 *   GET /api/resume/42/analysis → id = 42
 *
 * ALL ENDPOINTS REQUIRE AUTHENTICATION:
 * SecurityConfig has: anyRequest().authenticated()
 * So all these endpoints need a valid JWT in the Authorization header.
 * If the JWT is missing or invalid, Spring Security returns 401 before
 * the controller method is even called.
 *
 * API ENDPOINTS SUMMARY:
 * POST   /api/resume/upload         Upload PDF + trigger AI analysis
 * GET    /api/resume/history        List all uploads for this user
 * GET    /api/resume/latest         Get most recent resume summary
 * GET    /api/resume/{id}/analysis  Get full AI analysis for one resume
 */
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * Upload a resume PDF and trigger AI analysis.
     *
     * URL: POST http://localhost:8080/api/resume/upload
     * Headers: Authorization: Bearer <token>
     * Body: multipart/form-data with field "file" = your PDF
     *
     * Response (201 Created):
     * { "id": 1, "originalFileName": "resume.pdf", "atsScore": 72, "createdAt": "..." }
     *
     * This might take 5-15 seconds because of the Gemini API call.
     */
    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        ResumeResponse response = resumeService.uploadAndAnalyze(file, userEmail);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Get all previous resume uploads for the authenticated user.
     *
     * URL: GET http://localhost:8080/api/resume/history
     * Headers: Authorization: Bearer <token>
     *
     * Response (200 OK):
     * [
     *   { "id": 2, "originalFileName": "resume_v2.pdf", "atsScore": 80, "createdAt": "..." },
     *   { "id": 1, "originalFileName": "resume.pdf", "atsScore": 72, "createdAt": "..." }
     * ]
     */
    @GetMapping("/history")
    public ResponseEntity<List<ResumeResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ResumeResponse> history = resumeService.getHistory(userDetails.getUsername());
        return ResponseEntity.ok(history);
    }

    /**
     * Get the most recently uploaded resume.
     *
     * URL: GET http://localhost:8080/api/resume/latest
     * Headers: Authorization: Bearer <token>
     *
     * Response (200 OK):
     * { "id": 2, "originalFileName": "resume_v2.pdf", "atsScore": 80, "createdAt": "..." }
     */
    @GetMapping("/latest")
    public ResponseEntity<ResumeResponse> getLatest(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ResumeResponse latest = resumeService.getLatestResume(userDetails.getUsername());
        return ResponseEntity.ok(latest);
    }

    /**
     * Get the full AI analysis for a specific resume.
     *
     * URL: GET http://localhost:8080/api/resume/1/analysis
     * Headers: Authorization: Bearer <token>
     *
     * Response (200 OK):
     * {
     *   "resumeId": 1,
     *   "originalFileName": "resume.pdf",
     *   "atsScore": 72,
     *   "parsedData": "{\"name\":\"John\",\"skills\":[\"Java\",\"React\"]}",
     *   "suggestions": "{\"topProblems\":[\"...\"],\"improvements\":[\"...\"]}"
     * }
     */
    @GetMapping("/{id}/analysis")
    public ResponseEntity<AnalysisResponse> getAnalysis(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AnalysisResponse analysis = resumeService.getAnalysis(id, userDetails.getUsername());
        return ResponseEntity.ok(analysis);
    }
}
