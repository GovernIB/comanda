import { useEffect, useRef } from 'react';

type UseIntervalOptions = {
    tick: () => void;
    init?: () => void;
    timeout: number | null | undefined;
};


function useInterval({ tick, init, timeout }: UseIntervalOptions) {
    const savedCallback = useRef<() => void | null>(null);

    useEffect(() => {
        savedCallback.current = tick;
    }, [tick]);

    useEffect(() => {
        if (timeout && timeout > 0) {
            init?.();

            function tick() {
                savedCallback.current?.();
            }

            const intervalId = setInterval(tick, timeout);
            return () => clearInterval(intervalId);
        }
    }, [timeout]);
}

export default useInterval;
