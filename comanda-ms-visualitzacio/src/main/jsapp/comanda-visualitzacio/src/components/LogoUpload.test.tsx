import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import LogoUpload from './LogoUpload';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            form: {
                field: {
                    file: {
                        edit: 'Editar',
                        download: 'Descarregar',
                        clear: 'Esborrar',
                        avatarAlt: 'Avatar',
                    },
                },
            },
        })
    ),
    setFieldValueMock: vi.fn(),
    useFormContextMock: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    useFormContext: () => mocks.useFormContextMock(),
}));

describe('LogoUpload', () => {
    beforeEach(() => {
        mocks.useFormContextMock.mockReturnValue({
            data: {},
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
        vi.clearAllMocks();
    });

    it('LogoUpload_quanNoHiHaLogo_noMostraElBotoDeDescarrega', () => {
        // Comprova que el component amaga la descàrrega mentre no hi ha cap imatge carregada.
        render(<LogoUpload name="logo" label="Logotip" />);

        expect(screen.queryByLabelText('Descarregar')).not.toBeInTheDocument();
    });

    it('LogoUpload_quanEsPremEsborrar_netejaElCampDelFormulari', () => {
        // Verifica que el botó d'esborrar posa el valor del logo a null al formulari.
        mocks.useFormContextMock.mockReturnValue({
            data: { logo: 'YmFzZTY0' },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<LogoUpload name="logo" label="Logotip" />);

        fireEvent.click(screen.getByLabelText('Esborrar'));

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('logo', null);
    });

    it('LogoUpload_quanHiHaPreviewIPremDescarregar_creaLAnchorDeDescarrega', () => {
        // Comprova que el botó de descàrrega genera i dispara l'enllaç temporal amb la imatge actual.
        mocks.useFormContextMock.mockReturnValue({
            data: { logo: 'YmFzZTY0' },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });
        const appendChildSpy = vi.spyOn(document.body, 'appendChild');
        const removeChildSpy = vi.spyOn(document.body, 'removeChild');
        const clickMock = vi.fn();
        const originalCreateElement = document.createElement.bind(document);
        let createdAnchor: HTMLAnchorElement | null = null;
        const createElementSpy = vi
            .spyOn(document, 'createElement')
            .mockImplementation(((tagName: string) => {
                if (tagName === 'a') {
                    const anchor = originalCreateElement('a');
                    anchor.click = clickMock;
                    createdAnchor = anchor;
                    return anchor;
                }
                return originalCreateElement(tagName);
            }) as typeof document.createElement);

        render(<LogoUpload name="logo" label="Logotip" />);

        fireEvent.click(screen.getByLabelText('Descarregar'));

        expect(createElementSpy).toHaveBeenCalledWith('a');
        expect(createdAnchor).not.toBeNull();
        expect(appendChildSpy).toHaveBeenCalledWith(createdAnchor);
        expect(clickMock).toHaveBeenCalledTimes(1);
        expect(removeChildSpy).toHaveBeenCalledWith(createdAnchor);
    });
});
