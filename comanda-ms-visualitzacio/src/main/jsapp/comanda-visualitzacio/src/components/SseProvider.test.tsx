import React from 'react';
import { act, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { SseProvider, useSseContext } from './SseProvider';

type FakeEventSource = {
    addEventListener: ReturnType<typeof vi.fn>;
    close: ReturnType<typeof vi.fn>;
    onopen: (() => void) | null;
    onmessage: ((event: { data: string }) => void) | null;
    onerror: (() => void) | null;
};

const mocks = vi.hoisted(() => ({
    apiUrl: '/api',
    apiIsReady: true,
    httpHeaders: undefined as Record<string, string>[] | undefined,
    authIsReady: true,
    isAuthenticated: true,
    bearerTokenActive: false,
    getToken: vi.fn(() => 'test-token'),
    subscribeToNativeSseMock: vi.fn(),
    subscribeToSseMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    useResourceApiContext: ((() => ({
        apiUrl: mocks.apiUrl,
        isReady: mocks.apiIsReady,
        httpHeaders: mocks.httpHeaders,
    })) as unknown) as typeof import('reactlib').useResourceApiContext,
    useAuthContext: ((() => ({
        isLoading: false,
        isReady: mocks.authIsReady,
        isAuthenticated: mocks.isAuthenticated,
        bearerTokenActive: mocks.bearerTokenActive,
        getToken: mocks.getToken,
        getTokenParsed: undefined,
        getUserId: undefined,
        getUserName: undefined,
        getUserEmail: undefined,
        signIn: undefined,
        signOut: undefined,
    })) as unknown) as typeof import('reactlib').useAuthContext,
}));

vi.mock('../util/sse', () => ({
    subscribeToNativeSse: mocks.subscribeToNativeSseMock,
    subscribeToSse: mocks.subscribeToSseMock,
}));

const TestConsumer = () => {
    const { status, subscribe } = useSseContext();
    const [count, setCount] = React.useState(0);

    React.useEffect(() => {
        return subscribe('alarm.active.changed', (event) => {
            const payload = Array.isArray(event.payload) ? event.payload : [];
            setCount(payload.length);
        });
    }, [subscribe]);

    return (
        <>
            <div data-testid="status">{status}</div>
            <div data-testid="count">{count}</div>
        </>
    );
};

const createFakeEventSource = (): FakeEventSource => ({
    addEventListener: vi.fn(),
    close: vi.fn(),
    onopen: null,
    onmessage: null,
    onerror: null,
});

describe('SseProvider', () => {
    beforeEach(() => {
        mocks.apiUrl = '/api';
        mocks.apiIsReady = true;
        mocks.httpHeaders = undefined;
        mocks.authIsReady = true;
        mocks.isAuthenticated = true;
        mocks.bearerTokenActive = false;
        mocks.getToken.mockReturnValue('test-token');
        mocks.subscribeToSseMock.mockReset();
        mocks.subscribeToNativeSseMock.mockReset();
    });

    afterEach(() => {
        vi.useRealTimers();
        vi.restoreAllMocks();
    });

    it('SseProvider_quanNoHiHaBearer_usaEventSourceNAmbCookiesIDistribueixElsEvents', async () => {
        const eventSource = createFakeEventSource();
        let namedEventListener: ((event: MessageEvent) => void) | undefined;
        eventSource.addEventListener.mockImplementation((eventName: string, listener: EventListener) => {
            if (eventName === 'comanda-event') {
                namedEventListener = listener as unknown as (event: MessageEvent) => void;
            }
        });
        mocks.subscribeToNativeSseMock.mockImplementation((_url: string, options: { onOpen?: () => void }) => {
            eventSource.onopen = options.onOpen ?? null;
            return eventSource;
        });

        render(
            <SseProvider>
                <TestConsumer />
            </SseProvider>
        );

        expect(mocks.subscribeToNativeSseMock).toHaveBeenCalledWith('/api/events/stream', expect.objectContaining({
            withCredentials: true,
            onOpen: expect.any(Function),
            onMessage: expect.any(Function),
        }));

        await act(async () => {
            eventSource.onopen?.();
        });

        await waitFor(() => {
            expect(screen.getByTestId('status')).toHaveTextContent('connected');
        });

        await act(async () => {
            namedEventListener?.({
                data: JSON.stringify({
                    type: 'alarm.active.changed',
                    payload: [{ id: 1 }, { id: 2 }],
                }),
            } as MessageEvent);
        });

        expect(screen.getByTestId('count')).toHaveTextContent('2');
        expect(mocks.subscribeToSseMock).not.toHaveBeenCalled();
    });

    it('SseProvider_quanHiHaBearer_usaFetchSseAmbAuthorizationIDistribueixElsEvents', async () => {
        mocks.bearerTokenActive = true;
        let onOpen: (() => void) | undefined;
        let onMessage: ((message: { event: string; data: string }) => void) | undefined;
        mocks.subscribeToSseMock.mockImplementation(async (_url: string, options: { onOpen?: () => void; onMessage: (message: { event: string; data: string }) => void }) => {
            onOpen = options.onOpen;
            onMessage = options.onMessage;
        });

        render(
            <SseProvider>
                <TestConsumer />
            </SseProvider>
        );

        await waitFor(() => {
            expect(mocks.subscribeToSseMock).toHaveBeenCalledWith('/api/events/stream', expect.objectContaining({
                headers: {
                    Authorization: 'Bearer test-token',
                },
                onOpen: expect.any(Function),
                onMessage: expect.any(Function),
            }));
        });

        await act(async () => {
            onOpen?.();
            onMessage?.({
                event: 'comanda-event',
                data: JSON.stringify({
                    type: 'alarm.active.changed',
                    payload: [{ id: 7 }],
                }),
            });
        });

        await waitFor(() => {
            expect(screen.getByTestId('status')).toHaveTextContent('connected');
        });
        expect(screen.getByTestId('count')).toHaveTextContent('1');
        expect(mocks.subscribeToNativeSseMock).not.toHaveBeenCalled();
    });

    it('SseProvider_quanHiHaBearer_propagaCapcaleresPersonalitzades', async () => {
        mocks.bearerTokenActive = true;
        mocks.httpHeaders = [{ 'X-App-Role': 'COM_ADMIN' }];
        mocks.subscribeToSseMock.mockImplementation(async () => undefined);

        render(
            <SseProvider>
                <TestConsumer />
            </SseProvider>
        );

        await waitFor(() => {
            expect(mocks.subscribeToSseMock).toHaveBeenCalledWith('/api/events/stream', expect.objectContaining({
                headers: {
                    Authorization: 'Bearer test-token',
                    'X-App-Role': 'COM_ADMIN',
                },
            }));
        });
    });

    it('SseProvider_quanLaConnexioCau_programaReconnect', async () => {
        vi.useFakeTimers();
        const firstEventSource = createFakeEventSource();
        const secondEventSource = createFakeEventSource();
        mocks.subscribeToNativeSseMock
            .mockImplementationOnce((_url: string, options: { onOpen?: () => void }) => {
                firstEventSource.onopen = options.onOpen ?? null;
                return firstEventSource;
            })
            .mockImplementationOnce((_url: string, options: { onOpen?: () => void }) => {
                secondEventSource.onopen = options.onOpen ?? null;
                return secondEventSource;
            });

        render(
            <SseProvider>
                <TestConsumer />
            </SseProvider>
        );

        await act(async () => {
            firstEventSource.onerror?.();
        });

        expect(screen.getByTestId('status')).toHaveTextContent('disconnected');
        expect(firstEventSource.close).toHaveBeenCalled();

        act(() => {
            vi.advanceTimersByTime(5_000);
        });

        expect(mocks.subscribeToNativeSseMock).toHaveBeenCalledTimes(2);
        expect(screen.getByTestId('status')).toHaveTextContent('connecting');
    });
});
