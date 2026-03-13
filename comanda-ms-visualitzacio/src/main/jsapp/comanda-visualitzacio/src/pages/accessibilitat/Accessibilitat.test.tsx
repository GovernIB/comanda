import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Accessibilitat from './Accessibilitat';
import AccessibilitatCa from './AccessibilitatCa';
import AccessibilitatEs from './AccessibilitatEs';

const mocks = vi.hoisted(() => ({
    useBaseAppContextMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                accessibilitat: {
                    title: 'Accessibilitat',
                },
            },
        })
    ),
}));

vi.mock('reactlib', () => ({
    useBaseAppContext: () => mocks.useBaseAppContextMock(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('../../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('Accessibilitat', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('Accessibilitat_quanLidiomaEsCatala_renderitzaLaVersioCatalanaAmbLaUrlBase', () => {
        // Comprova que la pàgina selecciona la declaració en català i calcula correctament la URL base.
        mocks.useBaseAppContextMock.mockReturnValue({ currentLanguage: 'ca' });
        vi.stubGlobal('window', {
            ...window,
            location: {
                ...window.location,
                toString: () => 'https://comanda.test/accessibilitat',
            },
        });

        render(<Accessibilitat />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Accessibilitat');
        expect(screen.getByRole('heading', { name: 'DECLARACIÓ D’ACCESSIBILITAT' })).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'https://comanda.test/' })).toHaveAttribute(
            'href',
            'https://comanda.test/'
        );
    });

    it('Accessibilitat_quanLidiomaEsCastella_renderitzaLaVersioEspanyola', () => {
        // Verifica que la pàgina renderitza el contingut en castellà quan l'idioma actual és `es`.
        mocks.useBaseAppContextMock.mockReturnValue({ currentLanguage: 'es' });
        vi.stubGlobal('window', {
            ...window,
            location: {
                ...window.location,
                toString: () => 'https://comanda.test/accessibilitat/',
            },
        });

        render(<Accessibilitat />);

        expect(screen.getByRole('heading', { name: 'DECLARACIÓN DE ACCESIBILIDAD' })).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'https://comanda.test/' })).toHaveAttribute(
            'href',
            'https://comanda.test/'
        );
    });
});

describe('Accessibilitat llengües', () => {
    it('AccessibilitatCa_quanRepAppUrl_mostraLenllacAlLlocWeb', () => {
        // Comprova que la versió catalana inclou l'enllaç a l'aplicació informada.
        render(<AccessibilitatCa appUrl="https://comanda.test/" />);

        expect(screen.getByRole('link', { name: 'https://comanda.test/' })).toHaveAttribute(
            'href',
            'https://comanda.test/'
        );
    });

    it('AccessibilitatEs_quanEsRenderitza_mostraElContingutPrincipalEnCastella', () => {
        // Verifica que la versió castellana exposa el títol i l'estat de compliment.
        render(<AccessibilitatEs appUrl="https://comanda.test/" />);

        expect(screen.getByRole('heading', { name: 'DECLARACIÓN DE ACCESIBILIDAD' })).toBeInTheDocument();
        expect(screen.getAllByText(/parcialmente conforme/i).length).toBeGreaterThan(0);
    });
});
