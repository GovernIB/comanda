import { SalutData } from '../pages/Salut.tsx';
import { useState } from 'react';

export const useSalutLlistatExpansionState = () => {
    const [expanded, setExpanded] = useState<string[]>([]);
    const getExpandKey = (salutGroup: SalutData) => {
        return `app${salutGroup.groupedApp?.id}-entorn${salutGroup.groupedEntorn?.id}`
    }
    return {
        isExpanded: (salutGroup: SalutData) => expanded.includes(getExpandKey(salutGroup)),
        setExpanded: (expand: boolean, context: SalutData) => {
            if (expand) {
                setExpanded((prevState) => [...prevState, getExpandKey(context)]);
            } else {
                setExpanded((prevState) => prevState.filter(key => key !== getExpandKey(context)));
            }
        }
    }
};
