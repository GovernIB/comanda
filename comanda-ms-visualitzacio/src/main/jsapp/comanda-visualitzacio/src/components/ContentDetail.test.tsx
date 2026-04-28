import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import ContentDetail from './ContentDetail';

describe('ContentDetail', () => {
    it('ContentDetail_quanRepTitolIElements_mostraLaInformacioPrincipal', () => {
        // Comprova que el component mostra el títol i els valors principals informats.
        render(
            <ContentDetail
                title="Detall"
                elements={[
                    { label: 'Nom', value: 'Aplicació' },
                    { label: 'Versió', value: '1.0.0' },
                ]}
            />
        );

        expect(screen.getByText('Detall')).toBeInTheDocument();
        expect(screen.getByText('Nom')).toBeInTheDocument();
        expect(screen.getByText('Aplicació')).toBeInTheDocument();
        expect(screen.getByText('Versió')).toBeInTheDocument();
    });

    it('ContentDetail_quanUnElementTeSubelements_mostraLaJerarquiaFiltrantValorsBuids', () => {
        // Verifica que el component mostra els subelements útils i ignora els que no tenen contingut.
        render(
            <ContentDetail
                elements={[
                    {
                        label: 'Principal',
                        value: 'Valor principal',
                        subElements: [
                            { label: 'Fill visible', value: 'Contingut fill', boldLabel: true },
                            { label: 'Fill buit', value: '' },
                        ],
                    },
                ]}
            />
        );

        expect(screen.getByText('Fill visible')).toBeInTheDocument();
        expect(screen.getByText('Contingut fill')).toBeInTheDocument();
        expect(screen.queryByText('Fill buit')).not.toBeInTheDocument();
    });

    it('ContentDetail_quanUnElementTeContentValue_renderitzaElNodePersonalitzat', () => {
        // Comprova que el component respecta el contingut React passat directament en lloc del valor de text.
        render(
            <ContentDetail
                elements={[
                    {
                        label: 'Bloc',
                        contentValue: <span>Contingut personalitzat</span>,
                    },
                ]}
            />
        );

        expect(screen.getByText('Contingut personalitzat')).toBeInTheDocument();
    });
});
