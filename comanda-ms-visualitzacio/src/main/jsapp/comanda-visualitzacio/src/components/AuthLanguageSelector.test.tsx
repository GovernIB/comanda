import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import AuthLanguageSelector from './AuthLanguageSelector';

const mocks = vi.hoisted(() => ({
    setCurrentLanguageMock: vi.fn(),
    onLanguageChangeMock: vi.fn(),
    useBaseAppContextMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    useBaseAppContext: () => mocks.useBaseAppContextMock(),
}));

describe('AuthLanguageSelector', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('AuthLanguageSelector_quanNoHiHaLlengues_noRenderitzaCapOpcio', () => {
        // Comprova que el component no mostra res quan no rep cap llista de llengües.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca',
            setCurrentLanguage: mocks.setCurrentLanguageMock,
        });

        const { container } = render(<AuthLanguageSelector />);

        expect(container).toBeEmptyDOMElement();
    });

    it('AuthLanguageSelector_quanEsRenderitza_notificaLaLlenguaActual', () => {
        // Verifica que el component informa la llengua activa en el muntatge.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca',
            setCurrentLanguage: mocks.setCurrentLanguageMock,
        });

        render(
            <AuthLanguageSelector
                languages={['ca', 'es']}
                onLanguageChange={mocks.onLanguageChangeMock}
            />
        );

        expect(mocks.onLanguageChangeMock).toHaveBeenCalledWith('ca');
    });

    it('AuthLanguageSelector_quanEsPremUnaLlengua_canviaLaLlenguaActual', () => {
        // Comprova que clicar una opció invoca el canvi de llengua al context base.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca',
            setCurrentLanguage: mocks.setCurrentLanguageMock,
        });

        render(<AuthLanguageSelector languages={['ca', 'es']} />);

        fireEvent.click(screen.getByRole('button', { name: 'es' }));

        expect(mocks.setCurrentLanguageMock).toHaveBeenCalledWith('es');
    });
});
