import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  base: './',
  server: {
    port: 5173,
    // Proxy: forward /api requests to the Spring Boot backend.
    // This avoids CORS issues during development because the browser
    // sees both as the same origin (localhost:5173).
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
