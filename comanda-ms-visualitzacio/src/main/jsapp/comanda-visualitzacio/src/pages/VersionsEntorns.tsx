import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { GridPage, MuiGrid, MuiDataGridColDef, useResourceApiService } from 'reactlib';
import { Chip } from '@mui/material';

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
                minWidth: 300,
            },
        ];
        entorns.forEach((entorn) => {
            columns.push({
                field: entorn.codi,
                headerName: `${entorn.codi} (${entorn.nom})`,
                sortable: false,
                flex: 1,
                minWidth: 150,
                valueGetter: (_value, row) =>
                    row.entornApps?.find((entornApp: any) => entornApp.entorn.id === entorn.id)
                        ?.versio,
                renderCell: ({ formattedValue, row }) => {
                    if (formattedValue != null) {
                        const sortedVersions = toSortedVersions(
                            row.entornApps.map((entornApp: any) => entornApp.versio)
                        );
                        return (
                            <Chip
                                label={formattedValue}
                                color={
                                    formattedValue !== sortedVersions[0].unformatted
                                        ? 'warning'
                                        : 'success'
                                }
                            />
                        );
                    }
                },
            });
        });
        return columns;
    }, [entorns]);

    return (
        <GridPage disableMargins>
            <MuiGrid
                title={t('page.versionsEntorns.title')}
                resourceName="app"
                columns={columns}
                readOnly
                toolbarType="upper"
                paginationActive
            />
        </GridPage>
    );
};

export default Entorns;
