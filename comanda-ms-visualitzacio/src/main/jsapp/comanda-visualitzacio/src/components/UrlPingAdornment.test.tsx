import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import UrlPingAdornment from './UrlPingAdornment';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) => {
            if (typeof selector === 'function') {
                return selector({
                    page: { apps: { ping: { validationTrace: 'Traça', validationError: 'Error' } } },
                });
            }
            return selector ?? '';
        },
    }),
}));

vi.mock('./ContentDetail.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="content-detail">{title}</div>,
}));

describe('UrlPingAdornment', () => {
    it('UrlPingAdornment_quanNoHiHaUrl_deshabilitaElBotoDeComprovacio', () => {
        // Comprova que el component no permet fer el ping quan no s'ha informat cap URL.
        render(<UrlPingAdornment url="" formData={{}} onClick={vi.fn()} dialogShow={vi.fn()} />);

        const pingButton = screen.getByRole('button');

        expect(pingButton).toBeDisabled();
    });

    it('UrlPingAdornment_quanElPingTornaCert_mostraLaIconaDExit', async () => {
        // Verifica que el component mostra l'estat correcte després d'un ping satisfactori.
        const onClick = vi.fn().mockResolvedValue({ status: 'success', elements: [] });
        const dialogShow = vi.fn();

        render(
            <UrlPingAdornment url="https://api.test" formData={{ id: 7 }} onClick={onClick} dialogShow={dialogShow} />
        );

        const pingButton = screen.getAllByRole('button')[0];
        fireEvent.click(pingButton);

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
        const dialogShow = vi.fn();

        render(
            <UrlPingAdornment url="https://api.test" formData={{}} onClick={onClick} dialogShow={dialogShow} />
        );

        const pingButton = screen.getAllByRole('button')[0];
        fireEvent.click(pingButton);

        expect(await screen.findByText('cancel')).toBeInTheDocument();
        expect(screen.queryByText('check_circle')).not.toBeInTheDocument();
    });

    it('UrlPingAdornment_quanCanviaLaUrl_reiniciaLEstatVisual', async () => {
        // Verifica que un canvi d'URL elimina l'estat anterior per evitar mostrar un resultat obsolet.
        const onClick = vi.fn().mockResolvedValue({ status: 'success', elements: [] });
        const dialogShow = vi.fn();

        const { rerender } = render(
            <UrlPingAdornment url="https://api.test" formData={{}} onClick={onClick} dialogShow={dialogShow} />
        );

        const pingButton = screen.getAllByRole('button')[0];
        fireEvent.click(pingButton);

        expect(await screen.findByText('check_circle')).toBeInTheDocument();

        rerender(
            <UrlPingAdornment url="https://api2.test" formData={{}} onClick={onClick} dialogShow={dialogShow} />
        );

        await waitFor(() => {
            expect(screen.queryByText('check_circle')).not.toBeInTheDocument();
            expect(screen.queryByText('cancel')).not.toBeInTheDocument();
        });
    });

    it('UrlPingAdornment_quanElPingTornaWarning_mostraIconaIObreModalEnClic', async () => {
        // Comprova que en estat warning es mostra la icona i en fer clic s'obre el diàleg amb els detalls.
        const onClick = vi.fn().mockResolvedValue({
            status: 'warning',
            elements: [
                { field: 'url', message: 'URL no accessible' },
                { field: 'timeout', message: 'Temps d\'espera excedit' }
            ]
        });
        const dialogShow = vi.fn();

        render(
            <UrlPingAdornment
                url="https://api.test"
                formData={{ id: 7 }}
                onClick={onClick}
                dialogShow={dialogShow}
            />
        );

        const pingButton = screen.getAllByRole('button')[0];
        fireEvent.click(pingButton);

        const warningIcon = await screen.findByText('warning');
        expect(warningIcon).toBeInTheDocument();

        const warningButton = warningIcon.closest('button') as HTMLButtonElement;
        expect(warningButton).toBeInTheDocument();

        fireEvent.click(warningButton);

        expect(dialogShow).toHaveBeenCalledTimes(1);
        expect(dialogShow).toHaveBeenCalledWith(
            expect.stringContaining('Error'),
            expect.any(Object),
            undefined,
            expect.objectContaining({
                maxWidth: 'lg',
                fullWidth: true
            })
        );
    });
});