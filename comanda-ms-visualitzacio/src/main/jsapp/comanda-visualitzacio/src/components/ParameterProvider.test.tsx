import { render, waitFor, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach, MockedFunction } from 'vitest';
import ParameterProvider from './ParameterProvider';
import { useResourceApiService } from 'reactlib';
import { useParameterContext } from './ParameterContext';

vi.mock('reactlib', () => ({
    useResourceApiService: vi.fn(),
    springFilterBuilder: {
        eq: vi.fn((field: string, value: string) => `${field}=${value}`),
    },
}));

const TestConsumer = () => {
    const { maxResultSelects } = useParameterContext();
    return <div data-testid="max-result">{maxResultSelects}</div>;
};

describe('ParameterProvider', () => {
    const mockFind = vi.fn();
    const mockUseResourceApiService = useResourceApiService as MockedFunction<typeof useResourceApiService>;

    beforeEach(() => {
        vi.clearAllMocks();
        mockUseResourceApiService.mockReturnValue({
            isReady: true,
            find: mockFind,
        } as any);
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('debería renderizar los children correctamente', () => {
        mockFind.mockResolvedValue({ rows: [] });

        render(
            <ParameterProvider>
                <div data-testid="child">Hello World</div>
            </ParameterProvider>
        );

        expect(screen.getByTestId('child')).toHaveTextContent('Hello World');
    });

    it('debería obtener y parsear correctamente el valor numérico del parámetro', async () => {
        mockFind.mockResolvedValue({
            rows: [{ id: 1, codi: 'es.caib.comanda.monitor.general.max.result.selects', valor: '150' }],
        });

        render(
            <ParameterProvider>
                <TestConsumer />
            </ParameterProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId('max-result')).toHaveTextContent('150');
        });

        expect(mockFind).toHaveBeenCalledWith({
            filter: "codi='es.caib.comanda.monitor.general.max.result.selects'",
            unpaged: true,
            size: 1,
        });
    });

    it('debería establecer maxResultSelects en null si el valor es null o undefined', async () => {
        mockFind.mockResolvedValue({
            rows: [{ id: 1, codi: 'es.caib.comanda.monitor.general.max.result.selects', valor: null }],
        });

        render(
            <ParameterProvider>
                <TestConsumer />
            </ParameterProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId('max-result')).toHaveTextContent('');
        });
    });

    it('debería establecer maxResultSelects en null si no se encuentran filas', async () => {
        mockFind.mockResolvedValue({ rows: [] });

        render(
            <ParameterProvider>
                <TestConsumer />
            </ParameterProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId('max-result')).toHaveTextContent('');
        });
    });

    it('debería manejar errores de la API gracefully y establecer null', async () => {
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
        mockFind.mockRejectedValue(new Error('Network error'));

        render(
            <ParameterProvider>
                <TestConsumer />
            </ParameterProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId('max-result')).toHaveTextContent('');
        });

        expect(consoleErrorSpy).toHaveBeenCalledWith(
            'Error al cargar el parámetro maxResultSelects:',
            expect.any(Error)
        );

        consoleErrorSpy.mockRestore();
    });

    it('NO debería hacer la petición find si isReady es false', async () => {
        mockUseResourceApiService.mockReturnValue({
            isReady: false,
            find: mockFind,
        } as any);

        render(
            <ParameterProvider>
                <TestConsumer />
            </ParameterProvider>
        );

        await new Promise(resolve => setTimeout(resolve, 100));

        expect(mockFind).not.toHaveBeenCalled();
    });
});
