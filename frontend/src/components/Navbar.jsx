/**
 * ─────────────────────────────────────────────────────────
 * Navbar.jsx — Top Navigation Bar
 * Location: src/components/Navbar.jsx
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Displays the app name, current user, and navigation links.
 * Shows different links depending on whether the user is logged in.
 *
 * useNavigate(): React Router hook to programmatically navigate.
 * After logout, we call navigate('/login') to send the user to login.
 */

import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          
          {/* Logo / App Name */}
          <Link to={isAuthenticated ? '/dashboard' : '/'} className="flex items-center gap-2">
            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">ATS</span>
            </div>
            <span className="font-bold text-gray-900 text-lg">Resume Analyzer</span>
          </Link>

          {/* Navigation Links */}
          <div className="flex items-center gap-4">
            {isAuthenticated ? (
              <>
                <Link
                  to="/dashboard"
                  className="text-gray-600 hover:text-blue-600 font-medium text-sm transition-colors"
                >
                  Dashboard
                </Link>
                <Link
                  to="/upload"
                  className="text-gray-600 hover:text-blue-600 font-medium text-sm transition-colors"
                >
                  Upload
                </Link>
                {/* User greeting */}
                <span className="text-gray-500 text-sm">
                  Hi, <span className="font-semibold text-gray-800">{user?.name}</span>
                </span>
                <button
                  onClick={handleLogout}
                  className="btn-secondary text-sm px-4 py-2"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-gray-600 hover:text-blue-600 font-medium text-sm transition-colors"
                >
                  Login
                </Link>
                <Link to="/register" className="btn-primary text-sm px-4 py-2">
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
