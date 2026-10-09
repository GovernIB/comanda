import { createContext, useContext } from 'react';

export type ParameterContextType = {
    maxResultSelects: number | null,
};

export const ParameterContext = createContext<ParameterContextType | undefined>(undefined);

export const useParameterContext = () => {
    const context = useContext(ParameterContext);
    if (context === undefined) {
        throw new Error('ParameterContext Provider not found');
    }
    return context;
}
