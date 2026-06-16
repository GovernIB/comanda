import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { StacktraceBlock } from './RickTextDetail';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            components: {
                copiarContingutTitle: 'Copiar contingut',
                copiarContingut: 'Copiar',
                copiarContingutSuccess: 'Contingut copiat',
                copiarContingutError: 'Error al copiar',
            },
        })
    ),
    writeTextMock: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

describe('StacktraceBlock', () => {
    beforeEach(() => {
        vi.stubGlobal('navigator', {
            clipboard: {
                writeText: mocks.writeTextMock,
            },
        });
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.restoreAllMocks();
        vi.clearAllMocks();
    });

    it('StacktraceBlock_quanEsRenderitza_mostraElTitolIElContingut', () => {
        // Comprova que el bloc mostra el títol i el text rebut.
        render(<StacktraceBlock title="Stacktrace" value={'Línia 1\nLínia 2'} />);

        expect(screen.getByText('Stacktrace')).toBeInTheDocument();
        expect(
            screen.getByText((content, element) =>
                element?.tagName.toLowerCase() === 'pre' && content.includes('Línia 1')
            )
        ).toBeInTheDocument();
    });

    it('StacktraceBlock_quanEsPremCopiar_copiaElTextIMostraElSnackbar_quanEsSuportat', async () => {
        // Verifica que el botó copia el contingut i obre el missatge de confirmació quan isSecureContext és cert.
        vi.stubGlobal('isSecureContext', true);
        mocks.writeTextMock.mockResolvedValue(undefined);
        render(<StacktraceBlock title="Stacktrace" value="Error greu" />);

        fireEvent.click(screen.getByRole('button', { name: /Copiar/i }));

        await waitFor(() => {
            expect(mocks.writeTextMock).toHaveBeenCalledWith('Error greu');
        });
        expect(screen.getByText('Contingut copiat')).toBeInTheDocument();
    });

    it('StacktraceBlock_quanEsPremCopiar_mostraError_quanNoEsSuportat', async () => {
        // Verifica que el botó mostra error i NO copia quan isSecureContext és fals.
        vi.stubGlobal('isSecureContext', false);
        render(<StacktraceBlock title="Stacktrace" value="Error greu" />);

        fireEvent.click(screen.getByRole('button', { name: /Copiar/i }));

        await waitFor(() => {
            expect(screen.getByText('Error al copiar')).toBeInTheDocument();
        });
        expect(mocks.writeTextMock).not.toHaveBeenCalled();
    });
});
