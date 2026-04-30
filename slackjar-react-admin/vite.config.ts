import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
import {mockDevServerPlugin} from "vite-plugin-mock-dev-server";

export default defineConfig({
    plugins: [
        react(),
        mockDevServerPlugin(),
    ],

    server: {
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:8024/slack',
                // target: 'http://106.54.26.72:8025/slack',
                // target: 'http://192.168.1.110:8025/slack',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, ''),
            },
        },
    },
})
