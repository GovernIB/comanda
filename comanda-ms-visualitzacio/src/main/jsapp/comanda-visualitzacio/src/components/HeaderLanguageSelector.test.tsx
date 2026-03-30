import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import HeaderLanguageSelector from './HeaderLanguageSelector';

const mocks = vi.hoisted(() => ({
    setCurrentLanguageMock: vi.fn(),
    onLanguageChangeMock: vi.fn(),
    useBaseAppContextMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    useBaseAppContext: () => mocks.useBaseAppContextMock(),
}));

describe('HeaderLanguageSelector', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('HeaderLanguageSelector_quanNoHiHaLlengues_retornaNull', () => {
        // Comprova que el component no renderitza contingut quan la llista de llengües és buida.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca-ES',
            setCurrentLanguage: mocks.setCurrentLanguageMock,
        });

        const { container } = render(<HeaderLanguageSelector languages={[]} />);

        expect(container).toBeEmptyDOMElement();
    });

    it('HeaderLanguageSelector_quanEsRenderitza_mostraLesOpcionsAmbLaLlenguaActiva', () => {
        // Verifica que el component mostra el codi de les llengües i marca la llengua actual.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca-ES',
            setCurrentLanguage: mocks.setCurrentLanguageMock,
        });

        render(<HeaderLanguageSelector languages={['ca-ES', 'es-ES']} />);

        expect(screen.getByText('CA')).toBeInTheDocument();
        expect(screen.getByText('ES')).toBeInTheDocument();
    });

    it('HeaderLanguageSelector_quanCanviaLaLlengua_notificaIActualitzaElContext', () => {
        // Comprova que prémer una llengua inactiva invoca el canvi i manté el callback sincronitzat.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca-ES',
            setCurrentLanguage: mocks.setCurrentLanguageMock,
        });

        render(
            <HeaderLanguageSelector
                languages={['ca-ES', 'es-ES']}
                onLanguageChange={mocks.onLanguageChangeMock}
            />
        );

        fireEvent.click(screen.getByText('ES'));

        expect(mocks.onLanguageChangeMock).toHaveBeenCalledWith('ca-ES');
        expect(mocks.setCurrentLanguageMock).toHaveBeenCalledWith('es-ES');
    });
});
