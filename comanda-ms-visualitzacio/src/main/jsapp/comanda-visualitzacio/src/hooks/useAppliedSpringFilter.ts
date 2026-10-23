import * as React from 'react';

/**
 * Recorda l'últim Spring Filter aplicat per un `MuiFilter`, per al recompte de filtres de la barra de
 * títol (`FilterCount`). Torna el filtre aplicat i el callback per a `onSpringFilterChange`, que
 * també avisa el callback original.
 */
const useAppliedSpringFilter = (onSpringFilterChange?: (springFilter?: string) => void) => {
    const [appliedSpringFilter, setAppliedSpringFilter] = React.useState<string | undefined>();
    const handleSpringFilterChange = React.useCallback((springFilter?: string) => {
        setAppliedSpringFilter(springFilter);
        onSpringFilterChange?.(springFilter);
    }, [onSpringFilterChange]);
    return { appliedSpringFilter, handleSpringFilterChange };
};

export default useAppliedSpringFilter;
