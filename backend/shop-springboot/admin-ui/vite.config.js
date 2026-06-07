import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  base: '/admin/',
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    outDir: '../src/main/resources/static/admin',
    emptyOutDir: true
  },
  server: {
    host: '127.0.0.1',
    port: 5173,
    proxy: {
      '/admin/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/static/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
