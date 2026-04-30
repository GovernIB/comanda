import React from 'react';
import { useAuthContext, useResourceApiContext } from 'reactlib';
import { subscribeToNativeSse, subscribeToSse, SseMessage } from '../util/sse';

const SSE_EVENT_NAME = 'comanda-event';
const SSE_RECONNECT_DELAY_MS = 5000;

type SseEvent = {
    type?: string;
    payload?: unknown;
    timestamp?: string;
};

type SseStatus = 'connecting' | 'connected' | 'disconnected';
type SseListener = (event: SseEvent) => void;

type SseContextType = {
    connected: boolean;
    status: SseStatus;
    subscribe: (eventType: string, listener: SseListener) => () => void;
};

const SseContext = React.createContext<SseContextType | undefined>(undefined);

const buildSseUrl = (apiUrl: string) => {
    return `${apiUrl}${apiUrl.endsWith('/') ? '' : '/'}events/stream`;
};

export const useSseContext = () => {
    const context = React.useContext(SseContext);
    if (context === undefined) {
        throw new Error('useSseContext must be used within a SseProvider');
    }
    return context;
};

export const SseProvider: React.FC<React.PropsWithChildren> = ({ children }) => {
    const { apiUrl, isReady: apiIsReady, httpHeaders } = useResourceApiContext();
    const {
        isReady: authIsReady,
        isAuthenticated,
        bearerTokenActive,
        getToken,
    } = useAuthContext();
    const [status, setStatus] = React.useState<SseStatus>('connecting');
    const listenersRef = React.useRef<Map<string, Set<SseListener>>>(new Map());

    const dispatchEvent = React.useCallback((event: SseEvent) => {
        if (event.type == null) {
            return;
        }
        listenersRef.current.get(event.type)?.forEach(listener => listener(event));
        listenersRef.current.get('*')?.forEach(listener => listener(event));
    }, []);

    const subscribe = React.useCallback((eventType: string, listener: SseListener) => {
        const currentListeners = listenersRef.current.get(eventType) ?? new Set<SseListener>();
        currentListeners.add(listener);
        listenersRef.current.set(eventType, currentListeners);
        return () => {
            const registeredListeners = listenersRef.current.get(eventType);
            if (registeredListeners == null) {
                return;
            }
            registeredListeners.delete(listener);
            if (registeredListeners.size === 0) {
                listenersRef.current.delete(eventType);
            }
        };
    }, []);

    React.useEffect(() => {
        if (!apiIsReady || !authIsReady || !isAuthenticated) {
            return;
        }

        let disposed = false;
        let reconnectTimeout: ReturnType<typeof setTimeout> | undefined;
        let eventSource: EventSource | undefined;
        let abortController: AbortController | undefined;

        const cleanupCurrentConnection = () => {
            eventSource?.close();
            eventSource = undefined;
            abortController?.abort();
            abortController = undefined;
        };

        const scheduleReconnect = () => {
            if (disposed) {
                return;
            }
            cleanupCurrentConnection();
            setStatus('disconnected');
            reconnectTimeout = setTimeout(() => {
                if (!disposed) {
                    connect();
                }
            }, SSE_RECONNECT_DELAY_MS);
        };

        const handleRawMessage = ({ event, data }: SseMessage) => {
            if (event !== SSE_EVENT_NAME && event !== 'message') {
                return;
            }
            dispatchEvent(JSON.parse(data) as SseEvent);
        };

        const connect = () => {
            cleanupCurrentConnection();
            setStatus('connecting');

            if (!bearerTokenActive) {
                eventSource = subscribeToNativeSse(buildSseUrl(apiUrl), {
                    withCredentials: true,
                    onOpen: () => setStatus('connected'),
                    onMessage: handleRawMessage,
                });
                eventSource.addEventListener(SSE_EVENT_NAME, (event: MessageEvent) => {
                    handleRawMessage({ event: SSE_EVENT_NAME, data: event.data });
                });
                eventSource.onerror = () => {
                    scheduleReconnect();
                };
                return;
            }

            abortController = new AbortController();
            const token = getToken?.();
            const customHeaders = (httpHeaders ?? []).reduce<Record<string, string>>(
                (accumulator, currentHeader) => ({
                    ...accumulator,
                    ...currentHeader,
                }),
                {}
            );
            const headers = {
                ...customHeaders,
                ...(token
                    ? {
                        Authorization: `Bearer ${token}`,
                    }
                    : {}),
            };
            void subscribeToSse(buildSseUrl(apiUrl), {
                signal: abortController.signal,
                headers: Object.keys(headers).length > 0 ? headers : undefined,
                onOpen: () => setStatus('connected'),
                onMessage: handleRawMessage,
            }).then(() => {
                scheduleReconnect();
            }).catch(() => {
                scheduleReconnect();
            });
        };

        connect();

        return () => {
            disposed = true;
            if (reconnectTimeout != null) {
                clearTimeout(reconnectTimeout);
            }
            cleanupCurrentConnection();
        };
    }, [
        apiIsReady,
        authIsReady,
        isAuthenticated,
        bearerTokenActive,
        getToken,
        apiUrl,
        httpHeaders,
        dispatchEvent,
    ]);

    return (
        <SseContext.Provider
            value={{
                connected: status === 'connected',
                status,
                subscribe,
            }}
        >
            {children}
        </SseContext.Provider>
    );
};

export default SseProvider;
