import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import NotFound from './NotFound';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: (input: { page: { notFound: string } }) => string) =>
        selector({ page: { notFound: 'Pàgina no trobada' } })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

describe('NotFound', () => {
    it('NotFound_quanEsRenderitza_mostraElMissatgeDerror', () => {
        // Comprova que la pàgina de no trobat presenta el text traduït en pantalla.
        render(<NotFound />);

        expect(screen.getByText('Pàgina no trobada')).toBeInTheDocument();
    });

    it('NotFound_quanEsRenderitza_mostraElMissatgeDinsDelMarcDePantalla', () => {
        // Comprova que el missatge queda dins el marc lot2 de la pantalla.
        render(<NotFound />);

        const frame = document.querySelector('.screen-frame') as HTMLElement;
        expect(frame).not.toBeNull();
        expect(frame).toHaveTextContent('Pàgina no trobada');
    });
});
