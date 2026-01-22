import SalutChip from './SalutChip';
import { useGetColorBySubsistema, SalutModel } from '../../types/salut.model';
import { useTranslation } from 'react-i18next';

const SalutSubsistemesChips = ({ salutItem }: { salutItem: SalutModel }) => {
    const { t } = useTranslation();
    const getColorBySubsistema = useGetColorBySubsistema();
    return (
        <>
            <SalutChip
                label={salutItem.subsistemaUpCount}
                tooltip={t(($) => $.page.salut.subsistemes.subsistemaUpCount)}
                backgroundColor={getColorBySubsistema(SalutModel.SUBSISTEMA_UP_COUNT)}
            />
            &nbsp;/&nbsp;
            <SalutChip
                label={salutItem.subsistemaWarnCount}
                tooltip={t(($) => $.page.salut.subsistemes.subsistemaWarnCount)}
                backgroundColor={getColorBySubsistema(SalutModel.SUBSISTEMA_WARN_COUNT)}
            />
            &nbsp;/&nbsp;
            <SalutChip
                label={salutItem.subsistemaDownCount}
                tooltip={t(($) => $.page.salut.subsistemes.subsistemaDownCount)}
                backgroundColor={getColorBySubsistema(SalutModel.SUBSISTEMA_DOWN_COUNT)}
            />
            &nbsp;/&nbsp;
            <SalutChip
                label={salutItem.subsistemaDesconegutCount}
                tooltip={t(($) => $.page.salut.subsistemes.subsistemaDesconegutCount)}
                backgroundColor={getColorBySubsistema(SalutModel.SUBSISTEMA_DESCONEGUT_COUNT)}
            />
        </>
    );
};
export default SalutSubsistemesChips;
