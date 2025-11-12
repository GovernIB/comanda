import { describe, expect, it } from 'vitest';
import { getLowestCommonIntegracioEstat, getLowestCommonSubsistemesEstat } from './utils';
import { SalutEstatEnum, SalutModel } from '../../types/salut.model';

describe('getLowestCommonIntegracioEstat', () => {
    it('should return DOWN if integracioDownCount is greater than 0', () => {
        const input: Partial<SalutModel> = {
            integracioDownCount: 1,
            integracioWarnCount: 2,
            integracioDesconegutCount: 3,
            integracioUpCount: 4,
        };
        expect(getLowestCommonIntegracioEstat(input as SalutModel)).toBe(SalutEstatEnum.DOWN);
    });

    it('should return WARN if integracioDownCount is 0 and integracioWarnCount is greater than 0', () => {
        const input: Partial<SalutModel> = {
            integracioDownCount: 0,
            integracioWarnCount: 2,
            integracioDesconegutCount: 3,
            integracioUpCount: 4,
        };
        expect(getLowestCommonIntegracioEstat(input as SalutModel)).toBe(SalutEstatEnum.WARN);
    });

    it('should return UNKNOWN if integracioDesconegutCount is greater than 0 and all other counts are 0', () => {
        const input: Partial<SalutModel> = {
            integracioDesconegutCount: 1,
            integracioWarnCount: 0,
            integracioDownCount: 0,
            integracioUpCount: 0,
        };
        expect(getLowestCommonIntegracioEstat(input as SalutModel)).toBe(SalutEstatEnum.UNKNOWN);
    });

    it('should return UNKNOWN if no counts are provided', () => {
        const input: Partial<SalutModel> = {};
        expect(getLowestCommonIntegracioEstat(input as SalutModel)).toBe(SalutEstatEnum.UNKNOWN);
    });

    it('should return UP if only integracioUpCount is greater than 0', () => {
        const input: Partial<SalutModel> = {
            integracioUpCount: 3,
        };
        expect(getLowestCommonIntegracioEstat(input as SalutModel)).toBe(SalutEstatEnum.UP);
    });

    it('should return UP if only integracioUpCount is greater than 0 even though integracioDesconegutCount is not 0', () => {
        const input: Partial<SalutModel> = {
            integracioUpCount: 3,
            integracioDesconegutCount: 6,
        };
        expect(getLowestCommonIntegracioEstat(input as SalutModel)).toBe(SalutEstatEnum.UP);
    });
});

describe('getLowestCommonSubsistemesEstat', () => {
    it('should return DOWN if subsistemaDownCount is greater than 0', () => {
        const input: Partial<SalutModel> = {
            subsistemaDownCount: 1,
            subsistemaWarnCount: 2,
            subsistemaDesconegutCount: 3,
            subsistemaUpCount: 4,
        };
        expect(getLowestCommonSubsistemesEstat(input as SalutModel)).toBe(SalutEstatEnum.DOWN);
    });

    it('should return WARN if subsistemaDownCount is 0 and subsistemaWarnCount is greater than 0', () => {
        const input: Partial<SalutModel> = {
            subsistemaDownCount: 0,
            subsistemaWarnCount: 2,
            subsistemaDesconegutCount: 3,
            subsistemaUpCount: 4,
        };
        expect(getLowestCommonSubsistemesEstat(input as SalutModel)).toBe(SalutEstatEnum.WARN);
    });

    it('should return UNKNOWN if subsistemaDesconegutCount is greater than 0 and all other counts are 0', () => {
        const input: Partial<SalutModel> = {
            subsistemaDesconegutCount: 1,
            subsistemaWarnCount: 0,
            subsistemaDownCount: 0,
            subsistemaUpCount: 0,
        };
        expect(getLowestCommonSubsistemesEstat(input as SalutModel)).toBe(SalutEstatEnum.UNKNOWN);
    });

    it('should return UNKNOWN if no counts are provided', () => {
        const input: Partial<SalutModel> = {};
        expect(getLowestCommonSubsistemesEstat(input as SalutModel)).toBe(SalutEstatEnum.UNKNOWN);
    });

    it('should return UP if only subsistemaUpCount is greater than 0', () => {
        const input: Partial<SalutModel> = {
            subsistemaUpCount: 3,
        };
        expect(getLowestCommonSubsistemesEstat(input as SalutModel)).toBe(SalutEstatEnum.UP);
    });

    it('should return UP if only subsistemaUpCount is greater than 0 even though subsistemaDesconegutCount is not 0', () => {
        const input: Partial<SalutModel> = {
            subsistemaUpCount: 3,
            subsistemaDesconegutCount: 6,
        };
        expect(getLowestCommonSubsistemesEstat(input as SalutModel)).toBe(SalutEstatEnum.UP);
    });
})
