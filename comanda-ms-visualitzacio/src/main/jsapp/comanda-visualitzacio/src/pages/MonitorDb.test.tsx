import { render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import MonitorDb from './MonitorDb';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) => {
            const proxy: any = new Proxy({}, {
                get: (_t, prop: string) => (prop === 'title' ? 'Monitor BD' : proxy),
            });
            return typeof selector === 'function' ? (selector(proxy) === proxy ? 'text' : selector(proxy)) : selector;
        },
    }),
}));

vi.mock('reactlib', () => ({
    MuiDataGrid: () => <div data-testid="grid" />,
    useMuiDataGridApiRef: () => ({ current: null }),
    useAuthContext: () => ({ getToken: () => 'token' }),
    useResourceApiContext: () => ({ apiUrl: 'http://api' }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('MonitorDb', () => {
    beforeEach(() => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => ({}) }));
    });
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('MonitorDb_quanEsRenderitza_mostraElTitolDinsElScreenFrame', async () => {
        // La barra pròpia del ScreenFrame substitueix l'antiga Toolbar de la pàgina.
        const { container } = render(<MonitorDb />);

        const frame = container.querySelector('.screen-frame');
        expect(frame).not.toBeNull();
        const screenTitle = (frame as HTMLElement).querySelector('.screen-title');
        expect(screenTitle).not.toBeNull();
        expect(screenTitle).toHaveTextContent('Monitor BD');
        expect(screen.getByTestId('page-title')).toHaveTextContent('Monitor BD');
        expect(await screen.findByRole('tablist')).toBeInTheDocument();
    });
});
