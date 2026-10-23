import * as React from 'react';
import { useTheme } from '@mui/material/styles';
import { ScreenTitle, lot2ScreenGridProps } from '../lot2';

/**
 * Props de la graella principal d'una pantalla: el `ScreenTitle` com a `title` i la barra de títol
 * de lot2 (opt-in). No s'ha d'usar a subtaules ni a graelles de diàlegs.
 */
const useScreenGridProps = (icon: string, title: React.ReactNode) => {
    const theme = useTheme();
    return {
        title: <ScreenTitle icon={icon} title={title} />,
        ...lot2ScreenGridProps(theme),
    };
};

export default useScreenGridProps;
