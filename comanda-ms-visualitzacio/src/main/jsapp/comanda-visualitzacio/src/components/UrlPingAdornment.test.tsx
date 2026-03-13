import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import UrlPingAdornment from './UrlPingAdornment';

describe('UrlPingAdornment', () => {
    it('UrlPingAdornment_quanNoHiHaUrl_deshabilitaElBotoDeComprovacio', () => {
        // Comprova que el component no permet fer el ping quan no s'ha informat cap URL.
        render(<UrlPingAdornment url="" formData={{}} onClick={vi.fn()} />);

        expect(screen.getByRole('button')).toBeDisabled();
    });

    it('UrlPingAdornment_quanElPingTornaCert_mostraLaIconaDExit', async () => {
        // Verifica que el component mostra l'estat correcte després d'un ping satisfactori.
        const onClick = vi.fn().mockResolvedValue(true);

        render(<UrlPingAdornment url="https://api.test" formData={{ id: 7 }} onClick={onClick} />);

        fireEvent.click(screen.getByRole('button'));

        await waitFor(() =>
            expect(onClick).toHaveBeenCalledWith({
                endpoint: 'https://api.test',
                formData: { id: 7 },
            })
        );
        expect(await screen.findByText('check_circle')).toBeInTheDocument();
        expect(screen.queryByText('cancel')).not.toBeInTheDocument();
    });

    it('UrlPingAdornment_quanElPingFalla_mostraLaIconaDError', async () => {
        // Comprova que el component reflecteix al DOM un resultat d'error quan la comprovació falla.
        const onClick = vi.fn().mockRejectedValue(new Error('error'));

        render(<UrlPingAdornment url="https://api.test" formData={{}} onClick={onClick} />);

        fireEvent.click(screen.getByRole('button'));

        expect(await screen.findByText('cancel')).toBeInTheDocument();
        expect(screen.queryByText('check_circle')).not.toBeInTheDocument();
    });

    it('UrlPingAdornment_quanCanviaLaUrl_reiniciaLEstatVisual', async () => {
        // Verifica que un canvi d'URL elimina l'estat anterior per evitar mostrar un resultat obsolet.
        const onClick = vi.fn().mockResolvedValue(true);
        const { rerender } = render(
            <UrlPingAdornment url="https://api.test" formData={{}} onClick={onClick} />
        );

        fireEvent.click(screen.getByRole('button'));
        expect(await screen.findByText('check_circle')).toBeInTheDocument();

        rerender(<UrlPingAdornment url="https://api2.test" formData={{}} onClick={onClick} />);

        await waitFor(() => {
            expect(screen.queryByText('check_circle')).not.toBeInTheDocument();
            expect(screen.queryByText('cancel')).not.toBeInTheDocument();
        });
    });
});
