/// <reference types="vitest/config" />
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import svgr from "vite-plugin-svgr";
import tsconfigPaths from 'vite-tsconfig-paths';

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
    // Load env file based on `mode` in the current working directory.
    // Set the third parameter to '' to load all env regardless of the `VITE_` prefix.
    const env = loadEnv(mode, process.cwd(), '');

    return {
        preview: {
            port: 5173,
        },
        server: {
            open: env.DISABLE_OPEN_ON_START !== 'true',
        },
        plugins: [react(), tsconfigPaths(), svgr()],
        test: {
            globals: true,
            environment: 'jsdom',
            setupFiles: './vitest.setup.ts',
            clearMocks: true,
            restoreMocks: true,
            unstubGlobals: true,
            pool: 'forks',
            testTimeout: env.LOW_PERFORMANCE_TEST_MODE ? 10000 : undefined,
            reporters: ['default', 'junit', 'html'],
            outputFile: {
                junit: './test-reports/junit.xml',
                html: './test-reports/index.html',
            },
            coverage: {
                provider: 'v8',
                reportsDirectory: './coverage',
                reporter: ['text', 'json', 'json-summary', 'html', 'lcov'],
                exclude: ['lib/**'],
            },
        },
    };
});
