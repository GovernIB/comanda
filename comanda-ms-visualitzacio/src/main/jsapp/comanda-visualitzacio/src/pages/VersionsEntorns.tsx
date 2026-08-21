import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { MuiDataGrid, MuiDataGridColDef, useResourceApiService } from 'reactlib';
import { Chip, Skeleton, Stack, Tooltip } from '@mui/material';
import PageTitle from '../components/PageTitle.tsx';
import { truncateHashRevisio } from './salut/dataFetching.ts';

const toSortedVersions = (versions: string[]) =>
    versions
        .map((val) => ({
            unformatted: val,
            formatted: val != null ? val.split(/(\D)/) : [''],
        }))
        .map(({ unformatted, formatted }) => ({
            unformatted,
            formatted: formatted.map((formattedPart) =>
                /^\d+$/.test(formattedPart) ? parseInt(formattedPart) : formattedPart
            ),
        }))
        .sort(({ formatted: formattedA }, { formatted: formattedB }) => {
            for (let i = 0; i < Math.min(formattedA.length, formattedB.length); i++) {
                if (formattedA[i] === formattedB[i]) continue;

                if (typeof formattedA[i] === 'number' && typeof formattedB[i] !== 'number')
                    return -1;
                else if (typeof formattedA[i] !== 'number' && typeof formattedB[i] === 'number')
                    return 1;
                else if (typeof formattedA[i] !== 'number' && typeof formattedB[i] !== 'number')
                    return (formattedB[i] as string).localeCompare(formattedA[i] as string);
                else return (formattedB[i] as number) - (formattedA[i] as number);
            }
            return formattedA.length - formattedB.length; // Handles cases where arrays differ in length
        });

const Entorns: React.FC = () => {
    const { t } = useTranslation();
    const { isReady: entornApiIsReady, find: entornApiFind } = useResourceApiService('entorn');
    const [entorns, setEntorns] = React.useState<Array<{ id: number | string; codi: string; nom: string }>>();
    React.useEffect(() => {
        if (entornApiIsReady)
            entornApiFind({
                unpaged: true,
            }).then((response) => setEntorns(response.rows));
    }, [entornApiFind, entornApiIsReady]);

    const { isReady: entornAppApiIsReady, find: entornAppApiFind } = useResourceApiService('entornApp');
    const [entornApps, setEntornApps] = React.useState<Array<{
        id?: number | string;
        app?: { id?: number | string };
        entorn?: { id?: number | string };
        versio?: string;
        revisio?: string;
    }>>();
    React.useEffect(() => {
        if (entornAppApiIsReady)
            entornAppApiFind({
                unpaged: true,
            }).then((response) => setEntornApps(response.rows));
    }, [entornAppApiFind, entornAppApiIsReady]);

    const columns = React.useMemo(() => {
        const columns: MuiDataGridColDef[] = [
            {
                field: 'nom',
                flex: 2,
                minWidth: 150,
            },
        ];
        entorns?.forEach((entorn) => {
            columns.push({
                field: entorn.codi,
                headerName: `${entorn.codi} (${entorn.nom})`,
                sortable: false,
                flex: 1,
                minWidth: 165,
                valueGetter: (_value, row) =>
                    entornApps?.find((ea) => ea.app?.id === row.id && ea.entorn?.id === entorn.id)?.versio,
                renderCell: ({ formattedValue: versioValue, row }) => {
                    if (entornApps == null) {
                        return <Skeleton variant="rounded" width={80} height={8} />;
                    }
                    const entornApp = entornApps.find((ea) => ea.app?.id === row.id && ea.entorn?.id === entorn.id);
                    const revisioValue = entornApp?.revisio;
                    const versioColor = versioValue != null
                        ? (() => {
                            const appEntornApps = entornApps.filter((ea) => ea.app?.id === row.id);
                            const sortedVersions = toSortedVersions(
                                appEntornApps.map((ea) => ea.versio).filter(Boolean) as string[]
                            );
                            return versioValue !== sortedVersions[0]?.unformatted ? 'warning' : 'success';
                        })()
                        : undefined;
                    return (
                        <Stack direction="row" spacing={0.5} alignItems="center" flexWrap="wrap">
                            {versioValue != null && (
                                <Chip label={versioValue} color={versioColor}/>
                            )}
                            {revisioValue != null && (
                                <Tooltip title={revisioValue} arrow placement="top">
                                    <Chip
                                        label={truncateHashRevisio(revisioValue)}
                                        size="small"
                                        sx={{ bgcolor: 'info.light', color: 'info.contrastText', }}
                                    />
                                </Tooltip>
                            )}
                        </Stack>
                    );
                },
            });
        });
        return columns;
    }, [entorns, entornApps]);

    return (
        <>
            <PageTitle title={t($ => $.page.versionsEntorns.title)} />
            <MuiDataGrid
                title={t($ => $.page.versionsEntorns.title)}
                resourceName="app"
                columns={columns}
                readOnly
                toolbarType="upper"
                paginationActive
                toolbarHideRefresh
            />
        </>
    );
};

export default Entorns;
