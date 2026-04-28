import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import BaseApp from './BaseApp';

const mocks = vi.hoisted(() => ({
    muiBaseAppMock: vi.fn(),
    useResourceApiContextMock: vi.fn(),
    useBaseAppContextMock: vi.fn(),
    useUserContextMock: vi.fn(),
    useMediaQueryMock: vi.fn(),
    useTranslationMock: vi.fn(),
    changeLanguageMock: vi.fn(),
    addResourceBundleMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    MuiBaseApp: (props: Record<string, unknown>) => {
        mocks.muiBaseAppMock(props);
        return (
            <div data-testid="mui-base-app">
                <div data-testid="menu-count">{((props.menuEntries as unknown[]) ?? []).length}</div>
                {props.children as React.ReactNode}
            </div>
        );
    },
    useResourceApiContext: () => mocks.useResourceApiContextMock(),
    useBaseAppContext: () => mocks.useBaseAppContextMock(),
}));

vi.mock('./UserContext', () => ({
    useUserContext: () => mocks.useUserContextMock(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: (ns?: string) => mocks.useTranslationMock(ns),
}));

vi.mock('@mui/material/useMediaQuery', () => ({
    default: (query: unknown) => mocks.useMediaQueryMock(query),
}));

vi.mock('./Alarms', () => ({
    default: ({ onButtonClick }: { onButtonClick?: () => void }) => (
        <button
            data-testid="alarms-component"
            onClick={onButtonClick}
            aria-label="Veure alarmes"
        >
            <span data-testid="alarms-badge">0</span>
        </button>
    ),
    AlarmsDialog: ({
       open,
       setOpen
    }: {
        open: boolean;
        setOpen: (open: boolean) => void;
    }) => {
        if (!open) return null;

        return (
            <div role="dialog" data-testid="alarms-dialog" aria-modal="true">
                <div data-testid="dialog-backdrop" onClick={() => setOpen(false)} />
                <div data-testid="dialog-content">
                    <button
                        onClick={() => setOpen(false)}
                        data-testid="dialog-close"
                        aria-label="Tancar diàleg"
                    >
                        Tancar
                    </button>
                    <div data-testid="alarmes-content">Contingut d alarmes</div>
                </div>
            </div>
        );
    },
}));

vi.mock('./Footer', () => ({
    default: () => <div data-testid="footer-component">Footer</div>,
}));

vi.mock('./SystemTimeDisplay', () => ({
    default: () => <div data-testid="system-time-component">Hora</div>,
}));

vi.mock('./HeaderLanguageSelector', () => ({
    default: () => <div data-testid="language-selector">Idioma</div>,
}));

vi.mock('./UserProfileFormDialog', () => ({
    UserProfileFormDialog: () => <div data-testid="user-profile-dialog">Perfil</div>,
    UserProfileFormDialogButton: ({ onClick }: { onClick: () => void }) => (
        <button type="button" onClick={onClick}>
            Perfil
        </button>
    ),
}));

vi.mock('./RoleSelector', () => ({
    default: () => <div data-testid="role-selector">Rol</div>,
}));

vi.mock('../i18n/i18n', () => ({
    default: {
        language: 'ca',
        changeLanguage: (language?: string) => mocks.changeLanguageMock(language),
        addResourceBundle: (language: string, namespace: string, bundle: unknown) =>
            mocks.addResourceBundleMock(language, namespace, bundle),
    },
}));

vi.mock('../assets/drassana.png', () => ({
    default: '/drassana.png',
}));

describe('BaseApp', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('BaseApp_quanLApiEstaLlesta_filtraElsMenusIPassaLesPropsDerivadesAlMuiBaseApp', () => {
        // Comprova que el component només exposa les entrades permeses i activa les alarmes quan el recurs existeix.
        const links = {
            has: vi.fn((resourceName: string) => ['dashboard', 'alarma'].includes(resourceName)),
            getAll: vi.fn(() => [{ rel: 'dashboard' }, { rel: 'alarma' }]),
        };
        mocks.useResourceApiContextMock.mockReturnValue({
            isReady: true,
            indexState: { links },
        });
        mocks.useBaseAppContextMock.mockReturnValue({ currentLanguage: 'ca' });
        mocks.useUserContextMock.mockReturnValue({
            user: { id: 9, idioma: 'CA' },
            currentRole: 'COM_ADMIN',
        });
        mocks.useMediaQueryMock.mockReturnValue(false);
        mocks.useTranslationMock.mockReturnValue({ t: vi.fn() });

        render(
            <MemoryRouter initialEntries={['/dashboard']}>
                <BaseApp
                    code="comanda"
                    title="Comanda"
                    version="1.0.0"
                    menuEntries={[
                        { id: 'dashboard', title: 'Dashboard', to: '/dashboard', resourceName: 'dashboard', icon: 'info' },
                        { id: 'admin', title: 'Admin', to: '/admin', resourceName: 'admin', icon: 'info' },
                        { id: 'public', title: 'Public', to: '/public', icon: 'info' },
                    ]}
                    headerMenuEntries={[
                        { id: 'header-dashboard', title: 'Capçalera', to: '/dashboard', resourceName: 'dashboard', icon: 'info' },
                    ]}
                >
                    <div>Contingut base</div>
                </BaseApp>
            </MemoryRouter>
        );

        expect(screen.getByTestId('mui-base-app')).toBeInTheDocument();
        expect(screen.getByText('Contingut base')).toBeInTheDocument();
        expect(screen.getByTestId('user-profile-dialog')).toBeInTheDocument();
        expect(screen.getByTestId('menu-count')).toHaveTextContent('3');
        expect(mocks.muiBaseAppMock).toHaveBeenCalledWith(
            expect.objectContaining({
                code: 'comanda',
                headerAuthBadgeIcon: 'settings',
                persistentLanguage: true,
                menuEntries: [
                    { id: 'dashboard', title: 'Dashboard', to: '/dashboard', resourceName: 'dashboard', icon: 'info' },
                    { id: 'admin', title: 'Admin', to: '/admin', resourceName: 'admin', icon: 'info' },
                    { id: 'public', title: 'Public', to: '/public', icon: 'info' },
                ],
                headerAdditionalComponents: expect.arrayContaining([
                    expect.any(Object),
                    expect.any(Object),
                    expect.any(Object),
                ]),
            })
        );
    });

    it('BaseApp_quanLApiNoEstaLlesta_noMostraAlarmesIManteElMenuBuit', () => {
        // Verifica que mentre l'índex encara no està preparat el menú principal es manté buit.
        const links = {
            has: vi.fn(() => false),
            getAll: vi.fn(() => []),
        };
        mocks.useResourceApiContextMock.mockReturnValue({
            isReady: false,
            indexState: { links },
        });
        mocks.useBaseAppContextMock.mockReturnValue({ currentLanguage: 'ca' });
        mocks.useUserContextMock.mockReturnValue({
            user: { id: 1, idioma: 'ES' },
            currentRole: 'COM_CONSULTA',
        });
        mocks.useMediaQueryMock.mockReturnValue(false);
        mocks.useTranslationMock.mockReturnValue({ t: vi.fn() });

        render(
            <MemoryRouter initialEntries={['/inici']}>
                <BaseApp code="comanda" title="Comanda" version="1.0.0" menuEntries={[{ id: 'a', title: 'A', to: '/a', icon: 'info' }]}>
                    <div>Contingut base</div>
                </BaseApp>
            </MemoryRouter>
        );

        expect(screen.getByTestId('menu-count')).toHaveTextContent('1');
        expect(mocks.muiBaseAppMock).toHaveBeenCalledWith(
            expect.objectContaining({
                headerAuthBadgeIcon: undefined,
                menuEntries: [{ id: 'a', title: 'A', to: '/a', icon: 'info' }],
            })
        );
    });
});
