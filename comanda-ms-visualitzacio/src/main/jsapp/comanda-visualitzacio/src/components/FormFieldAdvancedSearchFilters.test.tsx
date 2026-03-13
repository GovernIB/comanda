import React from 'react';
import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import FormFieldAdvancedSearchFilters from './FormFieldAdvancedSearchFilters';

const mocks = vi.hoisted(() => ({
    useFormContextMock: vi.fn(),
    muiFilterMock: vi.fn(),
    customAdvancedSearchMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    MuiFilter: ({
        resourceName,
        code,
        springFilterBuilder,
        children,
    }: {
        resourceName: string;
        code: string;
        springFilterBuilder: (data: unknown) => string | undefined;
        children: React.ReactNode;
    }) => {
        mocks.muiFilterMock({ resourceName, code, springFilterBuilder, children });
        return <div data-testid="mui-filter">{children}</div>;
    },
    useFormContext: () => mocks.useFormContextMock(),
}));

vi.mock('./FormFieldCustomAdvancedSearch', () => ({
    default: (props: Record<string, unknown>) => {
        mocks.customAdvancedSearchMock(props);
        const toolbarAdditionalRow = (
            props.advancedSearchDataGridProps as { toolbarAdditionalRow?: React.ReactNode } | undefined
        )?.toolbarAdditionalRow;
        return (
            <div data-testid="custom-advanced-search">
                {props.name as string}
                {toolbarAdditionalRow}
            </div>
        );
    },
}));

describe('FormFieldAdvancedSearchFilters', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('FormFieldAdvancedSearchFilters_quanNoRepRecursUsaElDelContextDelFormulari', () => {
        // Comprova que el filtre fa servir el recurs del formulari quan no se n'indica un altre.
        mocks.useFormContextMock.mockReturnValue({ resourceName: 'widget' });

        render(
            <FormFieldAdvancedSearchFilters
                name="dimensions"
                advancedSearchFilterCode="filterCode"
                advancedSearchFilterContent={<span>Filtres</span>}
                advancedSearchFilterBuilder={() => 'actiu:true'}
                advancedSearchColumns={[]}
            />
        );

        expect(mocks.muiFilterMock).toHaveBeenCalledWith(
            expect.objectContaining({
                resourceName: 'widget',
                code: 'filterCode',
            })
        );
        expect(screen.getByTestId('custom-advanced-search')).toHaveTextContent('dimensions');
    });

    it('FormFieldAdvancedSearchFilters_quanRepRecursExpliciT_elPropagaAlMuiFilter', () => {
        // Verifica que el recurs específic del filtre sobreescriu el recurs global del context.
        mocks.useFormContextMock.mockReturnValue({ resourceName: 'widget' });

        render(
            <FormFieldAdvancedSearchFilters
                name="dimensions"
                advancedSearchFilterCode="filterCode"
                advancedSearchFilterResourceName="dimensio"
                advancedSearchFilterContent={<span>Filtres</span>}
                advancedSearchFilterBuilder={() => 'actiu:true'}
                advancedSearchColumns={[]}
            />
        );

        expect(mocks.muiFilterMock).toHaveBeenCalledWith(
            expect.objectContaining({
                resourceName: 'dimensio',
            })
        );
    });

    it('FormFieldAdvancedSearchFilters_quanConstrueixElComponentFusionaElToolbarAdditionalRow', () => {
        // Comprova que el wrapper injecta la fila addicional al datagrid sense perdre la resta de props.
        mocks.useFormContextMock.mockReturnValue({ resourceName: 'widget' });

        render(
            <FormFieldAdvancedSearchFilters
                name="dimensions"
                advancedSearchFilterCode="filterCode"
                advancedSearchFilterContent={<span>Filtres</span>}
                advancedSearchFilterBuilder={() => 'actiu:true'}
                advancedSearchColumns={[]}
                advancedSearchDataGridProps={{ rowHeight: 33 }}
            />
        );

        expect(mocks.customAdvancedSearchMock).toHaveBeenCalledWith(
            expect.objectContaining({
                advancedSearchDataGridProps: expect.objectContaining({
                    rowHeight: 33,
                    toolbarAdditionalRow: expect.any(Object),
                }),
            })
        );
    });
});
