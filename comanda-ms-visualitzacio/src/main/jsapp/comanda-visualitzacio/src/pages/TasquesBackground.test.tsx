import { render } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import TasquesBackground from './TasquesBackground';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            selector({
                page: { tasquesBackground: { title: 'Tasques en segon pla', columns: {}, reiniciar: { label: 'Reiniciar' } } },
            }),
    }),
}));

vi.mock('reactlib', () => ({
    MuiDataGrid: ({ title }: { title: React.ReactNode }) => <section>{title}</section>,
    useResourceApiService: () => ({ isReady: true, delete: vi.fn() }),
    useBaseAppContext: () => ({ messageDialogShow: vi.fn(), temporalMessageShow: vi.fn() }),
    useConfirmDialogButtons: () => ({}),
    useMuiDataGridApiRef: () => ({ current: null }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('TasquesBackground', () => {
    it('TasquesBackground_quanEsRenderitza_mostraElTitolDinsElScreenFrame', () => {
        // El títol de la graella és un ScreenTitle dins el ScreenFrame de la pàgina.
        const { container } = render(<TasquesBackground />);

        const frame = container.querySelector('.screen-frame');
        expect(frame).not.toBeNull();
        const screenTitle = (frame as HTMLElement).querySelector('.screen-title');
        expect(screenTitle).not.toBeNull();
        expect(screenTitle).toHaveTextContent('Tasques en segon pla');
    });
});
