/**
 * ─────────────────────────────────────────────────────────
 * ProtectedRoute.jsx — Guard for Authenticated Routes
 * Location: src/components/ProtectedRoute.jsx
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Some pages (Dashboard, Upload) should only be visible to logged-in users.
 * Without this, anyone could navigate to /dashboard even without a token.
 *
 * HOW IT WORKS:
 * Wrap any route in <ProtectedRoute>. If the user is not authenticated,
 * they're redirected to /login instead of seeing the page.
 *
 * <Navigate replace to="/login" />:
 * - replace: replaces the current history entry (back button won't return to the guarded page)
 * - to="/login": where to redirect
 *
 * USAGE in App.jsx:
 * <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
 */

import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children }) => {
  // Check if token exists in localStorage
  const token = localStorage.getItem('token');

  if (!token) {
    // Not logged in → redirect to login page
    return <Navigate replace to="/login" />;
  }

  // Logged in → render the actual page component
  return children;
};

export default ProtectedRoute;
