import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import MuiThemeProvider from './MuiThemeProvider';

const mocks = vi.hoisted(() => ({
    useUserContextMock: vi.fn(),
    useMediaQueryMock: vi.fn(),
    themeProviderMock: vi.fn(
        ({ theme, children }: { theme: { name: string }; children?: React.ReactNode }) => (
            <div data-testid="theme-provider" data-theme={theme.name}>
                {children}
            </div>
        )
    ),
}));

vi.mock('./UserContext', () => ({
    useUserContext: () => mocks.useUserContextMock(),
}));

vi.mock('../theme', () => ({
    darkTheme: { name: 'dark' },
    lightTheme: { name: 'light' },
}));

vi.mock('@mui/material', async () => {
    const actual = await vi.importActual<typeof import('@mui/material')>('@mui/material');
    return {
        ...actual,
        useMediaQuery: () => mocks.useMediaQueryMock(),
    };
});

vi.mock('@emotion/react', () => ({
    ThemeProvider: (props: { theme: { name: string }; children?: React.ReactNode }) =>
        mocks.themeProviderMock(props),
}));

describe('MuiThemeProvider', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('MuiThemeProvider_quanNoHiHaUsuari_usaElTemaDelSistema', () => {
        // Comprova que el component usa el tema del sistema quan no hi ha preferència d'usuari.
        mocks.useUserContextMock.mockReturnValue({ user: null });
        mocks.useMediaQueryMock.mockReturnValue(true);

        render(
            <MuiThemeProvider>
                <div>Contingut</div>
            </MuiThemeProvider>
        );

        expect(screen.getByTestId('theme-provider')).toHaveAttribute('data-theme', 'dark');
        expect(screen.getByText('Contingut')).toBeInTheDocument();
    });

    it('MuiThemeProvider_quanLUsuariDefineixTema_clavaLaPreferenciaDeLUsuari', () => {
        // Verifica que la preferència explícita de l'usuari preval sobre el tema del sistema.
        mocks.useUserContextMock.mockReturnValue({ user: { temaObscur: false } });
        mocks.useMediaQueryMock.mockReturnValue(true);

        render(
            <MuiThemeProvider>
                <div>Contingut</div>
            </MuiThemeProvider>
        );

        expect(screen.getByTestId('theme-provider')).toHaveAttribute('data-theme', 'light');
    });
});
