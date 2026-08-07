package com.ats.resume.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * ─────────────────────────────────────────────────────────
 * FileStorageUtil — Saves Uploaded Files to Disk
 * Location: util/FileStorageUtil.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * When a user uploads a PDF, we need to:
 * 1. Validate it's actually a PDF
 * 2. Give it a unique name so two "resume.pdf" files don't overwrite each other
 * 3. Save it to the upload directory
 * 4. Return the file path (to store in the database)
 *
 * We use Java NIO (java.nio.file.Path) instead of the old java.io.File
 * because NIO is cleaner and more platform-independent.
 *
 * @Value("${app.upload.dir}") → injects the upload directory from application.properties
 * @PostConstruct → annotated method runs once after the bean is created.
 *                  We use it to create the upload directory if it doesn't exist.
 *
 * UNIQUE FILENAME STRATEGY:
 * We prepend System.currentTimeMillis() to the filename.
 * Example: "resume.pdf" → "1720000000000_resume.pdf"
 * This prevents filename collisions between different uploads.
 */
@Component
@Slf4j
public class FileStorageUtil {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Called automatically after Spring creates this bean.
     * Creates the upload directory on disk if it doesn't already exist.
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    /**
     * Save an uploaded PDF file to disk and return its path.
     *
     * @param file the MultipartFile from the HTTP request
     * @return the path where the file was saved (stored in DB)
     */
    public String saveFile(MultipartFile file) {
        // Step 1: Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        // Step 2: Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        // Step 3: Create a unique filename
        // timestamp + underscore + original name
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "resume.pdf";
        }

        // Sanitize filename: remove path separators that could cause security issues
        // (e.g., an attacker submitting "../../../etc/passwd" as filename)
        String sanitized = Paths.get(originalFilename).getFileName().toString();
        String uniqueFilename = System.currentTimeMillis() + "_" + sanitized;

        // Step 4: Resolve the full path to save to
        Path targetPath = Paths.get(uploadDir).resolve(uniqueFilename);

        // Step 5: Copy the uploaded file bytes to the target path
        // REPLACE_EXISTING: if a file with this name already exists, overwrite it
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved: {}", targetPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }

        // Step 6: Return the relative path (stored in DB, used later to read the file)
        return targetPath.toString();
    }

    /**
     * Get the file at the given path as a Path object.
     * Used by the PDF extractor to read the saved file.
     */
    public Path getFilePath(String filePath) {
        return Paths.get(filePath);
    }
}
