/// <reference types="vitest" />
import path from 'path'
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

// WSL2 compatibility: WSL_DISTRO_NAME is set by Windows when running inside WSL2.
// Force the pure-JS rollup fallback so the missing linux-x64-gnu native binding
// (which npm may not download when invoked from the Windows side) does not crash builds.
if (process.env.WSL_DISTRO_NAME) {
  process.env.ROLLDOWN_SKIP_NATIVE = '1'
}

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    css: false,
  },
})
