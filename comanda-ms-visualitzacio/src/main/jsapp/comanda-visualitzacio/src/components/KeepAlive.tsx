import { useEffect, useState } from 'react';
import { useResourceApiContext } from 'reactlib';
import { buildHref } from '../util/requestUtils.ts';

/**
 * KeepAlive component
 * - Sends a keep-alive ping every 10 minutes
 * - Stops automatically after 12 hours
 */

const keepAliveInterval = 10 * 60 * 1000; // 10 minutes
const keepAliveTimeoutHours = 12;

function KeepAlive() {
    const [startTimestamp] = useState<number>(() => Date.now());
    const { requestHref } = useResourceApiContext();
    useEffect(() => {
        const interval = setInterval(() => {
            const elapsedHours = (Date.now() - startTimestamp) / (1000 * 60 * 60);
            if (elapsedHours >= keepAliveTimeoutHours) {
                clearInterval(interval);
                return;
            }

            requestHref(buildHref('ping'), null)
                .then(() => console.log('[KeepAlive] OK'))
                .catch((e) => console.error('[KeepAlive] Error', e));
        }, keepAliveInterval);

        return () => clearInterval(interval);
    }, [requestHref, startTimestamp]);

    return null;
}

export default KeepAlive;
