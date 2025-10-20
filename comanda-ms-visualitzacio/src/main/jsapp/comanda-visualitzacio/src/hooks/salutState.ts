import { SalutData } from '../pages/Salut.tsx';
import { useCallback, useState } from 'react';

export const useSalutLlistatExpansionState = () => {
    const [expanded, setExpanded] = useState<string[]>([]);
    const getExpandKey = (salutGroup: SalutData) => {
        return `app${salutGroup.groupedApp?.id}-entorn${salutGroup.groupedEntorn?.id}`;
    };
    const isExpanded = useCallback(
        (salutGroup: SalutData) => expanded.includes(getExpandKey(salutGroup)),
        [expanded]
    );
    const setExpandedWithContext = useCallback((expand: boolean, context: SalutData) => {
        if (expand) {
            setExpanded((prevState) => [...prevState, getExpandKey(context)]);
        } else {
            setExpanded((prevState) => prevState.filter((key) => key !== getExpandKey(context)));
        }
    }, []);
    return {
        isExpanded,
        setExpanded: setExpandedWithContext,
    };
};
