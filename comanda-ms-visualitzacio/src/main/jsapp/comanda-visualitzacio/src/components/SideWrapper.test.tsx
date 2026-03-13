import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import SideWrapper from './SideWrapper';

describe('SideWrapper', () => {
    it('SideWrapper_quanEsClicaFora_invocaElCallbackExterior', () => {
        // Comprova que el component detecta el clic a la capa exterior i el propaga al callback rebut.
        const onOutsideClick = vi.fn();
        const { container } = render(
            <SideWrapper onOutsideClick={onOutsideClick}>
                <div>Contingut</div>
            </SideWrapper>
        );

        fireEvent.click(container.firstChild as HTMLElement);

        expect(onOutsideClick).toHaveBeenCalledTimes(1);
    });

    it('SideWrapper_quanEsRenderitza_mostraElContingutIntern', () => {
        // Verifica que el component no impedeix el renderitzat del contingut que embolcalla.
        render(
            <SideWrapper onOutsideClick={() => undefined}>
                <div>Contingut lateral</div>
            </SideWrapper>
        );

        expect(screen.getByText('Contingut lateral')).toBeInTheDocument();
    });
});
