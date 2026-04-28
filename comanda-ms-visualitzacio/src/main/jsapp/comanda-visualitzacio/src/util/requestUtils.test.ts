import { beforeEach, describe, expect, it, vi } from 'vitest';

const { getEnvApiUrlMock } = vi.hoisted(() => ({
    getEnvApiUrlMock: vi.fn(),
}));

vi.mock('../main.tsx', () => ({
    getEnvApiUrl: getEnvApiUrlMock,
}));

import { buildHref, findOptions } from './requestUtils';

describe('findOptions', () => {
    it('findOptions_quanLaConsultaRetornaFiles_mapejaIdIDescription', async () => {
        // Comprova que la resposta de cerca es transforma a opcions de selector amb id i descripció.
        const findMock = vi.fn().mockResolvedValue({
            rows: [
                { id: 1, nom: 'Alpha' },
                { id: 2, nom: 'Beta' },
            ],
            page: { totalElements: 2 },
        });

        const result = await findOptions(findMock, 'nom', 'alp', 'actiu:true');

        expect(findMock).toHaveBeenCalledWith({ page: 0, size: 5, filter: 'actiu:true', quickFilter: 'alp' });
        expect(result).toEqual({
            options: [
                { id: 1, description: 'Alpha' },
                { id: 2, description: 'Beta' },
            ],
            page: { totalElements: 2 },
        });
    });
});

describe('buildHref', () => {
    beforeEach(() => {
        getEnvApiUrlMock.mockReset();
    });

    it('buildHref_quanLaBaseAcabaSenseBarra_construeixUnaUrlAmbSeparador', () => {
        // Verifica que la utilitat insereix la barra separadora si la base no la porta.
        getEnvApiUrlMock.mockReturnValue('https://api.example.com/api');

        expect(buildHref('users/findAll')).toBe('https://api.example.com/api/users/findAll');
    });

    it('buildHref_quanEsUtilsPath_eliminaElSuffixApiDeLaBase', () => {
        // Comprova que la variant utilsPath elimina el sufix /api abans d'afegir el fragment.
        getEnvApiUrlMock.mockReturnValue('https://api.example.com/api');

        expect(buildHref('utils/ping', true)).toBe('https://api.example.com/utils/ping');
    });

    it('buildHref_quanLaBaseJaAcabaAmbBarra_noDuplicaElSeparador', () => {
        // Verifica que no es dupliquen barres quan la base ja ve normalitzada.
        getEnvApiUrlMock.mockReturnValue('https://api.example.com/base/');

        expect(buildHref('users')).toBe('https://api.example.com/base/users');
    });
});
