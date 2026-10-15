import { describe, expect, it, vi, beforeEach, beforeAll, afterAll } from 'vitest';
import { envVar } from 'reactlib';
import { getAuthConfig, getEnvApiUrl, isAuthUrlPresent } from './envUtils';

vi.mock('reactlib', () => ({
    envVar: vi.fn(),
}));

const mockEnvVar = vi.mocked(envVar);

const originalLocation = window.location;

beforeAll(() => {
    Object.defineProperty(window, 'location', {
        value: {
            protocol: 'https:',
            host: 'localhost',
            port: '3000',
            href: 'https://localhost:3000',
            pathname: '/',
            search: '',
            hash: '',
            assign: vi.fn(),
            replace: vi.fn(),
            reload: vi.fn(),
        },
        writable: true,
        configurable: true,
    });
});

afterAll(() => {
    Object.defineProperty(window, 'location', {
        value: originalLocation,
        writable: true,
        configurable: true,
    });
});

beforeEach(() => {
    vi.clearAllMocks();
    mockEnvVar.mockReturnValue(undefined);
});

describe('Configuració d\'entorn (envConfig)', () => {
    describe('getAuthConfig', () => {
        it('retorna la configuració d\'autenticació quan les variables existeixen', () => {
            mockEnvVar.mockImplementation((key: string) => {
                if (key === 'VITE_AUTH_PROVIDER_URL') return 'https://auth.example.com';
                if (key === 'VITE_AUTH_PROVIDER_REALM') return 'my-realm';
                if (key === 'VITE_AUTH_PROVIDER_CLIENTID') return 'my-client';
                return undefined;
            });

            const config = getAuthConfig();

            expect(config).toEqual({
                url: 'https://auth.example.com',
                realm: 'my-realm',
                clientId: 'my-client',
            });
        });
    });

    describe('getEnvApiUrl', () => {
        it('prioritza VITE_API_PUBLIC_URL sobre VITE_API_URL', () => {
            mockEnvVar.mockImplementation((key: string) => {
                if (key === 'VITE_API_PUBLIC_URL') return 'https://public.example.com';
                if (key === 'VITE_API_URL') return 'https://internal.example.com';
                return undefined;
            });

            expect(getEnvApiUrl()).toBe('https://public.example.com');
        });

        it('utilitza VITE_API_URL si VITE_API_PUBLIC_URL no existeix', () => {
            mockEnvVar.mockImplementation((key: string) => {
                if (key === 'VITE_API_URL') return 'https://internal.example.com';
                return undefined;
            });

            expect(getEnvApiUrl()).toBe('https://internal.example.com');
        });

        it('construeix la URL amb VITE_API_BASE_URL i el suffix per defecte (/api)', () => {
            mockEnvVar.mockImplementation((key: string) => {
                if (key === 'VITE_API_BASE_URL') return 'https://base.example.com';
                return undefined;
            });

            expect(getEnvApiUrl()).toBe('https://base.example.com/api');
        });

        it('construeix la URL amb VITE_API_BASE_URL i un suffix personalitzat', () => {
            mockEnvVar.mockImplementation((key: string) => {
                if (key === 'VITE_API_BASE_URL') return 'https://base.example.com';
                if (key === 'VITE_API_SUFFIX') return '/custom-api';
                return undefined;
            });

            expect(getEnvApiUrl()).toBe('https://base.example.com/custom-api');
        });

        it('fa fallback a window.location quan no hi ha cap variable d\'API definida', () => {
            expect(getEnvApiUrl()).toBe('https://localhost:3000/api');
        });
    });

    describe('isAuthUrlPresent', () => {
        it('retorna true quan VITE_AUTH_PROVIDER_URL està definit', () => {
            mockEnvVar.mockReturnValue('https://auth.example.com');
            expect(isAuthUrlPresent()).toBe(true);
        });

        it('retorna false quan VITE_AUTH_PROVIDER_URL és null o undefined', () => {
            mockEnvVar.mockReturnValue(null);
            expect(isAuthUrlPresent()).toBe(false);

            mockEnvVar.mockReturnValue(undefined);
            expect(isAuthUrlPresent()).toBe(false);
        });
    });
});
