import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { DashboardWidget } from './DashboardWidget';

const mocks = vi.hoisted(() => ({
    useBaseAppContextMock: vi.fn(),
    numberFormatMock: vi.fn((value: number, _options: object, _language?: string) => `fmt:${value}`),
}));

vi.mock('reactlib', () => ({
    useBaseAppContext: () => mocks.useBaseAppContextMock(),
    numberFormat: (value: number, options: object, language?: string) =>
        mocks.numberFormatMock(value, options, language),
}));

const renderComponent = (ui: React.ReactElement) =>
    render(<ThemeProvider theme={createTheme()}>{ui}</ThemeProvider>);

describe('DashboardWidget', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('DashboardWidget_quanEsRenderitzaAmbRuta_mantéElComponentComAEnllac', () => {
        // Comprova que el widget es pot renderitzar com a enllaç quan rep una ruta de navegació.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca',
            getLinkComponent: () =>
                React.forwardRef<HTMLAnchorElement, { to?: string; children?: React.ReactNode }>(
                    ({ to, children, ...rest }, ref) => (
                        <a href={to} ref={ref} {...rest}>
                            {children}
                        </a>
                    )
                ),
        });

        renderComponent(<DashboardWidget resourceName="dashboard" to="/dashboard" icon="apps" />);

        expect(screen.getByRole('link')).toHaveAttribute('href', '/dashboard');
    });

    it('DashboardWidget_quanLaDadaEncaraCarrega_mostraElsSkeletons', () => {
        // Verifica que el component mostra placeholders mentre el codi actual està en estat de càrrega.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca',
            getLinkComponent: () => 'a',
        });

        const { container } = renderComponent(<DashboardWidget resourceName="dashboard" />);

        expect(container.querySelectorAll('.MuiSkeleton-root').length).toBeGreaterThan(0);
    });
});
