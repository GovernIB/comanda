import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Integracions from './Integracions';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                apps: {
                    logoAlt: 'Logotip',
                },
                integracions: {
                    title: 'Integracions',
                    noLogo: 'Sense logotip',
                },
            },
        })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
    MuiDataGrid: ({
        title,
        columns,
        popupEditFormContent,
    }: {
        title: string;
        columns: Array<{ field: string; renderCell?: (params: { value: unknown }) => React.ReactNode }>;
        popupEditFormContent?: React.ReactNode;
    }) => (
        <section>
            <div>{title}</div>
            <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
            <div data-testid="logo-cell">{columns[0]?.renderCell?.({ value: null })}</div>
            <div>{popupEditFormContent}</div>
        </section>
    ),
}));

vi.mock('../components/LogoUpload.tsx', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`logo-${name}`}>{name}</div>,
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../hooks/useReadOnlyGestor.ts', () => ({
  default: () => false,
}));

describe('Integracions', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('Integracions_quanEsRenderitza_mostraElTitolDinsElScreenFrame', () => {
        // El títol de la graella principal és un ScreenTitle dins el ScreenFrame de la pàgina.
        const { container } = render(<Integracions />);

        const frame = container.querySelector('.screen-frame');
        expect(frame).not.toBeNull();
        const screenTitle = (frame as HTMLElement).querySelector('.screen-title');
        expect(screenTitle).not.toBeNull();
        expect(screenTitle).toHaveTextContent('Integracions');
    });

    it('Integracions_quanEsRenderitza_mostraLaGraellaIElFormulariDeNomesLectura', () => {
        // Comprova que la pàgina renderitza les columnes esperades i el contingut bàsic del formulari d'edició.
        render(<Integracions />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Integracions');
        expect(screen.getByRole('heading', { name: 'Integracions' })).toBeInTheDocument();
        expect(screen.getByTestId('columns')).toHaveTextContent('logo,codi,nom');
        expect(screen.getByTestId('field-codi')).toBeInTheDocument();
        expect(screen.getByTestId('field-nom')).toBeInTheDocument();
        expect(screen.getByTestId('logo-logo')).toBeInTheDocument();
        expect(screen.getByLabelText('block')).toBeInTheDocument();
    });
});
