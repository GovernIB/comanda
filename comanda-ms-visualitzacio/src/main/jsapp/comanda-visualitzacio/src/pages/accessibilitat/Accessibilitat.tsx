import { Box } from '@mui/material';
import { ScreenFrame } from '../../lot2';
import { screenIcons } from '../../util/screenIcons.ts';
import PageTitle from '../../components/PageTitle.tsx';
import AccessibilitatCa from './AccessibilitatCa.tsx';
import AccessibilitatEs from './AccessibilitatEs.tsx';
import { useBaseAppContext } from 'reactlib';
import { useTranslation } from 'react-i18next';

const Accessibilitat = () => {
    const { t } = useTranslation();
    const { currentLanguage } = useBaseAppContext();
    const currentLocation = window.location.toString();
    let appUrl;
    if (currentLocation.endsWith('accessibilitat'))
        appUrl = currentLocation.substring(0, currentLocation.length - 'accessibilitat'.length);
    if (currentLocation.endsWith('accessibilitat/'))
        appUrl = currentLocation.substring(0, currentLocation.length - 'accessibilitat/'.length);
    return (
        <ScreenFrame icon={screenIcons.accessibilitat} title={t($ => $.page.accessibilitat.title)} padded>
            <Box sx={{ maxWidth: 600, mx: 'auto', width: '100%' }}>
                <PageTitle title={t($ => $.page.accessibilitat.title)} />
                {currentLanguage === 'es' ? <AccessibilitatEs appUrl={appUrl} /> : <AccessibilitatCa appUrl={appUrl} />}
            </Box>
        </ScreenFrame>
    );
};
export default Accessibilitat;
