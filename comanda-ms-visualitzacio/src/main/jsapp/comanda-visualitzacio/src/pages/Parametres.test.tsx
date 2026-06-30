import { render, screen, fireEvent } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import Parametres from './Parametres';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                parametres: {
                    title: 'Paràmetres',
                    find: 'Cercar paràmetres',
                    empty: 'Cap paràmetre trobat',
                    noGroup: "Selecciona un grup a l'esquerra",
                    save: {
                        success: 'Paràmetre desat correctament',
                        error: 'Error al desar el paràmetre',
                    },
                    detail: {
                        valuesTootip: { null: 'Valor nul', true: 'Valor cert', false: 'Valor fals' },
                    },
                },
            },
        })
    ),
    quickFilterChange: vi.fn(),
    groupChange: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
}));

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    useDebounce: (v: string) => v,
}));

vi.mock('./parametres/ParametresGrups', () => ({
    ParametresGrups: ({ onChange }: { onChange: (grup: string | null, subGrup: string | null) => void }) => (
        <div data-testid="parametres-grups">
            <button onClick={() => onChange('GRUP1', 'SUBGRUP1')}>Seleccionar grup</button>
        </div>
    ),
}));

vi.mock('./parametres/ParametresItems', () => ({
    ParametresItems: ({ grup, subGrup, quickFilter }: { grup: string | null; subGrup: string | null; quickFilter: string }) => (
        <div data-testid="parametres-items">
            <span data-testid="grup">{grup}</span>
            <span data-testid="subGrup">{subGrup}</span>
            <span data-testid="quickFilter">{quickFilter}</span>
        </div>
    ),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('Parametres', () => {
    it('Parametres_quanEsRenderitza_mostraElTitolIElsComponents', () => {
        render(<Parametres />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Paràmetres');
        expect(screen.getByTestId('parametres-grups')).toBeInTheDocument();
        expect(screen.getByTestId('parametres-items')).toBeInTheDocument();
    });

    it('Parametres_quanEsRenderitza_mostraElCampDeCerca', () => {
        render(<Parametres />);

        expect(screen.getByLabelText('Cercar paràmetres')).toBeInTheDocument();
    });

    it('Parametres_quanEsSeleccionaUnGrup_actualitzaElsPanellDItems', () => {
        render(<Parametres />);

        fireEvent.click(screen.getByRole('button', { name: 'Seleccionar grup' }));

        expect(screen.getByTestId('grup')).toHaveTextContent('GRUP1');
        expect(screen.getByTestId('subGrup')).toHaveTextContent('SUBGRUP1');
    });

    it('Parametres_quanSEscriuAlFiltre_propagaElValorAlsPanells', () => {
        render(<Parametres />);

        const input = screen.getByLabelText('Cercar paràmetres');
        fireEvent.change(input, { target: { value: 'mail' } });

        expect(screen.getByTestId('quickFilter')).toHaveTextContent('mail');
    });
});
