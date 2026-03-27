import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardHeader from '@mui/material/CardHeader';
import CardContent from '@mui/material/CardContent';
import Checkbox from '@mui/material/Checkbox';
import Skeleton from '@mui/material/Skeleton';
import FormControlLabel from '@mui/material/FormControlLabel';
import {
    FormPage,
    GridPage,
    MuiDataGrid,
    MuiForm,
    MuiFilter,
    FormField,
    useFormApiRef,
    useResourceApiService,
    useBaseAppContext,
    useConfirmDialogButtons,
    useMuiDataGridApiRef,
    springFilterBuilder,
    MuiDataGridColDef,
    useFilterApiRef,
} from 'reactlib';
import { Box, Button, Icon, IconButton } from '@mui/material';
import { useIsUserAdmin, useUserContext } from '../components/UserContext';
import CenteredCircularProgress from '../components/CenteredCircularProgress.tsx';

export const EntornAppSelector : React.FC<any> = (props) => {
    const { id, onEntornAppChange, validationErrors } = props;
    const formApiRef = useFormApiRef();
    const { isReady: apiIsReady, getOne: apiGetOne } = useResourceApiService('entornApp');
    const [entornApp, setEntornApp] = React.useState<any>();
    React.useEffect(() => {
        if (apiIsReady && id != null) {
            apiGetOne(id).then(value => {
                const entornApp = { id: value.id, description: value.entornAppDescription };
                setEntornApp(entornApp)
                formApiRef.current.setFieldValue('entornApp', entornApp);
            });
        }
    }, [apiIsReady, id]);
    return (!id || entornApp != null) ? <MuiFilter
        resourceName="entornApp"
        code="salut_entornApp_filter"
        commonFieldComponentProps={{ size: 'small' }}
        validationErrors={validationErrors}
        initialData={{ entornApp: entornApp }}
        springFilterBuilder={() => ''}
        onDataChange={data => onEntornAppChange(data?.entornApp)}
        componentProps={{
            margin: '0 !important',
        }}
        formApiRef={formApiRef}>
            <FormField
            name="entornApp"
            filter={springFilterBuilder.and(
                springFilterBuilder.eq('activa', true),
                springFilterBuilder.eq('app.activa', true),
            )}
            advancedSearchResourceName="entornApp"
            advancedSearchColumns={[{
                field: 'entornAppDescription',
                flex: 1,
            }]} />
    </MuiFilter> : <Skeleton height={'100%'}/>;
}

export const AlarmaConfigForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const formApiRef = useFormApiRef();
    const [entornAppId, setEntornAppId] = React.useState<any>();
    const [validationErrors, setValidationErrors] = React.useState<any>();
    const [condicioValorDisabled, setCondicioValorDisabled] = React.useState<boolean>(true);
    const [periodeShow, setPeriodeShow] = React.useState<boolean>();
    const isCurrentUserAdmin = useIsUserAdmin();
    const handleDataChange = (data: any) => {
        setEntornAppId(data?.entornAppId);
        const condicioValorDisabled = data?.tipus !== 'APP_LATENCIA';
        setCondicioValorDisabled(condicioValorDisabled);
        if (periodeShow === undefined) {
            const ps = data?.periodeValor != null || data?.periodeUnitat != null;
            setPeriodeShow(ps);
        }
    }
    const handleValidationErrorsChange = (_id: any, validationErrors?: any[]) => {
        const entornAppValidationError = validationErrors?.find(e => e.field === 'entornAppId');
        setValidationErrors(entornAppValidationError ? [{
            field: 'entornApp',
            code: entornAppValidationError.code,
            message: entornAppValidationError.message,
        }] : null);
    }
    const handleEntornAppChange = (entornApp: any) => {
        formApiRef.current.setFieldValue('entornAppId', entornApp?.id);
    }
    const handlePeriodeShowChange = (event: any) => {
        const newValue = event.target.checked;
        setPeriodeShow(newValue);
        if (!newValue) {
            formApiRef.current?.setFieldValue('periodeValor', null);
            formApiRef.current?.setFieldValue('periodeUnitat', null);
        } 
    }

    // const {goBack} = useBaseAppContext();
    // const afterDelete = () => {
    //     goBack("/alarma")
    // }

    // const {apiIsReady, apiDelete, tLib} = useAlarmaConfigAction(afterDelete)
    // // const elementsWithPositions = React.useMemo(() => [
    // //     {
    // //         position: 3,
    // //         element: toToolbarIcon('delete', {
    // //             title: tLib('form.delete.title'),
    // //             onClick: () => apiDelete(id),
    // //         }),
    // //     }
    // // ], [apiIsReady, tLib]);

    return (
        <FormPage>
            <MuiForm
                id={id}
                title={id ? t($ => $.page.alarmaConfig.update) : t($ => $.page.alarmaConfig.create)}
                resourceName="alarmaConfig"
                goBackLink="/alarma"
                createLink="form/{{id}}"
                apiRef={formApiRef}
                onDataChange={handleDataChange}
                hiddenDeleteButton
                // toolbarElementsWithPositions={elementsWithPositions}
                onValidationErrorsChange={handleValidationErrorsChange}>
                <Grid container spacing={2}>
                    <Grid size={3}>
                        <EntornAppSelector
                            id={entornAppId}
                            onEntornAppChange={handleEntornAppChange}
                            validationErrors={validationErrors} />
                    </Grid>
                    <Grid size={9}>
                        <FormField name="nom" componentProps={{ title: t($ => $.page.alarmaConfig.nomHelperText) }} />
                    </Grid>
                    <Grid size={12}>
                        <Card variant="outlined">
                            <CardHeader
                                title={t($ => $.page.alarmaConfig.condicio.title)}
                                subheader={t($ => $.page.alarmaConfig.condicio.subtitle)}
                                slotProps={{
                                    title: { variant: 'h6' },
                                    subheader: { variant: 'subtitle2', sx: { color: 'text.secondary' } }
                                }}
                                sx={{ mb: -2 }} />
                            <CardContent>
                                <Grid container spacing={1}>
                                    <Grid size={3}>
                                        <FormField name="tipus" />
                                    </Grid>
                                    <Grid size={6}>
                                        <FormField name="condicio" disabled={condicioValorDisabled} />
                                    </Grid>
                                    <Grid size={3}>
                                        <FormField name="valor" disabled={condicioValorDisabled} />
                                    </Grid>
                                </Grid>
                            </CardContent>
                        </Card>
                    </Grid>
                    <Grid size={12}>
                        <FormField name="missatge" />
                    </Grid>
                    {isCurrentUserAdmin && (
                        <><Grid size={6}>
                            <FormField name="admin" />
                        </Grid>
                        <Grid size={6}>
                            <FormField name="correuGeneric" />
                        </Grid></>
                    )}
                    <Grid size={6}>
                        <FormControlLabel
                            control={<Checkbox size="small" checked={periodeShow ?? false} onChange={handlePeriodeShowChange}/>}
                            label={t($ => $.page.alarmaConfig.periode.switch)}
                            sx={{ ml: 1 }} />
                    </Grid>
                    {periodeShow && <Grid size={12}>
                        <Card variant="outlined">
                            <CardHeader
                                title={t($ => $.page.alarmaConfig.periode.title)}
                                subheader={t($ => $.page.alarmaConfig.periode.subtitle)}
                                slotProps={{
                                    title: { variant: 'h6' },
                                    subheader: { variant: 'subtitle2', sx: { color: 'text.secondary' } }
                                }}
                                sx={{ mb: -2 }} />
                            <CardContent>
                                <Grid container spacing={1}>
                                    <Grid size={3}>
                                        <FormField name="periodeValor" />
                                    </Grid>
                                    <Grid size={9}>
                                        <FormField name="periodeUnitat" />
                                    </Grid>
                                </Grid>
                            </CardContent>
                        </Card>
                    </Grid>}
                </Grid>
            </MuiForm>
        </FormPage>
    );
}

const useAlarmaConfigAction = (refresh?: () => void) => {
    const {
        isReady: apiIsReady,
        artifactAction,
    } = useResourceApiService("alarmaConfig")
    const {messageDialogShow, temporalMessageShow, t: tLib} = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const confirmDialogComponentProps = {maxWidth: 'sm', fullWidth: true};

    const apiDelete = (id:any) => {
        messageDialogShow(
            tLib('datacommon.delete.single.label'),
            tLib('datacommon.delete.single.confirm'),
            confirmDialogButtons,
            confirmDialogComponentProps)
            .then((value: any) => {
                if (value) {
                    artifactAction(id, { code: 'delete_alarmaConfig' })
                        .then(() => {
                            refresh?.()
                            temporalMessageShow(null, tLib('datacommon.delete.single.success'), 'success');
                        })
                        .catch((error) => {
                            temporalMessageShow(tLib('datacommon.delete.single.error'), error?.message, 'error');
                        })
                }
            })
    }

    return {
        tLib,
        apiIsReady,
        apiDelete,
    }
}

type AlarmaConfigFilterProps = {
    onSpringFilterChange: (springFilter?: string) => void,
    entornApps?: any[],
    showOnlyOwn: boolean,
    setShowOnlyOwn: (value: boolean) => void,
};
const AlarmaConfigFilter = (props: AlarmaConfigFilterProps) => {
    const { onSpringFilterChange, entornApps, showOnlyOwn, setShowOnlyOwn } = props;
    const { t } = useTranslation();
    const { user } = useUserContext();
    const isCurrentUserAdmin = useIsUserAdmin();
    const [moreFields, setMoreFields] = React.useState<boolean>(false);
    const filterApiRef = useFilterApiRef();
    const formApiRef = useFormApiRef();
    const netejar = () => {
        filterApiRef.current?.clear();
        formApiRef.current?.setFieldValue('showOnlyOwn', showOnlyOwn);
    };
    React.useEffect(() => {
        formApiRef.current?.setFieldValue('showOnlyOwn', showOnlyOwn);
    }, [showOnlyOwn]);

    return (
        <MuiFilter
            apiRef={filterApiRef}
            resourceName="alarmaConfig"
            code="alarmaConfig_filter"
            persistentState
            formApiRef={formApiRef}
            commonFieldComponentProps={{ size: 'small' }}
            initialData={{ showOnlyOwn: showOnlyOwn }}
            onSpringFilterChange={onSpringFilterChange}
            springFilterBuilder={data => {
                return springFilterBuilder.and(
                    data?.entornApp && springFilterBuilder.eq('entornAppId', data?.entornApp?.id ?? data?.entornApp),
                    data?.nom && springFilterBuilder.like('nom', data?.nom),
                    data?.tipus && springFilterBuilder.like('tipus', data?.tipus),
                    moreFields && data?.admin && springFilterBuilder.eq('admin', data?.admin),
                    moreFields && data?.correuGeneric && springFilterBuilder.eq('correuGeneric', data?.correuGeneric),
                    isCurrentUserAdmin && user?.codi && data?.showOnlyOwn && springFilterBuilder.eq('createdBy', `'${user.codi}'`),
                ) || '';
            }}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={{xs: 12, sm:6}}>
                        <FormField
                            name={'entornApp'}
                            type={'reference'}
                            label={t($ => $.page.alarmaConfig.filter.entornApp)}
                            required={false}
                            optionsRequest={(q: string) => {
                                const opts = (entornApps ?? []).map((ea: any) => ({
                                    id: ea?.id,
                                    description: ea.entornAppDescription,
                                }));
                                const filtered = q
                                    ? opts.filter(o => o.description?.toLowerCase().includes(q.toLowerCase()))
                                    : opts;
                                return Promise.resolve({ options: filtered });
                            }}
                            componentProps={{ disabled: (entornApps ?? []).length === 0 }}
                        />
                    </Grid>
                    <Grid size={{xs: 12, sm:6}}><FormField name={'nom'} /></Grid>
                    {moreFields && <>
                        <Grid size={{xs: 12, sm:6}}><FormField name={'tipus'} /></Grid>
                        {isCurrentUserAdmin &&
                        (<><Grid size={{xs: 6, sm:3}}><FormField name={'admin'} /></Grid>
                           <Grid size={{xs: 6, sm:3}}><FormField name={'correuGeneric'} /></Grid></>)}
                    </>}
                </Grid>
                <Box sx={{ display: 'flex', flexDirection: 'row' }}>
                    <IconButton onClick={netejar} title={t($ => $.components.clear)}>
                        <Icon>filter_alt_off</Icon>
                    </IconButton>
                    <IconButton
                        onClick={() => setMoreFields(mf => !mf)}
                        title={t($ => $.page.alarmaConfig.filter.more)}
                        color={moreFields ? 'primary' : 'default'}>
                        <Icon>filter_list</Icon>
                    </IconButton>
                    {isCurrentUserAdmin && (
                        <Button
                            onClick={() => setShowOnlyOwn(!showOnlyOwn)}
                            variant={showOnlyOwn ? 'contained' : 'outlined'}
                            title={showOnlyOwn ?
                                    t($ => $.page.alarmaConfig.filter.showOnlyOwnEnabled) :
                                    t($ => $.page.alarmaConfig.filter.showOnlyOwnDisabled)
                            }
                            sx={{ mr: 2 }}
                        >
                            <Icon>{showOnlyOwn ? 'account_circle' : 'people'}</Icon>
                        </Button>
                    )}
                </Box>
            </Box>
        </MuiFilter>
    );
};

const AlarmaConfig = () => {
    const { t } = useTranslation();
    const apiRef = useMuiDataGridApiRef();
    const [showOnlyOwn, setShowOnlyOwn] = React.useState<boolean>(true);
    const isCurrentUserAdmin = useIsUserAdmin();
    const { isReady: apiIsReadyEntornApp, find: apiFindEntornApp } = useResourceApiService('entornApp');
    const [entornApps, setEntornApps] = React.useState<any[]>();
    const [filter, setFilter] = React.useState<string | undefined>();

    React.useEffect(() => {
        if (apiIsReadyEntornApp) {
            apiFindEntornApp({ unpaged: true }).then(response => {
                setEntornApps(response.rows);
            });
        }
    }, [apiIsReadyEntornApp]);

    const columns = React.useMemo(() => {
        if (!entornApps) return [];
        const baseColumns:MuiDataGridColDef[] = [{
            field: 'entornAppId',
            valueFormatter: (value?: number) => {
                if (value == null) return '';
                const entornApp = entornApps.find(ea => ea.id === value);
                return entornApp?.entornAppDescription ?? '';
            },
            flex: 1,
        }, {
            field: 'nom',
            flex: 3,
        }, {
            field: 'tipus',
            flex: 1,
        }];

        if (!showOnlyOwn && isCurrentUserAdmin) {
            baseColumns.push({
                field: 'tipusUsuariAlarma',
                flex: 1,
                sortable: false,
            },);
        }

        return baseColumns;
    }, [entornApps, showOnlyOwn, isCurrentUserAdmin, t]);

    const hideForRow = React.useCallback((row: any) => {
        return !isCurrentUserAdmin && (row?.admin || row?.correuGeneric);
    }, [isCurrentUserAdmin]);
    const refresh = () => {
        apiRef.current?.refresh?.();
    }
    const {apiIsReady, apiDelete, tLib} = useAlarmaConfigAction(refresh)
    const actions = React.useMemo(() => [
        {
            label: tLib('datacommon.delete.label'),
            icon: 'delete',
            showInMenu: true,
            onClick: apiDelete,
            hidden: hideForRow,
        }
    ], [apiIsReady, tLib, hideForRow]);
    const filterElement = React.useMemo(() => (
        <AlarmaConfigFilter
            onSpringFilterChange={setFilter}
            entornApps={entornApps}
            showOnlyOwn={showOnlyOwn}
            setShowOnlyOwn={setShowOnlyOwn} />
    ), [entornApps, showOnlyOwn]);

    if (!entornApps) return <CenteredCircularProgress />;

    return (
        <GridPage>
            <MuiDataGrid
                apiRef={apiRef}
                title={t($ => $.page.alarmaConfig.title)}
                resourceName="alarmaConfig"
                columns={columns}
                toolbarType="upper"
                rowAdditionalActions={actions}
                toolbarAdditionalRow={filterElement}
                filter={filter}
                toolbarCreateLink="form"
                rowUpdateLink="form/{{id}}"
                rowHideDeleteButton
                rowHideUpdateButton={hideForRow}
                toolbarHideQuickFilter
            />
        </GridPage>
    );
}

export default AlarmaConfig;
