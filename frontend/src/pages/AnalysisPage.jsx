/**
 * ─────────────────────────────────────────────────────────
 * AnalysisPage.jsx
 * Location: src/pages/AnalysisPage.jsx
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Shows the full AI analysis for a specific resume including:
 * - ATS score breakdown (overall, formatting, skills, experience)
 * - Parsed resume data (name, skills, education, experience, projects)
 * - AI suggestions (top problems, improvements, missing skills)
 *
 * KEY CONCEPTS:
 *
 * useParams():
 * React Router hook to read URL parameters.
 * Route: /analysis/:id
 * useParams().id → gets the ":id" part from the URL
 * Example: /analysis/42 → id = "42"
 *
 * JSON.parse():
 * The API returns parsedData and suggestions as JSON strings.
 * We parse them into JavaScript objects for rendering.
 * Always wrap in try/catch because JSON.parse() throws on invalid JSON.
 */

import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { resumeService } from '../services/resumeService';
import ScoreCircle from '../components/ScoreCircle';

const AnalysisPage = () => {
  const { id } = useParams();  // get resume ID from URL
  const [analysis, setAnalysis] = useState(null);
  const [parsedData, setParsedData] = useState(null);
  const [suggestions, setSuggestions] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAnalysis = async () => {
      try {
        const res = await resumeService.getAnalysis(id);
        const data = res.data;
        setAnalysis(data);

        // Parse the JSON strings from the API
        try {
          setParsedData(JSON.parse(data.parsedData));
        } catch {
          setParsedData({});
        }
        try {
          setSuggestions(JSON.parse(data.suggestions));
        } catch {
          setSuggestions({});
        }
      } catch (err) {
        setError(err.response?.data?.error || 'Failed to load analysis.');
      } finally {
        setLoading(false);
      }
    };
    fetchAnalysis();
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <svg className="animate-spin h-10 w-10 text-blue-600 mx-auto" viewBox="0 0 24 24" fill="none">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          <p className="text-gray-500 mt-3">Loading your analysis...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="card text-center max-w-md w-full">
          <p className="text-red-600 font-medium">{error}</p>
          <Link to="/dashboard" className="btn-primary inline-block mt-4">Back to Dashboard</Link>
        </div>
      </div>
    );
  }

  // Safely extract ATS score breakdown from parsedData
  // (parsedData doesn't have scores — those come from analysis.atsScore)
  const overallScore = analysis?.atsScore ?? 0;

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-5xl mx-auto space-y-6">

        {/* Breadcrumb */}
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <Link to="/dashboard" className="hover:text-blue-600">Dashboard</Link>
          <span>›</span>
          <span className="text-gray-800 font-medium">Analysis</span>
        </div>

        {/* Header */}
        <div className="card">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">{analysis.originalFileName}</h1>
              <p className="text-gray-500 text-sm mt-1">AI-powered ATS Analysis</p>
            </div>
            <Link to="/upload" className="btn-primary text-sm">
              Upload New Resume
            </Link>
          </div>
        </div>

        {/* ATS Score Section */}
        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-6">ATS Score Breakdown</h2>
          <div className="flex flex-wrap gap-8 justify-center sm:justify-start">
            <ScoreCircle score={overallScore} size={130} strokeWidth={11} label="Overall Score" />
            {/* Note: detailed breakdown scores would come from the parsedJson if we included them.
                For simplicity, we show the overall score. You can extend Gemini prompt to return
                formatting/skills/experience scores and display them here similarly. */}
          </div>

          {/* Score interpretation */}
          <div className="mt-6 grid grid-cols-1 sm:grid-cols-3 gap-3">
            {[
              { range: '0-49', label: 'Needs Work', color: 'red' },
              { range: '50-74', label: 'Average', color: 'yellow' },
              { range: '75-100', label: 'Good / Great', color: 'green' },
            ].map(({ range, label, color }) => (
              <div key={range} className={`rounded-lg px-4 py-3 bg-${color}-50 border border-${color}-200`}>
                <p className={`text-${color}-700 font-semibold text-sm`}>{label}</p>
                <p className={`text-${color}-600 text-xs`}>Score {range}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Two Column Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

          {/* LEFT: Parsed Resume Info */}
          <div className="space-y-6">

            {/* Personal Info */}
            {parsedData && (parsedData.name || parsedData.email || parsedData.phone) && (
              <div className="card">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Personal Information</h2>
                <div className="space-y-2 text-sm">
                  {parsedData.name && <InfoRow label="Name" value={parsedData.name} />}
                  {parsedData.email && <InfoRow label="Email" value={parsedData.email} />}
                  {parsedData.phone && <InfoRow label="Phone" value={parsedData.phone} />}
                </div>
              </div>
            )}

            {/* Skills */}
            {parsedData?.skills?.length > 0 && (
              <div className="card">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Skills</h2>
                <div className="flex flex-wrap gap-2">
                  {parsedData.skills.map((skill, i) => (
                    <span key={i} className="bg-blue-100 text-blue-800 text-xs font-medium px-3 py-1 rounded-full">
                      {skill}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Education */}
            {parsedData?.education?.length > 0 && (
              <div className="card">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Education</h2>
                <div className="space-y-3">
                  {parsedData.education.map((edu, i) => (
                    <div key={i} className="border-l-4 border-blue-200 pl-4">
                      <p className="font-semibold text-gray-800">{edu.degree}</p>
                      <p className="text-gray-600 text-sm">{edu.institution}</p>
                      {edu.year && <p className="text-gray-400 text-sm">{edu.year}</p>}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Experience */}
            {parsedData?.experience?.length > 0 && (
              <div className="card">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Experience</h2>
                <div className="space-y-4">
                  {parsedData.experience.map((exp, i) => (
                    <div key={i} className="border-l-4 border-indigo-200 pl-4">
                      <p className="font-semibold text-gray-800">{exp.title}</p>
                      <p className="text-gray-600 text-sm">{exp.company} · {exp.duration}</p>
                      {exp.description && (
                        <p className="text-gray-500 text-sm mt-1">{exp.description}</p>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Projects */}
            {parsedData?.projects?.length > 0 && (
              <div className="card">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Projects</h2>
                <div className="space-y-4">
                  {parsedData.projects.map((proj, i) => (
                    <div key={i} className="border-l-4 border-purple-200 pl-4">
                      <p className="font-semibold text-gray-800">{proj.name}</p>
                      <p className="text-gray-500 text-sm mt-0.5">{proj.description}</p>
                      {proj.technologies?.length > 0 && (
                        <div className="flex flex-wrap gap-1 mt-2">
                          {proj.technologies.map((tech, j) => (
                            <span key={j} className="bg-purple-100 text-purple-700 text-xs px-2 py-0.5 rounded">
                              {tech}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* RIGHT: AI Suggestions */}
          <div className="space-y-6">

            {/* Top Problems */}
            {suggestions?.topProblems?.length > 0 && (
              <div className="card">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">
                  ⚠️ Top {suggestions.topProblems.length} Problems
                </h2>
                <ul className="space-y-3">
                  {suggestions.topProblems.map((problem, i) => (
                    <li key={i} className="flex items-start gap-3">
                      <span className="flex-shrink-0 w-6 h-6 bg-red-100 text-red-700 rounded-full
                                       flex items-center justify-center text-xs font-bold">
                        {i + 1}
                      </span>
                      <span className="text-gray-700 text-sm">{problem}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Improvement Suggestions */}
            {suggestions?.improvements?.length > 0 && (
              <div className="card">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">
                  💡 Suggested Improvements
                </h2>
                <ul className="space-y-3">
                  {suggestions.improvements.map((tip, i) => (
                    <li key={i} className="flex items-start gap-3">
                      <span className="text-green-500 text-lg leading-none">✓</span>
                      <span className="text-gray-700 text-sm">{tip}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Missing Skills */}
            {suggestions?.missingSkills?.length > 0 && (
              <div className="card">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">
                  🔍 Missing Skills
                </h2>
                <p className="text-gray-500 text-sm mb-3">
                  Consider adding these skills to strengthen your profile:
                </p>
                <div className="flex flex-wrap gap-2">
                  {suggestions.missingSkills.map((skill, i) => (
                    <span key={i} className="bg-amber-100 text-amber-800 text-xs font-medium px-3 py-1 rounded-full border border-amber-200">
                      + {skill}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// Small helper component for key-value rows
const InfoRow = ({ label, value }) => (
  <div className="flex gap-3">
    <span className="text-gray-400 font-medium w-16 flex-shrink-0">{label}:</span>
    <span className="text-gray-800">{value}</span>
  </div>
);

export default AnalysisPage;
