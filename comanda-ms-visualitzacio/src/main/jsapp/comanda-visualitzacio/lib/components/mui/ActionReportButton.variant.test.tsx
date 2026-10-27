import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { BaseAppContext } from '../BaseAppContext';

const artifactAction = vi.hoisted(() => vi.fn(() => Promise.resolve({})));

// Es simulen l'API de recursos, els botons dels diàlegs i el diàleg del formulari.
vi.mock('../ResourceApiProvider', () => ({
    useResourceApiService: () => ({
        isReady: true,
        artifacts: () =>
            Promise.resolve([
                { type: 'ACTION', code: 'acc', formClassActive: false, _links: { exec_acc: { title: 'Acció' } } },
            ]),
        artifactAction,
        artifactReport: () => Promise.resolve({}),
    }),
}));
vi.mock('../AppButtons', () => ({
    useActionDialogButtons: () => [],
    useReportDialogButtons: () => [],
    useConfirmDialogButtons: () => [],
    useCloseDialogButtons: () => [],
}));
vi.mock('./form/FormDialog', () => ({
    useFormDialog: () => [() => Promise.resolve(), null, () => {}],
}));

import { ActionReportButton } from './ActionReportButton';

const baseApp: any = {
    t: (k: string) => k,
    temporalMessageShow: () => {},
    messageDialogShow: () => Promise.resolve(false),
};

const renderButton = (props: any) =>
    render(
        <BaseAppContext.Provider value={baseApp}>
            <ActionReportButton resourceName="x" action="acc" {...props} />
        </BaseAppContext.Provider>
    );

describe('ActionReportButton variant', () => {
    it("per defecte, amb icona, és un botó d'icona amb el títol com a title", async () => {
        renderButton({ icon: 'bolt', title: 'Executar' });
        const button = await screen.findByTitle('Executar');
        expect(button.className).toContain('MuiIconButton-root');
    });
    it("per defecte, sense icona, és un botó de text (com fins ara)", async () => {
        renderButton({ title: 'Executar', buttonIcon: 'bolt' });
        const button = await screen.findByRole('button', { name: /Executar/ });
        expect(button.className).toContain('MuiButton-root');
        expect(button.querySelector('.MuiButton-startIcon')?.textContent).toBe('bolt');
    });
    it("variant 'icon' amb icona és igual que per defecte", async () => {
        renderButton({ icon: 'bolt', title: 'Executar', variant: 'icon' });
        expect((await screen.findByTitle('Executar')).className).toContain('MuiIconButton-root');
    });
    it("variant 'button' amb icona: botó amb la icona al principi i el títol com a text", async () => {
        artifactAction.mockClear();
        renderButton({ icon: 'bolt', title: 'Executar', variant: 'button' });
        const button = await screen.findByRole('button', { name: /Executar/ });
        expect(button.className).toContain('MuiButton-root');
        expect(button.className).not.toContain('MuiIconButton-root');
        expect(button.querySelector('.MuiButton-startIcon')?.textContent).toBe('bolt');
        fireEvent.click(button);
        expect(artifactAction).toHaveBeenCalledTimes(1);
    });
    it("variant 'button' desactivat i amb insígnia", async () => {
        renderButton({ icon: 'bolt', title: 'Executar', variant: 'button', disabled: true, selectedCount: 2 });
        const button = await screen.findByRole('button', { name: /Executar/ });
        expect((button as HTMLButtonElement).disabled).toBe(true);
        expect(screen.getByText('2').className).toContain('MuiBadge-badge');
    });
});
