import { useBaseAppContext } from 'reactlib';
import React from 'react';

/**
 * Hook to disable/enable BaseApp margins
 * @param disableMargins
 */
const useDisableMargins = (disableMargins: boolean = true) => {
    const { setMarginsDisabled } = useBaseAppContext();
    React.useEffect(() => {
        setMarginsDisabled(disableMargins);
        return () => setMarginsDisabled(false);
    }, [disableMargins]);
};

export default useDisableMargins;
