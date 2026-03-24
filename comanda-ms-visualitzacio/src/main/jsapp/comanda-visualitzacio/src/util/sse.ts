export type SseMessage = {
    event: string;
    data: string;
};

export type SubscribeToSseOptions = {
    signal?: AbortSignal;
    headers?: HeadersInit;
    onOpen?: () => void;
    onMessage: (message: SseMessage) => void;
    withCredentials?: boolean;
};

const EVENT_PREFIX = 'event:';
const DATA_PREFIX = 'data:';

export const subscribeToSse = async (url: string, options: SubscribeToSseOptions) => {
    const response = await fetch(url, {
        method: 'GET',
        headers: {
            Accept: 'text/event-stream',
            ...options.headers,
        },
        credentials: 'include',
        signal: options.signal,
    });
    if (!response.ok || response.body == null) {
        throw new Error(`SSE connection failed with status ${response.status}`);
    }

    options.onOpen?.();

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    let currentEvent = 'message';
    let currentData: string[] = [];

    const dispatchEvent = () => {
        if (currentData.length === 0) {
            currentEvent = 'message';
            return;
        }
        options.onMessage({
            event: currentEvent,
            data: currentData.join('\n'),
        });
        currentEvent = 'message';
        currentData = [];
    };

    while (!options.signal?.aborted) {
        const { done, value } = await reader.read();
        if (done) {
            break;
        }
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split(/\r?\n/);
        buffer = lines.pop() ?? '';

        for (const line of lines) {
            if (line.length === 0) {
                dispatchEvent();
                continue;
            }
            if (line.startsWith(':')) {
                continue;
            }
            if (line.startsWith(EVENT_PREFIX)) {
                currentEvent = line.slice(EVENT_PREFIX.length).trim() || 'message';
                continue;
            }
            if (line.startsWith(DATA_PREFIX)) {
                currentData.push(line.slice(DATA_PREFIX.length).trimStart());
            }
        }
    }

    if (buffer.length > 0) {
        const trailingLines = buffer.split(/\r?\n/);
        for (const line of trailingLines) {
            if (line.startsWith(EVENT_PREFIX)) {
                currentEvent = line.slice(EVENT_PREFIX.length).trim() || 'message';
            } else if (line.startsWith(DATA_PREFIX)) {
                currentData.push(line.slice(DATA_PREFIX.length).trimStart());
            }
        }
    }
    dispatchEvent();
};

export const subscribeToNativeSse = (url: string, options: SubscribeToSseOptions) => {
    const eventSource = new EventSource(url, {
        withCredentials: options.withCredentials ?? true,
    });

    const close = () => {
        eventSource.close();
    };

    if (options.signal != null) {
        if (options.signal.aborted) {
            close();
            return eventSource;
        }
        options.signal.addEventListener('abort', close, { once: true });
    }

    eventSource.onopen = () => {
        options.onOpen?.();
    };
    eventSource.onmessage = (event) => {
        options.onMessage({
            event: 'message',
            data: event.data,
        });
    };

    return eventSource;
};
