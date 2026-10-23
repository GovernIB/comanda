import { render, screen, act, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { MuiBaseAppContext } from '../MuiBaseAppContext';
import { BaseAppContext } from '../../BaseAppContext';

// Es simula l'API de recursos: l'actualització queda pendent fins que el test la resol.
let resolveUpdate: ((value: any) => void) | undefined;
const update = vi.fn(
    () =>
        new Promise((resolve) => {
            resolveUpdate = resolve;
        })
);
const apiService = {
    isReady: true,
    currentFields: [],
    currentActions: { update: {} },
    getOne: () => Promise.resolve({ id: '1', _actions: { update: {} } }),
    onChange: () => Promise.resolve({}),
    create: vi.fn(),
    update,
    delete: vi.fn(),
    artifacts: () => Promise.resolve([]),
};
vi.mock('../../ResourceApiProvider', () => ({
    useResourceApiService: () => apiService,
}));

import { MuiForm, MuiFormProps } from './MuiForm';

const baseApp: any = {
    t: (k: string) => k,
    goBack: () => {},
    navigate: () => {},
    useLocationPath: () => '/x/1',
    anyHistoryEntryExist: () => false,
    temporalMessageShow: () => {},
    messageDialogShow: () => Promise.resolve(false),
    contentExpandsToAvailableHeight: false,
};

const renderForm = (props: Partial<MuiFormProps> = {}) =>
    render(
        <MemoryRouter>
            <ThemeProvider theme={createTheme()}>
                <BaseAppContext.Provider value={baseApp}>
                    <MuiBaseAppContext.Provider
                        value={{ defaultMuiComponentProps: {}, appearance: {} }}>
                        <MuiForm id="1" resourceName="x" {...props} />
                    </MuiBaseAppContext.Provider>
                </BaseAppContext.Provider>
            </ThemeProvider>
        </MemoryRouter>
    );

const saveButton = () => screen.getByTitle('form.update.title') as HTMLButtonElement;

describe('MuiForm protecció contra el doble enviament', () => {
    beforeEach(() => {
        update.mockClear();
        resolveUpdate = undefined;
    });
    it('doble clic a desar només envia una petició', async () => {
        renderForm();
        await waitFor(() => expect(saveButton()).toBeTruthy());
        act(() => {
            saveButton().click();
            saveButton().click();
        });
        expect(update).toHaveBeenCalledTimes(1);
        // Mentre es desa, el botó de desar queda desactivat
        expect(saveButton().disabled).toBe(true);
        await act(async () => {
            resolveUpdate?.({ id: '1' });
        });
        await waitFor(() => expect(saveButton().disabled).toBe(false));
        act(() => saveButton().click());
        expect(update).toHaveBeenCalledTimes(2);
    });
    it("si l'API retorna un error, el botó de desar es torna a activar", async () => {
        update.mockImplementationOnce(() => Promise.reject({ status: 500, message: 'error' }));
        renderForm();
        await waitFor(() => expect(saveButton()).toBeTruthy());
        await act(async () => saveButton().click());
        await waitFor(() => expect(saveButton().disabled).toBe(false));
        act(() => saveButton().click());
        expect(update).toHaveBeenCalledTimes(2);
    });
    it('si un callback llança una excepció, el formulari no queda bloquejat', async () => {
        const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
        let throwNow = true;
        const onBeforeSaveSuccess = (data: any) => {
            if (throwNow) {
                throwNow = false;
                throw new Error('callback');
            }
            return data;
        };
        renderForm({ onBeforeSaveSuccess });
        await waitFor(() => expect(saveButton()).toBeTruthy());
        await act(async () => saveButton().click());
        expect(update).not.toHaveBeenCalled();
        expect(saveButton().disabled).toBe(false);
        // L'error no queda amagat: es mostra a la consola
        expect(consoleError).toHaveBeenCalledWith(expect.objectContaining({ message: 'callback' }));
        act(() => saveButton().click());
        expect(update).toHaveBeenCalledTimes(1);
        consoleError.mockRestore();
    });
});
