import { useCallback, useEffect, useRef } from 'react';
import { throttle } from 'lodash';

/**
 * Throttle hook.
 * @param {T} callback
 * @param {number} delay
 * @returns {T}
 */
function useThrottle<T extends (...args: never[]) => void>(callback: T, delay: number): T {
	const callbackRef = useRef<T>(callback);

	// Update the current callback each time it changes.
	useEffect(() => {
		callbackRef.current = callback;
	}, [callback]);

	const throttledFn = useCallback(
		throttle((...args: never[]) => {
			callbackRef.current(...args);
		}, delay),
		[delay]
	);

	useEffect(() => {
		// Cleanup function to cancel any pending debounced calls
		return () => {
			throttledFn.cancel();
		};
	}, [throttledFn]);

	return throttledFn as unknown as T;
}

export default useThrottle;
