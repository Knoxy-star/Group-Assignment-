import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'

// Every backend call the app makes uses a relative path like
// "/flood-service/api/incidents". In dev, Vite proxies each of those
// prefixes straight through to the gateway, so the browser sees the app
// and the API as the same origin (localhost:5173) - exactly the property
// the old Thymeleaf pages got for free by being served through the
// gateway. That means zero CORS configuration anywhere in the backend.
const GATEWAY = 'http://localhost:8080'
const SERVICE_PREFIXES = [
  '/auth-service',
  '/flood-service',
  '/drought-service',
  '/fire-service',
  '/zoonotic-disease-service',
  '/mining-accident-service',
  '/report-service',
  '/alert-service',
  '/dashboard-service',
]

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: Object.fromEntries(
      SERVICE_PREFIXES.map((prefix) => [prefix, { target: GATEWAY, changeOrigin: true }])
    ),
  },
})
