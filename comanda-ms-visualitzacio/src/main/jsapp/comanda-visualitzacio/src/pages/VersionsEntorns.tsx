import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { GridPage, MuiDataGrid, MuiDataGridColDef, useResourceApiService } from 'reactlib';
import { Chip, Stack, Tooltip } from '@mui/material';
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
    const [entorns, setEntorns] = React.useState<any[]>([]);
    React.useEffect(() => {
        if (entornApiIsReady)
            entornApiFind({
                unpaged: true,
            }).then((response) => setEntorns(response.rows));
    }, [entornApiFind, entornApiIsReady]);
    const columns = React.useMemo(() => {
        const columns: MuiDataGridColDef[] = [
            {
                field: 'nom',
                flex: 2,
                minWidth: 150,
            },
        ];
        entorns.forEach((entorn) => {
            columns.push({
                field: entorn.codi,
                headerName: `${entorn.codi} (${entorn.nom})`,
                sortable: false,
                flex: 1,
                minWidth: 165,
                valueGetter: (_value, row) =>
                    row.entornApps?.find((entornApp: any) => entornApp.entorn.id === entorn.id)?.versio,
                renderCell: ({ formattedValue: versioValue, row }) => {
                    const entornApp = row.entornApps?.find((ea: any) => ea.entorn.id === entorn.id);
                    const revisioValue = entornApp?.revisio;
                    const versioColor = versioValue != null
                        ? (() => {
                            const sortedVersions = toSortedVersions(
                                row.entornApps.map((ea: any) => ea.versio).filter(Boolean)
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
    }, [entorns]);

    return (
        <GridPage disableMargins>
            <PageTitle title={t($ => $.page.versionsEntorns.title)} />
            <MuiDataGrid
                title={t($ => $.page.versionsEntorns.title)}
                resourceName="app"
                perspectives={["ENTORN_APPS"]}
                columns={columns}
                readOnly
                toolbarType="upper"
                paginationActive
            />
        </GridPage>
    );
};

export default Entorns;
