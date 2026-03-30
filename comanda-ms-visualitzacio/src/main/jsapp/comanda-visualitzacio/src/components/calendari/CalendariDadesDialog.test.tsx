import { fireEvent, render, screen, within } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import CalendariDadesDialog from './CalendariDadesDialog';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: (input: any) => string) =>
        selector({
            calendari: {
                modal_dades_dia: 'Dades del dia',
                indicadors: 'Indicadors',
                sense_dades: 'Sense dades',
                tancar: 'Tancar',
            },
            components: { clear: 'Netejar' },
            page: { avisos: { filter: { more: 'Més filtres' } } },
        })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

describe('CalendariDadesDialog', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('CalendariDadesDialog_quanEstaTancat_noRenderitzaRes', () => {
        // Comprova que el diàleg no pinta cap element al DOM mentre està tancat.
        const { container } = render(
            <CalendariDadesDialog
                dimensions={[]}
                indicadors={[]}
                currentDadesDia={[]}
                currentDataDia="2026-03-13"
                dadesDiaModalOpen={false}
                setDadesDiaModalOpen={vi.fn()}
            />
        );

        expect(container).toBeEmptyDOMElement();
    });

    it('CalendariDadesDialog_quanNoHiHaDades_mostraElMissatgeBuitITancaElDialeg', () => {
        // Verifica que sense dades es mostra el missatge buit i el botó de tancar notifica el callback.
        const setOpenMock = vi.fn();

        render(
            <CalendariDadesDialog
                dimensions={[]}
                indicadors={[]}
                currentDadesDia={[]}
                currentDataDia="2026-03-13"
                dadesDiaModalOpen={true}
                setDadesDiaModalOpen={setOpenMock}
            />
        );

        expect(screen.getByText('Dades del dia - 13/03/2026')).toBeInTheDocument();
        expect(screen.getByText('Sense dades')).toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: 'Tancar' }));

        expect(setOpenMock).toHaveBeenCalledWith(false);
    });

    it('CalendariDadesDialog_quanHiHaDades_permetFiltrarIndicadorsIMostrarLaTaula', async () => {
        // Comprova que el diàleg permet seleccionar indicadors i filtrar files per dimensions.
        render(
            <CalendariDadesDialog
                dimensions={[
                    { codi: 'servei', nom: 'Servei' },
                    { codi: 'entorn', nom: 'Entorn' },
                ]}
                indicadors={[
                    { codi: 'ok', nom: 'Correctes' },
                    { codi: 'ko', nom: 'Erronis' },
                ]}
                currentDadesDia={[
                    {
                        temps: {
                            data: '2026-03-13',
                            anualitat: 2026,
                            trimestre: 1,
                            mes: 3,
                            setmana: 11,
                            dia: 13,
                            diaSetmana: 'DIVENDRES',
                        },
                        entornAppId: 1,
                        dimensionsJson: { servei: 'Padro', entorn: 'Produccio' },
                        indicadorsJson: { ok: 8, ko: 1 },
                    },
                    {
                        temps: {
                            data: '2026-03-13',
                            anualitat: 2026,
                            trimestre: 1,
                            mes: 3,
                            setmana: 11,
                            dia: 13,
                            diaSetmana: 'DIVENDRES',
                        },
                        entornAppId: 1,
                        dimensionsJson: { servei: 'Hisenda', entorn: 'Preproduccio' },
                        indicadorsJson: { ok: 5, ko: 0 },
                    },
                ]}
                currentDataDia="2026-03-13"
                dadesDiaModalOpen={true}
                setDadesDiaModalOpen={vi.fn()}
            />
        );

        expect(screen.getByRole('table')).toBeInTheDocument();
        expect(screen.getByText('Correctes')).toBeInTheDocument();
        expect(screen.getByText('Erronis')).toBeInTheDocument();
        expect(screen.getByText('Padro')).toBeInTheDocument();
        expect(screen.getByText('Hisenda')).toBeInTheDocument();

        const indicatorsCombo = screen.getByRole('combobox');
        fireEvent.mouseDown(indicatorsCombo);
        fireEvent.click(await screen.findByRole('option', { name: 'Erronis' }));

        expect(document.querySelector('[role="combobox"]')).toHaveTextContent('ok');

        fireEvent.click(screen.getByTitle('Més filtres'));
        fireEvent.change(screen.getByLabelText('Servei'), {
            target: { value: 'Padro' },
        });

        expect(screen.getByText('Padro')).toBeInTheDocument();
        expect(screen.queryByText('Hisenda')).not.toBeInTheDocument();
    });

    it('CalendariDadesDialog_quanEsPremNetejar_buidaElsFiltresIElsIndicadorsSeleccionats', async () => {
        // Verifica que l'acció de netejar buida els filtres i desmarca els indicadors visibles.
        render(
            <CalendariDadesDialog
                dimensions={[{ codi: 'servei', nom: 'Servei' }]}
                indicadors={[
                    { codi: 'ok', nom: 'Correctes' },
                    { codi: 'ko', nom: 'Erronis' },
                ]}
                currentDadesDia={[
                    {
                        temps: {
                            data: '2026-03-13',
                            anualitat: 2026,
                            trimestre: 1,
                            mes: 3,
                            setmana: 11,
                            dia: 13,
                            diaSetmana: 'DIVENDRES',
                        },
                        entornAppId: 1,
                        dimensionsJson: { servei: 'Padro' },
                        indicadorsJson: { ok: 8, ko: 1 },
                    },
                ]}
                currentDataDia="2026-03-13"
                dadesDiaModalOpen={true}
                setDadesDiaModalOpen={vi.fn()}
            />
        );

        fireEvent.click(screen.getByTitle('Més filtres'));
        fireEvent.change(screen.getByLabelText('Servei'), {
            target: { value: 'Padro' },
        });

        fireEvent.click(screen.getByTitle('Netejar'));

        expect((screen.getByLabelText('Servei') as HTMLInputElement).value).toBe('');
        const bodyRows = screen.getAllByRole('row');
        expect(within(bodyRows[0]).queryByText('Correctes')).not.toBeInTheDocument();
        expect(within(bodyRows[0]).queryByText('Erronis')).not.toBeInTheDocument();
    });
});
