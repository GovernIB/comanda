import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import '@testing-library/jest-dom/vitest';
import { MuiBaseApp } from './MuiBaseApp';
import { MuiBaseAppAppearance } from './MuiBaseAppContext';
import { useHeaderHeight } from '../../util/useHeaderHeight';
import { useMuiBaseAppAppearance } from './MuiBaseAppContext';
import { ResourceApiContext } from '../ResourceApiContext';
import * as smallScreen from '../../util/useSmallScreen';

const Probe: React.FC = () => <span data-testid="probe">{useHeaderHeight()}</span>;

const resourceApiStub: any = {
    offline: false,
    isLoading: false,
    isReady: true,
    setOpenAnswerRequiredDialog: () => {},
    getOpenAnswerRequiredDialog: () => undefined,
};

const renderApp = (appearance?: MuiBaseAppAppearance, extra?: React.ReactNode) =>
    render(
        <ResourceApiContext.Provider value={resourceApiStub}>
            <MuiBaseApp
                code="test"
                headerTitle="Titol"
                menuEntries={[{ id: 'a', title: 'A', to: '/a' } as any]}
                footerHeight={0}
                i18nUseTranslation={() => ({ t: (k: string) => k })}
                routerGoBack={() => {}}
                routerNavigate={() => {}}
                routerAnyHistoryEntryExist={() => false}
                routerUseLocationPath={() => '/'}
                linkComponent="a"
                appearance={appearance}>
                <Probe />
                {extra}
            </MuiBaseApp>
        </ResourceApiContext.Provider>
    );

const mockSmallHeader = (value: boolean) =>
    vi.spyOn(smallScreen, 'useSmallHeader').mockReturnValue(value);

afterEach(() => vi.restoreAllMocks());

describe('MuiBaseApp appearance headerHeight', () => {
    it("headerHeight per defecte és 64 i l'AppBar no fixa alçada", () => {
        mockSmallHeader(false);
        renderApp();
        expect(screen.getByTestId('probe')).toHaveTextContent('64');
        const header = document.querySelector('header') as HTMLElement;
        expect(header).toBeTruthy();
        expect(getComputedStyle(header).height).not.toBe('64px');
        expect(getComputedStyle(header.querySelector('.MuiToolbar-root')!).height).toBe('');
    });
    it("headerHeight indicat s'aplica a hook, AppBar i menú", () => {
        mockSmallHeader(false);
        renderApp({ headerHeight: 74 });
        expect(screen.getByTestId('probe')).toHaveTextContent('74');
        const header = document.querySelector('header') as HTMLElement;
        const toolbar = header.querySelector('.MuiToolbar-root') as HTMLElement;
        expect(toolbar.style.height).toBe('74px');
        expect(toolbar.style.minHeight).toBe('74px');
        const spacers = Array.from(document.querySelectorAll('.MuiDrawer-root div')).filter(
            (e) => getComputedStyle(e).marginTop === '74px'
        );
        expect(spacers.length).toBeGreaterThan(0);
    });
    it('pantalla petita sense valor retorna 56', () => {
        mockSmallHeader(true);
        renderApp();
        expect(screen.getByTestId('probe')).toHaveTextContent('56');
    });
    it('pantalla petita amb valor indicat fa servir el valor', () => {
        mockSmallHeader(true);
        renderApp({ headerHeight: 74 });
        expect(screen.getByTestId('probe')).toHaveTextContent('74');
    });
    it('useMuiBaseAppAppearance retorna {} fora del provider', () => {
        const A: React.FC = () => (
            <span data-testid="a">{JSON.stringify(useMuiBaseAppAppearance())}</span>
        );
        render(<A />);
        expect(screen.getByTestId('a')).toHaveTextContent('{}');
    });
});
