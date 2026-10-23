import { useTranslation } from 'react-i18next';
import { Box, Typography } from '@mui/material';
import { ScreenFrame } from '../lot2';

const NotFound: React.FC = () => {
    const { t } = useTranslation();
    return (
        <ScreenFrame>
            <Box
                sx={{
                    flex: 1,
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                }}>
                <Typography variant="h2">{t($ => $.page.notFound)}</Typography>
            </Box>
        </ScreenFrame>
    );
}

export default NotFound;
