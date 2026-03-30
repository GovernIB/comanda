import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import CenteredCircularProgress from './CenteredCircularProgress';

describe('CenteredCircularProgress', () => {
    it('CenteredCircularProgress_quanEsRenderitza_mostraElProgressbar', () => {
        // Comprova que el component centra i renderitza un indicador de càrrega.
        render(<CenteredCircularProgress />);

        expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
});
