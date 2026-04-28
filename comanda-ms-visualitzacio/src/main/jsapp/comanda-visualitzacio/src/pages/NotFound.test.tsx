import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import NotFound from './NotFound';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: (input: { page: { notFound: string } }) => string) =>
        selector({ page: { notFound: 'Pàgina no trobada' } })
    ),
}));

vi.mock('reactlib', () => ({
    BasePage: ({ children }: { children: React.ReactNode }) => (
        <div data-testid="base-page">{children}</div>
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

describe('NotFound', () => {
    it('NotFound_quanEsRenderitza_mostraElMissatgeDerrorDinsLaPaginaBase', () => {
        // Comprova que la pàgina de no trobat presenta el text traduït dins l’estructura base.
        render(<NotFound />);

        expect(screen.getByTestId('base-page')).toBeInTheDocument();
        expect(screen.getByText('Pàgina no trobada')).toBeInTheDocument();
    });
});
