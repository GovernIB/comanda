import React from 'react';
import { act, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from './ContainerAuthProvider';
import AuthContext from './AuthContext';

const AUTH_TOKEN_SRC = 'http://localhost:8080/comandaback/authToken';

const base64Url = (value: object) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

const tokenValid = () =>
    `${base64Url({ alg: 'none' })}.${base64Url({ exp: Math.floor(Date.now() / 1000) + 3600, preferred_username: 'usuari' })}.firma`;

const SignOutButton: React.FC = () => {
    const auth = React.useContext(AuthContext) as any;
    return (
        <button type="button" onClick={() => auth.signOut?.()}>
            sortir
        </button>
    );
};

describe('ContainerAuthProvider', () => {
    let hrefAssignat: string | undefined;

    beforeEach(() => {
        // El proveïdor llegeix la URL de /authToken del tercer <script> del <head>
        document.head.innerHTML = '';
        ['a.js', 'b.js', AUTH_TOKEN_SRC].forEach((src) => {
            const script = document.createElement('script');
            script.src = src;
            document.head.appendChild(script);
        });
        (window as any).__AUTH_TOKEN__ = tokenValid();
        hrefAssignat = undefined;
        Object.defineProperty(window, 'location', {
            configurable: true,
            value: {
                ...window.location,
                set href(value: string) {
                    hrefAssignat = value;
                },
                get href() {
                    return hrefAssignat ?? 'http://localhost:8080/comandaback/';
                },
            },
        });
    });

    afterEach(() => {
        delete (window as any).__AUTH_TOKEN__;
        document.head.innerHTML = '';
    });

    it('ContainerAuthProvider_quanEsFaSignOut_navegaAlLogoutDelBackendSenseFetch', async () => {
        // El logout ha de ser una navegació real del navegador: amb fetch() el redirect a l'end_session_endpoint de
        // Keycloak no es fa mai des del navegador i la sessió SSO queda oberta (re-login silenciós).
        const fetchSpy = vi.fn();
        vi.stubGlobal('fetch', fetchSpy);

        render(
            <AuthProvider logoutUrl="/comandaback/">
                <SignOutButton />
            </AuthProvider>
        );
        const boto = await screen.findByRole('button', { name: 'sortir' });
        act(() => boto.click());

        expect(hrefAssignat).toBe('http://localhost:8080/comandaback/logout');
        expect(fetchSpy).not.toHaveBeenCalled();
    });
});
