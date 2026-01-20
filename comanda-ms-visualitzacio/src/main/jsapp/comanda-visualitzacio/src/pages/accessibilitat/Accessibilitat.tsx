import { Box } from '@mui/material';
import PageTitle from '../../components/PageTitle.tsx';
import AccessibilitatCa from './AccessibilitatCa.tsx';
import AccessibilitatEs from './AccessibilitatEs.tsx';
import { useBaseAppContext } from 'reactlib';
import { useTranslation } from 'react-i18next';

const Accessibilitat = () => {
    const { t } = useTranslation();
    const { currentLanguage } = useBaseAppContext();
    return (
        <Box sx={{ maxWidth: 600, mx: 'auto', p: 3 }}>
            <PageTitle title={t('page.accessibilitat.title')} />
            {currentLanguage === 'es' ? <AccessibilitatEs /> : <AccessibilitatCa />}
        </Box>
    );
};
export default Accessibilitat;
