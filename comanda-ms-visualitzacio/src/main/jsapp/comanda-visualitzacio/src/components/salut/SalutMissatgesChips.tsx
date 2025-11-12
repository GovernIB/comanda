import SalutChip from './SalutChip';
import { getColorByMissatge, SalutModel } from '../../types/salut.model';
import { useTranslation } from 'react-i18next';

const SalutMissatgesChips = ({ salutItem }: { salutItem: SalutModel }) => {
    const { t } = useTranslation();
    return (
        <>
            <SalutChip
                label={salutItem.missatgeErrorCount}
                tooltip={t($ => $.page.salut.msgs.missatgeErrorCount)}
                backgroundColor={getColorByMissatge(SalutModel.MISSATGE_ERROR_COUNT)}
            />
            &nbsp;/&nbsp;
            <SalutChip
                label={salutItem.missatgeWarnCount}
                tooltip={t($ => $.page.salut.msgs.missatgeWarnCount)}
                backgroundColor={getColorByMissatge(SalutModel.MISSATGE_WARN_COUNT)}
            />
            &nbsp;/&nbsp;
            <SalutChip
                label={salutItem.missatgeInfoCount}
                tooltip={t($ => $.page.salut.msgs.missatgeInfoCount)}
                backgroundColor={getColorByMissatge(SalutModel.MISSATGE_INFO_COUNT)}
            />
        </>
    );
};

export default SalutMissatgesChips;
