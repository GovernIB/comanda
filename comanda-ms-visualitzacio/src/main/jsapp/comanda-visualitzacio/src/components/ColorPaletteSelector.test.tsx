import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import ColorPaletteSelector from './ColorPaletteSelector';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: (input: {
        page: {
            widget: {
                editorPaleta: {
                    title: string;
                    color: string;
                    hex: string;
                    palet: string;
                    empty: string;
                    exist: string;
                };
                action: {
                    add: {
                        label: string;
                    };
                };
            };
        };
    }) => string) =>
        selector({
            page: {
                widget: {
                    editorPaleta: {
                        title: 'Paleta',
                        color: 'Color',
                        hex: 'Hex',
                        palet: 'Colors actuals',
                        empty: 'Sense colors',
                        exist: 'Aquest color ja existeix',
                    },
                    action: {
                        add: {
                            label: 'Afegir',
                        },
                    },
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

describe('ColorPaletteSelector', () => {
    beforeEach(() => {
        vi.restoreAllMocks();
    });

    it('ColorPaletteSelector_quanNoHiHaColorsInicials_mostraElMissatgeDePaletaBuida', () => {
        // Comprova que el component informa que no hi ha cap color si la paleta inicial està buida.
        render(
            <ColorPaletteSelector
                initialColors={undefined as unknown as string}
                onPaletteChange={vi.fn()}
            />
        );

        expect(screen.getByText('Sense colors')).toBeInTheDocument();
    });

    it('ColorPaletteSelector_quanSAfegeixUnColorNou_notificaElNouValorIRenderitzaElChip', async () => {
        // Verifica que afegir un color actualitza la paleta i mostra el nou valor al DOM.
        const onPaletteChange = vi.fn();
        render(<ColorPaletteSelector initialColors="#ffffff" onPaletteChange={onPaletteChange} />);

        fireEvent.change(screen.getByLabelText('Hex'), { target: { value: '#112233' } });
        fireEvent.click(screen.getByRole('button', { name: 'Afegir' }));

        expect(await screen.findByText('#112233'.toUpperCase())).toBeInTheDocument();
        expect(onPaletteChange).toHaveBeenCalledWith(['#ffffff', '#112233']);
    });

    it('ColorPaletteSelector_quanElColorJaExisteix_deshabilitaLAccioDAfegir', async () => {
        // Comprova que el component evita duplicats deshabilitant el botó quan el color ja existeix.
        render(<ColorPaletteSelector initialColors="#ffffff" onPaletteChange={vi.fn()} />);

        fireEvent.change(screen.getByLabelText('Hex'), { target: { value: '#ffffff' } });

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Afegir' })).toBeDisabled();
        });
    });
});
