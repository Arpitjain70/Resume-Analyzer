/**
 * ─────────────────────────────────────────────────────────
 * api.js — Axios Instance (Base HTTP Client)
 * Location: src/services/api.js
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Instead of calling axios.get('http://localhost:8080/api/...') everywhere,
 * we create a configured axios instance with:
 * 1. Base URL already set (no need to repeat it)
 * 2. Request interceptor: automatically attaches JWT token to every request
 * 3. Response interceptor: handles 401 errors globally (auto logout)
 *
 * INTERCEPTORS:
 * Think of interceptors as middleware for HTTP calls.
 * - Request interceptor: runs BEFORE the request is sent
 * - Response interceptor: runs AFTER the response arrives
 *
 * JWT ATTACHMENT FLOW:
 * 1. User logs in → token stored in localStorage
 * 2. User makes any API call (e.g., upload resume)
 * 3. Request interceptor reads token from localStorage
 * 4. Adds "Authorization: Bearer <token>" header automatically
 * 5. Backend JwtAuthenticationFilter validates the token
 *
 * WITHOUT THIS FILE:
 * Every component would need to manually add the Authorization header.
 * With this file, it happens automatically for ALL requests.
 */

import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

// Create a configured axios instance
const api = axios.create({
  baseURL: baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,            // 30 seconds (Gemini AI calls can take time)
});

/**
 * REQUEST INTERCEPTOR
 * Runs before every HTTP request is sent.
 * Reads the JWT from localStorage and adds it to the Authorization header.
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;             // must return config to continue the request
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * RESPONSE INTERCEPTOR
 * Runs after every HTTP response arrives.
 * If the server returns 401 (Unauthorized), the token has expired.
 * We clear localStorage and redirect to login.
 */
api.interceptors.response.use(
  (response) => response,    // pass through successful responses
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid — force logout
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
