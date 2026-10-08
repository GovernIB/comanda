import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import AppFormFieldReference from './AppFormFieldReference';
import { useParameterContext } from './ParameterContext';
import { FormFieldCustomProps } from '../../lib/components/form/FormField';

type TestFormFieldProps = FormFieldCustomProps & {
    optionsPageSize?: number;
    optionsUnpaged?: boolean;
    optionRenderer?: (args: { id: any; description: string }) => React.ReactNode;
};

const TestableAppFormFieldReference = AppFormFieldReference as React.FC<TestFormFieldProps>;

const defaultProps: TestFormFieldProps = {
    name: 'test-field',
    value: null,
    field: {
        name: 'test-field',
        dataSource: {
            href: '/api/test',
            valueField: 'id',
            labelField: 'description',
        },
    },
    onChange: vi.fn(),
    optionsPageSize: undefined,
    optionsUnpaged: false,
    optionRenderer: undefined,
};

vi.mock('./ParameterContext', () => ({
    useParameterContext: vi.fn(),
}));

vi.mock('../../lib/components/mui/form/FormFieldReference', () => ({
    FormFieldReference: vi.fn(({ optionsPageSize, optionsUnpaged, optionRenderer, ...rest }: any) => (
        <div
            data-testid="form-field-reference"
            data-page-size={optionsPageSize}
            data-unpaged={String(optionsUnpaged)}
        >
            {/* Simulamos que el componente interno renderiza la opción de paginación */}
            {optionRenderer && optionRenderer({ id: '___pageLabel', description: 'Mostrant 5 de 50' })}
            {/* Simulamos que también renderiza una opción normal */}
            {optionRenderer && optionRenderer({ id: '1', description: 'Opción Normal' })}
            <input data-testid="mock-input" {...rest} />
        </div>
    )),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (keyFn: any) => {
            if (typeof keyFn === 'function') {
                return keyFn({
                    components: {
                        appFormFieldReference: { mostrarTots: 'Mostrar tots' }
                    }
                });
            }
            return keyFn;
        },
    }),
}));

describe('AppFormFieldReference', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(useParameterContext).mockReturnValue({ maxResultSelects: null });
    });

    it('debería usar DEFAULT_PAGE_SIZE (20) si no hay valor en el contexto ni en las props', () => {
        render(<TestableAppFormFieldReference {...defaultProps} />);

        const fieldRef = screen.getByTestId('form-field-reference');
        expect(fieldRef).toHaveAttribute('data-page-size', '20');
        expect(fieldRef).toHaveAttribute('data-unpaged', 'false');
    });

    it('debería usar maxResultSelects del contexto si está disponible', () => {
        vi.mocked(useParameterContext).mockReturnValue({ maxResultSelects: 50 });

        render(<TestableAppFormFieldReference {...defaultProps} />);

        const fieldRef = screen.getByTestId('form-field-reference');
        expect(fieldRef).toHaveAttribute('data-page-size', '50');
    });

    it('debería priorizar optionsPageSize de las props sobre el contexto', () => {
        vi.mocked(useParameterContext).mockReturnValue({ maxResultSelects: 50 });

        render(<TestableAppFormFieldReference {...defaultProps} optionsPageSize={10} />);

        const fieldRef = screen.getByTestId('form-field-reference');
        expect(fieldRef).toHaveAttribute('data-page-size', '10');
    });

    it('debería renderizar el enlace "Mostrar tots" y al hacer clic, cambiar optionsUnpaged a true', async () => {
        render(<TestableAppFormFieldReference {...defaultProps} />);

        const fieldRef = screen.getByTestId('form-field-reference');
        expect(fieldRef).toHaveAttribute('data-unpaged', 'false');

        const showAllLink = screen.getByText('Mostrar tots');
        expect(showAllLink).toBeInTheDocument();

        fireEvent.click(showAllLink);

        await waitFor(() => {
            expect(fieldRef).toHaveAttribute('data-unpaged', 'true');
        });
    });

    it('debería respetar optionsUnpaged si ya viene en true desde las props originales', () => {
        render(<TestableAppFormFieldReference {...defaultProps} optionsUnpaged={true} />);

        const fieldRef = screen.getByTestId('form-field-reference');
        expect(fieldRef).toHaveAttribute('data-unpaged', 'true');
    });

    it('debería delegar en callerOptionRenderer para opciones que no son PAGE_LABEL_OPTION_ID', () => {
        const customRenderer = vi.fn(({ description }) => <span data-testid="custom-render">{description}</span>);

        render(<TestableAppFormFieldReference {...defaultProps} optionRenderer={customRenderer} />);

        expect(customRenderer).toHaveBeenCalledWith({ id: '1', description: 'Opción Normal' });
        expect(screen.getByTestId('custom-render')).toHaveTextContent('Opción Normal');

        const calls = customRenderer.mock.calls;
        const calledWithPageLabel = calls.some((call: any) => call[0].id === '___pageLabel');
        expect(calledWithPageLabel).toBe(false);
    });
});
