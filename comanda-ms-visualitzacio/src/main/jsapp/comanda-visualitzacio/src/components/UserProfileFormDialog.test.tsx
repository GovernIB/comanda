import { fireEvent, render, screen } from '@testing-library/react';
import { act } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import {
    TemaObscurSelector,
    UserProfileFormDialog,
    UserProfileFormDialogButton,
} from './UserProfileFormDialog';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            menu: {
                user: {
                    options: {
                        profile: {
                            title: 'Perfil',
                            tema: {
                                clar: 'Clar',
                                obscur: 'Obscur',
                                sistema: 'Sistema',
                            },
                            form: {
                                userData: 'Dades usuari',
                                genericConfig: 'Configuració genèrica',
                            },
                        },
                    },
                },
            },
        })
    ),
    setFieldValueMock: vi.fn(),
    useFormContextMock: vi.fn(),
    refreshMock: vi.fn(),
    muiFormDialogProps: null as any,
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name, disabled }: { name: string; disabled?: boolean }) => (
        <div data-disabled={disabled ? 'true' : 'false'}>{name}</div>
    ),
    MuiFormDialog: (props: any) => {
        mocks.muiFormDialogProps = props;
        return <div data-testid="mui-form-dialog">{props.children}</div>;
    },
    useFormContext: () => mocks.useFormContextMock(),
}));

vi.mock('./UserContext', () => ({
    useUserContext: () => ({
        refresh: mocks.refreshMock,
    }),
}));

vi.mock('../types/usuari.model.tsx', () => ({
    UsuariModel: {
        CODI: 'codi',
        NOM: 'nom',
        NIF: 'nif',
        EMAIL: 'email',
        ROLS: 'rols',
        EMAIL_ALTERNATIU: 'emailAlternatiu',
        ALARMA_MAIL: 'alarmaMail',
        ALARMA_MAIL_AGRUPAT: 'alarmaMailAgrupat',
        NUM_ELEMENTS_PAGINA: 'numElementsPagina',
        IDIOMA: 'idioma',
    },
}));

describe('UserProfileFormDialogButton', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('UserProfileFormDialogButton_quanEsPrem_invocaElCallback', () => {
        // Comprova que l'opció de menú del perfil executa l'acció rebuda.
        const onClick = vi.fn();

        render(<UserProfileFormDialogButton onClick={onClick} />);

        fireEvent.click(screen.getByText('Perfil'));

        expect(onClick).toHaveBeenCalledTimes(1);
    });
});

describe('TemaObscurSelector', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('TemaObscurSelector_quanEsSeleccionaUnaOpcio_actualitzaElCampDelFormulari', () => {
        // Verifica que el selector de tema escriu el nou valor al context del formulari.
        mocks.useFormContextMock.mockReturnValue({
            data: { temaObscur: false },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<TemaObscurSelector />);

        fireEvent.click(screen.getByText('Obscur'));

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('temaObscur', true);
    });
});

describe('UserProfileFormDialog', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('UserProfileFormDialog_quanEsRenderitza_configuraElDialegIElRefreshEnGuardar', () => {
        // Comprova que el diàleg de perfil es configura amb el títol i el callback de refresc esperats.
        mocks.useFormContextMock.mockReturnValue({
            data: { alarmaMail: true, temaObscur: '' },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });
        const dialogApiRef = { current: undefined };

        render(<UserProfileFormDialog dialogApiRef={dialogApiRef} />);

        expect(screen.getByTestId('mui-form-dialog')).toBeInTheDocument();
        expect(mocks.muiFormDialogProps.resourceName).toBe('usuari');
        expect(mocks.muiFormDialogProps.title).toBe('Perfil');

        act(() => {
            mocks.muiFormDialogProps.formComponentProps.onSaveSuccess();
        });

        expect(mocks.refreshMock).toHaveBeenCalledTimes(1);
    });
});
