import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import ProtectedRoute from './ProtectedRoute';
import {MemoryRouter, Routes, Route } from "react-router-dom";

const mocks = vi.hoisted(() => ({
    useResourceApiContextMock: vi.fn(),
    tMock: vi.fn((selector: (input: { page: { noPermissions: string } }) => string) =>
        selector({ page: { noPermissions: 'Sense permisos' } })
    ),
}));

vi.mock('reactlib', () => ({
    useResourceApiContext: () => mocks.useResourceApiContextMock(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

describe('ProtectedRoute', () => {
    it('ProtectedRoute_quanLEncaraNoSabenElsPermisos_mostraUnSpinnerACentreDePantalla', () => {
        // Comprova que el component mostra un estat de càrrega mentre encara no hi ha índex de recursos.
        mocks.useResourceApiContextMock.mockReturnValue({ indexState: undefined });

        render(
            <ProtectedRoute resourceName="dashboard">
                <div>Contingut</div>
            </ProtectedRoute>
        );

        expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('ProtectedRoute_quanNoHiHaAcces_mostraElMissatgeDeSensePermisos', () => {
        // Verifica que el component bloqueja el contingut quan falta algun resource requerit.
        mocks.useResourceApiContextMock.mockReturnValue({
            indexState: {
                links: new Map([['dashboard', {}]]),
            },
        });

        render(
            <ProtectedRoute resourceName={['dashboard', 'dashboardItem']}>
                <div>Contingut</div>
            </ProtectedRoute>
        );

        expect(screen.getByText('Sense permisos')).toBeInTheDocument();
        expect(screen.queryByText('Contingut')).not.toBeInTheDocument();
    });

    it('ProtectedRoute_quanNoHiHaAcces_iHiHaRedirect', () => {
        // Verifica que el component bloqueja el contingut y redirigeix.
        mocks.useResourceApiContextMock.mockReturnValue({
            indexState: {
                links: new Map([['dashboard', {}]]),
            },
        });

        render(
            <MemoryRouter initialEntries={['/contingut']}>
                <Routes>
                    <Route
                        path="/contingut"
                        element={
                            <ProtectedRoute
                                resourceName={['dashboard', 'dashboardItem']}
                                redirect="/contingut2"
                            >
                                <div>Contingut</div>
                            </ProtectedRoute>
                        }
                    />
                    <Route path="/contingut2" element={<div>Redireccionat</div>} />
                </Routes>
            </MemoryRouter>
        );

        // Verifica que ha redirigit
        expect(screen.getByText('Redireccionat')).toBeInTheDocument();

        // Opcional: asegurar que no muestra contingut protejit ni missatje
        expect(screen.queryByText('Contingut')).not.toBeInTheDocument();
        expect(screen.queryByText('Sense permisos')).not.toBeInTheDocument();
    });

    it('ProtectedRoute_quanTeAccesATotsElsResources_renderitzaElsFills', () => {
        // Comprova que el component deixa passar el contingut quan l'usuari té tots els recursos requerits.
        mocks.useResourceApiContextMock.mockReturnValue({
            indexState: {
                links: new Map([
                    ['dashboard', {}],
                    ['dashboardItem', {}],
                ]),
            },
        });

        render(
            <ProtectedRoute resourceName={['dashboard', 'dashboardItem']}>
                <div>Contingut permès</div>
            </ProtectedRoute>
        );

        expect(screen.getByText('Contingut permès')).toBeInTheDocument();
    });
});
