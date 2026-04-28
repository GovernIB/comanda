import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import ColumnesTable from './ColumnesTable';

const mocks = vi.hoisted(() => ({
    useFormContextMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                widget: {
                    taula: {
                        columna: {
                            indicador: 'Indicador',
                            titolIndicador: 'Títol',
                            tipusIndicador: 'Tipus',
                            periodeIndicador: 'Període',
                            arrossega: 'Arrossega',
                        },
                    },
                    action: {
                        addColumn: {
                            label: 'Afegir columna',
                        },
                    },
                },
            },
            generic: {
                tipus: 'Tipus',
                periode: 'Període',
            },
        })
    ),
    setFieldValueMock: vi.fn(),
    onChangeMock: vi.fn(),
}));

vi.mock('../../../lib/components/form/FormContext', () => ({
    useFormContext: () => mocks.useFormContextMock(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    FormField: ({
        name,
        value,
        disabled,
        fieldError,
        onChange,
        componentProps,
    }: {
        name: string;
        value?: unknown;
        disabled?: boolean;
        fieldError?: { code?: string } | undefined;
        onChange?: (value: unknown) => void;
        componentProps?: { onBlur?: () => void };
    }) => (
        <div data-testid={`field-${name}`}>
            <div>{String(value ?? '')}</div>
            <div data-testid={`disabled-${name}`}>{disabled ? 'true' : 'false'}</div>
            <div data-testid={`error-${name}`}>{fieldError?.code ?? ''}</div>
            <button type="button" onClick={() => onChange?.(`changed:${name}`)}>
                {`change:${name}`}
            </button>
            <button type="button" onClick={() => componentProps?.onBlur?.()}>
                {`blur:${name}`}
            </button>
        </div>
    ),
}));

describe('ColumnesTable', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('ColumnesTable_quanNoRepValor_inicialitzaUnaFilaBuidaIDeshabilitaEliminar', () => {
        // Comprova que el component crea una fila buida inicial i no permet esborrar-la si és l'única.
        mocks.useFormContextMock.mockReturnValue({
            data: { aplicacio: { id: 7 }, columnes: [] },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fields: [],
            fieldErrors: [],
        });

        render(<ColumnesTable name="columnes" mostrarUnitat={true} onChange={mocks.onChangeMock} />);

        expect(screen.getByTestId('field-columnes.0.indicador')).toBeInTheDocument();
        expect(screen.getAllByLabelText('delete')[0]).toBeDisabled();
    });

    it('ColumnesTable_quanEsPremAfegir_creaUnaNovaFilaINotificaElFormulari', () => {
        // Verifica que afegir una columna nova actualitza tant el formulari com el callback extern.
        mocks.useFormContextMock.mockReturnValue({
            data: { aplicacio: { id: 7 }, columnes: [] },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fields: [],
            fieldErrors: [],
        });

        render(<ColumnesTable name="columnes" mostrarUnitat={true} onChange={mocks.onChangeMock} />);

        fireEvent.click(screen.getByRole('button', { name: 'Afegir columna' }));

        expect(screen.getByTestId('field-columnes.1.indicador')).toBeInTheDocument();
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('columnes', [
            { indicador: null, titol: '' },
            { indicador: null, titol: '' },
        ]);
        expect(mocks.onChangeMock).toHaveBeenCalledWith([
            { indicador: null, titol: '' },
            { indicador: null, titol: '' },
        ]);
    });

    it('ColumnesTable_quanHiHaDuesFiles_iSEliminaUna_actualitzaLaColleccio', () => {
        // Comprova que eliminar una fila existent deixa la resta de columnes i ho propaga al formulari.
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 7 },
                columnes: [{ agregacio: 'SUM' }, { agregacio: 'SUM' }],
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fields: [],
            fieldErrors: [],
        });

        render(
            <ColumnesTable
                name="columnes"
                mostrarUnitat={true}
                onChange={mocks.onChangeMock}
                value={[
                    { indicador: { id: 1 }, titol: 'Columna 1', agregacio: 'SUM' },
                    { indicador: { id: 2 }, titol: 'Columna 2', agregacio: 'SUM' },
                ]}
            />
        );

        fireEvent.click(screen.getAllByLabelText('delete')[0]);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('columnes', [
            { indicador: { id: 2 }, titol: 'Columna 2', agregacio: 'SUM' },
        ]);
        expect(mocks.onChangeMock).toHaveBeenCalledWith([
            { indicador: { id: 2 }, titol: 'Columna 2', agregacio: 'SUM' },
        ]);
    });

    it('ColumnesTable_quanCanviaUnCamp_actualitzaLaFilaIControlaLestatDeLaUnitat', () => {
        // Verifica que els canvis de camp es propaguen i que la unitat només queda habilitada amb agregació AVERAGE.
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 7 },
                columnes: [{ agregacio: 'SUM' }],
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fields: [],
            fieldErrors: [],
        });

        render(
            <ColumnesTable
                name="columnes"
                mostrarUnitat={true}
                onChange={mocks.onChangeMock}
                value={[{ indicador: { id: 1 }, titol: 'Columna 1', agregacio: 'SUM' }]}
            />
        );

        expect(screen.getByTestId('disabled-columnes.0.unitatAgregacio')).toHaveTextContent('true');

        fireEvent.click(screen.getByRole('button', { name: 'change:columnes.0.titol' }));

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('columnes', [
            { indicador: { id: 1 }, titol: 'changed:columnes.0.titol', agregacio: 'SUM' },
        ]);
    });
});
