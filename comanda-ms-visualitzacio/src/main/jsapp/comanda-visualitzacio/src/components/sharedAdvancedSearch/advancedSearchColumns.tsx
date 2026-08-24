import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { TFunction } from 'i18next';
import { MuiDataGridColDef } from 'reactlib';

export const columnesIndicador: MuiDataGridColDef[] = [
    {
        field: 'codi',
        flex: 1,
    },
    {
        field: 'nom',
        flex: 2,
    },
    {
        field: 'descripcio',
        flex: 3,
    },
];

export const columnesDimensio: MuiDataGridColDef[] = [
    {
        field: 'codi',
        flex: 1,
    },
    {
        field: 'nom',
        flex: 2,
    },
    {
        field: 'descripcio',
        flex: 3,
    },
    {
        field: 'entornAppId',
        flex: 1,
    },
];

export const getColumnesDimensioValor = (t: TFunction): MuiDataGridColDef[] => [
    {
        field: 'codiNom',
        flex: 1,
    },
    {
        field: 'dimensio.description',
        headerName: t($ => $.generic.dimensio),
        flex: 2,
    },
];

export const useColumnesDimensioValor = (): MuiDataGridColDef[] => {
    const { t } = useTranslation();
    return useMemo(() => getColumnesDimensioValor(t), [t]);
};

