import React from 'react';
import { renderHook } from '@testing-library/react';
import { vi } from 'vitest';
import { describe, expect, it } from 'vitest';
import {
    UserContext,
    useIsUserAdmin,
    useIsUserConsulta,
    useIsUserUsuari,
    useUserContext,
} from './UserContext';

vi.mock('./UserProvider.tsx', () => ({
    ROLE_USER: 'COM_USER',
    ROLE_ADMIN: 'COM_ADMIN',
    ROLE_CONSULTA: 'COM_CONSULTA',
}));

describe('UserContext', () => {
    it('useUserContext_quanNoHiHaProvider_llencaUnaExcepcio', () => {
        // Comprova que el hook falla ràpid si s'utilitza fora del provider.
        expect(() => renderHook(() => useUserContext())).toThrow('UserContext Provider not found');
    });

    it('useUserContext_quanHiHaProvider_retornaElContextConfigurat', () => {
        // Verifica que el hook retorna el mateix objecte injectat pel provider.
        const contextValue = {
            user: { id: 7 } as any,
            refresh: () => undefined,
            previewUser: () => undefined,
            clearUserPreview: () => undefined,
            currentRole: 'COM_ADMIN',
            setCurrentRole: () => undefined,
        };

        const wrapper = ({ children }: { children: React.ReactNode }) =>
            React.createElement(UserContext.Provider, { value: contextValue }, children);

        const { result } = renderHook(() => useUserContext(), { wrapper });

        expect(result.current).toBe(contextValue);
    });

    it('useIsUserAdminIConsultaIUsuari_quanCanviaElRol_retornenLestatCorrecte', () => {
        // Comprova que els selectors deriven correctament l'estat a partir del rol actual.
        const wrapperAdmin = ({ children }: { children: React.ReactNode }) =>
            React.createElement(
                UserContext.Provider,
                {
                    value: {
                        refresh: () => undefined,
                        previewUser: () => undefined,
                        clearUserPreview: () => undefined,
                        currentRole: 'COM_ADMIN',
                        setCurrentRole: () => undefined,
                    },
                },
                children
            );
        const wrapperConsulta = ({ children }: { children: React.ReactNode }) =>
            React.createElement(
                UserContext.Provider,
                {
                    value: {
                        refresh: () => undefined,
                        previewUser: () => undefined,
                        clearUserPreview: () => undefined,
                        currentRole: 'COM_CONSULTA',
                        setCurrentRole: () => undefined,
                    },
                },
                children
            );
        const wrapperUsuari = ({ children }: { children: React.ReactNode }) =>
            React.createElement(
                UserContext.Provider,
                {
                    value: {
                        refresh: () => undefined,
                        previewUser: () => undefined,
                        clearUserPreview: () => undefined,
                        currentRole: 'COM_USER',
                        setCurrentRole: () => undefined,
                    },
                },
                children
            );

        const { result: adminResult } = renderHook(
            () => ({ isAdmin: useIsUserAdmin(), isConsulta: useIsUserConsulta(), isUsuari: useIsUserUsuari() }),
            { wrapper: wrapperAdmin }
        );
        const { result: consultaResult } = renderHook(
            () => ({ isAdmin: useIsUserAdmin(), isConsulta: useIsUserConsulta(), isUsuari: useIsUserUsuari() }),
            { wrapper: wrapperConsulta }
        );
        const { result: usuariResult } = renderHook(
            () => ({ isAdmin: useIsUserAdmin(), isConsulta: useIsUserConsulta(), isUsuari: useIsUserUsuari() }),
            { wrapper: wrapperUsuari }
        );

        expect(adminResult.current).toEqual({ isAdmin: true, isConsulta: false, isUsuari: false });
        expect(consultaResult.current).toEqual({ isAdmin: false, isConsulta: true, isUsuari: false });
        expect(usuariResult.current).toEqual({ isAdmin: false, isConsulta: false, isUsuari: true });
    });
});
