import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { MuiBaseAppContext } from '../MuiBaseAppContext';
import { BaseAppContext } from '../../BaseAppContext';

// Es mockegen els contextos de formulari per renderitzar només la barra d'eines.
vi.mock('../../form/Form', () => ({
    Form: ({ children }: any) => <>{children}</>,
    useFormApiContext: () => ({ current: undefined }),
}));
vi.mock('../../form/FormContext', () => ({
    useFormContext: () => ({
        modified: false,
        isSaveActionPresent: false,
        isDeleteActionPresent: false,
    }),
}));

import { MuiForm, MuiFormProps } from './MuiForm';

const baseApp: any = {
    t: (k: string) => k,
    goBack: () => {},
    anyHistoryEntryExist: () => false,
    contentExpandsToAvailableHeight: false,
};

const renderForm = (props: Partial<MuiFormProps> = {}, defaults: Partial<MuiFormProps> = {}) =>
    render(
        <ThemeProvider theme={createTheme()}>
            <BaseAppContext.Provider value={baseApp}>
                <MuiBaseAppContext.Provider
                    value={{ defaultMuiComponentProps: { form: defaults }, appearance: {} }}>
                    <MuiForm {...({ id: '1', resourceName: 'x', ...props } as MuiFormProps)}>
                        <span data-testid="content" />
                    </MuiForm>
                </MuiBaseAppContext.Provider>
            </BaseAppContext.Provider>
        </ThemeProvider>
    );

const contentBox = () => screen.getByTestId('content').parentElement as HTMLElement;
const toolbar = (c: HTMLElement) => c.querySelector('.MuiToolbar-root') as HTMLElement;

describe("MuiForm barra d'eines i marges", () => {
    it('per defecte marges m: 2, mt: 3 i barra grey[200] sticky', () => {
        const { container } = renderForm();
        const style = getComputedStyle(contentBox());
        expect(style.marginTop).toBe('24px');
        expect(style.marginLeft).toBe('16px');
        expect(style.marginBottom).toBe('16px');
        expect(getComputedStyle(toolbar(container)).backgroundColor).toBe('rgb(238, 238, 238)');
        expect(getComputedStyle(toolbar(container)).position).toBe('sticky');
    });
    it('title pot ser un element', () => {
        const { container } = renderForm({ title: <span data-testid="t" /> });
        expect(toolbar(container).querySelector('[data-testid="t"]')).not.toBeNull();
    });
    it("toolbarSx s'aplica a la barra mantenint el sticky", () => {
        const { container } = renderForm({ toolbarSx: { backgroundColor: 'transparent' } });
        expect(getComputedStyle(toolbar(container)).backgroundColor).toBe('rgba(0, 0, 0, 0)');
        expect(getComputedStyle(toolbar(container)).position).toBe('sticky');
    });
    it('marginsSx substitueix els marges', () => {
        renderForm({ marginsSx: { m: 0 } });
        const style = getComputedStyle(contentBox());
        expect(style.marginTop).toBe('0px');
        expect(style.marginLeft).toBe('0px');
    });
    it('marginsSx com a array aplica tots dos elements', () => {
        renderForm({ marginsSx: [{ m: 0 }, { p: 1 }] });
        const style = getComputedStyle(contentBox());
        expect(style.marginTop).toBe('0px');
        expect(style.paddingTop).toBe('8px');
    });
    it('es poden indicar a defaultMuiComponentProps.form', () => {
        const { container } = renderForm(
            {},
            { marginsSx: { m: 1 }, toolbarSx: { backgroundColor: 'transparent' } }
        );
        expect(getComputedStyle(contentBox()).marginTop).toBe('8px');
        expect(getComputedStyle(toolbar(container)).backgroundColor).toBe('rgba(0, 0, 0, 0)');
    });
    it('per defecte el títol és un h6', () => {
        const { container } = renderForm({ title: 'Fitxa' });
        expect(toolbar(container).querySelector('h6')?.textContent).toBe('Fitxa');
    });
    it("toolbarTitleComponent='h2' pinta un h2", () => {
        const { container } = renderForm({ title: 'Fitxa', toolbarTitleComponent: 'h2' });
        expect(toolbar(container).querySelector('h6')).toBeNull();
        expect(toolbar(container).querySelector('h2')?.textContent).toBe('Fitxa');
    });
    it('toolbarTitleComponent via defaultMuiComponentProps.form', () => {
        const { container } = renderForm({ title: 'Fitxa' }, { toolbarTitleComponent: 'h2' });
        expect(toolbar(container).querySelector('h2')?.textContent).toBe('Fitxa');
    });
});
