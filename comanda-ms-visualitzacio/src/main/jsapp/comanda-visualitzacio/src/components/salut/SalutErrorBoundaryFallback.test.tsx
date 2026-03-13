import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { SalutErrorBoundaryFallback } from './SalutErrorBoundaryFallback';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: (input: { page: { salut: { latencia: { error: string } } } }) => string) =>
        selector({ page: { salut: { latencia: { error: 'Error carregant la latència' } } } })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

describe('SalutErrorBoundaryFallback', () => {
    it('SalutErrorBoundaryFallback_quanEsRenderitza_mostraElMissatgeDerrorTraduït', () => {
        // Comprova que el fallback d'error mostra el text traduït de l'error de latència.
        render(<SalutErrorBoundaryFallback />);

        expect(screen.getByText('Error carregant la latència')).toBeInTheDocument();
    });
});
