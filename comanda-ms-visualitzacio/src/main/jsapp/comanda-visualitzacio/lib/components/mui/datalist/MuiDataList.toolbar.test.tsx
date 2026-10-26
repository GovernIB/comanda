import React from 'react';
import { render } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BaseAppContext } from '../../BaseAppContext';
import { MuiBaseAppContext, MuiBaseAppAppearance } from '../MuiBaseAppContext';

// Es simulen l'API de recursos i l'autenticació per renderitzar només la llista.
vi.mock('../../ResourceApiProvider', () => ({
    useResourceApiService: () => ({
        isReady: true,
        currentActions: {},
        currentError: undefined,
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
vi.mock('../datacommon/DataFormDialog', () => ({
    default: React.forwardRef(() => null),
    useDataFormDialogApiRef: () => ({ current: { show: vi.fn() } }),
}));

import { MuiDataList, MuiDataListProps } from './MuiDataList';

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

const renderList = (
    props: Partial<MuiDataListProps> = {},
    defaults: Partial<MuiDataListProps> = {}
) =>
    render(
        <ThemeProvider theme={createTheme()}>
            <BaseAppContext.Provider value={baseApp}>
                <MuiBaseAppContext.Provider
                    value={{
                        defaultMuiComponentProps: { dataList: defaults },
                        appearance: {} as MuiBaseAppAppearance,
                    }}>
                    <MuiDataList
                        {...({
                            title: 'Llista',
                            resourceName: 'x',
                            primaryField: 'a',
                            ...props,
                        } as MuiDataListProps)}
                    />
                </MuiBaseAppContext.Provider>
            </BaseAppContext.Provider>
        </ThemeProvider>
    );

const toolbarEl = (c: HTMLElement) => c.querySelector('.MuiToolbar-root') as HTMLElement;

describe("MuiDataList nivell d'encapçalament del títol", () => {
    it('per defecte el títol és un h6', () => {
        const { container } = renderList();
        expect(toolbarEl(container).querySelector('h6')?.textContent).toBe('Llista');
    });
    it("toolbarTitleComponent='h2' pinta un h2", () => {
        const { container } = renderList({ toolbarTitleComponent: 'h2' });
        expect(toolbarEl(container).querySelector('h6')).toBeNull();
        expect(toolbarEl(container).querySelector('h2')?.textContent).toBe('Llista');
    });
    it('toolbarTitleComponent via defaultMuiComponentProps.dataList', () => {
        const { container } = renderList({}, { toolbarTitleComponent: 'h2' });
        expect(toolbarEl(container).querySelector('h2')?.textContent).toBe('Llista');
    });
});
