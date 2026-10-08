import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { ParameterContext, useParameterContext, ParameterContextType } from './ParameterContext';

const TestComponent = () => {
    const context = useParameterContext();
    return <div data-testid="context-value">{context.maxResultSelects}</div>;
};

describe('ParameterContext', () => {
    it('debería devolver el valor del contexto cuando se usa dentro del Provider', () => {
        const mockValue: ParameterContextType = { maxResultSelects: 50 };

        render(
            <ParameterContext.Provider value={mockValue}>
                <TestComponent />
            </ParameterContext.Provider>
        );

        expect(screen.getByTestId('context-value').textContent).toBe('50');
    });

    it('debería lanzar un error si se usa useParameterContext fuera del Provider', () => {
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

        expect(() => {
            render(<TestComponent />);
        }).toThrow('ParameterContext Provider not found');

        consoleErrorSpy.mockRestore();
    });
});
