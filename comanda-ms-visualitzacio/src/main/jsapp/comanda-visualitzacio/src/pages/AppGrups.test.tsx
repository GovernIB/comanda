import React from 'react';
import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import AppGrups from './AppGrups';

const mocks = vi.hoisted(() => ({
    readOnlyGestorMock: vi.fn(() => false),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                appGrups: {
                    title: "Grups d'aplicacions",
                    nom: 'Nom',
                    colorClar: 'Color clar',
                    colorFosc: 'Color fosc',
                },
            },
        })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name, type }: { name: string; type?: string }) => (
        <div data-testid={`field-${name}`} data-field-type={type}>
            {name}
        </div>
    ),
    MuiDataGrid: ({
        title,
        columns,
        toolbarElementsWithPositions,
        popupEditFormContent,
        resourceName,
        rowHideDeleteButton,
    }: {
        title: string;
        resourceName: string;
        columns: Array<{ field: string; renderCell?: (params: any) => React.ReactNode }>;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
        popupEditFormContent?: React.ReactNode;
        rowHideDeleteButton?: boolean;
    }) => (
        <section data-testid="data-grid" data-resource={resourceName} data-hide-delete={rowHideDeleteButton}>
            <h2>{title}</h2>
            <div data-testid="columns">{columns.map((c) => c.field).join(',')}</div>
            <div data-testid="cells">
                {columns.map((c) => {
                    if (c.renderCell) {
                        return (
                            <div key={c.field} data-testid={`cell-${c.field}`}>
                                {c.renderCell({ value: '#123456' })}
                            </div>
                        );
                    }
                    return null;
                })}
            </div>
            {toolbarElementsWithPositions?.map((entry, index) => (
                <div key={index}>{entry.element}</div>
            ))}
            <div data-testid="popup-form">{popupEditFormContent}</div>
        </section>
    ),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../hooks/useReadOnlyGestor.ts', () => ({
    default: () => mocks.readOnlyGestorMock(),
}));

describe('AppGrups', () => {
    afterEach(() => {
        vi.clearAllMocks();
        mocks.readOnlyGestorMock.mockReturnValue(false);
    });

    it('AppGrups_quanEsRenderitza_mostraElTitolLaGraellaILesColumnes', () => {
        render(<AppGrups />);

        expect(screen.getByTestId('page-title')).toHaveTextContent("Grups d'aplicacions");
        expect(screen.getByRole('heading', { name: "Grups d'aplicacions" })).toBeInTheDocument();
        expect(screen.getByTestId('data-grid')).toHaveAttribute('data-resource', 'appGrup');
        expect(screen.getByTestId('columns')).toHaveTextContent('nom,colorClar,colorFosc');
        expect(screen.queryByText('Reordenant grups')).not.toBeInTheDocument();
    });

    it('AppGrups_senseReordenacio_noConfiguraElementsDeReordenacioNiDataGridProps', () => {
        render(<AppGrups />);

        expect(screen.queryByText('Reordenant grups')).not.toBeInTheDocument();
    });

    it('AppGrups_quanEsRenderitzenLesColumnesDeColor_mostraElValorIElColor', () => {
        render(<AppGrups />);

        expect(screen.getByTestId('cell-colorClar')).toHaveTextContent('#123456');
        expect(screen.getByTestId('cell-colorFosc')).toHaveTextContent('#123456');
    });

    it('AppGrups_formulariPopup_conteElsCampsNomColorClarIColorFosc', () => {
        render(<AppGrups />);

        const fieldNom = screen.getByTestId('field-nom');
        const fieldColorClar = screen.getByTestId('field-colorClar');
        const fieldColorFosc = screen.getByTestId('field-colorFosc');

        expect(fieldNom).toBeInTheDocument();
        expect(fieldColorClar).toBeInTheDocument();
        expect(fieldColorClar).toHaveAttribute('data-field-type', 'color');
        expect(fieldColorFosc).toBeInTheDocument();
        expect(fieldColorFosc).toHaveAttribute('data-field-type', 'color');
    });

    it('AppGrups_quanEsGestorReadOnly_ocultaElBotoEsborrar', () => {
        mocks.readOnlyGestorMock.mockReturnValue(true);

        render(<AppGrups />);

        expect(screen.getByTestId('data-grid')).toHaveAttribute('data-hide-delete', 'true');
    });
});
