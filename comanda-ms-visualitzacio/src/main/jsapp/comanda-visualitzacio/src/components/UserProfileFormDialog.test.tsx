import { fireEvent, render, screen } from '@testing-library/react';
import { act } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import {
    EstilMenuSelector,
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
                            estilMenu: {
                                tema: 'Mateix que el tema',
                                temaInvertit: 'Tema invertit',
                                peu: 'Colors del peu',
                            },
                            form: {
                                userData: 'Dades usuari',
                                genericConfig: 'Configuració genèrica',
                                applicationTheme: "Tema de l'aplicació",
                                menuTheme: 'Tema del menú',
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
    MenuEstil: {
        TEMA: 'TEMA',
        TEMA_INVERTIT: 'TEMA_INVERTIT',
        PEU: 'PEU',
    },
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
        ESTIL_MENU: 'estilMenu',
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

describe('EstilMenuSelector', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstilMenuSelector_quanEsSeleccionaUnaOpcio_actualitzaElCampDelFormulari', () => {
        mocks.useFormContextMock.mockReturnValue({
            data: { estilMenu: 'TEMA' },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstilMenuSelector />);

        fireEvent.click(screen.getByText('Tema invertit'));

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('estilMenu', 'TEMA_INVERTIT');
    });
});

describe('UserProfileFormDialog', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('UserProfileFormDialog_quanEsRenderitza_totsElsCampsSonRenderitzats', () => {
        // Verifica que tots els camps esperats es mostren en el formulari del perfil.
        mocks.useFormContextMock.mockReturnValue({
            data: { alarmaMail: true, temaObscur: '', estilMenu: 'TEMA' },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });
        const dialogApiRef = { current: undefined };

        render(<UserProfileFormDialog dialogApiRef={dialogApiRef} />);

        // Camps del model UsuariModel
        expect(screen.getByText('codi')).toBeInTheDocument();
        expect(screen.getByText('nom')).toBeInTheDocument();
        expect(screen.getByText('email')).toBeInTheDocument();
        expect(screen.getByText('rols')).toBeInTheDocument();
        expect(screen.getByText('emailAlternatiu')).toBeInTheDocument();
        expect(screen.getByText('alarmaMail')).toBeInTheDocument();
        expect(screen.getByText('alarmaMailAgrupat')).toBeInTheDocument();
        expect(screen.getByText('numElementsPagina')).toBeInTheDocument();
        expect(screen.getByText('idioma')).toBeInTheDocument();
        expect(screen.getByText('Mateix que el tema')).toBeInTheDocument();
        expect(screen.getByText('Tema invertit')).toBeInTheDocument();
        expect(screen.getByText('Colors del peu')).toBeInTheDocument();

        // TemaObscurSelector (que conté les tres opcions)
        expect(screen.getByText("Tema de l'aplicació")).toBeInTheDocument();
        expect(screen.getByText('Tema del menú')).toBeInTheDocument();
        expect(screen.getByText('Clar')).toBeInTheDocument();
        expect(screen.getByText('Obscur')).toBeInTheDocument();
        expect(screen.getByText('Sistema')).toBeInTheDocument();
    });

    it('UserProfileFormDialog_quanEsRenderitza_configuraElDialegIElRefreshEnGuardar', () => {
        // Comprova que el diàleg de perfil es configura amb el títol i el callback de refresc esperats.
        mocks.useFormContextMock.mockReturnValue({
            data: { alarmaMail: true, temaObscur: '', estilMenu: 'TEMA' },
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
