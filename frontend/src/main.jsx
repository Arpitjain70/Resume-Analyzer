/**
 * ─────────────────────────────────────────────────────────
 * main.jsx — React Application Entry Point
 * Location: src/main.jsx
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * This is where React mounts itself onto the HTML page.
 *
 * document.getElementById('root') → finds the <div id="root"> in index.html
 * createRoot(rootElement) → creates a React root (React 18 API)
 * root.render(...) → renders our App into that div
 *
 * BrowserRouter:
 * Wraps the entire app to enable React Router.
 * Must be at the root level so all components can use useNavigate,
 * useParams, Link, etc.
 *
 * StrictMode:
 * React's development-only tool. In strict mode:
 * - Components render twice (intentionally) to detect side effects
 * - Deprecated APIs are flagged
 * - Only active in development, has zero impact in production
 */

import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);
