import { act, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EntitatRefreshUOProgressDialog from './EntitatRefreshUOProgressDialog';

const mocks = vi.hoisted(() => ({
    subscribeMock: vi.fn(),
    tMock: vi.fn((selector: any, options?: Record<string, unknown>) => {
        const template = selector({
            page: {
                entitats: {
                    action: {
                        refreshUO: {
                            progress: {
                                title: 'Actualitzant unitats organitzatives',
                                waiting: "Preparant l'actualització...",
                                processed: '{{processats}} de {{total}} processades',
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

describe('EntitatRefreshUOProgressDialog', () => {
    afterEach(() => {
        vi.clearAllMocks();
        lastListener = undefined;
    });

    it('EntitatRefreshUOProgressDialog_quanNoEstaObert_noEsRenderitzaRes', () => {
        render(<EntitatRefreshUOProgressDialog open={false} entitatId={5} />);

        expect(screen.queryByText('Actualitzant unitats organitzatives')).not.toBeInTheDocument();
    });

    it('EntitatRefreshUOProgressDialog_quanSObreSenseEventsEncara_mostraProgresIndeterminatIMissatgeDEspera', () => {
        render(<EntitatRefreshUOProgressDialog open entitatId={5} />);

        expect(screen.getByText('Actualitzant unitats organitzatives')).toBeInTheDocument();
        expect(screen.getByText("Preparant l'actualització...")).toBeInTheDocument();
        expect(screen.getByRole('progressbar')).not.toHaveAttribute('aria-valuenow');
    });

    it('EntitatRefreshUOProgressDialog_quanArribaUnEventDeProgresPelMateixId_actualitzaLaBarraDeterminada', () => {
        render(<EntitatRefreshUOProgressDialog open entitatId={5} />);

        act(() => {
            lastListener?.({ type: 'entitat.refreshUO.progress', payload: { entitatId: 5, processats: 3, total: 10 } });
        });

        expect(screen.getByText('3 de 10 processades')).toBeInTheDocument();
        expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '30');
    });

    it('EntitatRefreshUOProgressDialog_quanArribaUnEventDeProgresPerUnAltreId_lIgnora', () => {
        render(<EntitatRefreshUOProgressDialog open entitatId={5} />);

        act(() => {
            lastListener?.({ type: 'entitat.refreshUO.progress', payload: { entitatId: 99, processats: 3, total: 10 } });
        });

        expect(screen.getByText("Preparant l'actualització...")).toBeInTheDocument();
    });

    it('EntitatRefreshUOProgressDialog_quanEsTanca_reiniciaElProgresPerLaSeguentObertura', () => {
        const { rerender } = render(<EntitatRefreshUOProgressDialog open entitatId={5} />);

        act(() => {
            lastListener?.({ type: 'entitat.refreshUO.progress', payload: { entitatId: 5, processats: 3, total: 10 } });
        });
        expect(screen.getByText('3 de 10 processades')).toBeInTheDocument();

        rerender(<EntitatRefreshUOProgressDialog open={false} entitatId={5} />);
        rerender(<EntitatRefreshUOProgressDialog open entitatId={5} />);

        expect(screen.getByText("Preparant l'actualització...")).toBeInTheDocument();
    });

    it('EntitatRefreshUOProgressDialog_quanElProgresArribaAl100Per100_cridaOnCompleteSenseError', () => {
        const onComplete = vi.fn();
        render(<EntitatRefreshUOProgressDialog open entitatId={5} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'entitat.refreshUO.progress', payload: { entitatId: 5, processats: 10, total: 10 } });
        });

        expect(onComplete).toHaveBeenCalledWith(false);
    });

    it('EntitatRefreshUOProgressDialog_quanElProgresEsParcial_noCridaOnComplete', () => {
        const onComplete = vi.fn();
        render(<EntitatRefreshUOProgressDialog open entitatId={5} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'entitat.refreshUO.progress', payload: { entitatId: 5, processats: 3, total: 10 } });
        });

        expect(onComplete).not.toHaveBeenCalled();
    });

    it('EntitatRefreshUOProgressDialog_quanLEntitatNoTeCapUnitatATotal0_cridaOnCompleteSenseError', () => {
        // Entitat sense cap unitat organitzativa a Dir3: l'únic event que arribarà és (0 de 0), i cal que
        // igualment es doni el procés per acabat (vegeu el comentari a EntitatRefreshUOProgressDialog).
        const onComplete = vi.fn();
        render(<EntitatRefreshUOProgressDialog open entitatId={5} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'entitat.refreshUO.progress', payload: { entitatId: 5, processats: 0, total: 0 } });
        });

        expect(onComplete).toHaveBeenCalledWith(false);
    });

    it('EntitatRefreshUOProgressDialog_quanArribaUnEventDErrorPelMateixId_cridaOnCompleteAmbError', () => {
        const onComplete = vi.fn();
        render(<EntitatRefreshUOProgressDialog open entitatId={5} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'entitat.refreshUO.progress', payload: { entitatId: 5, processats: 4, total: 10, error: true } });
        });

        expect(onComplete).toHaveBeenCalledWith(true);
    });

    it('EntitatRefreshUOProgressDialog_quanArribaUnEventDErrorPerUnAltreId_noCridaOnComplete', () => {
        const onComplete = vi.fn();
        render(<EntitatRefreshUOProgressDialog open entitatId={5} onComplete={onComplete} />);

        act(() => {
            lastListener?.({ type: 'entitat.refreshUO.progress', payload: { entitatId: 99, processats: 4, total: 10, error: true } });
        });

        expect(onComplete).not.toHaveBeenCalled();
    });
});
