import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { ParametresItems, ParamTipus, groupBySubGrup } from './ParametresItems';

const findMock = vi.fn();
const patchMock = vi.fn();
const temporalMessageShowMock = vi.fn();

vi.mock('reactlib', () => ({
    useResourceApiService: vi.fn(() => ({
        isReady: true,
        find: findMock,
        patch: patchMock,
    })),
    useBaseAppContext: vi.fn(() => ({
        temporalMessageShow: temporalMessageShowMock,
    })),
    springFilterBuilder: {
        eq: (field: string, value: string) => `${field}=${value}`,
        and: (...parts: string[]) => parts.join(' AND '),
    },
    TextHighlight: ({ text }: any) => <span>{text}</span>,
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) => {
            if (typeof selector === 'function') {
                return selector({
                    page: {
                        parametres: {
                            detail: {
                                valuesTootip: { null: 'Null', true: 'True', false: 'False' }
                            },
                            save: { success: 'Guardat', error: 'Error' },
                            noGroup: 'Sense grup',
                            empty: 'Buit'
                        }
                    },
                    common: { save: 'Guardar' }
                });
            }
            return selector;
        }
    }),
}));

beforeEach(() => {
    vi.clearAllMocks();
    findMock.mockResolvedValue({ rows: [] });
    patchMock.mockResolvedValue({});
});

afterEach(() => {
    vi.clearAllMocks();
    document.body.innerHTML = '';
});

describe('groupBySubGrup', () => {
    it('agrupa correctament els ítems per subGrup', () => {
        const items = [
            { id: 1, subGrup: 'A', codi: 'C1' },
            { id: 2, subGrup: 'A', codi: 'C2' },
            { id: 3, subGrup: 'B', codi: 'C3' },
            { id: 4, subGrup: null, codi: 'C4' },
        ];

        const result = groupBySubGrup(items);

        expect(result.size).toBe(3);
        expect(result.get('A')).toHaveLength(2);
        expect(result.get('B')).toHaveLength(1);
        expect(result.get('')).toHaveLength(1);
    });
});

describe('ParametresItems', () => {
    it('mostra missatge "noGroup" quan no hi ha grup seleccionat', () => {
        render(<ParametresItems grup={null} subGrup={null} />);
        expect(screen.getByText('Sense grup')).toBeInTheDocument();
        expect(findMock).not.toHaveBeenCalled();
    });

    it('crida a find amb els filtres correctes quan hi ha grup i subgrup', async () => {
        render(<ParametresItems grup="G1" subGrup="SG1" quickFilter="test" />);

        await waitFor(() => {
            expect(findMock).toHaveBeenCalledWith({
                filter: "grup='G1' AND subGrup='SG1'",
                quickFilter: 'test',
                sorts: ['subGrup,asc', 'codi,asc'],
                unpaged: true,
            });
        });
    });

    it('mostra estat buit quan no hi ha paràmetres', async () => {
        findMock.mockResolvedValue({ rows: [] });
        render(<ParametresItems grup="G1" subGrup={null} />);

        expect(await screen.findByText('Buit')).toBeInTheDocument();
    });

    it('renderitza la llista AGRUPADA quan subGrup és null', async () => {
        findMock.mockResolvedValue({
            rows: [
                { id: 1, nom: 'Param 1', codi: 'P1', tipus: ParamTipus.TEXT, valor: 'V1', subGrup: 'SubA', editable: false },
                { id: 2, nom: 'Param 2', codi: 'P2', tipus: ParamTipus.TEXT, valor: 'V2', subGrup: 'SubA', editable: false },
            ]
        });

        render(<ParametresItems grup="G1" subGrup={null} />);

        expect(await screen.findByText('SubA')).toBeInTheDocument();
        expect(screen.getByText('Param 1')).toBeInTheDocument();
        expect(screen.getByText('Param 2')).toBeInTheDocument();
    });

    it('renderitza la llista PLA (no agrupada) quan subGrup té valor', async () => {
        findMock.mockResolvedValue({
            rows: [
                { id: 1, nom: 'Param 1', codi: 'P1', tipus: ParamTipus.TEXT, valor: 'V1', editable: false },
            ]
        });

        render(<ParametresItems grup="G1" subGrup="SG1" />);

        expect(await screen.findByText('SG1')).toBeInTheDocument();
        expect(screen.getByText('Param 1')).toBeInTheDocument();
        expect(screen.queryByText('SubA')).not.toBeInTheDocument();
    });
});

describe('Comportament d\'edició (ParametreRow)', () => {
    it('permet editar un paràmetre de tipus TEXT i guardar els canvis', async () => {
        findMock.mockResolvedValue({
            rows: [
                { id: 10, nom: 'Editable Text', codi: 'ET1', tipus: ParamTipus.TEXT, valor: 'ValorInicial', editable: true },
            ]
        });

        render(<ParametresItems grup="G1" subGrup={null} />);

        const input = await screen.findByDisplayValue('ValorInicial');

        fireEvent.change(input, { target: { value: 'NouValor' } });

        const saveButton = screen.getByTitle('Guardar');
        expect(saveButton).toBeInTheDocument();

        fireEvent.click(saveButton);

        await waitFor(() => {
            expect(patchMock).toHaveBeenCalledWith(10, { data: { valor: 'NouValor' } });
            expect(temporalMessageShowMock).toHaveBeenCalledWith(null, 'Guardat', 'success');
        });
    });

    it('permet editar un paràmetre de tipus BOOLEAN i guardar els canvis', async () => {
        findMock.mockResolvedValue({
            rows: [
                { id: 20, nom: 'Editable Bool', codi: 'EB1', tipus: ParamTipus.BOOLEAN, valorBoolean: false, editable: true },
            ]
        });

        render(<ParametresItems grup="G1" subGrup={null} />);

        const checkbox = await screen.findByRole('checkbox');
        expect(checkbox).not.toBeChecked();

        fireEvent.click(checkbox);

        const saveButton = screen.getByTitle('Guardar');
        fireEvent.click(saveButton);

        await waitFor(() => {
            expect(patchMock).toHaveBeenCalledWith(20, { data: { valorBoolean: true } });
        });
    });

    it('mostra error si el guardat falla', async () => {
        findMock.mockResolvedValue({
            rows: [
                { id: 30, nom: 'Error Param', codi: 'EP1', tipus: ParamTipus.TEXT, valor: 'V', editable: true },
            ]
        });
        patchMock.mockRejectedValue(new Error('Error de servidor'));

        render(<ParametresItems grup="G1" subGrup={null} />);

        const input = await screen.findByDisplayValue('V');
        fireEvent.change(input, { target: { value: 'Nou' } });
        fireEvent.click(screen.getByTitle('Guardar'));

        await waitFor(() => {
            expect(temporalMessageShowMock).toHaveBeenCalledWith('Error', 'Error de servidor', 'error');
        });
    });
});
