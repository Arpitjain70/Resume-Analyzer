/**
 * ─────────────────────────────────────────────────────────
 * DashboardPage.jsx
 * Location: src/pages/DashboardPage.jsx
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * The main landing page after login. Shows:
 * 1. Latest resume ATS score (big score circle)
 * 2. Quick link to upload a new resume
 * 3. History of all previous uploads with scores
 *
 * KEY REACT CONCEPT — useEffect:
 * useEffect(() => { ... }, []) runs ONCE after the component mounts.
 * It's how we fetch data from the API when a page loads.
 * The empty [] means "run this only once" (not on every re-render).
 *
 * The dependency array controls when the effect re-runs:
 * []     → run once on mount
 * [id]   → run whenever 'id' changes
 * (none) → run on every render (usually a mistake!)
 */

import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { resumeService } from '../services/resumeService';
import { useAuth } from '../hooks/useAuth';
import ScoreCircle from '../components/ScoreCircle';

const DashboardPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [latest, setLatest] = useState(null);          // Most recent resume
  const [history, setHistory] = useState([]);           // All resumes
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Fetch data on component mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch both in parallel using Promise.all (faster than sequential await)
        const [historyRes, latestRes] = await Promise.allSettled([
          resumeService.getHistory(),
          resumeService.getLatest(),
        ]);

        if (historyRes.status === 'fulfilled') {
          setHistory(historyRes.value.data);
        }
        if (latestRes.status === 'fulfilled') {
          setLatest(latestRes.value.data);
        }
      } catch (err) {
        setError('Failed to load dashboard data.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []); // Empty array = run once on mount

  // Format date for display
  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric',
    });
  };

  // Score badge color
  const getScoreBadgeClass = (score) => {
    if (score >= 80) return 'bg-green-100 text-green-800';
    if (score >= 60) return 'bg-yellow-100 text-yellow-800';
    return 'bg-red-100 text-red-800';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <svg className="animate-spin h-10 w-10 text-blue-600 mx-auto" viewBox="0 0 24 24" fill="none">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          <p className="text-gray-500 mt-3">Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-5xl mx-auto space-y-8">

        {/* Welcome Header */}
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Welcome back, {user?.name?.split(' ')[0]}! 👋
          </h1>
          <p className="text-gray-500 mt-1">Here's an overview of your resume performance.</p>
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Top Row: Latest Score + Upload CTA */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

          {/* Latest Resume Score Card */}
          {latest ? (
            <div className="card">
              <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">
                Latest Resume
              </h2>
              <div className="flex items-center gap-6">
                <ScoreCircle score={latest.atsScore} size={100} strokeWidth={9} />
                <div>
                  <p className="font-semibold text-gray-900 text-lg">
                    {latest.originalFileName}
                  </p>
                  <p className="text-gray-400 text-sm mt-1">{formatDate(latest.createdAt)}</p>
                  <Link
                    to={`/analysis/${latest.id}`}
                    className="inline-block mt-3 text-blue-600 hover:underline text-sm font-medium"
                  >
                    View Full Analysis →
                  </Link>
                </div>
              </div>
            </div>
          ) : (
            <div className="card flex items-center justify-center text-center py-10">
              <div>
                <p className="text-gray-500 font-medium">No resumes yet</p>
                <p className="text-gray-400 text-sm mt-1">Upload your first resume to get started</p>
              </div>
            </div>
          )}

          {/* Upload CTA Card */}
          <div className="card bg-gradient-to-br from-blue-600 to-indigo-700 text-white flex flex-col justify-between">
            <div>
              <h2 className="text-xl font-bold mb-2">Improve Your Score</h2>
              <p className="text-blue-100 text-sm">
                Upload a new version of your resume to get AI-powered feedback and a fresh ATS score.
              </p>
            </div>
            <Link
              to="/upload"
              className="mt-6 bg-white text-blue-700 font-semibold px-5 py-2.5 rounded-lg
                         hover:bg-blue-50 transition-colors inline-block text-center"
            >
              Upload New Resume
            </Link>
          </div>
        </div>

        {/* History Table */}
        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-5">Upload History</h2>

          {history.length === 0 ? (
            <div className="text-center py-12 text-gray-400">
              <p>No uploads yet. Upload your first resume to see it here!</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200">
                    <th className="text-left py-3 px-2 font-semibold text-gray-600">File Name</th>
                    <th className="text-left py-3 px-2 font-semibold text-gray-600">Date</th>
                    <th className="text-left py-3 px-2 font-semibold text-gray-600">ATS Score</th>
                    <th className="text-right py-3 px-2 font-semibold text-gray-600">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((resume) => (
                    <tr
                      key={resume.id}
                      className="border-b border-gray-100 hover:bg-gray-50 transition-colors"
                    >
                      <td className="py-3 px-2 font-medium text-gray-800">
                        {resume.originalFileName}
                      </td>
                      <td className="py-3 px-2 text-gray-500">
                        {formatDate(resume.createdAt)}
                      </td>
                      <td className="py-3 px-2">
                        <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${getScoreBadgeClass(resume.atsScore)}`}>
                          {resume.atsScore}/100
                        </span>
                      </td>
                      <td className="py-3 px-2 text-right">
                        <Link
                          to={`/analysis/${resume.id}`}
                          className="text-blue-600 hover:underline font-medium"
                        >
                          View →
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
