/**
 * ─────────────────────────────────────────────────────────
 * authService.js — Auth API Calls
 * Location: src/services/authService.js
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Keeps all authentication-related API calls in one place.
 * Components call these functions instead of calling axios directly.
 * This is called the "Service Layer" pattern in frontend code.
 *
 * HOW IT CONNECTS:
 * LoginPage → authService.login() → api.post('/auth/login') → Spring Boot
 * RegisterPage → authService.register() → api.post('/auth/register') → Spring Boot
 */

import api from './api';

export const authService = {
  /**
   * Register a new user.
   * @param {Object} data - { name, email, password }
   * @returns {Promise} - { token, email, name }
   */
  register: (data) => api.post('/auth/register', data),

  /**
   * Login with existing credentials.
   * @param {Object} data - { email, password }
   * @returns {Promise} - { token, email, name }
   */
  login: (data) => api.post('/auth/login', data),
};
