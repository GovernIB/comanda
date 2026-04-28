import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import SalutChip from './SalutChip';

describe('SalutChip', () => {
    it('SalutChip_quanNoHiHaTooltip_renderitzaNomesElChipAmbLEtiqueta', () => {
        // Comprova que el component mostra l'etiqueta directament quan no hi ha tooltip associat.
        render(<SalutChip label="Operatiu" backgroundColor="#008000" />);

        expect(screen.getByText('Operatiu')).toBeInTheDocument();
    });

    it('SalutChip_quanHiHaTooltip_elMostraEnPassarPerDamunt', async () => {
        // Verifica que el component encapsula el xip amb un tooltip quan rep text d'ajuda.
        render(<SalutChip label="Operatiu" tooltip="Tot correcte" backgroundColor="#008000" />);

        fireEvent.mouseOver(screen.getByText('Operatiu'));

        expect(await screen.findByRole('tooltip')).toHaveTextContent('Tot correcte');
    });
});
