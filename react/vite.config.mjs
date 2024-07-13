import react from "@vitejs/plugin-react"
import {defineConfig} from "vite"

export default defineConfig({
    plugins: [
        react(),
    ],
    server: {
        host: '0.0.0.0',
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8101',
                changeOrigin: true,
            }
        }
    },
    build: {
        outDir: "build",
    },
})
