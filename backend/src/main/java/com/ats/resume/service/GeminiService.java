package com.ats.resume.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * ─────────────────────────────────────────────────────────
 * GeminiService — Sends Resume Text to Google Gemini AI
 * Location: service/GeminiService.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * After extracting text from the PDF, we send it to Google Gemini
 * with a detailed prompt asking for:
 * 1. Parsed resume data (name, email, skills, etc.)
 * 2. ATS scores (overall, formatting, skills, experience)
 * 3. AI suggestions (top problems, improvements, missing skills)
 *
 * HOW GEMINI API WORKS:
 * We make an HTTP POST request to:
 *   https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=YOUR_KEY
 *
 * Request body (JSON):
 * {
 *   "contents": [{
 *     "parts": [{"text": "our prompt"}]
 *   }]
 * }
 *
 * Response body (JSON):
 * {
 *   "candidates": [{
 *     "content": {
 *       "parts": [{"text": "AI's response as JSON string"}]
 *     }
 *   }]
 * }
 *
 * RESTTEMPLATE:
 * Spring's synchronous HTTP client. We use it to make the Gemini API call.
 * (In newer Spring projects you'd use WebClient, but RestTemplate is simpler to learn.)
 *
 * OBJECTMAPPER:
 * Jackson's class for parsing JSON strings into Java objects (and vice versa).
 *
 * AI FLOW:
 * 1. Build prompt with resume text
 * 2. POST to Gemini API with API key
 * 3. Extract the response text from candidates[0].content.parts[0].text
 * 4. That text is our JSON with parsed data + scores + suggestions
 * 5. Return as two separate JSON strings (parsedJson, suggestionsJson)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Analyze resume text using Gemini AI.
     *
     * @param resumeText the full text extracted from the PDF
     * @return GeminiResult containing parsedJson, suggestionsJson, and atsScore
     */
    public GeminiResult analyzeResume(String resumeText) {
        // Step 1: Build the prompt
        String prompt = buildPrompt(resumeText);

        // Step 2: Build the Gemini API request body
        // This is the JSON structure Gemini expects
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                },
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", 8192,
                        "responseMimeType", "application/json"
                )
        );

        // Step 3: Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Newer AQ. API keys require the key in the header, not as a query param
        headers.set("x-goog-api-key", apiKey);

        // Step 4: Make the HTTP POST request to Gemini
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl, request, String.class
            );

            // Step 5: Parse the Gemini response
            String responseBody = response.getBody();
            log.debug("Gemini raw response received");

            return parseGeminiResponse(responseBody);

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            // Return a fallback result so the app doesn't crash
            return createFallbackResult();
        }
    }

    /**
     * Build the AI prompt.
     *
     * This is the most important part of working with AI APIs.
     * A well-crafted prompt gets structured, consistent output.
     *
     * We explicitly ask for JSON output with a specific structure.
     * "respond ONLY with valid JSON" is critical — without it, the AI
     * might wrap the response in markdown code blocks or add text before/after.
     */
    private String buildPrompt(String resumeText) {
        return """
                You are an expert ATS (Applicant Tracking System) analyzer and resume expert.
                Analyze the following resume text and respond ONLY with valid JSON (no markdown, no extra text).
                
                Resume Text:
                ---
                """ + resumeText + """
                ---
                
                Respond with EXACTLY this JSON structure:
                {
                  "parsedData": {
                    "name": "full name or empty string",
                    "email": "email or empty string",
                    "phone": "phone number or empty string",
                    "education": [
                      {"degree": "...", "institution": "...", "year": "..."}
                    ],
                    "experience": [
                      {"title": "...", "company": "...", "duration": "...", "description": "..."}
                    ],
                    "skills": ["skill1", "skill2", "skill3"],
                    "projects": [
                      {"name": "...", "description": "...", "technologies": ["..."]}
                    ]
                  },
                  "atsScore": {
                    "overall": 75,
                    "formatting": 80,
                    "skills": 70,
                    "experience": 75
                  },
                  "suggestions": {
                    "topProblems": [
                      "Problem 1",
                      "Problem 2",
                      "Problem 3",
                      "Problem 4",
                      "Problem 5"
                    ],
                    "improvements": [
                      "Improvement suggestion 1",
                      "Improvement suggestion 2",
                      "Improvement suggestion 3"
                    ],
                    "missingSkills": [
                      "Skill 1",
                      "Skill 2",
                      "Skill 3"
                    ]
                  }
                }
                
                Rules:
                - All scores must be integers between 0 and 100
                - topProblems must have exactly 5 items
                - If information is not found, use empty string or empty array
                - Be honest and critical about the resume quality
                - DO NOT include any markdown formatting, code blocks, or extra text
                """;
    }

    /**
     * Parse the Gemini API response and extract our structured data.
     *
     * Gemini wraps the AI's response in a nested JSON structure.
     * We navigate to candidates[0].content.parts[0].text to get the actual response.
     */
    private GeminiResult parseGeminiResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // Navigate the Gemini response structure
        String aiText = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text")
                .asText();

        log.debug("AI response text: {}", aiText.substring(0, Math.min(200, aiText.length())));

        // Sometimes Gemini wraps in ```json ... ``` even when asked not to.
        // Clean that up if present.
        aiText = cleanJsonResponse(aiText);

        // Parse the AI's JSON response
        JsonNode analysisNode = objectMapper.readTree(aiText);

        // Extract parsedData as JSON string
        JsonNode parsedDataNode = analysisNode.path("parsedData");
        String parsedJson = objectMapper.writeValueAsString(parsedDataNode);

        // Extract suggestions as JSON string
        JsonNode suggestionsNode = analysisNode.path("suggestions");
        String suggestionsJson = objectMapper.writeValueAsString(suggestionsNode);

        // Extract overall ATS score
        int atsScore = analysisNode.path("atsScore").path("overall").asInt(50);

        // Clamp score between 0 and 100
        atsScore = Math.max(0, Math.min(100, atsScore));

        return new GeminiResult(parsedJson, suggestionsJson, atsScore);
    }

    /**
     * Remove markdown code blocks if Gemini adds them despite our instructions.
     */
    private String cleanJsonResponse(String text) {
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    /**
     * Fallback result in case the Gemini API fails.
     * This prevents the upload from completely failing if AI is unavailable.
     */
    private GeminiResult createFallbackResult() {
        String parsedJson = """
                {"name":"","email":"","phone":"","education":[],"experience":[],"skills":[],"projects":[]}
                """;
        String suggestionsJson = """
                {"topProblems":["AI analysis failed. Please try again."],"improvements":[],"missingSkills":[]}
                """;
        return new GeminiResult(parsedJson.trim(), suggestionsJson.trim(), 0);
    }

    /**
     * Simple record to hold the analysis result.
     * A 'record' in Java is an immutable data class — like a DTO with auto-generated
     * constructor, getters, equals, hashCode, and toString.
     */
    public record GeminiResult(String parsedJson, String suggestionsJson, int atsScore) {}
}
