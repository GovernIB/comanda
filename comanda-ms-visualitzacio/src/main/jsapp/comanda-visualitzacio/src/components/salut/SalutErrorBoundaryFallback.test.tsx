import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { SalutErrorBoundaryFallback } from './SalutErrorBoundaryFallback';

const translations = {
    common: {
        error: 'Error',
    },
    page: {
        salut: {
            latencia: {
                error: 'Hi ha hagut un error al mostrar el component',
                errorDetailsButton: "Veure detall de l'error",
                errorDetailsTitle: "Detall de l'error",
                errorDetailsClose: 'Tancar',
            },
        },
        widget: {
            noErrorTrace: "No hi ha traça de l'error disponible",
        },
    },
    components: {
        copiarContingut: 'Copiar contingut',
        copiarContingutTitle: 'Copiar el contingut',
        copiarContingutSuccess: 'Contingut copiat al portapapers',
        copiarContingutError: 'Acció no suportada pel navegador',
    },
};

const mocks = vi.hoisted(() => ({
    tMock: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

describe('SalutErrorBoundaryFallback', () => {
    beforeEach(() => {
        mocks.tMock.mockImplementation((selector: (input: typeof translations) => string) => selector(translations));
        vi.stubGlobal('navigator', {
            clipboard: {
                writeText: vi.fn(),
            },
        });
        vi.stubGlobal('isSecureContext', true);
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.restoreAllMocks();
        vi.clearAllMocks();
    });

    it('SalutErrorBoundaryFallback_quanEsRenderitza_mostraElMissatgeDerrorTraduït', () => {
        // Comprova que el fallback d'error mostra el text traduït de l'error de latència.
        render(<SalutErrorBoundaryFallback />);

        expect(screen.getByText('Hi ha hagut un error al mostrar el component')).toBeInTheDocument();
    });

    it('SalutErrorBoundaryFallback_quanEsRenderitza_mostraLaIconaDeDetallDeLerror', () => {
        // Comprova que hi ha una icona/botó al costat del missatge per veure el detall de l'error.
        render(<SalutErrorBoundaryFallback error={new Error('boom')} />);

        expect(screen.getByRole('button', { name: "Veure detall de l'error" })).toBeInTheDocument();
    });

    it('SalutErrorBoundaryFallback_quanEsPremLaIcona_obreElDialogAmbLaTracaDeLerror', () => {
        // Comprova que en clicar la icona s'obre un diàleg amb la traça de l'error.
        const error = new Error('boom');
        error.stack = 'Error: boom\n    at algunLloc.tsx:10';
        render(<SalutErrorBoundaryFallback error={error} />);

        fireEvent.click(screen.getByRole('button', { name: "Veure detall de l'error" }));

        expect(screen.getByText("Detall de l'error")).toBeInTheDocument();
        expect(
            screen.getByText((content, element) =>
                element?.tagName.toLowerCase() === 'pre' && content.includes('at algunLloc.tsx:10')
            )
        ).toBeInTheDocument();
    });

    it('SalutErrorBoundaryFallback_quanLerrorNoTeTraca_mostraElMissatgeDerrorComAFallback', () => {
        // Comprova que si l'error no té traça, es mostra el missatge de l'error com a alternativa.
        const error = { message: 'boom sense traça' };
        render(<SalutErrorBoundaryFallback error={error} />);

        fireEvent.click(screen.getByRole('button', { name: "Veure detall de l'error" }));

        expect(
            screen.getByText((content, element) =>
                element?.tagName.toLowerCase() === 'pre' && content.includes('boom sense traça')
            )
        ).toBeInTheDocument();
    });

    it('SalutErrorBoundaryFallback_quanEsPremLaIcona_noPropagaElMouseDownAlPare', () => {
        // Comprova que el mousedown a la icona no arriba al contenidor pare, per evitar que
        // el mode disseny del dashboard interpreti el clic com una selecció del widget.
        const parentMouseDown = vi.fn();
        render(
            <div onMouseDown={parentMouseDown}>
                <SalutErrorBoundaryFallback error={new Error('boom')} />
            </div>
        );

        fireEvent.mouseDown(screen.getByRole('button', { name: "Veure detall de l'error" }));

        expect(parentMouseDown).not.toHaveBeenCalled();
    });

    it('SalutErrorBoundaryFallback_quanEsPremTancar_tancaElDialog', async () => {
        // Comprova que el botó de tancar oculta el diàleg de detall.
        render(<SalutErrorBoundaryFallback error={new Error('boom')} />);

        fireEvent.click(screen.getByRole('button', { name: "Veure detall de l'error" }));
        expect(screen.getByText("Detall de l'error")).toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: 'Tancar' }));

        await waitFor(() => {
            expect(screen.queryByText("Detall de l'error")).not.toBeInTheDocument();
        });
    });
});
