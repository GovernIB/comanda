import { render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { HelmetProvider } from '@dr.pogodin/react-helmet';
import PageTitle from './PageTitle';

describe('PageTitle', () => {
    it('PageTitle_quanRepUnTitol_actualitzaElDocumentTitle', async () => {
        // Comprova que el component sincronitza el títol del document amb el text de la pàgina.
        render(
            <HelmetProvider>
                <PageTitle title="Dashboard" />
            </HelmetProvider>
        );

        expect(document.title).toBe('Comanda - Dashboard');
    });
});
