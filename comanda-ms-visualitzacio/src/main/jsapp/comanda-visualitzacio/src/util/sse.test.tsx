import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { subscribeToSse, subscribeToNativeSse, type SubscribeToSseOptions } from './sse';

let mockClose: ReturnType<typeof vi.fn>;
let mockAddEventListener: ReturnType<typeof vi.fn>;
let mockFetch: ReturnType<typeof vi.fn>;
let MockEventSource: ReturnType<typeof vi.fn>;

beforeEach(() => {
    vi.unstubAllGlobals();
    vi.clearAllMocks();
    mockClose = vi.fn();
    mockAddEventListener = vi.fn();
    mockFetch = vi.fn();
    MockEventSource = vi.fn().mockImplementation(() => ({
        close: mockClose,
        onopen: null as (() => void) | null,
        onmessage: null as ((event: any) => void) | null,
        addEventListener: mockAddEventListener,
    }));
    vi.stubGlobal('EventSource', MockEventSource);
    vi.stubGlobal('fetch', mockFetch);
    mockFetch.mockResolvedValue({
        ok: true,
        body: createMockStream([]),
    });
});

afterEach(() => {
    vi.unstubAllGlobals();
    vi.clearAllMocks();
});

const createMockStream = (chunks: string[]) => {
    const encoder = new TextEncoder();
    const encodedChunks = chunks.map((chunk) => encoder.encode(chunk));

    return new ReadableStream({
        start(controller) {
            encodedChunks.forEach((chunk) => controller.enqueue(chunk));
            controller.close();
        },
    });
};

describe('subscribeToSse (Custom Fetch)', () => {
    it('llança un error si la resposta no és ok o el body és null', async () => {
        mockFetch.mockResolvedValue({ ok: false, status: 500 });

        const options: SubscribeToSseOptions = { onMessage: vi.fn() };

        await expect(subscribeToSse('http://test.com', options)).rejects.toThrow('SSE connection failed with status 500');
    });

    it('crida onOpen i processa un missatge bàsic correctament', async () => {
        const onOpenMock = vi.fn();
        const onMessageMock = vi.fn();

        mockFetch.mockResolvedValue({
            ok: true,
            body: createMockStream(['data: hola món\n\n']),
        });

        await subscribeToSse('http://test.com', { onOpen: onOpenMock, onMessage: onMessageMock });

        expect(onOpenMock).toHaveBeenCalledTimes(1);
        expect(onMessageMock).toHaveBeenCalledWith({ event: 'message', data: 'hola món' });
    });

    it('processa esdeveniments personalitzats (event:) i dades multilínia', async () => {
        const onMessageMock = vi.fn();
        const streamData = 'event: actualitzacio\ndata: línia 1\ndata: línia 2\n\n';

        mockFetch.mockResolvedValue({
            ok: true,
            body: createMockStream([streamData]),
        });

        await subscribeToSse('http://test.com', { onMessage: onMessageMock });

        expect(onMessageMock).toHaveBeenCalledWith({
            event: 'actualitzacio',
            data: 'línia 1\nlínia 2'
        });
    });

    it('ignora les línies de comentari (que comencen per :)', async () => {
        const onMessageMock = vi.fn();
        const streamData = ': això és un comentari\ndata: dada real\n\n';

        mockFetch.mockResolvedValue({
            ok: true,
            body: createMockStream([streamData]),
        });

        await subscribeToSse('http://test.com', { onMessage: onMessageMock });

        expect(onMessageMock).toHaveBeenCalledWith({ event: 'message', data: 'dada real' });
    });

    it('atura el bucle de lectura quan el signal és abortat', async () => {
        const onMessageMock = vi.fn();
        const controller = new AbortController();

        let resolveRead: (value: any) => void;
        const readPromise = new Promise((resolve) => { resolveRead = resolve; });

        mockFetch.mockResolvedValue({
            ok: true,
            body: {
                getReader: () => ({
                    read: () => readPromise,
                }),
            },
        });

        const ssePromise = subscribeToSse('http://test.com', {
            signal: controller.signal,
            onMessage: onMessageMock
        });

        controller.abort();
        resolveRead!({ done: false, value: new Uint8Array() });

        await ssePromise;

        expect(onMessageMock).not.toHaveBeenCalled();
    });
});

describe('subscribeToNativeSse (Native Wrapper)', () => {
    it('inicialitza EventSource amb les opcions correctes i registra els callbacks', () => {
        const onOpenMock = vi.fn();
        const onMessageMock = vi.fn();

        subscribeToNativeSse('http://test.com', {
            onOpen: onOpenMock,
            onMessage: onMessageMock,
            withCredentials: false,
        });

        expect(MockEventSource).toHaveBeenCalledWith('http://test.com', { withCredentials: false });

        const instance = MockEventSource.mock.results[0].value;
        instance.onopen?.();
        expect(onOpenMock).toHaveBeenCalledTimes(1);

        instance.onmessage?.({ data: 'missatge natiu' });
        expect(onMessageMock).toHaveBeenCalledWith({ event: 'message', data: 'missatge natiu' });
    });

    it('tanca la connexió immediatament si el signal ja està abortat', () => {
        const controller = new AbortController();
        controller.abort();

        subscribeToNativeSse('http://test.com', {
            signal: controller.signal,
            onMessage: vi.fn(),
        });

        expect(mockClose).toHaveBeenCalledTimes(1);
    });

    it('tanca la connexió quan es dispara l\'event d\'abort del signal', () => {
        const controller = new AbortController();

        subscribeToNativeSse('http://test.com', {
            signal: controller.signal,
            onMessage: vi.fn(),
        });

        expect(mockClose).not.toHaveBeenCalled();

        controller.abort();

        expect(mockClose).toHaveBeenCalledTimes(1);
    });
});
