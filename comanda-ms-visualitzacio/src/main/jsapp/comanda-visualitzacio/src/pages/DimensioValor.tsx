import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Autocomplete from '@mui/material/Autocomplete';
import {
    MuiDataGrid,
    MuiDataGridColDef,
    springFilterBuilder,
    FormField,
    MuiFilter,
    MuiDialog,
    useFilterApiRef,
    useResourceApiService,
    useBaseAppContext, useMuiDataGridApiRef,
} from 'reactlib';
import PageTitle from '../components/PageTitle.tsx';
import useReadOnlyGestor from '../hooks/useReadOnlyGestor.ts';
import { useOrganigramaDialog } from '../components/EntitatOrganigrama.tsx';

const UO_ESTATS = ['V', 'E', 'A', 'T'];

/**
 * Edita/mapeja el recurs real vinculat a un valor de dimensió (Entitat o UnitatOrganitzativa), tenint en compte
 * quin és el tipus de la dimensió. Per a dimensions ENTITAT sense cap Entitat resolta (mapatge manual pendent),
 * permet triar-ne una d'existent en lloc de mostrar-ne els camps.
 */
const useEditVinculatDialog = (dimension: any, refresh?: () => void) => {
    const { t } = useTranslation();
    const { temporalMessageShow } = useBaseAppContext();
    const { getOne: getOneDimensioValor, update: updateDimensioValor } = useResourceApiService('dimensioValor');
    const { getOne: getOneEntitat, update: updateEntitat, find: findEntitat } = useResourceApiService('entitat');
    const { getOne: getOneUO, update: updateUO } = useResourceApiService('unitatOrganitzativa');

    const [state, setState] = React.useState<
        | { kind: 'entitat'; entitat: any }
        | { kind: 'unitatOrganitzativa'; uo: any }
        | { kind: 'mapeja'; dimensioValorId: any; entitatMapejadaId: any }
        | null
    >(null);
    const [entitatOptions, setEntitatOptions] = React.useState<any[]>([]);

    const handleOpen = (id: any) => {
        getOneDimensioValor(id).then((dv: any) => {
            if (dimension?.tipus === 'ENTITAT') {
                if (dv?.entitat?.id != null) {
                    getOneEntitat(dv.entitat.id).then((e: any) => setState({ kind: 'entitat', entitat: e }));
                } else {
                    findEntitat({ unpaged: true }).then((response: any) => setEntitatOptions(response?.rows ?? []));
                    setState({ kind: 'mapeja', dimensioValorId: id, entitatMapejadaId: null });
                }
            } else if (dv?.unitatOrganitzativa?.id != null) {
                getOneUO(dv.unitatOrganitzativa.id).then((u: any) => setState({ kind: 'unitatOrganitzativa', uo: u }));
            }
        });
    };

    const handleClose = () => setState(null);

    const handleSave = () => {
        if (!state) return;
        const promise = state.kind === 'entitat'
            ? updateEntitat(state.entitat.id, { data: state.entitat })
            : state.kind === 'unitatOrganitzativa'
                ? updateUO(state.uo.id, { data: state.uo })
                : updateDimensioValor(state.dimensioValorId, { data: { entitatMapejada: state.entitatMapejadaId } });

        promise
            .then(() => {
                setState(null);
                refresh?.();
                temporalMessageShow(null, t($ => $.page.dimensions.action.editar.ok), 'success');
            })
            .catch((error: any) => temporalMessageShow(null, error.message, 'error'));
    };

    const dialog = (
        <MuiDialog
            open={state != null}
            closeCallback={handleClose}
            title={t($ => $.page.dimensions.action.editar.dialogTitle)}
            componentProps={{ fullWidth: true, maxWidth: 'sm' }}
        >
            {state?.kind === 'entitat' && (
                <Grid container spacing={2} sx={{ mt: 1 }}>
                    <Grid size={12}>
                        <TextField
                            fullWidth
                            label={t($ => $.page.dimensions.editaEntitat.field.codi)}
                            value={state.entitat.codi ?? ''}
                            onChange={(e) => setState({ kind: 'entitat', entitat: { ...state.entitat, codi: e.target.value } })}
                        />
                    </Grid>
                    <Grid size={12}>
                        <TextField
                            fullWidth
                            label={t($ => $.page.dimensions.editaEntitat.field.nom)}
                            value={state.entitat.nom ?? ''}
                            onChange={(e) => setState({ kind: 'entitat', entitat: { ...state.entitat, nom: e.target.value } })}
                        />
                    </Grid>
                    <Grid size={12}>
                        <TextField
                            fullWidth
                            label={t($ => $.page.dimensions.editaEntitat.field.codiDir3)}
                            value={state.entitat.codiDir3 ?? ''}
                            onChange={(e) => setState({ kind: 'entitat', entitat: { ...state.entitat, codiDir3: e.target.value } })}
                        />
                    </Grid>
                    <Grid size={12}>
                        <TextField
                            fullWidth
                            label={t($ => $.page.dimensions.editaEntitat.field.cif)}
                            value={state.entitat.cif ?? ''}
                            onChange={(e) => setState({ kind: 'entitat', entitat: { ...state.entitat, cif: e.target.value } })}
                        />
                    </Grid>
                </Grid>
            )}
            {state?.kind === 'unitatOrganitzativa' && (
                <Grid container spacing={2} sx={{ mt: 1 }}>
                    <Grid size={12}>
                        <TextField
                            fullWidth
                            label={t($ => $.page.dimensions.editaUnitatOrganitzativa.field.codi)}
                            value={state.uo.codi ?? ''}
                            onChange={(e) => setState({ kind: 'unitatOrganitzativa', uo: { ...state.uo, codi: e.target.value } })}
                        />
                    </Grid>
                    <Grid size={12}>
                        <TextField
                            fullWidth
                            label={t($ => $.page.dimensions.editaUnitatOrganitzativa.field.denominacio)}
                            value={state.uo.denominacio ?? ''}
                            onChange={(e) => setState({ kind: 'unitatOrganitzativa', uo: { ...state.uo, denominacio: e.target.value } })}
                        />
                    </Grid>
                    <Grid size={12}>
                        <TextField
                            fullWidth
                            select
                            slotProps={{ select: { native: true } }}
                            label={t($ => $.page.dimensions.editaUnitatOrganitzativa.field.estat)}
                            value={state.uo.estat ?? ''}
                            onChange={(e) => setState({ kind: 'unitatOrganitzativa', uo: { ...state.uo, estat: e.target.value } })}
                        >
                            {UO_ESTATS.map((estat) => (
                                <option key={estat} value={estat}>
                                    {t(($ => ($.page.dimensions.editaUnitatOrganitzativa.estatOptions as any)[estat]))}
                                </option>
                            ))}
                        </TextField>
                    </Grid>
                </Grid>
            )}
            {state?.kind === 'mapeja' && (
                <Grid container spacing={2} sx={{ mt: 1 }}>
                    <Grid size={12}>
                        <Autocomplete
                            options={entitatOptions}
                            getOptionLabel={(o: any) => o.nom ?? o.codi ?? ''}
                            onChange={(_event, value: any) => setState({ kind: 'mapeja', dimensioValorId: state.dimensioValorId, entitatMapejadaId: value?.id ?? null })}
                            renderInput={(params) => (
                                <TextField {...params} label={t($ => $.page.dimensions.mapejaEntitat.field.entitat)} />
                            )}
                        />
                    </Grid>
                </Grid>
            )}
            {state != null && (
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
                    <Button onClick={handleSave} variant="contained">{t($ => $.page.dimensions.action.editar.save)}</Button>
                </Box>
            )}
        </MuiDialog>
    );

    return { handleOpen, dialog };
};

const DimensioValorFilter: React.FC<{ onSpringFilterChange?: (f?: string) => void; onDataChange?: (f?: any) => void }> = ({ onSpringFilterChange, onDataChange }) => {
    const { t } = useTranslation();
    const filterApiRef = useFilterApiRef();

    const netejar = () => {
        filterApiRef?.current?.clear();
    };

    return (
        <MuiFilter
            apiRef={filterApiRef}
            resourceName="dimensioValor"
            code="dimensioValorFilter"
            commonFieldComponentProps={{ size: 'small' }}
            onSpringFilterChange={onSpringFilterChange}
            springFilterBuilder={(data) => {
                onDataChange?.(data)
                return springFilterBuilder.and(
                    data?.valor && springFilterBuilder.like('valor', data?.valor),
                    // data?.agrupable != null && springFilterBuilder.eq('agrupable', data?.agrupable),
                    // data?.valorAgrupacio && springFilterBuilder.like('valorAgrupacio', data?.valorAgrupacio),
                ) || '';
            }}
        >
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={{ xs: 12, md: 4 }}><FormField name={'valor'} /></Grid>
                    {/*<Grid size={{ xs: 12, md: 4 }}><FormField name={'agrupable'} type={'checkbox'} /></Grid>*/}
                    {/*<Grid size={{ xs: 12, md: 4 }}><FormField name={'valorAgrupacio'} /></Grid>*/}
                </Grid>
                <IconButton onClick={netejar} title={t($ => $.components.clear)} sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
    );
};

const DimensioValor: React.FC = () => {
    const { t } = useTranslation();
    const gestorReadOnly = useReadOnlyGestor();
    const { id } = useParams();
    const { goBack, anyHistoryEntryExist } = useBaseAppContext();
    const { isReady, getOne: getDimensio } = useResourceApiService('dimensio');
    const { artifactAction: apiAction, getOne: getOneDimensioValor } = useResourceApiService('dimensioValor');
    const { artifactAction: entitatApiAction, getOne: getOneEntitat } = useResourceApiService('entitat');
    const { temporalMessageShow } = useBaseAppContext();

    const [dimension, setDimension] = React.useState<any>();

    React.useEffect(() => {
        if (id && isReady) {
            getDimensio(id as string).then((d: { nom?: string; description?: string } | null) => setDimension(d));
        }
    }, [id, isReady, getDimensio]);

    const columns: MuiDataGridColDef[] = [
        { field: 'codiNom', flex: 2 },
        // { field: 'agrupable', flex: 1 },
        // { field: 'valorAgrupacio', flex: 2 },
    ];

    const fixedFilter = React.useMemo(() => springFilterBuilder.eq('dimensio.id', id), [id]);

    const toolbarElementsWithPositions = React.useMemo(() => {
        const backButtonDisabled = !anyHistoryEntryExist();
        return [
            {
                position: 0,
                element: (
                    <IconButton
                        onClick={() => goBack('/dimensio')}
                        disabled={backButtonDisabled}
                        sx={{ mr: 1 }}
                    >
                        <Icon>arrow_back</Icon>
                    </IconButton>
                ),
            },
        ];
    }, [anyHistoryEntryExist, goBack, t]);

    const [quickfilter, setQuickfilter] = React.useState<string | undefined>();
    const filterElement = <DimensioValorFilter onDataChange={(data) => setQuickfilter(data.valor) } />;
    const namedQueries = React.useMemo(() => {
        const queries: string[] = [];
        if (quickfilter) queries.push(`filterByUONom:${quickfilter}`);
        return queries.length > 0 ? queries : undefined;
    }, [quickfilter]);

    const gridTitle = t($ => $.page.dimensions.valuesTitle, { nom: dimension?.nom ?? dimension?.description ?? '' });

    const gridApiRef = useMuiDataGridApiRef();
    const refresh = () => {
        gridApiRef?.current?.refresh?.();
    }

    const { handleOpen: handleEditaOpen, dialog: editaDialog } = useEditVinculatDialog(dimension, refresh);
    const { handleOpen: handleOrganigramaOpen, dialog: organigramaDialog } = useOrganigramaDialog();

    const refreshUOEntitat = (id: any) => {
        getOneDimensioValor(id)
            .then((dv: any) => {
                const entitatId = dv?.entitat?.id;
                if (entitatId == null) return;
                return entitatApiAction(entitatId, { code: 'REFRESH_UO' })
                    .then(() => temporalMessageShow(null, t($ => $.page.entitats.action.refreshUO.ok), 'success'));
            })
            .catch((error: any) => temporalMessageShow(null, error.message, 'error'));
    };

    const showOrganigrama = (id: any) => {
        getOneDimensioValor(id)
            .then((dv: any) => {
                const entitatId = dv?.entitat?.id;
                if (entitatId == null) {
                    temporalMessageShow(null, t($ => $.page.entitats.action.organigrama.ko), 'error');
                    return;
                }
                return getOneEntitat(entitatId).then((e: any) => handleOrganigramaOpen(entitatId, e?.codiDir3));
            })
            .catch((error: any) => temporalMessageShow(null, error.message, 'error'));
    };

    return (
        <>
            <PageTitle title={gridTitle} />
            <MuiDataGrid
                apiRef={gridApiRef}
                title={gridTitle}
                resourceName="dimensioValor"
                columns={columns}
                toolbarType="upper"
                paginationActive
                toolbarHideQuickFilter
                toolbarAdditionalRow={filterElement}
                toolbarElementsWithPositions={toolbarElementsWithPositions}
                toolbarHideCreate
                fixedFilter={fixedFilter}
                namedQueries={namedQueries}
                popupEditActive={false}
                rowHideDeleteButton={gestorReadOnly}
                rowAdditionalActions={[
                    {
                        label: t($ => $.page.dimensions.action.sincronitzar.label),
                        icon: 'refresh',
                        showInMenu: false,
                        action: 'UO_DIR3',
                        onClick: (id) => {
                            apiAction(id, {code: 'UO_DIR3'})
                                .then(() => {
                                    refresh()
                                    temporalMessageShow(null, t($ => $.page.dimensions.action.sincronitzar.ok), 'success')
                                })
                                .catch(error => temporalMessageShow(null, error.message, 'error'))
                        },
                        hidden: !dimension?.tipus || (dimension?.tipus != 'ORGAN_GESTOR' && dimension?.tipus != 'CONSELLERIA')
                    },
                    {
                        label: t($ => $.page.dimensions.action.editar.label),
                        icon: 'edit',
                        showInMenu: false,
                        onClick: handleEditaOpen,
                        // Per a dimensions ENTITAT, l'edició directa només té sentit quan el mapeig és MANUAL:
                        // amb CODI/CODI_DIR3/NOM l'entitat es resol/crea automàticament a partir del valor.
                        hidden: dimension?.tipus !== 'ENTITAT' || dimension?.entitatValorTipus !== 'MANUAL'
                    },
                    {
                        label: t($ => $.page.entitats.action.refreshUO.label),
                        icon: 'refresh',
                        showInMenu: true,
                        // No especifiquem "action" aquí: REFRESH_UO és una acció registrada al recurs 'entitat',
                        // no a 'dimensioValor' (el resourceName d'aquest MuiDataGrid), i MuiDataGrid amagaria
                        // l'entrada silenciosament en comprovar-la contra els artifacts equivocats.
                        onClick: refreshUOEntitat,
                        hidden: dimension?.tipus !== 'ENTITAT'
                    },
                    {
                        label: t($ => $.page.entitats.action.organigrama.label),
                        icon: 'list',
                        showInMenu: true,
                        onClick: showOrganigrama,
                        hidden: dimension?.tipus !== 'ENTITAT'
                    }
                ]}
            />
            {editaDialog}
            {organigramaDialog}
        </>
    );
};

export default DimensioValor;
