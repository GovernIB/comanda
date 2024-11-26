import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';

export const useSmallScreen = () => {
    const theme = useTheme();
    return useMediaQuery(theme.breakpoints.down('sm'));
}

export default useSmallScreen;