import { render } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { MuiBaseApp } from './mui/MuiBaseApp';
import { ResourceApiContext } from './ResourceApiContext';

const resourceApiStub: any = {
    offline: false,
    isLoading: false,
    isReady: true,
    setOpenAnswerRequiredDialog: () => {},
    getOpenAnswerRequiredDialog: () => undefined,
};

const renderApp = (extra?: Record<string, any>) =>
    render(
        <ResourceApiContext.Provider value={resourceApiStub}>
            <MuiBaseApp
                code="test"
                footerHeight={0}
                i18nUseTranslation={() => ({ t: (k: string) => k })}
                routerGoBack={() => {}}
                routerNavigate={() => {}}
                routerAnyHistoryEntryExist={() => false}
                routerUseLocationPath={() => '/'}
                linkComponent="a"
                {...extra}>
                <span>contingut</span>
            </MuiBaseApp>
        </ResourceApiContext.Provider>
    );

const getMain = (container: HTMLElement) => container.querySelector('main') as HTMLElement;

describe('BaseApp estils del contingut', () => {
    it('per defecte main té margin 16px 24px', () => {
        const { container } = renderApp();
        expect(getMain(container).style.margin).toBe('16px 24px');
    });
    it('amb marginsDisabled main no té margin', () => {
        const { container } = renderApp({ marginsDisabled: true });
        expect(getMain(container).style.margin).toBe('');
    });
    it('contentMainStyle es combina amb els estils de main', () => {
        const { container } = renderApp({ contentMainStyle: { padding: 24 } });
        const main = getMain(container);
        expect(main.style.padding).toBe('24px');
        expect(main.style.margin).toBe('16px 24px');
        expect(main.style.flexGrow).toBe('1');
    });
    it("contentRootStyle es combina amb els estils de l'arrel", () => {
        const { container } = renderApp({ contentRootStyle: { background: 'red' } });
        const root = getMain(container).parentElement!.parentElement as HTMLElement;
        expect(root.style.background).toBe('red');
        expect(root.style.display).toBe('flex');
    });
});
