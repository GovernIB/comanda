import * as React from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Indicadors, { buildDefaultFormulaTermes, computeFormulaTermePayloads, mapTermeRowsToFormulaTermes } from './Indicadors';

const mocks = vi.hoisted(() => ({
    clearMock: vi.fn(),
    useReadOnlyGestorMock: vi.fn(() => false),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                indicadors: {
                    title: 'Indicadors',
                    column: {
                        entornApp: 'Entorn app',
                        indicadorMitjana: 'Indicador mitjana',
                        tipus: 'Tipus',
                    },
                    action: {
                        createFormula: 'Crear indicador de fórmula',
                        editFormula: 'Editar fórmula',
                    },
                    formulaForm: {
                        createTitle: 'Crear indicador de fórmula',
                        editTitle: 'Editar fórmula',
                        termesTitle: 'Termes de la fórmula',
                        termeIndicador: 'Indicador',
                        addTerme: 'Afegir terme',
                        removeTerme: 'Eliminar terme',
                        success: 'Fórmula desada correctament',
                    },
                },
            },
            components: {
                clear: 'Netejar',
            },
            common: {
                error: 'Error',
            },
        })
    ),
    findMock: vi.fn(),
    entornAppFindMock: vi.fn(),
    indicadorFindMock: vi.fn(),
    indicadorDeleteMock: vi.fn(),
    termeFindMock: vi.fn(),
    termeCreateMock: vi.fn(),
    termeDeleteMock: vi.fn(),
    gridRefreshMock: vi.fn(),
    formDialogShowMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    dataDispatchActionMock: vi.fn(),
    formComponentPropsCapture: undefined as any,
    rowAdditionalActionsCapture: undefined as any,
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    MuiDataGrid: ({
        title,
        filter,
        toolbarAdditionalRow,
        popupEditFormContent,
        columns,
        rowHideDeleteButton,
        rowAdditionalActions,
        toolbarElementsWithPositions,
    }: {
        title: string;
        filter?: string;
        toolbarAdditionalRow?: React.ReactNode;
        popupEditFormContent?: React.ReactNode;
        columns: Array<{ field: string }>;
        rowHideDeleteButton?: boolean;
        rowAdditionalActions?: any[];
        toolbarElementsWithPositions?: Array<{ position: number; element: React.ReactNode }>;
    }) => {
        mocks.rowAdditionalActionsCapture = rowAdditionalActions;
        return (
            <section>
                <h2>{title}</h2>
                <div data-testid="filter-value">{filter}</div>
                <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
                <div data-testid="hide-delete">{String(rowHideDeleteButton)}</div>
                <div>{toolbarAdditionalRow}</div>
                <div>{popupEditFormContent}</div>
                <div>{(toolbarElementsWithPositions ?? []).map((e, i) => React.cloneElement(e.element as React.ReactElement, { key: i }))}</div>
            </section>
        );
    },
    MuiFilter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    MuiFormDialog: ({ children, formComponentProps }: { children: React.ReactNode; formComponentProps?: any }) => {
        mocks.formComponentPropsCapture = formComponentProps;
        return <div data-testid="formula-dialog">{children}</div>;
    },
    FormField: ({ name, label, optionsRequest }: { name: string; label?: string; optionsRequest?: (q: string) => Promise<{ options: Array<{ description?: string }> }> }) => (
        <div>
            <span data-testid={`field-${name}`}>{label ?? name}</span>
            {optionsRequest ? <button onClick={async () => {
                const result = await optionsRequest('prova');
                const descriptions = result.options.map((option) => option.description).join(',');
                document.body.setAttribute('data-options', descriptions);
            }}>Carrega opcions</button> : null}
        </div>
    ),
    springFilterBuilder: {
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
        like: (field: string, value: unknown) => `${field}~${String(value)}`,
        and: (...parts: Array<string | undefined | false>) => parts.filter(Boolean).join(' && '),
    },
    useFilterApiRef: () => ({
        current: {
            clear: mocks.clearMock,
        },
    }),
    useFormApiRef: () => ({ current: {} }),
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: mocks.gridRefreshMock,
        },
    }),
    useMuiFormDialogApiRef: () => ({
        current: {
            show: mocks.formDialogShowMock,
            close: vi.fn(),
        },
    }),
    useFormDialogButtons: () => [],
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useResourceApiService: (resourceName?: string) => {
        if (resourceName === 'entornApp') {
            return { isReady: true, find: mocks.entornAppFindMock };
        }
        if (resourceName === 'indicadorFormulaTerme') {
            return {
                isReady: true,
                find: mocks.termeFindMock,
                create: mocks.termeCreateMock,
                delete: mocks.termeDeleteMock,
            };
        }
        if (resourceName === 'indicador') {
            return { isReady: true, find: mocks.indicadorFindMock, delete: mocks.indicadorDeleteMock };
        }
        return { isReady: true, find: mocks.findMock };
    },
    useFormContext: () => ({
        data: {
            compactable: true,
            tipusCompactacio: 'MITJANA',
            entornAppId: 7,
        },
        dataGetFieldValue: () => undefined,
        dataDispatchAction: mocks.dataDispatchActionMock,
        fields: [],
        fieldErrors: [],
    }),
}));

vi.mock('../components/sharedAdvancedSearch/advancedSearchColumns', () => ({
    columnesIndicador: [{ field: 'codi' }],
}));

vi.mock('../components/FormFieldCustomAdvancedSearch', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`advanced-${name}`}>{name}</div>,
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../../lib/components/form/FormContext.tsx', () => ({
    FormFieldDataActionType: { RESET: 'RESET', FIELD_CHANGE: 'FIELD_CHANGE' },
}));

vi.mock('../hooks/useReadOnlyGestor.ts', () => ({
    default: () => mocks.useReadOnlyGestorMock(),
}));

describe('Indicadors', () => {
    beforeEach(() => {
        vi.spyOn(console, 'log').mockImplementation(() => undefined);
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, entornAppDescription: 'Entorn prova' }],
        });
        mocks.termeFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
    });

    afterEach(() => {
        vi.clearAllMocks();
        document.body.removeAttribute('data-options');
        mocks.useReadOnlyGestorMock.mockReturnValue(false);
    });

    it('Indicadors_quanEsRenderitza_mostraLaGraellaElFiltreIElFormulariCondicional', async () => {
        // Comprova que la pàgina mostra les columnes, el filtre inicial i el camp avançat quan la compactació és per mitjana.
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        expect(screen.getByTestId('page-title')).toHaveTextContent('Indicadors');
        expect(screen.getByRole('heading', { name: 'Indicadors' })).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent('entornAppId=0');
        expect(screen.getByTestId('columns')).toHaveTextContent(
            'codi,nom,descripcio,format,tipus,compactable,tipusCompactacio,indicadorComptadorPerMitjana.description'
        );
        expect(screen.getByTestId('advanced-indicadorComptadorPerMitjana')).toBeInTheDocument();
        expect(screen.getByTestId('hide-delete')).toHaveTextContent('false');
    });

    it('Indicadors_quanGestorEsReadOnly_ocultaLaccioDesborrar', async () => {
        mocks.useReadOnlyGestorMock.mockReturnValue(true);
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        expect(screen.getByTestId('hide-delete')).toHaveTextContent('true');
    });

    it('Indicadors_quanEsCarreguenOpcionsDelFiltre_retornaElsEntornsDisponibles', async () => {
        // Verifica que el camp de filtre d'entorn app reusa les dades carregades per oferir opcions filtrables.
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        fireEvent.click(screen.getAllByRole('button', { name: 'Carrega opcions' })[0]);

        await waitFor(() => {
            expect(document.body.getAttribute('data-options')).toContain('Entorn prova');
        });
    });

    it('Indicadors_quanEsPremNetejar_executaLaNetejaDelFiltre', async () => {
        // Comprova que el botó de neteja delega correctament al `filterApiRef`.
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        fireEvent.click(screen.getByTitle('Netejar'));

        expect(mocks.clearMock).toHaveBeenCalled();
    });

    it('Indicadors_enTriarEntornAppAlFormulariDeFormula_escriuEntornAppIdNoUnObjecteEntornApp', async () => {
        // Regressió: el backend espera el camp escalar `entornAppId` (Long), no un objecte `entornApp` de
        // referència — vegeu la validació NotNull d'entornAppId que fallava abans d'aquest fix.
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        const user = userEvent.setup();
        const input = screen.getByLabelText('Entorn app *');
        await user.click(input);

        const option = await screen.findByRole('option', { name: 'Entorn prova' });
        await user.click(option);

        expect(mocks.dataDispatchActionMock).toHaveBeenCalledWith({
            type: 'FIELD_CHANGE',
            payload: { fieldName: 'entornAppId', field: undefined, value: 7 },
        });
    });

    it('Indicadors_quanEsPremCrearIndicadorDeFormula_obreElDialegEnModeCreacio', async () => {
        // El botó "Crear indicador de fórmula" ha d'obrir el diàleg sense id (creació) i amb tipus=FORMULA precarregat.
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        fireEvent.click(screen.getByRole('button', { name: /Crear indicador de fórmula/ }));

        expect(mocks.formDialogShowMock).toHaveBeenCalledWith(
            undefined,
            { tipus: 'FORMULA', compactable: false },
            'Crear indicador de fórmula'
        );
    });

    it('Indicadors_enPremerAfegirTerme_afegeixUnaNovaFilaDeTermeImmediatament', async () => {
        // Regressió: afegir un terme s'ha de reflectir a l'instant al propi editor, sense necessitat de
        // tancar i reobrir el diàleg (vegeu el comentari de disseny a FormulaTermesEditor sobre per què
        // l'estat dels termes es gestiona amb un useState/ref propis en lloc de viure al component Indicadors).
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        expect(screen.getAllByLabelText('Indicador *')).toHaveLength(1);

        fireEvent.click(screen.getByRole('button', { name: /Afegir terme/ }));

        expect(screen.getAllByLabelText('Indicador *')).toHaveLength(2);
    });

    it('Indicadors_laccioEditarFormula_nomesEsVisibleAFilesDeTipusFormula', async () => {
        // L'acció de fila "Editar fórmula" només s'ha de mostrar quan tipus === 'FORMULA'.
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        const action = mocks.rowAdditionalActionsCapture?.[0];
        expect(action).toBeDefined();
        expect(action.hidden({ tipus: 'FORMULA' })).toBe(false);
        expect(action.hidden({ tipus: 'SIMPLE' })).toBe(true);
    });

    it('Indicadors_enPremerEditarFormula_carregaElsTermesIObreElDialegEnModeEdicio', async () => {
        // Clicar "Editar fórmula" ha de carregar els termes existents (filtrats per indicadorFormula.id) i obrir el diàleg amb l'id.
        mocks.termeFindMock.mockResolvedValueOnce({
            rows: [
                { id: 1, ordre: 1, operador: 'RESTA', indicadorComponent: { id: 5 } },
                { id: 2, ordre: 0, operador: 'SUMA', indicadorComponent: { id: 3 } },
            ],
        });
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        const action = mocks.rowAdditionalActionsCapture?.[0];
        await act(async () => {
            await action.onClick(99, { id: 99, tipus: 'FORMULA' });
        });

        expect(mocks.termeFindMock).toHaveBeenCalledWith(
            expect.objectContaining({ filter: 'indicadorFormula.id=99' })
        );
        expect(mocks.formDialogShowMock).toHaveBeenCalledWith(99, undefined, 'Editar fórmula');
    });

    it('Indicadors_enCrearLaFormula_elimTermesExistentsICreaElsNousIRefrescaLaGraella', async () => {
        // Regressió: onCreateSuccess (no onSaveSuccess) és qui rep les dades REALMENT desades pel backend
        // (amb l'id assignat pel servidor) — onSaveSuccess rebia les dades locals del formulari abans de
        // desar, sense id en creació, produint un filtre "indicadorFormula.id:undefined" invàlid.
        mocks.termeFindMock.mockResolvedValueOnce({
            rows: [{ id: 10 }, { id: 11 }],
        });
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        await mocks.formComponentPropsCapture.onCreateSuccess({ id: 42 });

        expect(mocks.termeFindMock).toHaveBeenCalledWith(
            expect.objectContaining({ filter: 'indicadorFormula.id=42' })
        );
        expect(mocks.termeDeleteMock).toHaveBeenCalledWith(10);
        expect(mocks.termeDeleteMock).toHaveBeenCalledWith(11);
        expect(mocks.gridRefreshMock).toHaveBeenCalled();
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Fórmula desada correctament', 'success');
        expect(mocks.indicadorDeleteMock).not.toHaveBeenCalled();
    });

    it('Indicadors_enEditarLaFormula_tambeUsaOnUpdateSuccessAmbLIdDelIndicador', async () => {
        mocks.termeFindMock.mockResolvedValueOnce({ rows: [] });
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        await mocks.formComponentPropsCapture.onUpdateSuccess({ id: 42 });

        expect(mocks.termeFindMock).toHaveBeenCalledWith(
            expect.objectContaining({ filter: 'indicadorFormula.id=42' })
        );
        expect(mocks.gridRefreshMock).toHaveBeenCalled();
    });

    it('Indicadors_siElDesatDelsTermesFallaEnCrear_esfaLindicadorAcabatDeCrearIMostraError', async () => {
        // No es pot deixar un indicador de fórmula "orfe" sense termes si falla el segon pas (crear els
        // termes és una crida HTTP separada de crear l'indicador, no hi ha una única transacció).
        mocks.termeFindMock.mockRejectedValueOnce(new Error('Error de xarxa'));
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        await mocks.formComponentPropsCapture.onCreateSuccess({ id: 42 });

        expect(mocks.indicadorDeleteMock).toHaveBeenCalledWith(42);
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Error de xarxa', 'error');
        expect(mocks.gridRefreshMock).not.toHaveBeenCalled();
    });

    it('Indicadors_siElDesatDelsTermesFallaEnEditar_noEsfaLindicadorPerqueTeniaDadesPrevies', async () => {
        // En edició no es desfà res: els camps ja desats de l'indicador (codi/nom/...) eren canvis vàlids
        // que no s'han de perdre només perquè els termes no s'han pogut actualitzar.
        mocks.termeFindMock.mockRejectedValueOnce(new Error('Error de xarxa'));
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        await mocks.formComponentPropsCapture.onUpdateSuccess({ id: 42 });

        expect(mocks.indicadorDeleteMock).not.toHaveBeenCalled();
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Error de xarxa', 'error');
    });
});

describe('buildDefaultFormulaTermes', () => {
    it('buildDefaultFormulaTermes_semprePrimerTermeSumaSenseIndicador', () => {
        const termes = buildDefaultFormulaTermes();
        expect(termes).toHaveLength(1);
        expect(termes[0].operador).toBe('SUMA');
        expect(termes[0].indicadorId).toBeNull();
    });
});

describe('mapTermeRowsToFormulaTermes', () => {
    it('mapTermeRowsToFormulaTermes_ordenaPerOrdreIExtreuLIdDelComponent', () => {
        const result = mapTermeRowsToFormulaTermes([
            { id: 1, ordre: 1, operador: 'RESTA', indicadorComponent: { id: 5 } },
            { id: 2, ordre: 0, operador: 'SUMA', indicadorComponent: { id: 3 } },
        ]);

        expect(result.map(t => ({ indicadorId: t.indicadorId, operador: t.operador }))).toEqual([
            { indicadorId: 3, operador: 'SUMA' },
            { indicadorId: 5, operador: 'RESTA' },
        ]);
    });

    it('mapTermeRowsToFormulaTermes_ambLlistaBuida_retornaLlistaBuida', () => {
        expect(mapTermeRowsToFormulaTermes([])).toEqual([]);
    });
});

describe('computeFormulaTermePayloads', () => {
    it('computeFormulaTermePayloads_descartaFilesSenseIndicadorIReassignaOrdre', () => {
        const payloads = computeFormulaTermePayloads(
            [
                { key: 'a', indicadorId: 3, operador: 'SUMA' },
                { key: 'b', indicadorId: null, operador: 'SUMA' },
                { key: 'c', indicadorId: 5, operador: 'RESTA' },
            ],
            42
        );

        expect(payloads).toEqual([
            { indicadorFormula: { id: 42 }, indicadorComponent: { id: 3 }, operador: 'SUMA', ordre: 0 },
            { indicadorFormula: { id: 42 }, indicadorComponent: { id: 5 }, operador: 'RESTA', ordre: 1 },
        ]);
    });

    it('computeFormulaTermePayloads_ambLlistaBuida_retornaLlistaBuida', () => {
        expect(computeFormulaTermePayloads([], 42)).toEqual([]);
    });
});
