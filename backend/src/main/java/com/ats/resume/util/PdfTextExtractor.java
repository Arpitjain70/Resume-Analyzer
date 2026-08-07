package com.ats.resume.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

/**
 * ─────────────────────────────────────────────────────────
 * PdfTextExtractor — Reads Text From PDF Files
 * Location: util/PdfTextExtractor.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * After saving the PDF to disk, we need to read its text content.
 * We can't send a binary PDF file to the Gemini AI API directly
 * (in this beginner approach). Instead, we:
 * 1. Open the PDF with Apache PDFBox
 * 2. Extract all text from all pages
 * 3. Pass that text string to Gemini as a prompt
 *
 * APACHE PDFBOX:
 * PDDocument → represents an open PDF document
 * PDFTextStripper → walks through each page and extracts text
 *
 * WHY TRY-WITH-RESOURCES?
 * PDDocument opens a file handle. If we forget to close it, we leak
 * file handles (memory/system resource leak). try-with-resources
 * automatically calls document.close() even if an exception occurs.
 *
 * LIMITATIONS:
 * - PDFBox extracts text from text-based PDFs. Image-only PDFs (scans) return empty string.
 * - Formatting may not be perfectly preserved.
 * - For a beginner project this is fine. Production would use cloud OCR for scanned PDFs.
 */
@Component
@Slf4j
public class PdfTextExtractor {

    /**
     * Extract all text from a PDF file.
     *
     * @param pdfPath the path to the PDF file on disk
     * @return the full text content of the PDF
     */
    public String extractText(Path pdfPath) {
        // try-with-resources: document.close() is called automatically
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {

            // Check if the PDF has pages
            if (document.getNumberOfPages() == 0) {
                log.warn("PDF has no pages: {}", pdfPath);
                return "";
            }

            // PDFTextStripper walks through each page and collects all text
            PDFTextStripper stripper = new PDFTextStripper();
            // sortByPosition=true attempts to preserve reading order
            stripper.setSortByPosition(true);

            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF: {}", text.length(), pdfPath.getFileName());

            if (text.isBlank()) {
                log.warn("No text found in PDF. It may be a scanned/image-only PDF: {}", pdfPath);
                return "No readable text found. This may be a scanned image PDF.";
            }

            return text;

        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", pdfPath, e);
            throw new RuntimeException("Could not read PDF file: " + pdfPath.getFileName(), e);
        }
    }
}
