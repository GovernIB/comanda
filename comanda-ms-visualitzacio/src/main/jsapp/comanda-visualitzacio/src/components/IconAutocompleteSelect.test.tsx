import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import IconAutocompleteSelect from './IconAutocompleteSelect';

const mocks = vi.hoisted(() => ({
    setFieldValueMock: vi.fn(),
    dataGetFieldValueMock: vi.fn(),
}));

vi.mock('../../lib/components/form/FormContext', () => ({
    useFormContext: () => ({
        data: {},
        apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        dataGetFieldValue: mocks.dataGetFieldValueMock,
    }),
}));

vi.mock('@mui/icons-material', () => ({
    Add: () => <svg data-testid="icon-add" />,
    Delete: () => <svg data-testid="icon-delete" />,
}));

vi.mock('react-window', () => ({
    FixedSizeList: ({
        itemCount,
        children,
    }: {
        itemCount: number;
        children: (props: { index: number; style: React.CSSProperties }) => React.ReactNode;
    }) => (
        <div>
            {Array.from({ length: itemCount }).map((_, index) => (
                <div key={index}>{children({ index, style: {} })}</div>
            ))}
        </div>
    ),
}));

describe('IconAutocompleteSelect', () => {
    beforeEach(() => {
        mocks.dataGetFieldValueMock.mockReturnValue(null);
        vi.stubGlobal(
            'fetch',
            vi.fn().mockResolvedValue({
                json: () =>
                    Promise.resolve({
                        Add: ['afegir'],
                        Delete: ['esborrar'],
                    }),
            })
        );
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.restoreAllMocks();
        vi.clearAllMocks();
    });

    it('IconAutocompleteSelect_quanSObri_mostraLaLlistaAmbLOpcioSenseIcona', async () => {
        // Comprova que el selector obre el popover i ofereix l'opció de no tenir cap icona.
        render(<IconAutocompleteSelect name="icona" label="Icona" />);

        fireEvent.click(screen.getByLabelText('Icona'));

        expect(await screen.findByText('(Sense icona)')).toBeInTheDocument();
    });

    it('IconAutocompleteSelect_quanEsSeleccionaUnaIcona_actualitzaElFormulariINotificaElCanvi', async () => {
        // Verifica que seleccionar una icona escriu el valor al formulari i crida el callback extern.
        const onChange = vi.fn();
        render(<IconAutocompleteSelect name="icona" label="Icona" onChange={onChange} />);

        fireEvent.click(screen.getByLabelText('Icona'));
        fireEvent.change(await screen.findByPlaceholderText('Cerca icona...'), {
            target: { value: 'Add' },
        });
        fireEvent.click(await screen.findByText('Add'));

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('icona', 'Add');
        expect(onChange).toHaveBeenCalledWith('Add');
    });

    it('IconAutocompleteSelect_quanEsFiltraPerAlias_mostraLaIconaCoincident', async () => {
        // Comprova que la cerca també filtra per àlies carregats del fitxer JSON.
        render(<IconAutocompleteSelect name="icona" label="Icona" />);

        fireEvent.click(screen.getByLabelText('Icona'));
        fireEvent.change(await screen.findByPlaceholderText('Cerca icona...'), {
            target: { value: 'esborrar' },
        });

        await waitFor(() => {
            expect(screen.getByText('Delete')).toBeInTheDocument();
        });
    });
});
