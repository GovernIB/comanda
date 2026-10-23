import { describe, it, expect, vi } from 'vitest';
import { render } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { BaseAppContext, MuiBaseAppContext, MuiDataGrid } from 'reactlib';
import type { MuiBaseAppAppearance, MuiDataGridProps } from 'reactlib';
import { lot2DefaultMuiComponentProps, lot2ScreenGridProps } from './lot2ComponentProps';
import { createLot2Themes } from './theme';

// Es simulen l'API de recursos i l'autenticació per renderitzar només la graella.
vi.mock('../../lib/components/ResourceApiProvider', () => ({
    useResourceApiService: () => ({
        isReady: true,
        currentActions: {},
        currentFields: [],
        find: () => Promise.resolve({ rows: [], page: {} }),
        delete: () => Promise.resolve(),
        bulkDelete: () => Promise.resolve(),
    }),
}));
vi.mock('../../lib/components/ResourceApiContext', () => ({
    useResourceApiContext: () => ({ currentLanguage: 'ca' }),
}));
vi.mock('../../lib/components/AuthContext', () => ({
    useAuthContext: () => ({ isAuthenticated: true, getUserId: () => 'u' }),
}));

const theme = createLot2Themes({ main: '#83cb41', dark: '#497e3a', soft: '#F2F9EB' }).light;

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
        <ThemeProvider theme={theme}>
            <BaseAppContext.Provider value={baseApp}>
                <MuiBaseAppContext.Provider
                    value={{
                        defaultMuiComponentProps: lot2DefaultMuiComponentProps(theme),
                        appearance: {} as MuiBaseAppAppearance,
                    }}>
                    <MuiDataGrid resourceName="x" columns={[{ field: 'a' }]} title="Graella" {...props} />
                </MuiBaseAppContext.Provider>
            </BaseAppContext.Provider>
        </ThemeProvider>
    );

const toolbarEl = (c: HTMLElement) => c.querySelector('.MuiToolbar-root') as HTMLElement;
const gridEl = (c: HTMLElement) => c.querySelector('.MuiDataGrid-root') as HTMLElement;

describe('MuiDataGrid amb les props de lot2', () => {
    it('amb només les props per defecte, la barra és la de la llibreria (sense barra de títol)', () => {
        const { container } = renderGrid();
        const toolbar = getComputedStyle(toolbarEl(container));
        expect(toolbar.borderLeftStyle).not.toBe('solid');
        expect(toolbar.backgroundImage).not.toContain('linear-gradient');
        expect(getComputedStyle(gridEl(container)).margin).not.toBe('15px');
    });

    it('amb lot2ScreenGridProps, la barra superior és la barra de títol i la graella té 15px de marge', () => {
        const { container } = renderGrid(lot2ScreenGridProps(theme));
        const toolbar = getComputedStyle(toolbarEl(container));
        expect(toolbar.borderLeftStyle).toBe('solid');
        expect(toolbar.borderLeftWidth).toBe('4px');
        expect(getComputedStyle(gridEl(container)).margin).toBe('15px');
    });
});
