/**
 * ─────────────────────────────────────────────────────────
 * UploadPage.jsx
 * Location: src/pages/UploadPage.jsx
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Lets users upload a PDF resume. Shows upload progress, validates
 * the file type and size on the client side, then calls the API.
 *
 * KEY CONCEPTS:
 *
 * Drag and Drop:
 * We listen to onDragOver (prevent default to allow drop) and
 * onDrop (read the dropped file). onDragEnter/Leave toggle the
 * visual "drag over" state.
 *
 * File Input:
 * <input type="file" accept=".pdf"> lets users pick a file.
 * We use a ref to programmatically click it from a custom button
 * (so we can style the button however we want).
 *
 * useRef:
 * fileInputRef.current is the actual DOM element.
 * fileInputRef.current.click() opens the file picker dialog.
 *
 * After successful upload, navigate to the analysis page for that resume.
 */

import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { resumeService } from '../services/resumeService';

const MAX_SIZE_MB = 5;
const MAX_SIZE_BYTES = MAX_SIZE_MB * 1024 * 1024;

const UploadPage = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const [selectedFile, setSelectedFile] = useState(null);
  const [dragOver, setDragOver] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  // Validate the selected file on the client side
  const validateFile = (file) => {
    if (!file) return 'Please select a file.';
    if (file.type !== 'application/pdf') return 'Only PDF files are accepted.';
    if (file.size > MAX_SIZE_BYTES) return `File must be smaller than ${MAX_SIZE_MB} MB.`;
    return null; // null = valid
  };

  const handleFileSelect = (file) => {
    const validationError = validateFile(file);
    if (validationError) {
      setError(validationError);
      setSelectedFile(null);
      return;
    }
    setError('');
    setSelectedFile(file);
  };

  // Drag & Drop handlers
  const handleDragOver = (e) => {
    e.preventDefault();  // Required! Without this, drop events don't fire.
    setDragOver(true);
  };
  const handleDragLeave = () => setDragOver(false);
  const handleDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    const file = e.dataTransfer.files[0];
    handleFileSelect(file);
  };

  // File input change handler
  const handleInputChange = (e) => {
    handleFileSelect(e.target.files[0]);
  };

  // Upload the file
  const handleUpload = async () => {
    if (!selectedFile) {
      setError('Please select a PDF file first.');
      return;
    }

    setUploading(true);
    setError('');

    try {
      const response = await resumeService.upload(selectedFile);
      const resume = response.data;
      // Navigate to the analysis page for this newly uploaded resume
      navigate(`/analysis/${resume.id}`);
    } catch (err) {
      setError(
        err.response?.data?.error ||
        'Upload failed. Please check your connection and try again.'
      );
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-2xl mx-auto">
        
        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Upload Your Resume</h1>
          <p className="text-gray-500 mt-2">
            Upload a PDF resume to receive an AI-powered ATS analysis with scores and improvement tips.
          </p>
        </div>

        {/* Upload Area */}
        <div className="card">
          {/* Drag & Drop Zone */}
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`
              border-2 border-dashed rounded-xl p-12 text-center cursor-pointer
              transition-all duration-200
              ${dragOver
                ? 'border-blue-500 bg-blue-50'
                : selectedFile
                  ? 'border-green-400 bg-green-50'
                  : 'border-gray-300 hover:border-blue-400 hover:bg-gray-50'
              }
            `}
          >
            {/* Hidden file input */}
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf"
              onChange={handleInputChange}
              className="hidden"
            />

            {/* Icon */}
            <div className="flex justify-center mb-4">
              {selectedFile ? (
                <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
                  <svg className="w-8 h-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
              ) : (
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center">
                  <svg className="w-8 h-8 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                      d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                  </svg>
                </div>
              )}
            </div>

            {/* Text */}
            {selectedFile ? (
              <div>
                <p className="font-semibold text-green-700 text-lg">{selectedFile.name}</p>
                <p className="text-green-600 text-sm mt-1">
                  {(selectedFile.size / (1024 * 1024)).toFixed(2)} MB — Ready to upload
                </p>
                <p className="text-gray-400 text-sm mt-2">Click to choose a different file</p>
              </div>
            ) : (
              <div>
                <p className="font-semibold text-gray-700 text-lg">
                  {dragOver ? 'Drop your PDF here' : 'Drag & drop your resume here'}
                </p>
                <p className="text-gray-500 text-sm mt-1">or click to browse files</p>
                <p className="text-gray-400 text-xs mt-3">PDF only • Max {MAX_SIZE_MB} MB</p>
              </div>
            )}
          </div>

          {/* Error Message */}
          {error && (
            <div className="mt-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {/* Upload Button */}
          <button
            onClick={handleUpload}
            disabled={!selectedFile || uploading}
            className="btn-primary w-full mt-6 flex items-center justify-center gap-2"
          >
            {uploading ? (
              <>
                <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                Analyzing with AI... (this may take 10-20 seconds)
              </>
            ) : (
              <>
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
                Analyze Resume
              </>
            )}
          </button>

          {/* Info Box */}
          <div className="mt-6 bg-blue-50 border border-blue-100 rounded-lg p-4">
            <p className="text-sm font-semibold text-blue-800 mb-2">What we analyze:</p>
            <ul className="text-sm text-blue-700 space-y-1">
              <li>✅ ATS score (overall, formatting, skills, experience)</li>
              <li>✅ Parsed data (name, email, skills, experience, projects)</li>
              <li>✅ Top 5 problems in your resume</li>
              <li>✅ Personalized improvement suggestions</li>
              <li>✅ Missing skills for your target role</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UploadPage;
