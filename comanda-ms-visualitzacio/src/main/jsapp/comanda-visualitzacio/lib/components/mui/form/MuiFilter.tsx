import React from 'react';
import Box from '@mui/material/Box';
import Filter, { FilterProps } from '../../form/Filter';
import { useMuiBaseAppContext } from '../MuiBaseAppContext';
import { useOptionalDataGridContext } from '../datagrid/DataGridContext';

/**
 * Propietats del component MuiFilter (també conté les propietats del component Filter).
 */
export type MuiFilterProps = FilterProps & {
    /** Indica que el filtre ha d'actuar de forma aïllada (no ha d'actualitzar el filtre del MuiDataGrid pare) */
    detached?: boolean;
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
    const { detached, componentProps, onSpringFilterChange, children, ...otherProps } = {
        ...defaultMuiComponentProps.filter,
        ...props,
    };
    const gridContext = useOptionalDataGridContext();
    const handleSpringFilterChange = (filter: string | undefined) => {
        if (gridContext != null && !detached) {
            gridContext.apiRef.current?.setFilter(filter);
        }
        onSpringFilterChange?.(filter);
    };
    const outerBoxSx = {
        mt: 1,
        ...componentProps?.sx,
    };
    return (
        <Filter onSpringFilterChange={handleSpringFilterChange} {...otherProps}>
            <Box {...componentProps} sx={outerBoxSx}>
                {children}
            </Box>
        </Filter>
    );
};

export default MuiFilter;
