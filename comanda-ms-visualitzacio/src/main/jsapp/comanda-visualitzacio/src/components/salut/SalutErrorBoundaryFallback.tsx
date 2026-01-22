import { useTranslation } from 'react-i18next';
import Typography from '@mui/material/Typography';

export const SalutErrorBoundaryFallback = () => {
    const { t } = useTranslation();
    return (
        <Typography
            sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
            }}
            color="error"
        >
            {t($ => $.page.salut.latencia.error)}
        </Typography>
    );
};
