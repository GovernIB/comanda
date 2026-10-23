import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BaseAppContext } from '../../BaseAppContext';
import { MuiBaseAppContext, MuiBaseAppAppearance } from '../MuiBaseAppContext';

// Es simulen l'API de recursos i l'autenticació per renderitzar la graella amb una fila.
const rows = [{ id: 1, a: 'fila', _actions: { update: {}, delete: {} } }];
vi.mock('../../ResourceApiProvider', () => ({
    useResourceApiService: () => ({
        isReady: true,
        currentActions: { create: {} },
        currentFields: [],
        find: () => Promise.resolve({ rows, page: {} }),
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
vi.mock('../datacommon/DataFormDialog', () => ({
    default: React.forwardRef(() => null),
    useDataFormDialogApiRef: () => ({ current: { show: () => Promise.resolve({}) } }),
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

const renderGrid = (props: Partial<MuiDataGridProps> = {}) =>
    render(
        <ThemeProvider theme={createTheme()}>
            <BaseAppContext.Provider value={baseApp}>
                <MuiBaseAppContext.Provider
                    value={{
                        defaultMuiComponentProps: {},
                        appearance: {} as MuiBaseAppAppearance,
                    }}>
                    <MuiDataGrid
                        resourceName="x"
                        columns={[{ field: 'a' }]}
                        toolbarType="upper"
                        popupEditCreateActive
                        popupEditUpdateActive
                        disableVirtualization
                        {...props}
                    />
                </MuiBaseAppContext.Provider>
            </BaseAppContext.Provider>
        </ThemeProvider>
    );

const rowEl = async (props: Partial<MuiDataGridProps> = {}) => {
    renderGrid(props);
    await screen.findByText('fila');
    return screen.getByText('fila').closest('.MuiDataGrid-row') as HTMLElement;
};

describe('MuiDataGrid rowUpdateShowInMenu', () => {
    it("per defecte, l'acció d'editar és una icona de la fila i esborrar és al menú", async () => {
        const row = await rowEl();
        expect(within(row).getByRole('menuitem', { name: 'datacommon.update.label' })).toBeTruthy();
        expect(within(row).queryByRole('menuitem', { name: 'datacommon.delete.label' })).toBeNull();
        fireEvent.click(row.querySelector('[aria-haspopup="menu"]') as HTMLElement);
        await waitFor(() =>
            expect(screen.getByRole('menuitem', { name: 'datacommon.delete.label' })).toBeTruthy()
        );
        expect(screen.queryAllByText('datacommon.update.label')).toHaveLength(0);
    });
    it("rowUpdateShowInMenu mou l'acció d'editar al menú de fila", async () => {
        const row = await rowEl({ rowUpdateShowInMenu: true });
        expect(within(row).queryByRole('menuitem', { name: 'datacommon.update.label' })).toBeNull();
        fireEvent.click(row.querySelector('[aria-haspopup="menu"]') as HTMLElement);
        await waitFor(() =>
            expect(screen.getByRole('menuitem', { name: 'datacommon.update.label' })).toBeTruthy()
        );
        expect(screen.getByRole('menuitem', { name: 'datacommon.delete.label' })).toBeTruthy();
    });
});
