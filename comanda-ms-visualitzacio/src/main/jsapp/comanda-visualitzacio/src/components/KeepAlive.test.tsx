import { render } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import KeepAlive from './KeepAlive';

const mocks = vi.hoisted(() => ({
    requestHrefMock: vi.fn(),
    useResourceApiContextMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    useResourceApiContext: () => mocks.useResourceApiContextMock(),
}));

vi.mock('../util/requestUtils.ts', () => ({
    buildHref: (href: string) => `/api/${href}`,
}));

describe('KeepAlive', () => {
    beforeEach(() => {
        vi.useFakeTimers();
        mocks.useResourceApiContextMock.mockReturnValue({
            requestHref: mocks.requestHrefMock,
        });
        mocks.requestHrefMock.mockResolvedValue(undefined);
        vi.spyOn(console, 'log').mockImplementation(() => undefined);
        vi.spyOn(console, 'error').mockImplementation(() => undefined);
    });

    afterEach(() => {
        vi.useRealTimers();
        vi.clearAllMocks();
    });

    it('KeepAlive_quanPassaLInterval_faUnaPeticioDePing', async () => {
        // Comprova que el component envia un ping quan transcorre el temps configurat.
        render(<KeepAlive />);

        await vi.advanceTimersByTimeAsync(10 * 60 * 1000);

        expect(mocks.requestHrefMock).toHaveBeenCalledWith('/api/ping', null);
    });

    it('KeepAlive_quanSuperaElTempsLimit_deixaDeFerPings', async () => {
        // Verifica que el component deixa de programar peticions després del màxim d'hores definit.
        render(<KeepAlive />);

        await vi.advanceTimersByTimeAsync(12 * 60 * 60 * 1000);
        mocks.requestHrefMock.mockClear();

        await vi.advanceTimersByTimeAsync(10 * 60 * 1000);

        expect(mocks.requestHrefMock).not.toHaveBeenCalled();
    });
});
