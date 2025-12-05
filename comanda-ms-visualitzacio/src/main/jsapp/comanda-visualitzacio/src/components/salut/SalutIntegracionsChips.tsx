import SalutChip from './SalutChip';
import { useGetColorByIntegracio, SalutModel } from '../../types/salut.model';
import { useTranslation } from 'react-i18next';

const SalutIntegracionsChips = ({ salutItem }: { salutItem: SalutModel }) => {
    const { t } = useTranslation();
    const getColorByIntegracio = useGetColorByIntegracio();
    return (
        <>
            <SalutChip
                label={salutItem.integracioUpCount}
                tooltip={t(($) => $.page.salut.integracions.integracioUpCount)}
                backgroundColor={getColorByIntegracio(SalutModel.INTEGRACIO_UP_COUNT)}
            />
            &nbsp;/&nbsp;
            <SalutChip
                label={salutItem.integracioWarnCount}
                tooltip={t(($) => $.page.salut.integracions.integracioWarnCount)}
                backgroundColor={getColorByIntegracio(SalutModel.INTEGRACIO_WARN_COUNT)}
            />
            &nbsp;/&nbsp;
            <SalutChip
                label={salutItem.integracioDownCount}
                tooltip={t(($) => $.page.salut.integracions.integracioDownCount)}
                backgroundColor={getColorByIntegracio(SalutModel.INTEGRACIO_DOWN_COUNT)}
            />
            &nbsp;/&nbsp;
            <SalutChip
                label={salutItem.integracioDesconegutCount}
                tooltip={t(($) => $.page.salut.integracions.integracioDesconegutCount)}
                backgroundColor={getColorByIntegracio(SalutModel.INTEGRACIO_DESCONEGUT_COUNT)}
            />
        </>
    );
};

export default SalutIntegracionsChips;
