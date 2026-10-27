import React from 'react';
import { render, screen, fireEvent, act, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { BaseAppContext } from '../../BaseAppContext';
import DataQuickFilter from './DataQuickFilter';

const baseApp: any = { t: (k: string) => k };

const Controlled: React.FC<{ initial?: string; collapsible?: boolean }> = ({ initial = '', collapsible }) => {
    const [value, setValue] = React.useState(initial);
    return (
        <BaseAppContext.Provider value={baseApp}>
            <DataQuickFilter value={value} onChange={setValue} collapsible={collapsible} />
            <button>fora</button>
        </BaseAppContext.Provider>
    );
};

const input = () => screen.queryByRole('textbox') as HTMLInputElement | null;
const searchButton = () => screen.queryByRole('button', { name: 'datacommon.quickfilter.label' });

describe('DataQuickFilter per defecte', () => {
    it('és un camp de text sempre visible', () => {
        render(<Controlled />);
        expect(input()).not.toBeNull();
        expect(input()!.closest('.MuiTextField-root')).not.toBeNull();
        expect(searchButton()).toBeNull();
        fireEvent.blur(input()!);
        expect(input()).not.toBeNull();
    });
});

describe('DataQuickFilter plegable', () => {
    it("buit, es mostra com un botó d'icona de cerca", () => {
        render(<Controlled collapsible />);
        expect(input()).toBeNull();
        const button = searchButton()!;
        expect(button.className).toContain('MuiIconButton-root');
        expect(button.getAttribute('title')).toBe('datacommon.quickfilter.label');
        expect(button.textContent).toBe('search');
    });
    it("en fer-hi clic s'obre el camp amb el focus", () => {
        render(<Controlled collapsible />);
        fireEvent.click(searchButton()!);
        expect(input()).not.toBeNull();
        expect(document.activeElement).toBe(input());
        expect(searchButton()).toBeNull();
    });
    it('en perdre el focus buit es torna a plegar', () => {
        render(<Controlled collapsible />);
        fireEvent.click(searchButton()!);
        fireEvent.blur(input()!);
        expect(input()).toBeNull();
        expect(searchButton()).not.toBeNull();
    });
    it('amb valor es queda obert en perdre el focus', () => {
        render(<Controlled collapsible />);
        fireEvent.click(searchButton()!);
        fireEvent.change(input()!, { target: { value: 'abc' } });
        fireEvent.blur(input()!);
        expect(input()!.value).toBe('abc');
    });
    it('amb un valor inicial (persistit) comença obert', () => {
        render(<Controlled collapsible initial="persistit" />);
        expect(input()!.value).toBe('persistit');
        expect(searchButton()).toBeNull();
    });
    it('netejar el valor torna el focus al camp i en perdre el focus es plega', () => {
        render(<Controlled collapsible initial="abc" />);
        fireEvent.click(screen.getByText('clear'));
        expect(input()!.value).toBe('');
        expect(document.activeElement).toBe(input());
        fireEvent.blur(input()!);
        expect(input()).toBeNull();
    });
});

const WithToolbar: React.FC<{ initial?: string; onExport?: () => void }> = ({ initial = '', onExport }) => {
    const [value, setValue] = React.useState(initial);
    return (
        <BaseAppContext.Provider value={baseApp}>
            <DataQuickFilter value={value} onChange={setValue} collapsible />
            <button onClick={onExport}>exportar</button>
            <button onClick={() => setValue('')}>netejar fora</button>
        </BaseAppContext.Provider>
    );
};

describe('DataQuickFilter plegable: correccions de la revisió', () => {
    it('amb un valor persistit, esborrar-lo amb el teclat no el plega mentre té el focus', () => {
        render(<WithToolbar initial="abc" />);
        act(() => input()!.focus());
        fireEvent.change(input()!, { target: { value: '' } });
        expect(input()).not.toBeNull();
        expect(document.activeElement).toBe(input());
    });
    it("en clicar un altre botó de la barra no es plega fins després del clic (el clic no es perd)", async () => {
        const onExport = vi.fn();
        render(<WithToolbar onExport={onExport} />);
        fireEvent.click(searchButton()!);
        const exportButton = screen.getByText('exportar');
        fireEvent.mouseDown(exportButton);
        act(() => exportButton.focus());
        expect(input()).not.toBeNull();
        fireEvent.mouseUp(exportButton);
        fireEvent.click(exportButton);
        expect(onExport).toHaveBeenCalledTimes(1);
        await waitFor(() => expect(input()).toBeNull());
        expect(searchButton()).not.toBeNull();
    });
    it('amb el teclat (Tab a un altre control) es plega de seguida', () => {
        render(<WithToolbar />);
        fireEvent.click(searchButton()!);
        act(() => screen.getByText('exportar').focus());
        expect(input()).toBeNull();
    });
    it('el botó plegat indica aria-expanded=false i controla el camp', () => {
        render(<WithToolbar />);
        const button = searchButton()!;
        expect(button.getAttribute('aria-expanded')).toBe('false');
        const controls = button.getAttribute('aria-controls');
        expect(controls).toBeTruthy();
        fireEvent.click(button);
        expect(input()!.id).toBe(controls);
    });
    it('Escape amb el camp buit el plega i torna el focus al botó de cerca', () => {
        render(<WithToolbar />);
        fireEvent.click(searchButton()!);
        fireEvent.keyDown(input()!, { key: 'Escape' });
        expect(input()).toBeNull();
        expect(document.activeElement).toBe(searchButton());
    });
    it('Escape amb valor no el plega', () => {
        render(<WithToolbar initial="abc" />);
        fireEvent.keyDown(input()!, { key: 'Escape' });
        expect(input()).not.toBeNull();
    });
    it('si el valor es buida des de fora amb el camp obert i sense focus, es plega', () => {
        render(<WithToolbar initial="abc" />);
        fireEvent.click(screen.getByText('netejar fora'));
        expect(input()).toBeNull();
        expect(searchButton()).not.toBeNull();
    });
    it('si el valor es buida des de fora després d\'haver-lo obert i perdut el focus, es plega', () => {
        render(<WithToolbar />);
        fireEvent.click(searchButton()!);
        fireEvent.change(input()!, { target: { value: 'x' } });
        act(() => input()!.blur());
        expect(input()).not.toBeNull();
        fireEvent.click(screen.getByText('netejar fora'));
        expect(input()).toBeNull();
    });
});
