import useThrottle from './useThrottle.ts';
import { RefCallback, useCallback, useRef, useState } from 'react';
import { isEqual } from 'lodash';

type Size = {
    width: number;
    height: number;
}

const useSizeTracker = (delay = 100) => {
    const observerRef = useRef<ResizeObserver>(null);
    const [size, setSize] = useState<Size>();
    const deepEqualSetSize = useCallback((newSize: Size) => {
        setSize((prevSize) => (isEqual(prevSize, newSize) ? prevSize : newSize));
    }, []);
    const throttledSetSize = useThrottle(deepEqualSetSize, delay);

    const refCallback: RefCallback<HTMLElement> = (node) => {
        observerRef.current = new ResizeObserver((entries) => {
            const entry = entries[0];
            if (entry) {
                throttledSetSize({
                    height: entry.contentRect.height,
                    width: entry.contentRect.width,
                });
            }
        });
        if (node) {
            observerRef.current.observe(node);
        }
        return () => {
            if (observerRef.current) {
                observerRef.current.disconnect();
            }
        };
    };
    return { refCallback, size };
};

export default useSizeTracker;
