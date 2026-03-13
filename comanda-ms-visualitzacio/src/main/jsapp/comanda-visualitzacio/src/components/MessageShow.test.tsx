import { render, screen } from '@testing-library/react';
import { act, renderHook } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import Message, { useMessage } from './MessageShow';

describe('Message', () => {
    it('Message_quanRepTitolIMissatge_elsMostraDinsLalerta', () => {
        // Comprova que el component renderitza el títol i el missatge passats per props.
        render(
            <Message
                open={true}
                setOpen={() => undefined}
                title="Atenció"
                message="Missatge informatiu"
                severity="warning"
            />
        );

        expect(screen.getByText('Atenció')).toBeInTheDocument();
        expect(screen.getByText('Missatge informatiu')).toBeInTheDocument();
        expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    it('Message_quanRepComponentsAddicionals_elsRenderitzaDinsLSnackbar', () => {
        // Verifica que el component inclou els elements addicionals dins el contingut del missatge.
        render(
            <Message
                open={true}
                setOpen={() => undefined}
                message="Missatge"
                additionalComponents={[<span key="extra">Extra</span>]}
            />
        );

        expect(screen.getByText('Extra')).toBeInTheDocument();
    });
});

describe('useMessage', () => {
    it('useMessage_quanEsCridaShow_obriElMissatgeAmbElContingutIndicats', () => {
        // Comprova que el hook actualitza l'estat i exposa un component visible després d'invocar show.
        const { result } = renderHook(() => useMessage());

        act(() => {
            result.current.show('Títol', 'Cos del missatge', 'success');
        });

        render(result.current.component);

        expect(screen.getByText('Títol')).toBeInTheDocument();
        expect(screen.getByText('Cos del missatge')).toBeInTheDocument();
    });

    it('useMessage_quanEsCridaShowTemporal_acceptaComponentsAddicionals', () => {
        // Verifica que la variant temporal conserva també els components addicionals.
        const { result } = renderHook(() => useMessage());

        act(() => {
            result.current.showTemporal('', 'Temporal', 'info', [<span key="extra">Adjunt</span>], 5000);
        });

        render(result.current.component);

        expect(screen.getByText('Temporal')).toBeInTheDocument();
        expect(screen.getByText('Adjunt')).toBeInTheDocument();
    });
});
