import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { grey } from '@mui/material/colors';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BaseAppContext } from '../../BaseAppContext';
import { MuiBaseAppContext, MuiBaseAppAppearance } from '../MuiBaseAppContext';

// Es simulen l'API de recursos i l'autenticació per renderitzar només la graella.
vi.mock('../../ResourceApiProvider', () => ({
    useResourceApiService: () => ({
        isReady: true,
        currentActions: { create: {} },
        currentFields: [],
        find: () => Promise.resolve({ rows: [], page: {} }),
        delete: () => Promise.resolve(),
        bulkDelete: () => Promise.resolve(),
    }),
}));
vi.mock('../../ResourceApiContext', () => ({
    useResourceApiContext: () => ({ currentLanguage: 'ca' }),
}));
vi.mock('../../AuthContext', () => ({
    useAuthContext: () => ({ isAuthenticated: true, getUserId: () => 'u' }),
}));
// El diàleg del formulari emergent es substitueix per un element senzill.
const show = vi.fn(() => Promise.resolve({}));
vi.mock('../datacommon/DataFormDialog', () => ({
    default: React.forwardRef(() => null),
    useDataFormDialogApiRef: () => ({ current: { show } }),
}));

import { MuiDataGrid, MuiDataGridProps } from './MuiDataGrid';

const baseApp: any = {
    t: (k: string) => k,
    goBack: () => {},
    getLinkComponent: () => 'a',
    code: 'c',
    currentLanguage: 'ca',
    topLevelRouteChanged: () => {},
    temporalMessageShow: () => {},
    messageDialogShow: () => Promise.resolve(false),
};

const renderGrid = (
    props: Partial<MuiDataGridProps> = {},
    defaults: Partial<MuiDataGridProps> = {}
) =>
    render(
        <ThemeProvider theme={createTheme()}>
            <BaseAppContext.Provider value={baseApp}>
                <MuiBaseAppContext.Provider
                    value={{
                        defaultMuiComponentProps: { dataGrid: defaults },
                        appearance: {} as MuiBaseAppAppearance,
                    }}>
                    <MuiDataGrid
                        resourceName="x"
                        columns={[{ field: 'a' }]}
                        toolbarType="upper"
                        popupEditCreateActive
                        popupEditUpdateActive
                        rowUpdateLink="u"
                        {...props}
                    />
                </MuiBaseAppContext.Provider>
            </BaseAppContext.Provider>
        </ThemeProvider>
    );

const toolbarEl = (c: HTMLElement) => c.querySelector('.MuiToolbar-root') as HTMLElement;
const gridEl = (c: HTMLElement) => c.querySelector('.MuiDataGrid-root') as HTMLElement;

describe("MuiDataGrid barra d'eines per defecte", () => {
    it('fons grey[200], marge 16px i botó de crear amb icona', () => {
        const { container } = renderGrid();
        expect(getComputedStyle(toolbarEl(container)).backgroundColor).toBe('rgb(238, 238, 238)');
        expect(grey[200]).toBe('#eeeeee');
        expect(getComputedStyle(gridEl(container)).margin).toBe('16px');
        const create = screen.getByTitle('datacommon.create.label');
        expect(create.className).toContain('MuiIconButton-root');
    });
});

describe("MuiDataGrid barra d'eines configurable", () => {
    it("toolbarSx s'aplica a la barra", () => {
        const { container } = renderGrid({ toolbarSx: { backgroundColor: 'transparent' } });
        expect(getComputedStyle(toolbarEl(container)).backgroundColor).toBe('rgba(0, 0, 0, 0)');
    });
    it("marginsSx s'aplica a la graella", () => {
        const { container } = renderGrid({ marginsSx: { m: 0 } });
        expect(getComputedStyle(gridEl(container)).margin).toBe('0px');
    });
    it('sx com a array aplica tots dos elements', () => {
        const { container } = renderGrid({ sx: [{ color: 'red' }, { opacity: 0.5 }] });
        const style = getComputedStyle(gridEl(container));
        expect(style.color).toBe('rgb(255, 0, 0)');
        expect(style.opacity).toBe('0.5');
    });
});

describe('MuiDataGrid botó de crear amb text', () => {
    it("mostra un botó amb el text i crida l'acció de crear", () => {
        show.mockClear();
        renderGrid({ toolbarCreateVariant: 'button', toolbarCreateLabel: 'Nova aplicació' });
        const button = screen.getByRole('button', { name: /Nova aplicació/ });
        expect(button.className).toContain('MuiButton-contained');
        expect((button as HTMLButtonElement).disabled).toBe(false);
        fireEvent.click(button);
        expect(show).toHaveBeenCalledTimes(1);
    });
    it('per defecte el text és datacommon.create.label', () => {
        renderGrid({ toolbarCreateVariant: 'button' });
        const button = screen.getByRole('button', { name: /datacommon.create.label/ });
        expect(button.className).toContain('MuiButton-contained');
    });
    it('desactivat i sense acció amb error de configuració', () => {
        show.mockClear();
        renderGrid({
            toolbarCreateVariant: 'button',
            toolbarCreateLabel: 'Nova aplicació',
            popupEditCreateActive: undefined,
        });
        const button = screen.getByRole('button', { name: /Nova aplicació/ });
        expect((button as HTMLButtonElement).disabled).toBe(true);
        fireEvent.click(button);
        expect(show).not.toHaveBeenCalled();
    });
});

describe("MuiDataGrid propietats per defecte de l'aplicació", () => {
    it('toolbarCreateVariant i toolbarCreateLabel via defaultMuiComponentProps.dataGrid', () => {
        renderGrid({}, { toolbarCreateVariant: 'button', toolbarCreateLabel: 'Nou' });
        const button = screen.getByRole('button', { name: /Nou/ });
        expect(button.className).toContain('MuiButton-contained');
    });
});
