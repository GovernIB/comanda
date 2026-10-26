import React from 'react';
import { render, screen, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { BaseAppContext, DialogButton } from '../../BaseAppContext';

// El formulari es substitueix per un element senzill que exposa un save() pendent.
const save = vi.fn(() => new Promise(() => {}));
vi.mock('./MuiForm', () => ({
    default: (props: any) => {
        props.apiRef.current = { save, getId: () => undefined, getData: () => ({}) };
        return <span data-testid="form" />;
    },
}));

import { useFormDialog } from './FormDialog';

const baseApp: any = { t: (k: string) => k };

type HarnessProps = {
    extraActions?: React.ReactNode;
    liveButtons?: DialogButton[];
};

const Harness: React.FC<HarnessProps> = ({ extraActions, liveButtons }) => {
    const [show, component] = useFormDialog(
        'x',
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        undefined,
        extraActions,
        liveButtons
    );
    React.useEffect(() => {
        show(1).catch(() => {});
    }, []);
    return component;
};

const renderDialog = (props: HarnessProps = {}) =>
    render(
        <ThemeProvider theme={createTheme()}>
            <BaseAppContext.Provider value={baseApp}>
                <Harness {...props} />
            </BaseAppContext.Provider>
        </ThemeProvider>
    );

const actionsEl = () => document.querySelector('.MuiDialogActions-root') as HTMLElement;
const buttonTexts = () =>
    Array.from(actionsEl().querySelectorAll('button')).map((b) => b.textContent);

describe('useFormDialog extraActions i liveButtons', () => {
    beforeEach(() => {
        save.mockClear();
    });
    it('per defecte, la fila de botons només té cancel·lar i desar', async () => {
        renderDialog();
        await screen.findByTestId('form');
        expect(buttonTexts()).toEqual(['buttons.form.cancel', 'savebuttons.form.save']);
        expect(actionsEl().children).toHaveLength(2);
    });
    it('extraActions es mostren a la barra de botons, davant dels botons', async () => {
        renderDialog({ extraActions: <button data-testid="extra">extra</button> });
        await screen.findByTestId('form');
        const extra = screen.getByTestId('extra');
        expect(actionsEl().contains(extra)).toBe(true);
        expect(actionsEl().firstElementChild?.contains(extra)).toBe(true);
    });
    it('liveButtons substitueixen els botons del diàleg', async () => {
        renderDialog({ liveButtons: [{ value: false, text: 'Tancar' }] });
        await screen.findByTestId('form');
        expect(buttonTexts()).toEqual(['Tancar']);
    });
    it('doble clic a desar només envia una petició', async () => {
        renderDialog();
        await screen.findByTestId('form');
        const saveBtn = Array.from(actionsEl().querySelectorAll('button')).find((b) =>
            b.textContent?.includes('buttons.form.save')
        ) as HTMLButtonElement;
        act(() => {
            saveBtn.click();
            saveBtn.click();
        });
        expect(save).toHaveBeenCalledTimes(1);
    });
    it('si desar llança una excepció, el diàleg es pot tornar a enviar', async () => {
        const onError = (e: ErrorEvent) => e.preventDefault();
        window.addEventListener('error', onError);
        const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
        save.mockImplementationOnce(() => {
            throw new Error('save');
        });
        renderDialog();
        await screen.findByTestId('form');
        const saveBtn = () =>
            Array.from(actionsEl().querySelectorAll('button')).find((b) =>
                b.textContent?.includes('buttons.form.save')
            ) as HTMLButtonElement;
        act(() => saveBtn().click());
        act(() => saveBtn().click());
        expect(save).toHaveBeenCalledTimes(2);
        consoleError.mockRestore();
        window.removeEventListener('error', onError);
    });
});

describe('useFormDialog dialogButtons passats a show()', () => {
    const custom: DialogButton[] = [{ value: 'a', text: 'Botó propi' } as DialogButton];
    const ShowHarness: React.FC<{ tick: number }> = () => {
        const [show, component] = useFormDialog(
            'x',
            undefined,
            undefined,
            // Cada pintada genera un array nou
            [{ value: 'p', text: 'Del pare' } as DialogButton]
        );
        React.useEffect(() => {
            show(1, { dialogButtons: custom }).catch(() => {});
        }, []);
        return component;
    };
    it('sobreviuen a un re-render del pare amb un array nou', async () => {
        const wrap = (tick: number) => (
            <ThemeProvider theme={createTheme()}>
                <BaseAppContext.Provider value={baseApp}>
                    <ShowHarness tick={tick} />
                </BaseAppContext.Provider>
            </ThemeProvider>
        );
        const { rerender } = render(wrap(0));
        await screen.findByTestId('form');
        expect(buttonTexts()).toEqual(['Botó propi']);
        rerender(wrap(1));
        expect(buttonTexts()).toEqual(['Botó propi']);
    });
});
