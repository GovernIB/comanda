import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import FormFieldCustomAdvancedSearch from './FormFieldCustomAdvancedSearch';
import { FormFieldDataActionType } from '../../lib/components/form/FormContext';

const mocks = vi.hoisted(() => ({
    formFieldMock: vi.fn(),
    muiDialogMock: vi.fn(),
    muiDataGridMock: vi.fn(),
    useBaseAppContextMock: vi.fn(),
    useFormContextMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    FormField: (props: Record<string, unknown>) => {
        mocks.formFieldMock(props);
        return (
            <div>
                <div data-testid="form-field-name">{props.name as string}</div>
                {props.componentProps && (
                    (props.componentProps as any).slotProps.input.startAdornment
                )}
            </div>
        );
    },
    MuiDialog: (props: Record<string, unknown>) => {
        mocks.muiDialogMock(props);
        return props.open ? <div data-testid="advanced-dialog">{props.children as React.ReactNode}</div> : null;
    },
    MuiDataGrid: (props: Record<string, unknown>) => {
        mocks.muiDataGridMock(props);
        return (
            <div data-testid="advanced-grid">
                <button
                    type="button"
                    onClick={() =>
                        (props.onRowClick as (params: { row: unknown }) => void)({
                            row: { id: 7, nom: 'Indicador 7', extra: 'valor' },
                        })
                    }
                >
                    Selecciona fila
                </button>
            </div>
        );
    },
    useBaseAppContext: () => mocks.useBaseAppContextMock(),
    useFormContext: () => mocks.useFormContextMock(),
}));

describe('FormFieldCustomAdvancedSearch', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('FormFieldCustomAdvancedSearch_quanEsPremLaLupa_obreElDialegAmbElDataGrid', () => {
        // Comprova que l'adorn inicial obre el diàleg de cerca avançada amb les props del grid.
        mocks.useBaseAppContextMock.mockReturnValue({
            t: vi.fn((key: string) => `tr:${key}`),
        });
        mocks.useFormContextMock.mockReturnValue({
            resourceName: 'widget',
            resourceType: 'resource',
            resourceTypeCode: 'WID',
            dataDispatchAction: vi.fn(),
            fields: [{ name: 'indicador', dataSource: { valueField: 'id', labelField: 'nom' } }],
            data: {},
        });

        render(
            <FormFieldCustomAdvancedSearch
                name="indicador"
                advancedSearchColumns={[{ field: 'nom' }]}
                advancedSearchDialogHeight={480}
                namedQueries={['filterByApp:1']}
            />
        );

        fireEvent.click(screen.getByRole('button'));

        expect(screen.getByTestId('advanced-dialog')).toBeInTheDocument();
        expect(screen.getByTestId('advanced-grid')).toBeInTheDocument();
        expect(mocks.muiDialogMock).toHaveBeenCalledWith(
            expect.objectContaining({
                title: 'tr:form.field.reference.advanced.title',
                componentProps: expect.objectContaining({ fullWidth: true, maxWidth: 'md' }),
            })
        );
        expect(mocks.muiDataGridMock).toHaveBeenCalledWith(
            expect.objectContaining({
                resourceName: 'widget',
                resourceFieldName: 'indicador',
                height: 480,
                namedQueries: ['filterByApp:1'],
            })
        );
    });

    it('FormFieldCustomAdvancedSearch_quanEsSeleccionaUnaFila_actualitzaElCampSimpleITancaElDialeg', () => {
        // Verifica que la selecció d'una fila transforma el valor i l'envia al dispatcher del formulari.
        const dataDispatchActionMock = vi.fn();
        mocks.useBaseAppContextMock.mockReturnValue({
            t: vi.fn((key: string) => `tr:${key}`),
        });
        mocks.useFormContextMock.mockReturnValue({
            resourceName: 'widget',
            resourceType: 'resource',
            resourceTypeCode: 'WID',
            dataDispatchAction: dataDispatchActionMock,
            fields: [{ name: 'indicador', dataSource: { valueField: 'id', labelField: 'nom' } }],
            data: {},
        });

        render(
            <FormFieldCustomAdvancedSearch
                name="indicador"
                advancedSearchColumns={[{ field: 'nom' }]}
            />
        );

        fireEvent.click(screen.getByRole('button'));
        fireEvent.click(screen.getByRole('button', { name: 'Selecciona fila' }));

        expect(dataDispatchActionMock).toHaveBeenCalledWith({
            type: FormFieldDataActionType.FIELD_CHANGE,
            payload: {
                fieldName: 'indicador',
                field: { name: 'indicador', dataSource: { valueField: 'id', labelField: 'nom' } },
                value: {
                    id: 7,
                    description: 'Indicador 7',
                    data: { id: 7, nom: 'Indicador 7', extra: 'valor' },
                },
            },
        });
        expect(screen.queryByTestId('advanced-dialog')).not.toBeInTheDocument();
    });

    it('FormFieldCustomAdvancedSearch_quanEsMultiple_noDuplicaUnaSeleccioJaPresent', () => {
        // Comprova que en mode múltiple no afegeix de nou una referència que ja existia al camp.
        const dataDispatchActionMock = vi.fn();
        mocks.useBaseAppContextMock.mockReturnValue({
            t: vi.fn((key: string) => `tr:${key}`),
        });
        mocks.useFormContextMock.mockReturnValue({
            resourceName: 'widget',
            resourceType: 'resource',
            resourceTypeCode: 'WID',
            dataDispatchAction: dataDispatchActionMock,
            fields: [{ name: 'indicador', dataSource: { valueField: 'id', labelField: 'nom' } }],
            data: {
                indicador: [
                    {
                        id: 7,
                        description: 'Indicador 7',
                        data: { id: 7, nom: 'Indicador 7' },
                    },
                ],
            },
        });

        render(
            <FormFieldCustomAdvancedSearch
                name="indicador"
                multiple
                advancedSearchColumns={[{ field: 'nom' }]}
            />
        );

        fireEvent.click(screen.getByRole('button'));
        fireEvent.click(screen.getByRole('button', { name: 'Selecciona fila' }));

        expect(dataDispatchActionMock).toHaveBeenCalledWith(
            expect.objectContaining({
                payload: expect.objectContaining({
                    value: [
                        {
                            id: 7,
                            description: 'Indicador 7',
                            data: { id: 7, nom: 'Indicador 7' },
                        },
                    ],
                }),
            })
        );
    });
});
