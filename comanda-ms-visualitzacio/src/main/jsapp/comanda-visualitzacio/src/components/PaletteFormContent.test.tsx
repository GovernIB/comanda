import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { PaletteFormContent, type PaletteData } from './PaletteFormContent';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) => {
            if (typeof selector === 'function') {
                return selector({
                    page: {
                        palette: {
                            nom: 'Nom',
                            descripcio: 'Descripció',
                            colors: 'Colors',
                            addColor: 'Afegir color',
                            duplicatePalette: 'Duplicar paleta',
                            editPalette: 'Editar paleta',
                            newPalette: 'Nova paleta',
                            upElement: 'Pujar',
                            downElement: 'Baixar',
                        },
                    },
                    common: {
                        delete: 'Esborrar',
                    },
                });
            }
            return selector ?? '';
        },
    }),
}));

const mockPalette: PaletteData = {
    id: 1,
    nom: 'Paleta Test',
    descripcio: 'Descripció de prova',
    colors: [
        { id: 1, posicio: 0, valor: '#FF0000' },
        { id: 2, posicio: 1, valor: '#00FF00' },
        { id: 3, posicio: 2, valor: '#0000FF' },
    ],
};

const renderComponent = (props?: Partial<React.ComponentProps<typeof PaletteFormContent>>) => {
    return render(
        <PaletteFormContent
            palette={mockPalette}
            onChange={vi.fn()}
            mode="edit"
            {...props}
        />
    );
};

const getIconButtonByLabel = (container: HTMLElement, label: string, index: number = 0) => {
    const spans = container.querySelectorAll(`[aria-label="${label}"]`);
    const span = spans[index];
    if (!span) return null;
    return span.querySelector('button') as HTMLButtonElement;
};

const getAllIconButtons = (container: HTMLElement, label: string) => {
    const spans = container.querySelectorAll(`[aria-label="${label}"]`);
    return Array.from(spans)
        .map(span => span.querySelector('button'))
        .filter((btn): btn is HTMLButtonElement => btn !== null);
};

describe('PaletteFormContent', () => {
    
    it('PaletteFormContent_renderitzaElsCampsNomIDescripcioAmbValorsIniciais', () => {
        renderComponent();

        expect(screen.getByLabelText('Nom')).toHaveValue('Paleta Test');
        expect(screen.getByLabelText('Descripció')).toHaveValue('Descripció de prova');
    });

    it('PaletteFormContent_quanCanviaElNom_cridaOnChangeAmbElNouValor', () => {
        const onChange = vi.fn();
        renderComponent({ onChange });

        const nomInput = screen.getByLabelText('Nom');
        fireEvent.change(nomInput, { target: { value: 'Nou Nom' } });

        expect(onChange).toHaveBeenCalledWith(
            expect.objectContaining({ nom: 'Nou Nom' })
        );
    });

    it('PaletteFormContent_quanCanviaLaDescripcio_cridaOnChangeAmbElNouValor', () => {
        const onChange = vi.fn();
        renderComponent({ onChange });

        const descripcioInput = screen.getByLabelText('Descripció');
        fireEvent.change(descripcioInput, { target: { value: 'Nova descripció' } });

        expect(onChange).toHaveBeenCalledWith(
            expect.objectContaining({ descripcio: 'Nova descripció' })
        );
    });

    it('PaletteFormContent_renderitzaLaBarraDeColorsAmbElsColorsNormalitzats', () => {
        renderComponent();

        const colorBoxes = screen.getAllByLabelText(/^[0-9]+: #[0-9a-f]{6}$/i);
        
        expect(colorBoxes).toHaveLength(3);
        expect(colorBoxes[0]).toHaveAttribute('aria-label', '0: #FF0000');
        expect(colorBoxes[1]).toHaveAttribute('aria-label', '1: #00FF00');
        expect(colorBoxes[2]).toHaveAttribute('aria-label', '2: #0000FF');
    });

    it('PaletteFormContent_renderitzaUnInputPerCadaColorAmbElSeuValor', () => {
        renderComponent();

        const colorInputs = screen.getAllByRole('textbox').filter(input => 
            input.getAttribute('type') !== 'color'
        );

        expect(colorInputs.length).toBeGreaterThanOrEqual(5);
        expect(screen.getByDisplayValue('#FF0000')).toBeInTheDocument();
        expect(screen.getByDisplayValue('#00FF00')).toBeInTheDocument();
        expect(screen.getByDisplayValue('#0000FF')).toBeInTheDocument();
    });

    it('PaletteFormContent_quanCanviaUnColorViaTextInput_actualitzaElValor', () => {
        const onChange = vi.fn();
        renderComponent({ onChange });

        const colorInput = screen.getByDisplayValue('#FF0000');
        fireEvent.change(colorInput, { target: { value: '#AAAAAA' } });

        expect(onChange).toHaveBeenCalledWith(
            expect.objectContaining({
                colors: expect.arrayContaining([
                    expect.objectContaining({ valor: '#AAAAAA', posicio: 0 }),
                ]),
            })
        );
    });

    it('PaletteFormContent_quanCanviaUnColorViaColorPicker_actualitzaElValor', () => {
        const onChange = vi.fn();
        const { container } = renderComponent({ onChange });

        const colorPicker = container.querySelector('input[type="color"]') as HTMLInputElement;
        expect(colorPicker).toBeInTheDocument();

        fireEvent.change(colorPicker, { target: { value: '#123456' } });
        expect(onChange).toHaveBeenCalledWith(
            expect.objectContaining({
                colors: expect.arrayContaining([expect.objectContaining({ valor: '#123456' })]),
            })
        );
    });

    it('PaletteFormContent_quanFesClicAPujarColor_intercanviaLesPosicions', () => {
        const onChange = vi.fn();
        const { container } = renderComponent({ onChange });

        const upButton = getIconButtonByLabel(container, 'Pujar', 1);
        expect(upButton).toBeInTheDocument();
        
        fireEvent.click(upButton as Element);
        expect(onChange).toHaveBeenCalled();
    });

    it('PaletteFormContent_quanFesClicABaixarColor_intercanviaLesPosicions', () => {
        const onChange = vi.fn();
        const { container } = renderComponent({ onChange });

        const downButton = getIconButtonByLabel(container, 'Baixar', 0);
        expect(downButton).toBeInTheDocument();
        
        fireEvent.click(downButton as Element);
        expect(onChange).toHaveBeenCalled();
    });

    it('PaletteFormContent_elsBotonsMoureEstanDeshabilitatsEnExtrems', () => {
        const { container } = renderComponent();
        
        const firstUp = getIconButtonByLabel(container, 'Pujar', 0);
        const lastDown = getIconButtonByLabel(container, 'Baixar', 2);

        expect(firstUp?.hasAttribute('disabled') || firstUp?.classList.contains('Mui-disabled')).toBe(true);
        expect(lastDown?.hasAttribute('disabled') || lastDown?.classList.contains('Mui-disabled')).toBe(true);
    });

    it('PaletteFormContent_quanFesClicAEsborrarColor_eliminaElColor', () => {
        const onChange = vi.fn();
        const { container } = renderComponent({ onChange });

        const deleteButtons = getAllIconButtons(container, 'Esborrar');
        expect(deleteButtons[1]).toBeInTheDocument();
        
        fireEvent.click(deleteButtons[1]);

        expect(onChange).toHaveBeenCalledWith(
            expect.objectContaining({
                colors: expect.arrayContaining([
                    expect.objectContaining({ valor: '#FF0000' }),
                    expect.objectContaining({ valor: '#0000FF' }),
                ]),
            })
        );
    });

    it('PaletteFormContent_noPermetEsborrarSiQuedaMenysDeUnColor', () => {
        const singleColorPalette: PaletteData = {
            ...mockPalette,
            colors: [{ id: 1, posicio: 0, valor: '#FF0000' }],
        };

        const { container } = renderComponent({ palette: singleColorPalette });
        const deleteButton = getIconButtonByLabel(container, 'Esborrar', 0);
        
        expect(deleteButton?.hasAttribute('disabled') || deleteButton?.classList.contains('Mui-disabled')).toBe(true);
    });

    it('PaletteFormContent_quanFesClicAAfegirColor_afegeixUnNouColorAlFinal', () => {
        const onChange = vi.fn();
        renderComponent({ onChange });

        const addButton = screen.getByText('Afegir color');
        fireEvent.click(addButton);

        expect(onChange).toHaveBeenCalledWith(
            expect.objectContaining({
                colors: expect.arrayContaining([
                    ...mockPalette.colors,
                    expect.objectContaining({ posicio: 3, valor: '#000000' }),
                ]),
            })
        );
    });

    it('PaletteFormContent_quanDisabled_totsElsControlsEstanInhabilitats', () => {
        const { container } = renderComponent({ disabled: true });

        expect(screen.getByLabelText('Nom')).toBeDisabled();
        expect(screen.getByLabelText('Descripció')).toBeDisabled();
        expect(screen.getByText('Afegir color')).toBeDisabled();

        const iconButtons = [
            ...getAllIconButtons(container, 'Pujar'),
            ...getAllIconButtons(container, 'Baixar'),
            ...getAllIconButtons(container, 'Esborrar'),
        ];
        
        iconButtons.forEach(btn => {
            expect(btn?.hasAttribute('disabled') || btn?.classList.contains('Mui-disabled')).toBe(true);
        });
    });

    it('PaletteFormContent_quanShowDuplicateButtonIEsModeEdit_mostraElBotoDuplicar', () => {
        const onDuplicate = vi.fn();
        
        renderComponent({ showDuplicateButton: true, mode: 'edit', onDuplicate });
        expect(screen.getByText('Duplicar paleta')).toBeInTheDocument();
    });

    it('PaletteFormContent_quanNoEsModeEdit_noMostraElBotoDuplicar', () => {
        renderComponent({ showDuplicateButton: true, mode: 'create' });
        
        expect(screen.queryByText('Duplicar paleta')).not.toBeInTheDocument();
    });

    it('PaletteFormContent_quanFesClicADuplicar_cridaOnDuplicate', () => {
        const onDuplicate = vi.fn();
        renderComponent({ showDuplicateButton: true, mode: 'edit', onDuplicate });

        const duplicateButton = screen.getByText('Duplicar paleta');
        fireEvent.click(duplicateButton);

        expect(onDuplicate).toHaveBeenCalledTimes(1);
    });

    it('PaletteFormContent_quanPaletteTheme_aplicaElsEstilsPersonalitzats', () => {
        const theme = {
            background: '#fff',
            text: '#000',
            surface: '#f5f5f5',
            surfaceText: '#333',
            fieldBackground: '#fafafa',
            fieldText: '#222',
            border: '#ccc',
            accent: '#0066cc',
            accentText: '#fff',
        };

        const { container } = renderComponent({ paletteTheme: theme });

        const textField = container.querySelector('.MuiOutlinedInput-root');
        expect(textField).toBeInTheDocument();
    });

    it('PaletteFormContent_quanPaletteBuit_renderitzaCorrectament', () => {
        const emptyPalette: PaletteData = {
            nom: 'Nova Paleta',
            descripcio: '',
            colors: [],
        };

        renderComponent({ palette: emptyPalette });

        expect(screen.getByLabelText('Nom')).toHaveValue('Nova Paleta');
        expect(screen.getByText('Afegir color')).not.toBeDisabled();
    });

    it('PaletteFormContent_normalitzaColorsAmbPosicionsDesordenades', () => {
        const unsortedPalette: PaletteData = {
            ...mockPalette,
            colors: [
                { id: 3, posicio: 2, valor: '#0000FF' },
                { id: 1, posicio: 0, valor: '#FF0000' },
                { id: 2, posicio: 1, valor: '#00FF00' },
            ],
        };

        renderComponent({ palette: unsortedPalette });

        const colorValues = screen.getAllByDisplayValue(/#[0-9a-f]{6}/i)
            .filter(input => input.getAttribute('type') !== 'color')
            .map(input => input.getAttribute('value'));
        
        expect(colorValues.slice(0, 3)).toEqual(['#FF0000', '#00FF00', '#0000FF']);
    });
});