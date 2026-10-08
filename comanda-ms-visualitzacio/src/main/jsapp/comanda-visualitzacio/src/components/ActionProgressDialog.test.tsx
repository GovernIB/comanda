import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import ActionProgressDialog from './ActionProgressDialog';

const mocks = vi.hoisted(() => ({
    subscribeMock: vi.fn(),
}));

let lastListener: ((event: any) => void) | undefined;
vi.mock('./SseProvider.tsx', () => ({
    useSseContext: () => ({
        subscribe: (_eventType: string, listener: (event: any) => void) => {
            lastListener = listener;
            mocks.subscribeMock(_eventType, listener);
            return () => {
                lastListener = undefined;
            };
        },
    }),
}));

describe('ActionProgressDialog', () => {
    afterEach(() => {
        vi.clearAllMocks();
        lastListener = undefined;
    });

    const defaultProps = {
        open: true,
        itemId: 42,
        eventType: 'custom.action.progress',
        title: 'Títol de prova',
        waitingMessage: 'Esperant...',
        processedMessage: (processats: number, total: number) => `${processats} de ${total} fets`,
        hideButtonLabel: 'Amagar',
    };

    it('ActionProgressDialog_quanNoEstaObert_noEsRenderitzaRes', () => {
        render(<ActionProgressDialog {...defaultProps} open={false} />);

        expect(screen.queryByText('Títol de prova')).not.toBeInTheDocument();
    });

    it('ActionProgressDialog_quanSObreSenseEventsEncara_mostraProgresIndeterminatIMissatgeDEspera', () => {
        render(<ActionProgressDialog {...defaultProps} />);

        expect(screen.getByText('Títol de prova')).toBeInTheDocument();
        expect(screen.getByText('Esperant...')).toBeInTheDocument();
        expect(screen.getByRole('progressbar')).not.toHaveAttribute('aria-valuenow');
    });

    it('ActionProgressDialog_quanArribaUnEventDeProgresPelMateixId_actualitzaLaBarraDeterminada', () => {
        render(<ActionProgressDialog {...defaultProps} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42, processats: 5, total: 20 } });
        });

        expect(screen.getByText('5 de 20 fets')).toBeInTheDocument();
        expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '25');
    });

    it('ActionProgressDialog_quanArribaUnEventDeProgresPerUnAltreId_lIgnora', () => {
        render(<ActionProgressDialog {...defaultProps} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 999, processats: 5, total: 20 } });
        });

        expect(screen.getByText('Esperant...')).toBeInTheDocument();
    });

    it('ActionProgressDialog_quanEsTanca_reiniciaElProgresPerLaSeguentObertura', () => {
        const { rerender } = render(<ActionProgressDialog {...defaultProps} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42, processats: 5, total: 20 } });
        });
        expect(screen.getByText('5 de 20 fets')).toBeInTheDocument();

        rerender(<ActionProgressDialog {...defaultProps} open={false} />);
        rerender(<ActionProgressDialog {...defaultProps} open={true} />);

        expect(screen.getByText('Esperant...')).toBeInTheDocument();
    });

    it('ActionProgressDialog_quanElProgresArribaAlTotal_cridaOnCompleteSenseError', () => {
        const onComplete = vi.fn();
        render(<ActionProgressDialog {...defaultProps} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42, processats: 20, total: 20 } });
        });

        expect(onComplete).toHaveBeenCalledWith(false);
    });

    it('ActionProgressDialog_quanElProgresEsParcial_noCridaOnComplete', () => {
        const onComplete = vi.fn();
        render(<ActionProgressDialog {...defaultProps} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42, processats: 5, total: 20 } });
        });

        expect(onComplete).not.toHaveBeenCalled();
    });

    it('ActionProgressDialog_quanElTotalEsZero_cridaOnCompleteSenseErrorImmediatament', () => {
        const onComplete = vi.fn();
        render(<ActionProgressDialog {...defaultProps} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42, processats: 0, total: 0 } });
        });

        expect(onComplete).toHaveBeenCalledWith(false);
    });

    it('ActionProgressDialog_quanArribaUnEventDErrorPelMateixId_cridaOnCompleteAmbError', () => {
        const onComplete = vi.fn();
        render(<ActionProgressDialog {...defaultProps} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42, processats: 3, total: 20, error: true } });
        });

        expect(onComplete).toHaveBeenCalledWith(true);
    });

    it('ActionProgressDialog_quanArribaUnEventDErrorPerUnAltreId_noCridaOnComplete', () => {
        const onComplete = vi.fn();
        render(<ActionProgressDialog {...defaultProps} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 999, processats: 3, total: 20, error: true } });
        });

        expect(onComplete).not.toHaveBeenCalled();
    });

    it('ActionProgressDialog_quanEsPremElBotoAmagar_cridaOnHide', () => {
        const onHide = vi.fn();
        render(<ActionProgressDialog {...defaultProps} onHide={onHide} />);

        fireEvent.click(screen.getByText('Amagar'));

        expect(onHide).toHaveBeenCalledTimes(1);
    });

    it('ActionProgressDialog_quanEsProporcionaGetItemIdPersonalitzat_lUtilitza', () => {
        const getItemId = vi.fn((payload: any) => payload.customKey);
        render(<ActionProgressDialog {...defaultProps} getItemId={getItemId} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { customKey: 42, processats: 2, total: 10 } });
        });

        expect(getItemId).toHaveBeenCalled();
        expect(screen.getByText('2 de 10 fets')).toBeInTheDocument();
    });

    it('ActionProgressDialog_suportaFallbackAutomaticDId', () => {
        render(<ActionProgressDialog {...defaultProps} itemId="dim-10" />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { dimensioId: 'dim-10', processats: 4, total: 10 } });
        });

        expect(screen.getByText('4 de 10 fets')).toBeInTheDocument();
    });

    it('ActionProgressDialog_quanArribenMultiplesEventsDespresDeCompletar_cridaOnCompleteNomesUnCop', () => {
        const onComplete = vi.fn();
        render(<ActionProgressDialog {...defaultProps} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42, processats: 20, total: 20 } });
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42, processats: 20, total: 20 } });
        });

        expect(onComplete).toHaveBeenCalledTimes(1);
        expect(onComplete).toHaveBeenCalledWith(false);
    });

    it('ActionProgressDialog_quanArribaEventSenseTotal_lIgnoraINoCompleta', () => {
        const onComplete = vi.fn();
        render(<ActionProgressDialog {...defaultProps} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42 } });
        });

        expect(onComplete).not.toHaveBeenCalled();
    });

    it('ActionProgressDialog_quanItemIdEsDefinitIPayloadNoTeId_lIgnora', () => {
        const onComplete = vi.fn();
        render(<ActionProgressDialog {...defaultProps} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { processats: 20, total: 20 } });
        });

        expect(onComplete).not.toHaveBeenCalled();
    });

    it('ActionProgressDialog_quanItemIdEsNullIPayloadTeId_lIgnora', () => {
        const onComplete = vi.fn();
        render(<ActionProgressDialog {...defaultProps} itemId={null} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'custom.action.progress', payload: { id: 42, processats: 20, total: 20 } });
        });

        expect(onComplete).not.toHaveBeenCalled();
    });
});
