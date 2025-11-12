import { SalutEstatEnum, SalutModel } from '../../types/salut.model';

export const getLowestCommonIntegracioEstat = (salutCurrentApp: SalutModel) => {
    if (salutCurrentApp.integracioDownCount) return SalutEstatEnum.DOWN;
    if (salutCurrentApp.integracioWarnCount) return SalutEstatEnum.WARN;
    if (salutCurrentApp.integracioUpCount) return SalutEstatEnum.UP;
    return SalutEstatEnum.UNKNOWN;
};

export const getLowestCommonSubsistemesEstat = (salutCurrentApp: SalutModel) => {
    if (salutCurrentApp.subsistemaDownCount) return SalutEstatEnum.DOWN;
    if (salutCurrentApp.subsistemaWarnCount) return SalutEstatEnum.WARN;
    if (salutCurrentApp.subsistemaUpCount) return SalutEstatEnum.UP;
    return SalutEstatEnum.UNKNOWN;
};
