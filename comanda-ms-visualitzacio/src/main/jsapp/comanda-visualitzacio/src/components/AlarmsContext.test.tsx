import { describe, expect, it } from 'vitest';
import { render } from '@testing-library/react';
import { AlarmsContext, useAlarmsContext } from './AlarmsContext';

describe('AlarmsContext', () => {
    it('useAlarmsContext_lanzaError_siSeUsaFueraDelProvider', () => {
        const TestComponent = () => {
            useAlarmsContext();
            return null;
        };
        expect(() => render(<TestComponent />)).toThrow('AlarmsContext Provider not found');
    });

    it('useAlarmsContext_devuelveElContexto_cuandoEstaDentroDelProvider', () => {
        const mockAlarms = [{ id: 1, entornAppId: 1 }];
        const TestConsumer = () => {
            const context = useAlarmsContext();
            return <span data-testid="alarms-count">{context.alarms?.length}</span>;
        };
        const { getByTestId } = render(
            <AlarmsContext.Provider value={{ alarms: mockAlarms }}>
                <TestConsumer />
            </AlarmsContext.Provider>
        );
        expect(getByTestId('alarms-count')).toHaveTextContent('1');
    });
});
