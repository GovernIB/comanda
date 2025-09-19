import React from 'react';
import Box from '@mui/material/Box';
import Filter, { FilterProps } from '../../form/Filter';
import { useMuiBaseAppContext } from '../MuiBaseAppContext';
import { useOptionalDataGridContext } from '../datagrid/DataGridContext';

/**
 * Propietats del component MuiFilter (també conté les propietats del component Filter).
 */
export type MuiFilterProps = FilterProps & {
    /** Propietats per a l'element que conté el filtre */
    componentProps?: any;
};

/**
 * Component de filtre per a la llibreria MUI.
 *
 * @param props - Propietats del component.
 * @returns Element JSX del filtre.
 */
export const MuiFilter: React.FC<MuiFilterProps> = (props) => {
    const { defaultMuiComponentProps } = useMuiBaseAppContext();
    const { componentProps, onSpringFilterChange, children, ...otherProps } = {
        ...defaultMuiComponentProps.filter,
        ...props,
    };
    const gridContext = useOptionalDataGridContext();
    const handleSpringFilterChange = (filter: string | undefined) => {
        if (gridContext != null) {
            gridContext.apiRef.current?.setFilter(filter);
        }
        onSpringFilterChange?.(filter);
    };
    return (
        <Box {...componentProps}>
            <Filter onSpringFilterChange={handleSpringFilterChange} {...otherProps}>
                {children}
            </Filter>
        </Box>
    );
};

export default MuiFilter;
