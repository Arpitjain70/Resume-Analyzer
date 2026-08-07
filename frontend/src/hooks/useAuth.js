/**
 * ─────────────────────────────────────────────────────────
 * useAuth.js — Custom React Hook for Authentication State
 * Location: src/hooks/useAuth.js
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * We need to know if a user is logged in from multiple components
 * (Navbar, ProtectedRoute, Dashboard). Instead of reading from
 * localStorage in every component, we centralize it here.
 *
 * WHAT IS A CUSTOM HOOK?
 * A custom hook is a regular JS function that starts with "use" and
 * can call other hooks (useState, useEffect, etc.).
 * It extracts reusable stateful logic from components.
 *
 * HOW IT WORKS:
 * - Reads user data from localStorage on mount
 * - Provides login() and logout() functions
 * - Components using this hook re-render when auth state changes
 *
 * STORAGE STRATEGY:
 * We store two things in localStorage:
 * - 'token': the JWT string (attached to API requests)
 * - 'user': JSON with { name, email } (for display in UI)
 */

import { useState, useEffect } from 'react';

export const useAuth = () => {
  // Initialize state from localStorage (persists across page refreshes)
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(() => localStorage.getItem('token'));

  /**
   * Called after successful login or register.
   * Stores token and user info in localStorage + state.
   */
  const login = (authResponse) => {
    const userData = {
      name: authResponse.name,
      email: authResponse.email,
    };
    localStorage.setItem('token', authResponse.token);
    localStorage.setItem('user', JSON.stringify(userData));
    setToken(authResponse.token);
    setUser(userData);
  };

  /**
   * Called when user clicks "Logout".
   * Clears localStorage + state.
   */
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  // isAuthenticated: true if we have a token
  const isAuthenticated = !!token;

  return { user, token, isAuthenticated, login, logout };
};
