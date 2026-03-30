import React from 'react';
import { render, screen } from '@testing-library/react';
import { act, renderHook } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { useAclPermissionManager } from './AclPermissionManager';

const mocks = vi.hoisted(() => ({
    showMock: vi.fn(),
    closeMock: vi.fn(),
    tMock: vi.fn((selector: (input: { components: { permisos: { title: string; resourceTitle: string } } }) => string) =>
        selector({ components: { permisos: { title: 'Permisos', resourceTitle: 'Permís del recurs' } } })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name }: { name: string }) => <div>{name}</div>,
    MuiDataGridDialog: ({ apiRef }: { apiRef: React.MutableRefObject<any> }) => {
        apiRef.current = {
            show: mocks.showMock,
            close: mocks.closeMock,
        };
        return <div data-testid="acl-dialog">Diàleg ACL</div>;
    },
}));

describe('useAclPermissionManager', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('useAclPermissionManager_quanEsRenderitza_exposaElComponentDelDialeg', () => {
        // Comprova que el hook retorna el component visual necessari per al diàleg de permisos.
        const { result } = renderHook(() => useAclPermissionManager('widget'));

        render(result.current.component);

        expect(screen.getByTestId('acl-dialog')).toBeInTheDocument();
    });

    it('useAclPermissionManager_quanEsCridaShow_obriElDialegAmbElFiltreEsperat', () => {
        // Verifica que el hook obre el diàleg amb el filtre i les dades inicials del recurs.
        const { result } = renderHook(() => useAclPermissionManager('widget'));
        render(result.current.component);

        act(() => {
            result.current.show(25, 'Widget principal');
        });

        expect(mocks.showMock).toHaveBeenCalledWith({
            title: 'Widget principal',
            dataGridComponentProps: expect.objectContaining({
                title: 'Permisos',
                staticFilter: "resourceType:'widget' and resourceId:25",
                formAdditionalData: expect.any(Function),
                popupEditFormDialogResourceTitle: 'Permís del recurs',
            }),
        });
        const formAdditionalData = mocks.showMock.mock.calls[0][0].dataGridComponentProps.formAdditionalData;
        expect(formAdditionalData(undefined, 'create')).toEqual({
            resourceType: 'widget',
            resourceId: 25,
            subjectType: 'ROLE',
            readAllowed: true,
        });
        expect(formAdditionalData({ subjectType: 'USER' }, 'update')).toEqual({
            resourceType: 'widget',
            resourceId: 25,
        });
    });

    it('useAclPermissionManager_quanEsCridaClose_tancaElDialeg', () => {
        // Comprova que el hook delega el tancament a l'API interna del diàleg.
        const { result } = renderHook(() => useAclPermissionManager('widget'));
        render(result.current.component);

        act(() => {
            result.current.close();
        });

        expect(mocks.closeMock).toHaveBeenCalledTimes(1);
    });
});
