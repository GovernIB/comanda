import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
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
