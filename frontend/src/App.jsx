/**
 * ─────────────────────────────────────────────────────────
 * App.jsx — Root Component with Routing
 * Location: src/App.jsx
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * App.jsx is the root component. It defines all the routes
 * and wraps pages in the Navbar.
 *
 * REACT ROUTER CONCEPTS:
 *
 * BrowserRouter: (in main.jsx) wraps the entire app to enable routing.
 *   Uses the HTML5 History API (window.history) for clean URLs.
 *
 * Routes: Container that renders the first matching route.
 *
 * Route path="/login" element={<LoginPage />}:
 *   When URL is /login, render LoginPage.
 *
 * <Navigate to="/dashboard" />:
 *   If user goes to "/" and is logged in, redirect to dashboard.
 *
 * <ProtectedRoute>:
 *   Wraps pages that require login.
 *   If not logged in → redirects to /login.
 *
 * ROUTE STRUCTURE:
 * /              → redirect (to /dashboard if logged in, /login if not)
 * /login         → LoginPage (public)
 * /register      → RegisterPage (public)
 * /dashboard     → DashboardPage (protected)
 * /upload        → UploadPage (protected)
 * /analysis/:id  → AnalysisPage (protected)
 */

import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import UploadPage from './pages/UploadPage';
import AnalysisPage from './pages/AnalysisPage';

const App = () => {
  const isLoggedIn = !!localStorage.getItem('token');

  return (
    <div className="min-h-screen flex flex-col">
      {/* Navbar is always shown at the top */}
      <Navbar />

      {/* Main content area */}
      <main className="flex-1">
        <Routes>
          {/* Root redirect */}
          <Route
            path="/"
            element={isLoggedIn ? <Navigate to="/dashboard" /> : <Navigate to="/login" />}
          />

          {/* Public Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected Routes — require JWT */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/upload"
            element={
              <ProtectedRoute>
                <UploadPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/analysis/:id"
            element={
              <ProtectedRoute>
                <AnalysisPage />
              </ProtectedRoute>
            }
          />

          {/* 404 Fallback */}
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </main>
    </div>
  );
};

export default App;
