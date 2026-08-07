package com.ats.resume.service;

import com.ats.resume.dto.AnalysisResponse;
import com.ats.resume.dto.ResumeResponse;
import com.ats.resume.entity.Resume;
import com.ats.resume.entity.ResumeAnalysis;
import com.ats.resume.entity.User;
import com.ats.resume.repository.ResumeAnalysisRepository;
import com.ats.resume.repository.ResumeRepository;
import com.ats.resume.repository.UserRepository;
import com.ats.resume.util.FileStorageUtil;
import com.ats.resume.util.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ─────────────────────────────────────────────────────────
 * ResumeService — Core Business Logic for Resume Operations
 * Location: service/ResumeService.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Orchestrates the complete resume processing pipeline:
 * 1. Save the uploaded PDF file to disk
 * 2. Save resume metadata to the database
 * 3. Extract text from the PDF
 * 4. Send text to Gemini AI for analysis
 * 5. Save analysis results to the database
 *
 * Also handles dashboard queries:
 * - Get upload history
 * - Get latest resume
 * - Get full analysis for one resume
 *
 * @Transactional:
 * Multiple DB writes happen in uploadResume() (save Resume + save ResumeAnalysis).
 * @Transactional wraps them in a single DB transaction.
 * If anything fails (e.g., Gemini API error), BOTH writes are rolled back.
 * This prevents partial saves (Resume saved but no Analysis).
 *
 * COMPLETE UPLOAD FLOW:
 * POST /api/resume/upload (with PDF file + JWT)
 *   → Controller calls resumeService.uploadAndAnalyze(file, userEmail)
 *   → Save file to disk (FileStorageUtil)
 *   → Create Resume entity, save to DB
 *   → Extract text (PdfTextExtractor)
 *   → Call Gemini AI (GeminiService)
 *   → Create ResumeAnalysis entity, save to DB
 *   → Update Resume.atsScore
 *   → Return ResumeResponse to controller
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final FileStorageUtil fileStorageUtil;
    private final PdfTextExtractor pdfTextExtractor;
    private final GeminiService geminiService;

    /**
     * Handle the complete resume upload and analysis pipeline.
     *
     * @param file      the uploaded PDF file
     * @param userEmail the authenticated user's email (from JWT)
     * @return ResumeResponse with id, filename, atsScore, createdAt
     */
    @Transactional
    public ResumeResponse uploadAndAnalyze(MultipartFile file, String userEmail) {
        log.info("Starting resume upload for user: {}", userEmail);

        // Step 1: Look up the authenticated user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        // Step 2: Save file to disk, get back the path
        String filePath = fileStorageUtil.saveFile(file);
        log.info("File saved to: {}", filePath);

        // Step 3: Create and save Resume entity to DB (initial save with score=0)
        Resume resume = Resume.builder()
                .user(user)
                .originalFileName(file.getOriginalFilename())
                .filePath(filePath)
                .atsScore(0)
                .build();
        resume = resumeRepository.save(resume);

        // Step 4: Extract text from the PDF
        String extractedText;
        try {
            extractedText = pdfTextExtractor.extractText(fileStorageUtil.getFilePath(filePath));
        } catch (Exception e) {
            log.error("PDF text extraction failed: {}", e.getMessage());
            extractedText = "Could not extract text from PDF.";
        }

        // Step 5: Send to Gemini AI for analysis
        log.info("Sending resume text to Gemini AI...");
        GeminiService.GeminiResult result = geminiService.analyzeResume(extractedText);

        // Step 6: Save the analysis to DB
        ResumeAnalysis analysis = ResumeAnalysis.builder()
                .resume(resume)
                .parsedJson(result.parsedJson())
                .suggestionsJson(result.suggestionsJson())
                .atsScore(result.atsScore())
                .build();
        analysisRepository.save(analysis);

        // Step 7: Update the Resume with the ATS score (quick-access field)
        resume.setAtsScore(result.atsScore());
        resumeRepository.save(resume);

        log.info("Resume analyzed. ATS Score: {}", result.atsScore());

        return toResumeResponse(resume);
    }

    /**
     * Get all resumes for a user, newest first.
     * Used in the dashboard history section.
     */
    public List<ResumeResponse> getHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return resumeRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get the most recent resume for a user.
     * Used in the dashboard "latest" card.
     */
    public ResumeResponse getLatestResume(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Resume latest = resumeRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No resumes found. Please upload your resume first."));

        return toResumeResponse(latest);
    }

    /**
     * Get the full analysis for a specific resume.
     * Checks that the resume belongs to the requesting user (security check).
     */
    public AnalysisResponse getAnalysis(Long resumeId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Security check: only allow access if this resume belongs to THIS user
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Resume not found or access denied"));

        ResumeAnalysis analysis = analysisRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found for this resume"));

        return AnalysisResponse.builder()
                .resumeId(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .atsScore(analysis.getAtsScore())
                .parsedData(analysis.getParsedJson())
                .suggestions(analysis.getSuggestionsJson())
                .build();
    }

    /**
     * Helper method: convert Resume entity → ResumeResponse DTO.
     * This keeps entity-to-DTO mapping in one place.
     */
    private ResumeResponse toResumeResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .atsScore(resume.getAtsScore())
                .createdAt(resume.getCreatedAt())
                .build();
    }
}
