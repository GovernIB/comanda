import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import CalendariProcessosDialog from './CalendariProcessosDialog';
import { ProcesBaixaPrioritat } from './CalendariTypes';

const mockT = vi.fn((selector: any) =>
    selector({
        common: {
            estat: 'Estat',
            actions: 'Accions',
        },
        calendari: {
            titol_processos_baixa_prioritat: 'Processos de baixa prioritat',
            sense_processos: 'No hi ha cap procés de baixa prioritat',
            estat_en_execucio: 'En execució',
            estat_finalitzat: 'Finalitzat',
            estat_error: 'Error',
            estat_cancelat: 'Cancel·lat',
            estat_pendent: 'Pendent',
            cancelar_proces: 'Cancel·lar procés',
            refrescar: 'Refrescar',
            progres: 'Progrés',
            periode: 'Període',
            entorn: 'Entorn',
            filtre_tots_entorns: 'Tots els entorns',
            filtre_entorn_actual: 'Només entorn actual',
            estat: 'Estat',
            accions: 'Accions',
            tancar: 'Tancar',
            dia: 'dia',
            dies: 'dies',
            errors: 'errors',
            processant: 'Processant:',
            velocitat: 'Velocitat',
            prioritat_baixa: 'Baixa (10 minuts)',
            prioritat_mitja: 'Mitja (3 minuts)',
            prioritat_alta: 'Alta (Sense retard)',
        },
    })
);

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mockT,
    }),
}));

describe('CalendariProcessosDialog', () => {
    const mockProcessos: ProcesBaixaPrioritat[] = [
        {
            id: 'proc-1',
            entornAppId: 10,
            entornAppNom: 'App Test - PRO',
            dataInici: '2026-03-01',
            dataFi: '2026-03-10',
            estat: 'EN_EXECUCIO',
            totalDies: 10,
            diesProcessats: 5,
            diesAmbDades: 4,
            diesAmbErrors: 1,
            percentatge: 50,
            diaActual: '2026-03-05',
        },
        {
            id: 'proc-2',
            entornAppId: 20,
            entornAppNom: 'App Two - PRE',
            dataInici: '2026-02-01',
            dataFi: '2026-02-05',
            estat: 'FINALITZAT',
            totalDies: 5,
            diesProcessats: 5,
            diesAmbDades: 5,
            diesAmbErrors: 0,
            percentatge: 100,
        },
    ];

    it('quan open=false_noRenderitzaRes', () => {
        const { container } = render(
            <CalendariProcessosDialog
                open={false}
                onClose={vi.fn()}
                processos={[]}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
            />
        );
        expect(container.firstChild).toBeNull();
    });

    it('quanNoHiHaProcessos_mostraMissatgeBuit', () => {
        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={[]}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
            />
        );

        expect(screen.getByText('No hi ha cap procés de baixa prioritat')).toBeInTheDocument();
    });

    it('quanHiHaProcessos_renderitzaLaTaulaAmbDades', () => {
        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={mockProcessos}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
                entornAppId={10}
            />
        );

        // Per defecte mostra el de l'entornAppId=10
        expect(screen.getByText('App Test - PRO')).toBeInTheDocument();
        expect(screen.getByText('En execució')).toBeInTheDocument();
        expect(screen.getByText('50%')).toBeInTheDocument();
        expect(screen.getByText(/5 \/ 10 dies/)).toBeInTheDocument();
        expect(screen.getByTestId('btn-cancel-proc-1')).toBeInTheDocument();
    });

    it('quanCanviaElSwitchDeFiltre_cridaOnMostrarTotsChangePerqueElPareRefresqui', () => {
        const onMostrarTotsChangeMock = vi.fn();
        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={mockProcessos}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
                entornAppId={10}
                mostrarTots={false}
                onMostrarTotsChange={onMostrarTotsChangeMock}
            />
        );

        const switchElem = screen.getByTestId('switch-mostrar-tots');
        expect(switchElem).not.toBeChecked();
        fireEvent.click(switchElem);

        expect(onMostrarTotsChangeMock).toHaveBeenCalledWith(true);
    });

    it('quanEsPremRefrescar_cridaOnRefresh', () => {
        const onRefreshMock = vi.fn();
        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={mockProcessos}
                onRefresh={onRefreshMock}
                onCancelProces={vi.fn()}
            />
        );

        fireEvent.click(screen.getByTestId('btn-refresh-processos'));
        expect(onRefreshMock).toHaveBeenCalledTimes(1);
    });

    it('quanEsPremCancelarProces_cridaOnCancelProces', async () => {
        const onCancelMock = vi.fn().mockResolvedValue(true);
        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={mockProcessos}
                onRefresh={vi.fn()}
                onCancelProces={onCancelMock}
                entornAppId={10}
            />
        );

        fireEvent.click(screen.getByTestId('btn-cancel-proc-1'));
        await waitFor(() => {
            expect(onCancelMock).toHaveBeenCalledWith('proc-1');
        });
    });

    it('quanEsPremTancar_cridaOnClose', () => {
        const onCloseMock = vi.fn();
        render(
            <CalendariProcessosDialog
                open={true}
                onClose={onCloseMock}
                processos={[]}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', { name: 'Tancar' }));
        expect(onCloseMock).toHaveBeenCalledTimes(1);
    });

    it('quanProcesTeError_mostraElMissatgeDelProces', () => {
        const procesError: ProcesBaixaPrioritat = {
            id: 'proc-err',
            entornAppId: 10,
            entornAppNom: 'App Test - PRO',
            dataInici: '2026-03-01',
            dataFi: '2026-03-02',
            estat: 'ERROR',
            totalDies: 2,
            diesProcessats: 2,
            diesAmbDades: 0,
            diesAmbErrors: 2,
            percentatge: 100,
            missatge: 'Error de connexió amb el servidor remot',
        };

        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={[procesError]}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
                entornAppId={10}
            />
        );

        expect(screen.getByText('Error de connexió amb el servidor remot')).toBeInTheDocument();
        expect(screen.getByTestId('msg-proces-proc-err')).toBeInTheDocument();
    });

    it('quanProcesFinalitzatAmbErrors_mostraElMissatgeIComptadorsTraduits', () => {
        const procesParcial: ProcesBaixaPrioritat = {
            id: 'proc-parcial',
            entornAppId: 10,
            entornAppNom: 'App Test - PRO',
            dataInici: '2026-03-01',
            dataFi: '2026-03-05',
            estat: 'FINALITZAT',
            totalDies: 5,
            diesProcessats: 5,
            diesAmbDades: 3,
            diesAmbErrors: 2,
            percentatge: 100,
            missatge: 'Procés finalitzat amb 2 dies amb error',
        };

        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={[procesParcial]}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
                entornAppId={10}
            />
        );

        expect(screen.getByText('Procés finalitzat amb 2 dies amb error')).toBeInTheDocument();
        expect(screen.getByText(/5 \/ 5 dies/)).toBeInTheDocument();
        expect(screen.getByText(/\(2 errors\)/)).toBeInTheDocument();
    });

    it('quanTotalDiesEsUn_mostraSingularDia', () => {
        const procesUnDia: ProcesBaixaPrioritat = {
            id: 'proc-1-dia',
            entornAppId: 10,
            entornAppNom: 'App Test - PRO',
            dataInici: '2026-03-01',
            dataFi: '2026-03-01',
            estat: 'FINALITZAT',
            totalDies: 1,
            diesProcessats: 1,
            diesAmbDades: 1,
            diesAmbErrors: 0,
            percentatge: 100,
        };

        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={[procesUnDia]}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
                entornAppId={10}
            />
        );

        expect(screen.getByText('1 dia')).toBeInTheDocument();
        expect(screen.getByText(/1 \/ 1 dia/)).toBeInTheDocument();
    });

    it('quanMissatgeEsMoltLlarg_renderitzaElContenidorSenseErrors', () => {
        const stackTrace = 'Error: Connection timeout\n at fetch (http.ts:42)\n at process (worker.ts:100)'.repeat(20);
        const procesLlarg: ProcesBaixaPrioritat = {
            id: 'proc-long',
            entornAppId: 10,
            entornAppNom: 'App Test - PRO',
            dataInici: '2026-03-01',
            dataFi: '2026-03-02',
            estat: 'ERROR',
            totalDies: 2,
            diesProcessats: 1,
            diesAmbDades: 0,
            diesAmbErrors: 1,
            percentatge: 50,
            missatge: stackTrace,
        };

        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={[procesLlarg]}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
                entornAppId={10}
            />
        );

        const msgElem = screen.getByTestId('msg-proces-proc-long');
        expect(msgElem).toBeInTheDocument();
        expect(msgElem.textContent).toContain('Connection timeout');
    });

    it('quanProcessosTenenPausa_mostraChipVelocitatCorresponent', () => {
        const processosAmbPausa: ProcesBaixaPrioritat[] = [
            {
                id: 'p-baixa',
                entornAppId: 10,
                dataInici: '2026-03-01',
                dataFi: '2026-03-05',
                pausaMs: 600000,
                estat: 'EN_EXECUCIO',
                totalDies: 5,
                diesProcessats: 2,
                diesAmbDades: 2,
                diesAmbErrors: 0,
                percentatge: 40,
            },
            {
                id: 'p-mitja',
                entornAppId: 10,
                dataInici: '2026-03-01',
                dataFi: '2026-03-05',
                pausaMs: 180000,
                estat: 'EN_EXECUCIO',
                totalDies: 5,
                diesProcessats: 2,
                diesAmbDades: 2,
                diesAmbErrors: 0,
                percentatge: 40,
            },
            {
                id: 'p-alta',
                entornAppId: 10,
                dataInici: '2026-03-01',
                dataFi: '2026-03-05',
                pausaMs: 500,
                estat: 'EN_EXECUCIO',
                totalDies: 5,
                diesProcessats: 2,
                diesAmbDades: 2,
                diesAmbErrors: 0,
                percentatge: 40,
            },
        ];

        render(
            <CalendariProcessosDialog
                open={true}
                onClose={vi.fn()}
                processos={processosAmbPausa}
                onRefresh={vi.fn()}
                onCancelProces={vi.fn()}
            />
        );

        expect(screen.getByTestId('chip-prioritat-baixa')).toHaveTextContent('Baixa (10 minuts)');
        expect(screen.getByTestId('chip-prioritat-mitja')).toHaveTextContent('Mitja (3 minuts)');
        expect(screen.getByTestId('chip-prioritat-alta')).toHaveTextContent('Alta (Sense retard)');
    });
});
