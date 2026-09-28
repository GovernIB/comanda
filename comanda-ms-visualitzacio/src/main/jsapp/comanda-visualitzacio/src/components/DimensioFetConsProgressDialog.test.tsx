import { act, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import DimensioFetConsProgressDialog from './DimensioFetConsProgressDialog';

const mocks = vi.hoisted(() => ({
    subscribeMock: vi.fn(),
    tMock: vi.fn((selector: any, options?: Record<string, unknown>) => {
        const template = selector({
            page: {
                dimensions: {
                    action: {
                        refreshCons: {
                            progress: {
                                title: 'Actualitzant conselleries',
                                waiting: "Preparant l'actualització...",
                                processed: '{{processats}} de {{total}} processats',
                            },
                        },
                    },
                },
            },
        });
        return Object.entries(options ?? {}).reduce(
            (text: string, [key, value]) => text.split(`{{${key}}}`).join(String(value)),
            template
        );
    }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
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

describe('DimensioFetConsProgressDialog', () => {
    afterEach(() => {
        vi.clearAllMocks();
        lastListener = undefined;
    });

    it('DimensioFetConsProgressDialog_quanNoEstaObert_noEsRenderitzaRes', () => {
        render(<DimensioFetConsProgressDialog open={false} dimensioId={5} />);

        expect(screen.queryByText('Actualitzant conselleries')).not.toBeInTheDocument();
    });

    it('DimensioFetConsProgressDialog_quanSObreSenseEventsEncara_mostraProgresIndeterminatIMissatgeDEspera', () => {
        render(<DimensioFetConsProgressDialog open dimensioId={5} />);

        expect(screen.getByText('Actualitzant conselleries')).toBeInTheDocument();
        expect(screen.getByText("Preparant l'actualització...")).toBeInTheDocument();
        expect(screen.getByRole('progressbar')).not.toHaveAttribute('aria-valuenow');
    });

    it('DimensioFetConsProgressDialog_quanArribaUnEventDeProgresPelMateixId_actualitzaLaBarraDeterminada', () => {
        render(<DimensioFetConsProgressDialog open dimensioId={5} />);

        act(() => {
            lastListener?.({ type: 'dimensio.fetCons.progress', payload: { dimensioId: 5, processats: 3, total: 10 } });
        });

        expect(screen.getByText('3 de 10 processats')).toBeInTheDocument();
        expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '30');
    });

    it('DimensioFetConsProgressDialog_quanArribaUnEventDeProgresPerUnAltreId_lIgnora', () => {
        render(<DimensioFetConsProgressDialog open dimensioId={5} />);

        act(() => {
            lastListener?.({ type: 'dimensio.fetCons.progress', payload: { dimensioId: 99, processats: 3, total: 10 } });
        });

        expect(screen.getByText("Preparant l'actualització...")).toBeInTheDocument();
    });

    it('DimensioFetConsProgressDialog_quanEsTanca_reiniciaElProgresPerLaSeguentObertura', () => {
        const { rerender } = render(<DimensioFetConsProgressDialog open dimensioId={5} />);

        act(() => {
            lastListener?.({ type: 'dimensio.fetCons.progress', payload: { dimensioId: 5, processats: 3, total: 10 } });
        });
        expect(screen.getByText('3 de 10 processats')).toBeInTheDocument();

        rerender(<DimensioFetConsProgressDialog open={false} dimensioId={5} />);
        rerender(<DimensioFetConsProgressDialog open dimensioId={5} />);

        expect(screen.getByText("Preparant l'actualització...")).toBeInTheDocument();
    });

    it('DimensioFetConsProgressDialog_quanElProgresArribaAl100Per100_cridaOnCompleteSenseError', () => {
        const onComplete = vi.fn();
        render(<DimensioFetConsProgressDialog open dimensioId={5} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'dimensio.fetCons.progress', payload: { dimensioId: 5, processats: 10, total: 10 } });
        });

        expect(onComplete).toHaveBeenCalledWith(false);
    });

    it('DimensioFetConsProgressDialog_quanElProgresEsParcial_noCridaOnComplete', () => {
        const onComplete = vi.fn();
        render(<DimensioFetConsProgressDialog open dimensioId={5} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'dimensio.fetCons.progress', payload: { dimensioId: 5, processats: 3, total: 10 } });
        });

        expect(onComplete).not.toHaveBeenCalled();
    });

    it('DimensioFetConsProgressDialog_quanArribaUnEventDErrorPelMateixId_cridaOnCompleteAmbError', () => {
        const onComplete = vi.fn();
        render(<DimensioFetConsProgressDialog open dimensioId={5} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'dimensio.fetCons.progress', payload: { dimensioId: 5, processats: 4, total: 10, error: true } });
        });

        expect(onComplete).toHaveBeenCalledWith(true);
    });

    it('DimensioFetConsProgressDialog_quanArribaUnEventDErrorPerUnAltreId_noCridaOnComplete', () => {
        const onComplete = vi.fn();
        render(<DimensioFetConsProgressDialog open dimensioId={5} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'dimensio.fetCons.progress', payload: { dimensioId: 99, processats: 4, total: 10, error: true } });
        });

        expect(onComplete).not.toHaveBeenCalled();
    });
});
