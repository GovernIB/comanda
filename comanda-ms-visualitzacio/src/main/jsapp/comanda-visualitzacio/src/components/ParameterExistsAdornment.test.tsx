import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import ParameterExistsAdornment from './ParameterExistsAdornment';

// Mock de @mui/material para que los <Icon> se rendericen con su contenido de texto
vi.mock('@mui/material', async () => {
    const actual = await vi.importActual('@mui/material');
    return {
        ...actual,
        Icon: ({ children, fontSize, color }: any) => (
            <span data-testid={`icon-${color || 'default'}`} data-fontsize={fontSize}>
                {children}
            </span>
        ),
        IconButton: ({ children, onClick, disabled, ...props }: any) => (
            <button onClick={onClick} disabled={disabled} {...props}>
                {children}
            </button>
        ),
        CircularProgress: ({ size }: any) => (
            <div data-testid="loading-spinner" data-size={size}>Loading...</div>
        ),
    };
});

describe('ParameterExistsAdornment', () => {
    it('ParameterExistsAdornment_quanNoHiHaValor_deshabilitaElBotoDeComprovacio', () => {
        // Comprova que el component no permet verificar quan no s'ha informat cap valor.
        const onClick = vi.fn();
        render(<ParameterExistsAdornment value={undefined} onClick={onClick} />);

        const verifyButton = screen.getByRole('button');
        expect(verifyButton).toBeDisabled();
    });

    it('ParameterExistsAdornment_quanEstaDeshabilitat_noPermetLaComprovacio', () => {
        // Verifica que la prop disabled externa també deshabilita el botó encara que hi hagi valor.
        const onClick = vi.fn();
        render(<ParameterExistsAdornment value="PARAM_123" onClick={onClick} disabled={true} />);

        const verifyButton = screen.getByRole('button');
        expect(verifyButton).toBeDisabled();
    });

    it('ParameterExistsAdornment_quanElParametreExisteix_mostraLaIconaDExit', async () => {
        // Comprova que es mostra l'estat d'èxit quan el backend confirma que el paràmetre existeix.
        const onClick = vi.fn().mockResolvedValue(true);

        render(<ParameterExistsAdornment value="PARAM_123" onClick={onClick} />);

        const verifyButton = screen.getByRole('button');
        fireEvent.click(verifyButton);

        await waitFor(() =>
            expect(onClick).toHaveBeenCalledWith('PARAM_123')
        );

        expect(await screen.findByText('check_circle')).toBeInTheDocument();
        expect(screen.queryByText('cancel')).not.toBeInTheDocument();
    });

    it('ParameterExistsAdornment_quanElParametreNoExisteix_mostraLaIconaDError', async () => {
        // Verifica que el component reflecteix al DOM un resultat negatiu quan el paràmetre no existeix.
        const onClick = vi.fn().mockResolvedValue(false);

        render(<ParameterExistsAdornment value="PARAM_INVALID" onClick={onClick} />);

        const verifyButton = screen.getByRole('button');
        fireEvent.click(verifyButton);

        expect(await screen.findByText('cancel')).toBeInTheDocument();
        expect(screen.queryByText('check_circle')).not.toBeInTheDocument();
    });

    it('ParameterExistsAdornment_quanLaComprovacioFalla_mostraLaIconaDError', async () => {
        // Comprova que un error de l'API es tracta com un resultat negatiu per no confondre l'usuari.
        const onClick = vi.fn().mockRejectedValue(new Error('Network error'));

        render(<ParameterExistsAdornment value="PARAM_123" onClick={onClick} />);

        const verifyButton = screen.getByRole('button');
        fireEvent.click(verifyButton);

        expect(await screen.findByText('cancel')).toBeInTheDocument();
        expect(screen.queryByText('check_circle')).not.toBeInTheDocument();
    });

    it('ParameterExistsAdornment_mostraUnSpinnerMentreCarrega', async () => {
        // Verifica que es mostra un indicador de càrrega mentre es resol la petició asíncrona.
        let resolvePromise: (value: boolean) => void;
        const onClick = vi.fn().mockImplementation(
            () => new Promise<boolean>((resolve) => { resolvePromise = resolve; })
        );

        render(<ParameterExistsAdornment value="PARAM_123" onClick={onClick} />);

        const verifyButton = screen.getByRole('button');
        fireEvent.click(verifyButton);

        expect(await screen.findByTestId('loading-spinner')).toBeInTheDocument();
        expect(verifyButton).toBeDisabled();

        resolvePromise!(true);

        await waitFor(() => {
            expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument();
        });
        expect(await screen.findByText('check_circle')).toBeInTheDocument();
    });

    it('ParameterExistsAdornment_quanCanviaElValor_reiniciaLEstatVisual', async () => {
        // Verifica que un canvi de valor elimina l'estat anterior per evitar mostrar un resultat obsolet.
        const onClick = vi.fn().mockResolvedValue(true);

        const { rerender } = render(
            <ParameterExistsAdornment value="PARAM_123" onClick={onClick} />
        );

        const verifyButton = screen.getByRole('button');
        fireEvent.click(verifyButton);

        expect(await screen.findByText('check_circle')).toBeInTheDocument();

        rerender(<ParameterExistsAdornment value="PARAM_456" onClick={onClick} />);

        await waitFor(() => {
            expect(screen.queryByText('check_circle')).not.toBeInTheDocument();
            expect(screen.queryByText('cancel')).not.toBeInTheDocument();
        });
    });

    it('ParameterExistsAdornment_mostraLaIconaFactCheckPerDefecte', () => {
        // Comprova que en estat idle es mostra la icona principal del botó (fact_check).
        const onClick = vi.fn();
        render(<ParameterExistsAdornment value="PARAM_123" onClick={onClick} />);

        expect(screen.getByText('fact_check')).toBeInTheDocument();
        expect(screen.queryByText('check_circle')).not.toBeInTheDocument();
        expect(screen.queryByText('cancel')).not.toBeInTheDocument();
    });
});