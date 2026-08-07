/**
 * ─────────────────────────────────────────────────────────
 * resumeService.js — Resume API Calls
 * Location: src/services/resumeService.js
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Centralizes all resume-related API calls.
 *
 * NOTE on file upload:
 * When uploading a PDF, we must use multipart/form-data.
 * FormData is the browser's built-in way to do this.
 * We override the Content-Type header to let axios set the
 * multipart boundary automatically.
 */

import api from './api';

export const resumeService = {
  /**
   * Upload a PDF resume file.
   * @param {File} file - The PDF file from the file input
   * @returns {Promise} - ResumeResponse { id, originalFileName, atsScore, createdAt }
   */
  upload: (file) => {
    const formData = new FormData();
    formData.append('file', file);  // 'file' must match @RequestParam("file") in Spring
    return api.post('/resume/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',  // overrides the default 'application/json'
      },
    });
  },

  /**
   * Get all resume uploads for the current user (newest first).
   * @returns {Promise} - Array of ResumeResponse objects
   */
  getHistory: () => api.get('/resume/history'),

  /**
   * Get the most recently uploaded resume.
   * @returns {Promise} - ResumeResponse
   */
  getLatest: () => api.get('/resume/latest'),

  /**
   * Get the full AI analysis for a specific resume.
   * @param {number} id - The resume ID
   * @returns {Promise} - AnalysisResponse { resumeId, originalFileName, atsScore, parsedData, suggestions }
   */
  getAnalysis: (id) => api.get(`/resume/${id}/analysis`),
};
