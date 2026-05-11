import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [
      react(),
      tailwindcss(),
    ],
    define: {
      global: 'window',
    },
    server: {
      port: 5173,
      host: true,
      proxy: {
        '/api': {
          target: env.VITE_API_TARGET || 'http://localhost:80',
          changeOrigin: true,
          secure: false,
        },

        '/ws-quiz': {
          target: env.VITE_WS_BASE_URL || 'ws://localhost:80',
          ws: true,
          changeOrigin: true,
          secure: false,
        }
      }
    }
  }
})
