import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import IOSSwitch from './IOSSwitch.tsx';

describe('IOSSwitch', () => {
    it('IOSSwitch_quanChecked_mostraElSwitchMarcat', () => {
        render(<IOSSwitch checked onChange={vi.fn()} />);

        expect(screen.getByRole('switch')).toBeChecked();
    });

    it('IOSSwitch_quanNoChecked_mostraElSwitchDesmarcat', () => {
        render(<IOSSwitch checked={false} onChange={vi.fn()} />);

        expect(screen.getByRole('switch')).not.toBeChecked();
    });

    it('IOSSwitch_enFerClic_notificaOnChangeAmbElNouValor', () => {
        const onChange = vi.fn();
        render(<IOSSwitch checked={false} onChange={onChange} />);

        fireEvent.click(screen.getByRole('switch'));

        expect(onChange).toHaveBeenCalledWith(expect.anything(), true);
    });
});
