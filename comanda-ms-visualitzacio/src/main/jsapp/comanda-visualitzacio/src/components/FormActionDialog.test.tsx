import { render } from '@testing-library/react';
import { act } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import FormActionDialog, { FormReportDialog } from './FormActionDialog';

const mocks = vi.hoisted(() => ({
    temporalMessageShowMock: vi.fn(),
    useMuiActionReportLogicMock: vi.fn(),
    execMock: vi.fn(),
    closeMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useMuiActionReportLogic: (...args: unknown[]) => mocks.useMuiActionReportLogicMock(...args),
}));

describe('FormActionDialog', () => {
    beforeEach(() => {
        mocks.useMuiActionReportLogicMock.mockReturnValue({
            available: true,
            formDialogComponent: <div>Diàleg</div>,
            exec: mocks.execMock,
            close: mocks.closeMock,
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('FormActionDialog_quanHiHaApiRef_exposaShowICloseAlComponentPare', () => {
        // Comprova que el component publica una API imperativa amb les funcions de mostrar i tancar.
        const apiRef = { current: null as { show: (id: any, data?: any) => void; close: () => void } | null };

        render(
            <FormActionDialog
                title="Acció"
                resourceName="widget"
                action="executa"
                initialOnChange={false}
                apiRef={apiRef}
            >
                <button>Fill</button>
            </FormActionDialog>
        );

        expect(apiRef.current).not.toBeNull();
        expect(typeof apiRef.current?.show).toBe('function');
        expect(apiRef.current?.close).toBe(mocks.closeMock);
    });

    it('FormActionDialog_quanEsCridaShowIEstaDisponible_executaLAccioAmbElTitolCalculat', () => {
        // Verifica que la funció show invoca l'executor amb l'identificador i el títol final.
        const apiRef = { current: null as { show: (id: any, data?: any) => void } | null };

        render(
            <FormActionDialog
                title={(data) => `Acció ${data.nom}`}
                resourceName="widget"
                action="executa"
                initialOnChange={false}
                apiRef={apiRef}
            >
                <button>Fill</button>
            </FormActionDialog>
        );

        act(() => {
            apiRef.current?.show(7, { nom: 'test' });
        });

        expect(mocks.execMock).toHaveBeenCalledWith(7, 'Acció test', { nom: 'test' });
    });

    it('FormReportDialog_quanEsCridaShow_executaElReportAmbElFitxerPerDefecte', () => {
        // Comprova que la variant de report reutilitza la mateixa API i executa el report amb el títol indicat.
        const apiRef = { current: null as { show: (id: any, data?: any) => void } | null };

        render(
            <FormReportDialog
                title="Informe"
                resourceName="widget"
                report="informe"
                reportFileType="PDF"
                initialOnChange={false}
                apiRef={apiRef}
            >
                <button>Fill</button>
            </FormReportDialog>
        );

        act(() => {
            apiRef.current?.show(9, { filtre: 'ok' });
        });

        expect(mocks.useMuiActionReportLogicMock).toHaveBeenCalledWith(
            'widget',
            undefined,
            'informe',
            'PDF',
            false,
            undefined,
            undefined,
            undefined,
            false,
            expect.anything(),
            undefined,
            undefined,
            undefined,
            undefined,
            expect.any(Function),
            expect.any(Function),
            expect.any(Function)
        );
        expect(mocks.execMock).toHaveBeenCalledWith(9, 'Informe', { filtre: 'ok' });
    });
});
