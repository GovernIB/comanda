import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { grey } from '@mui/material/colors';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BaseAppContext } from '../../BaseAppContext';
import { MuiBaseAppContext, MuiBaseAppAppearance } from '../MuiBaseAppContext';

const apiState = vi.hoisted(() => ({ error: undefined as Error | undefined }));
const mediaState = vi.hoisted(() => ({ upMd: true }));

// Es simula useMediaQuery: 'up' retorna upMd i 'down' el contrari.
vi.mock('@mui/material/useMediaQuery', () => ({
    default: (query: string) =>
        typeof query === 'string' && query.includes('min-width')
            ? mediaState.upMd
            : !mediaState.upMd,
}));

// Es simulen l'API de recursos i l'autenticació per renderitzar només la graella.
vi.mock('../../ResourceApiProvider', () => ({
    useResourceApiService: () => ({
        isReady: true,
        currentActions: { create: {} },
        currentError: apiState.error,
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
    defaults: Partial<MuiDataGridProps> = {},
    theme = createTheme()
) =>
    render(
        <ThemeProvider theme={theme}>
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

describe('MuiDataGrid botó de crear responsiu', () => {
    afterEach(() => {
        mediaState.upMd = true;
    });
    it('a partir de md mostra el botó amb text', () => {
        mediaState.upMd = true;
        renderGrid({ toolbarCreateVariant: 'responsive', toolbarCreateLabel: 'Nova aplicació' });
        const button = screen.getByRole('button', { name: /Nova aplicació/ });
        expect(button.className).toContain('MuiButton-contained');
    });
    it('per sota de md mostra una icona amb title igual al text', () => {
        mediaState.upMd = false;
        renderGrid({ toolbarCreateVariant: 'responsive', toolbarCreateLabel: 'Nova aplicació' });
        const create = screen.getByTitle('Nova aplicació');
        expect(create.className).toContain('MuiIconButton-root');
        expect(screen.queryByText('Nova aplicació')).toBeNull();
        show.mockClear();
        fireEvent.click(create);
        expect(show).toHaveBeenCalledTimes(1);
    });
});

describe("MuiDataGrid propietats per defecte de l'aplicació", () => {
    it('toolbarCreateVariant i toolbarCreateLabel via defaultMuiComponentProps.dataGrid', () => {
        renderGrid({}, { toolbarCreateVariant: 'button', toolbarCreateLabel: 'Nou' });
        const button = screen.getByRole('button', { name: /Nou/ });
        expect(button.className).toContain('MuiButton-contained');
    });
});

describe("MuiDataGrid barra d'eines en error de l'API", () => {
    beforeEach(() => {
        apiState.error = new Error('Error de prova');
    });
    afterEach(() => {
        apiState.error = undefined;
    });
    it("amb títol element el manté i mostra l'error al subtítol", async () => {
        renderGrid({ title: <h1>Pantalla</h1> });
        expect(await screen.findByText('Error de prova')).toBeTruthy();
        expect(screen.getByRole('heading', { level: 1, name: 'Pantalla' })).toBeTruthy();
    });
    it("amb toolbarSx amb degradat el fons de la barra és el d'error", async () => {
        const { container } = renderGrid({
            title: <h1>Pantalla</h1>,
            toolbarSx: { background: 'linear-gradient(to right, rgb(0, 0, 255), rgb(0, 255, 0))' },
        });
        await screen.findByText('Error de prova');
        const style = getComputedStyle(toolbarEl(container));
        expect(style.backgroundColor).toBe('rgb(255, 205, 210)');
        expect(style.backgroundImage).not.toContain('gradient');
        expect(style.borderLeft).toContain('4px');
        expect(style.boxSizing).toBe('border-box');
    });
    it("al tema fosc el fons d'error és red[900] i el subtítol és text.primary", async () => {
        const { container } = renderGrid(
            { title: <h1>Pantalla</h1> },
            {},
            createTheme({ palette: { mode: 'dark' } })
        );
        const subtitle = await screen.findByText('Error de prova');
        expect(getComputedStyle(toolbarEl(container)).backgroundColor).toBe('rgb(183, 28, 28)');
        expect(getComputedStyle(subtitle).color).toBe('rgb(255, 255, 255)');
    });
    it("amb títol element i error sense missatge mostra el text d'error genèric al subtítol", async () => {
        apiState.error = new Error('');
        renderGrid({ title: <h1>Pantalla</h1> });
        expect(await screen.findByText('datacommon.toolbar.error')).toBeTruthy();
        expect(screen.getByRole('heading', { level: 1, name: 'Pantalla' })).toBeTruthy();
    });
    it("amb títol text es comporta com fins ara: títol substituït pel text d'error", async () => {
        renderGrid({ title: 'Pantalla' });
        expect(await screen.findByText('datacommon.toolbar.error')).toBeTruthy();
        expect(screen.queryByText('Pantalla')).toBeNull();
        expect(screen.getByText('Error de prova')).toBeTruthy();
    });
});

describe('MuiDataGrid fileres parelles amb striped', () => {
    const evenRowBg = (theme: any) => {
        const { container } = renderGrid({ striped: true }, {}, theme);
        const row = gridEl(container).ownerDocument.createElement('div');
        row.className = `${gridEl(container).className} MuiDataGrid-root`;
        const inner = document.createElement('div');
        inner.className = 'MuiDataGrid-row even';
        row.appendChild(inner);
        document.body.appendChild(row);
        const bg = getComputedStyle(inner).backgroundColor;
        row.remove();
        return bg;
    };
    it('al tema fosc no són #eeeeee', () => {
        const bg = evenRowBg(createTheme({ palette: { mode: 'dark' } }));
        expect(bg).not.toBe('rgb(238, 238, 238)');
        expect(bg).toBe('rgba(255, 255, 255, 0.04)');
    });
    it('al tema clar continuen sent grey[200]', () => {
        expect(evenRowBg(createTheme())).toBe('rgb(238, 238, 238)');
    });
});

describe("MuiDataGrid nivell d'encapçalament del títol", () => {
    it('per defecte el títol és un h6', () => {
        const { container } = renderGrid({ title: 'Pantalla' });
        expect(toolbarEl(container).querySelector('h6')?.textContent).toBe('Pantalla');
    });
    it("toolbarTitleComponent='h2' pinta un h2", () => {
        const { container } = renderGrid({ title: 'Pantalla', toolbarTitleComponent: 'h2' });
        expect(toolbarEl(container).querySelector('h6')).toBeNull();
        expect(toolbarEl(container).querySelector('h2')?.textContent).toBe('Pantalla');
    });
    it('toolbarTitleComponent via defaultMuiComponentProps.dataGrid', () => {
        const { container } = renderGrid({ title: 'Pantalla' }, { toolbarTitleComponent: 'h2' });
        expect(toolbarEl(container).querySelector('h2')?.textContent).toBe('Pantalla');
    });
    it("amb title='' no hi ha cap encapçalament buit", () => {
        const { container } = renderGrid({ title: '' });
        expect(toolbarEl(container).querySelector('h1, h2, h3, h4, h5, h6')).toBeNull();
    });
});

describe('MuiDataGrid filtre ràpid plegable', () => {
    const quickFilterInput = (c: HTMLElement) =>
        toolbarEl(c).querySelector('input[aria-label="datacommon.quickfilter.label"]');
    it('per defecte el filtre ràpid és un camp de text', () => {
        const { container } = renderGrid();
        expect(quickFilterInput(container)).not.toBeNull();
        expect(screen.queryByRole('button', { name: 'datacommon.quickfilter.label' })).toBeNull();
    });
    it('toolbarQuickFilterCollapsible mostra el botó de cerca i en fer-hi clic el camp', () => {
        const { container } = renderGrid({ toolbarQuickFilterCollapsible: true });
        expect(quickFilterInput(container)).toBeNull();
        fireEvent.click(screen.getByRole('button', { name: 'datacommon.quickfilter.label' }));
        expect(quickFilterInput(container)).not.toBeNull();
        expect(document.activeElement).toBe(quickFilterInput(container));
    });
    it('toolbarQuickFilterCollapsible via defaultMuiComponentProps.dataGrid', () => {
        const { container } = renderGrid({}, { toolbarQuickFilterCollapsible: true });
        expect(quickFilterInput(container)).toBeNull();
        expect(screen.getByRole('button', { name: 'datacommon.quickfilter.label' })).toBeTruthy();
    });
    it('amb un valor inicial del filtre ràpid comença obert', () => {
        const { container } = renderGrid({
            toolbarQuickFilterCollapsible: true,
            quickFilterInitialValue: 'abc',
        });
        expect((quickFilterInput(container) as HTMLInputElement).value).toBe('abc');
    });
});

describe("MuiDataGrid barra d'eines: elements buits", () => {
    it('amb toolbarHideCreate no queda cap element buit a la barra (a part del separador)', () => {
        const { container } = renderGrid({ toolbarHideCreate: true, title: 'Pantalla' });
        const empty = Array.from(toolbarEl(container).children).filter(
            (c) => c.childNodes.length === 0 && (c as HTMLElement).style.flexGrow !== '1'
        );
        expect(empty.length).toBe(0);
    });
    it('per defecte els botons de la barra mantenen els marges de la llibreria', () => {
        renderGrid({ toolbarBackButton: true });
        const back = screen.getByTitle('datacommon.back.label');
        expect(getComputedStyle(back).marginRight).toBe('8px');
    });
});

describe("MuiDataGrid aparença dels botons de la barra d'eines", () => {
    const labels = ['datacommon.back.label', 'datacommon.export.label', 'datacommon.refresh.label'];
    it("per defecte tornar, exportar i refrescar són botons d'icona", () => {
        renderGrid({ toolbarBackButton: true, toolbarHideExport: false });
        labels.forEach((l) => expect(screen.getByTitle(l).className).toContain('MuiIconButton-root'));
    });
    it("toolbarButtonsVariant='button' els pinta com a botons amb text", () => {
        renderGrid({ toolbarBackButton: true, toolbarHideExport: false, toolbarButtonsVariant: 'button' });
        labels.forEach((l) => {
            const button = screen.getByRole('button', { name: new RegExp(l) });
            expect(button.className).toContain('MuiButton-root');
            expect(button.className).not.toContain('MuiIconButton-root');
        });
    });
    it('toolbarButtonsVariant via defaultMuiComponentProps.dataGrid', () => {
        renderGrid({}, { toolbarButtonsVariant: 'button' });
        const button = screen.getByRole('button', { name: /datacommon.refresh.label/ });
        expect(button.className).toContain('MuiButton-root');
    });
});
