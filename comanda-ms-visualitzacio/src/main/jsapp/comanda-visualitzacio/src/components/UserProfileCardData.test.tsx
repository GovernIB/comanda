import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { CardPage, ContenidoData, UserProfileCardData } from './UserProfileCardData';

describe('UserProfileCardData', () => {
    it('UserProfileCardData_quanEsRenderitza_mostraCapcaleraContingutIBotons', () => {
        // Comprova que la targeta mostra títol, capçalera, contingut i botons configurats.
        const onClick = vi.fn();

        render(
            <UserProfileCardData
                icon={<span>Icona</span>}
                title="Perfil"
                header={<span>Capçalera</span>}
                buttons={[
                    {
                        text: 'Editar',
                        icon: 'edit',
                        onClick,
                        flex: 12,
                        buttonProps: {},
                        hidden: false,
                    },
                ]}
            >
                <div>Cos</div>
            </UserProfileCardData>
        );

        expect(screen.getByText('Perfil')).toBeInTheDocument();
        expect(screen.getByText('Capçalera')).toBeInTheDocument();
        expect(screen.getByText('Cos')).toBeInTheDocument();
        fireEvent.click(screen.getByTitle('Editar'));
        expect(onClick).toHaveBeenCalledTimes(1);
    });

    it('UserProfileCardData_quanEstaOcult_noRenderitzaCapContingut', () => {
        // Verifica que el component retorna buit quan es marca com a ocult.
        const { container } = render(
            <UserProfileCardData
                icon={<span>Icona</span>}
                title="Perfil"
                hidden={true}
            />
        );

        expect(container).toBeEmptyDOMElement();
    });
});

describe('CardPage', () => {
    it('CardPage_quanEsRenderitza_mostraTitolCapcaleraIContingut', () => {
        // Comprova que la targeta de pàgina composa correctament les tres zones principals.
        render(
            <CardPage title="Títol pàgina" header={<span>Header</span>}>
                <div>Contingut pàgina</div>
            </CardPage>
        );

        expect(screen.getByText('Títol pàgina')).toBeInTheDocument();
        expect(screen.getByText('Header')).toBeInTheDocument();
        expect(screen.getByText('Contingut pàgina')).toBeInTheDocument();
    });
});

describe('ContenidoData', () => {
    it('ContenidoData_quanEsVisible_mostraElTitolIElText', () => {
        // Verifica que el component mostra el títol i el contingut textual associat.
        render(
            <ContenidoData
                title="Camp"
                titleXs={4}
                textXs={8}
                xs={12}
                componentTitleProps={{}}
                componentTextProps={{}}
                hidden={false}
            >
                Valor
            </ContenidoData>
        );

        expect(screen.getByText('Camp')).toBeInTheDocument();
        expect(screen.getByText('Valor')).toBeInTheDocument();
    });

    it('ContenidoData_quanEstaOcult_noRenderitzaRes', () => {
        // Comprova que el component retorna buit quan es marca com a ocult.
        const { container } = render(
            <ContenidoData
                title="Camp"
                titleXs={4}
                textXs={8}
                xs={12}
                componentTitleProps={{}}
                componentTextProps={{}}
                hidden={true}
            >
                Valor
            </ContenidoData>
        );

        expect(container).toBeEmptyDOMElement();
    });
});
