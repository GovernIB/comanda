import { SalutData } from './Salut.tsx';
import { useCallback, useState } from 'react';

export type SalutExpansionStateKey = `app${string}-entorn${string}`;

export const getSalutExpansionKey = (salutGroup: Pick<SalutData, "groupedApp" | "groupedEntorn">): SalutExpansionStateKey =>
    `app${salutGroup.groupedApp?.id}-entorn${salutGroup.groupedEntorn?.id}`;

export const useSalutLlistatExpansionState = () => {
    const [expanded, setExpanded] = useState<string[]>([]);
    const isExpanded = useCallback(
        (expansionKey: SalutExpansionStateKey) => expanded.includes(expansionKey),
        [expanded]
    );
    const setExpandedWithContext = useCallback(
        (expand: boolean, expansionKey: SalutExpansionStateKey) => {
            if (expand) {
                setExpanded(prevState => [...prevState, expansionKey]);
            } else {
                setExpanded(prevState => prevState.filter(key => key !== expansionKey));
            }
        },
        []
    );
    return {
        isExpanded,
        setExpanded: setExpandedWithContext,
    };
};
