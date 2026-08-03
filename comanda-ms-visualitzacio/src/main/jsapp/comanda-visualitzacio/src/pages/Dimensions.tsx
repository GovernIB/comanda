import * as React from 'react';
import { useState, useEffect } from "react";
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import {
    MuiDataGrid,
    MuiDataGridColDef,
    springFilterBuilder,
    MuiFilter,
    FormField,
    useFilterApiRef,
    useResourceApiService, useBaseAppContext, useMuiDataGridApiRef, useConfirmDialogButtons, useMuiFormDialogApiRef
} from 'reactlib';
import PageTitle from '../components/PageTitle.tsx';
import FormActionDialog from "../components/FormActionDialog.tsx";

const useChangeTipus = (refresh?: () => void, addConstToFet?: (id:any) => void) => {
    const { t } = useTranslation();
    const apiRef = useMuiFormDialogApiRef();
    const { temporalMessageShow, messageDialogShow } = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const confirmDialogComponentProps = { maxWidth: 'sm', fullWidth: true };

    const handleShow = (id:any, row:any) :void => {
        apiRef.current?.show?.(id, {entornAppId: row.entornAppId})
    }
    const onSuccess = (response:any) :void => {
        refresh?.()
        temporalMessageShow(null, t($ => $.page.dimensions.action.changeTipus.ok), 'success')
        if (response.tipus == 'ORGAN_GESTOR') {
            messageDialogShow(
                t($ => $.page.dimensions.action.refreshCons.title),
                '',
                confirmDialogButtons,
                confirmDialogComponentProps
            )
                .then((value: any) => {
                    if (value) {
                        addConstToFet?.(response.id)
                    }
                })
        }
    }
    return {
        handleShow,
        content: <FormActionDialog
            resourceName={"dimensio"}
            title={'Cambiar tipus dimensió'}
            action={"CHANGE_TIPUS"}
            apiRef={apiRef}
            onSuccess={onSuccess}
        >
            <FormField name={'tipus'} hiddenEnumValues={["CONSELLERIA"]} required/>
        </FormActionDialog>
    }
}

type DimensionsFilterProps = { onSpringFilterChange: (springFilter?: string) => void };
const DimensionsFilter = (props: DimensionsFilterProps) => {
    const { onSpringFilterChange } = props;
    const { t } = useTranslation();
    const { isReady: entornAppApiIsReady, find: entornAppGetAll } = useResourceApiService('entornApp');
    const filterApiRef = useFilterApiRef();
    type EntornAppItem = { id: string | number; entornAppDescription?: string };
    const [entornApp, setEntornApp] = useState<EntornAppItem[] | null>([]);

    // Al obrir la pàgina carreguem el llistat de EntornApp actius
    useEffect(() => {
        if (entornAppApiIsReady) {
            console.log('EntornApp API ready');
            entornAppGetAll({
                unpaged: true,
                filter: 'activa : true AND app.activa : true',
            }).then(response => {
                console.log('EntornApp API response received:', response.rows.length, 'items');
                setEntornApp(response.rows);
            });
        }
    }, [entornAppApiIsReady, entornAppGetAll]);

    const netejar = () => {
        filterApiRef?.current?.clear();
    }

    return (
        <MuiFilter
            apiRef={filterApiRef}
            // TODO Si el filtre no conte cap camp del modul de dimensions, considerar canviar el filtre per un al recurs de entornApp,
            // de manera que no s'hagi de fer la petició manualment del llistat de entornApp
            resourceName="dimensio"
            code="dimensioFilter"
            commonFieldComponentProps={{ size: 'small' }}
            onSpringFilterChange={onSpringFilterChange}
            springFilterBuilder={data => {
                // Build Spring filter based on available fields in the artifact
                // Fallback to empty if no values provided
                return springFilterBuilder.and(
                    data?.entornApp && springFilterBuilder.eq('entornAppId', data?.entornApp?.id ?? data?.entornApp),
                    data?.codi && springFilterBuilder.like('codi', data?.codi),
                    data?.nom && springFilterBuilder.like('nom', data?.nom),
                ) || '';
            }}>
            <Box sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={4}>
                        <FormField
                            name={'entornApp'}
                            type={'reference'}
                            label={t($ => $.page.dimensions.column.entornApp)}
                            required={false}
                            optionsRequest={(q: string) => {
                                const opts = (entornApp ?? []).map((ea: EntornAppItem) => ({
                                    id: ea?.id,
                                    description: ea.entornAppDescription,
                                }));
                                const filtered = q
                                    ? opts.filter(o => o.description?.toLowerCase().includes(q.toLowerCase()))
                                    : opts;
                                return Promise.resolve({ options: filtered });
                            }}
                            componentProps={{ disabled: (entornApp ?? []).length === 0 }}
                        />
                    </Grid>
                    <Grid size={4}><FormField name={'codi'} /></Grid>
                    <Grid size={4}><FormField name={'nom'} /></Grid>
                </Grid>
                <IconButton
                    onClick={netejar}
                    title={t($ => $.components.clear)}
                    sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
    );
};

const Dimensions: React.FC = () => {
    const { t } = useTranslation();
    const [filter, setFilter] = React.useState<string | undefined>(springFilterBuilder.eq('entornAppId', 0));

    const { artifactAction: apiAction } = useResourceApiService('dimensio');
    const { temporalMessageShow } = useBaseAppContext();

    const columns: MuiDataGridColDef[] = [
        { field: 'codi', flex: 1 },
        { field: 'nom', flex: 2 },
        { field: 'descripcio', flex: 4 },
        { field: 'tipus', flex: 2 },
        // { field: 'agrupableCount', headerName: t('page.dimensions.column.agrupacions'), flex: 1 },
    ];
    const filterElement = <DimensionsFilter onSpringFilterChange={setFilter}/>;

    const gridApiRef = useMuiDataGridApiRef();
    const refresh = () => {
        gridApiRef?.current?.refresh?.();
    }

    const addConstToFet = (id:any) => {
        apiAction(id, {code: 'FET_CONS'})
            .then(() => {
                refresh()
                temporalMessageShow(null, t($ => $.page.dimensions.action.refreshCons.ok), 'success')
            })
            .catch(error => temporalMessageShow(null, error.message, 'error'))
    }
    const clearTipus = (id:any) => {
        apiAction(id, {code: 'CHANGE_TIPUS', data: {tipus: null}})
            .then(() => {
                refresh()
                temporalMessageShow(null, t($ => $.page.dimensions.action.desmarcar.ok), 'success')
            })
            .catch(error => temporalMessageShow(null, error.message, 'error'))
    }

    const {handleShow, content} = useChangeTipus(refresh, addConstToFet);

    return (
        <>
            <PageTitle title={t($ => $.page.dimensions.title)} />
            <MuiDataGrid
                apiRef={gridApiRef}
                title={t($ => $.page.dimensions.title)}
                resourceName="dimensio"
                columns={columns}
                toolbarType="upper"
                paginationActive
                toolbarHideQuickFilter
                toolbarAdditionalRow={filterElement}
                filter={filter}
                rowAdditionalActions={[
                    {
                        label: t($ => $.page.dimensions.action.refreshCons.label),
                        icon: 'refresh',
                        showInMenu: false,
                        action: 'FET_CONS',
                        onClick: addConstToFet,
                        // hidden: (row) => !row?.tipus || row?.tipus == 'CONSELLERIA'
                        hidden: (row) => row?.tipus != 'ORGAN_GESTOR'
                    },
                    {
                        label: t($ => $.page.dimensions.action.changeTipus.label),
                        icon: 'check_box_outline_blank',
                        showInMenu: false,
                        action: 'CHANGE_TIPUS',
                        onClick: handleShow,
                        hidden: (row) => row?.tipus
                    },
                    {
                        label: t($ => $.page.dimensions.action.desmarcar.label),
                        icon: 'check_box',
                        showInMenu: false,
                        action: 'CHANGE_TIPUS',
                        onClick: clearTipus,
                        disabled: (row) => row?.tipus == 'CONSELLERIA',
                        hidden: (row) => !row?.tipus,
                    },
                    {
                        label: t($ => $.page.dimensions.values),
                        icon: 'list',
                        linkTo: 'valor/{{id}}'
                    }
                ]}
                readOnly
            />
            {content}
        </>
    );
};

export default Dimensions;
