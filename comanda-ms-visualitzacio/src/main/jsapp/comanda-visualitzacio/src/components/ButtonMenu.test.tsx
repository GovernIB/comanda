import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import ButtonMenu from './ButtonMenu';

describe('ButtonMenu', () => {
    it('ButtonMenu_quanEsPremElBoto_mostraElContingutDelMenu', () => {
        // Comprova que el menú s'obre i mostra els fills quan l'usuari prem el botó.
        render(
            <ButtonMenu title="Accions">
                <div>Opció 1</div>
            </ButtonMenu>
        );

        fireEvent.click(screen.getByRole('button', { name: 'Accions' }));

        expect(screen.getByText('Opció 1')).toBeInTheDocument();
    });

    it('ButtonMenu_quanEstaDeshabilitat_noObriElMenu', () => {
        // Verifica que un botó deshabilitat no permet obrir el menú.
        render(
            <ButtonMenu title="Accions" disabled>
                <div>Opció 1</div>
            </ButtonMenu>
        );

        expect(screen.getByRole('button', { name: 'Accions' })).toBeDisabled();
        expect(screen.queryByText('Opció 1')).not.toBeInTheDocument();
    });
});
