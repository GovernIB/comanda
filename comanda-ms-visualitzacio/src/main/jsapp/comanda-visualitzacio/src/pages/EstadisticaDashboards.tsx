import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Backdrop from '@mui/material/Backdrop';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import { SimpleTreeView, TreeItem } from '@mui/x-tree-view';
import {
    MuiDataGrid,
    FormField,
    useFormContext,
    springFilterBuilder,
    useResourceApiService,
    useBaseAppContext,
    MuiFormDialogApi,
    useMuiDataGridApiRef,
} from 'reactlib';
import {iniciaDescargaJSON} from "../util/commonsActions.ts";
import FormActionDialog from '../components/FormActionDialog.tsx';
import { findOptions } from '../util/requestUtils.ts';
import PageTitle from '../components/PageTitle.tsx';
import IconButton from "@mui/material/IconButton";
import Icon from "@mui/material/Icon";
import {useAclCustomPermissionManager} from "../components/AclPermissionManager.tsx";
import {useMemo} from "react";
import {useIsUserAdmin, useIsUserUsuari} from "../components/UserContext.ts";
import Badge from "@mui/material/Badge";
import { TFunction } from 'i18next';

const EstadisticaDashboardForm: React.FC = () => {
    const { data } = useFormContext();
    const { isReady: appIsReady, find: appFind } = useResourceApiService("app");
    const { isReady: entornIsReady, find: entornFind } = useResourceApiService("entorn");
    const { isReady: plantillaIsReady, find: plantillaFind } = useResourceApiService("plantilla");
    const filterAplicacio = springFilterBuilder.and(
        springFilterBuilder.eq('activa', true),
        springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornApps.entorn.id', data?.entorn?.id))));
    const filterEntorn = springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornAppEntities.app.id', data?.aplicacio?.id)));

    if (!appIsReady || !entornIsReady || !plantillaIsReady)
        return;
    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="titol" />
            </Grid>
            <Grid size={12}>
                <FormField name="descripcio" />
            </Grid>
            <Grid size={12}>
                <FormField
                    name="aplicacio"
                    optionsRequest={(quickFilter: string) => findOptions(appFind, 'nom', quickFilter, filterAplicacio)}
                />
            </Grid>
            <Grid size={12}>
                <FormField
                    name="entorn"
                    optionsRequest={(quickFilter: string) => findOptions(entornFind, 'nom', quickFilter, filterEntorn)}
                />
            </Grid>
            <Grid size={12}>
                <FormField
                    name="plantilla"
                    optionsRequest={(quickFilter: string) => findOptions(plantillaFind, 'nom', quickFilter, '')}
                />
            </Grid>
        </Grid>
    );
};

const useCloneDashboardAction = (refresh?: () => void) => {
    const { t } = useTranslation();
    const apiRef = React.useRef<MuiFormDialogApi>(null);
    const {temporalMessageShow} = useBaseAppContext();
    const handleShow = (id:any, row:any) :void => {
        apiRef.current?.show?.(id, row)
    }
    const onSuccess = () :void => {
        refresh?.();
        temporalMessageShow(null, t($ => $.page.dashboards.cloneDashboard.success), 'success');
    }
    const formulario =
        <FormActionDialog
            resourceName={"dashboard"}
            action={"clone_dashboard"}
            apiRef={apiRef}
            title={t($ => $.page.dashboards.cloneDashboard.title)}
            onSuccess={onSuccess}
            initialOnChange={false}
        >
            <EstadisticaDashboardForm/>
        </FormActionDialog>;
    return {
        handleShow,
        content: formulario
    }
}

type Conflicte = { titol: string; tipo: string; overwrite?: string; nouNom?: string; appId?: number; suggerenciaNouNom?: string };

interface ConflictGroupMeta {
    tipo: string;
    label: string;
    icon: string;
    items: { index: number; conflict: Conflicte }[];
}

const groupConflicts = (conflicts: Conflicte[], t: TFunction): ConflictGroupMeta[] => {
    const knownTypes = [
        { tipo: 'DashboardExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.dashboard), icon: 'dashboard' },
        { tipo: 'EstadisticaWidgetExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.widget), icon: 'widgets' },
        { tipo: 'PlantillaExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.plantilla), icon: 'palette' },
        { tipo: 'PaletaExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.paleta), icon: 'format_color_fill' },
    ];

    const map = new Map<string, { index: number; conflict: Conflicte }[]>();
    conflicts.forEach((conflict, index) => {
        const key = conflict.tipo || 'Other';
        if (!map.has(key)) {
            map.set(key, []);
        }
        map.get(key)!.push({ index, conflict });
    });

    const groups: ConflictGroupMeta[] = [];

    knownTypes.forEach(({ tipo, getLabel, icon }) => {
        if (map.has(tipo)) {
            groups.push({
                tipo,
                label: getLabel(),
                icon,
                items: map.get(tipo)!,
            });
            map.delete(tipo);
        }
    });

    map.forEach((items, tipo) => {
        groups.push({
            tipo,
            label: tipo,
            icon: 'category',
            items,
        });
    });

    return groups;
};

const ConflictsTreeViewItemChild = React.memo(
    ({
        index,
        conflict,
        fieldOverwrite,
        updateConflict,
        fieldNouNom,
        group,
    }: {
        index: number;
        conflict: Conflicte;
        fieldOverwrite: string;
        updateConflict: (index: number, changes: Partial<Conflicte>) => void;
        fieldNouNom: string;
        group: ConflictGroupMeta;
    }) => {
        return (
            <TreeItem
                itemId={`conflict-${index}`}
                sx={{
                    // Afegit perquè el label del FormField no es talli
                    "& .MuiTreeItem-label": {
                        overflow: 'visible',
                    },
                    "& .MuiTreeItem-content": {
                        alignItems: 'start',
                    },
                }}
                label={
                    <Box
                        sx={{
                            display: 'flex',
                            flexWrap: 'wrap',
                            alignItems: 'start',
                            justifyContent: 'space-between',
                            pr: 1,
                            gap: 1,
                            width: '100%',
                        }}
                    >
                        <Box
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                height: 24, // Aquesta altura ha de ser la mateixa de l'element MuiSimpleTreeView-itemCheckbox
                                gap: 1,
                                flex: 1,
                                minWidth: 150,
                            }}
                        >
                            <Icon fontSize="small" sx={{ color: 'text.secondary', flexShrink: 0 }}>
                                {group.icon}
                            </Icon>
                            <Typography
                                variant="body2"
                                noWrap
                                sx={{ fontWeight: 500 }}
                                title={conflict.titol}
                            >
                                {conflict.titol}
                            </Typography>
                        </Box>
                        <Box
                            sx={{
                                display: 'flex',
                                alignItems: 'start',
                                gap: 1,
                                flexShrink: 0,
                            }}
                            onClick={e => e.stopPropagation()}
                            onKeyDown={e => e.stopPropagation()}
                        >
                            <Box sx={{ minWidth: 160 }}>
                                <FormField
                                    name={`conflicts[${index}].overwrite`}
                                    value={conflict.overwrite}
                                    field={fieldOverwrite}
                                    onChange={value => updateConflict(index, { overwrite: value })}
                                    componentProps={{ size: 'small', variant: 'standard', label: "" }}
                                    required
                                />
                            </Box>
                            {conflict.overwrite === 'CREAR_AMB_ALTRE_NOM' && (
                                <Box sx={{ minWidth: 160 }}>
                                    <FormField
                                        name={`conflicts[${index}].nouNom`}
                                        field={fieldNouNom}
                                        value={conflict.nouNom}
                                        onChange={value => updateConflict(index, { nouNom: value })}
                                        componentProps={{ size: 'small', variant: 'standard', label: "", placeholder: conflict.suggerenciaNouNom || conflict.titol }}
                                    />
                                </Box>
                            )}
                        </Box>
                    </Box>
                }
            />
        );
    }
);

const ConflictsTreeViewItemGroup = React.memo(
    ({
        group,
        fieldOverwrite,
        fieldNouNom,
        updateConflict,
    }: {
        group: ConflictGroupMeta;
        fieldOverwrite: string;
        fieldNouNom: string;
        updateConflict: (index: number, changes: Partial<Conflicte>) => void;
    }) => {
        return (
            <TreeItem
                itemId={`group-${group.tipo}`}
                label={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 0.5 }}>
                        <Icon fontSize="small" color="primary">
                            {group.icon}
                        </Icon>
                        <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                            {group.label}
                        </Typography>
                        <Chip
                            label={group.items.length}
                            size="small"
                            variant="outlined"
                            sx={{ height: 20, fontSize: '0.75rem' }}
                        />
                    </Box>
                }
            >
                {group.items.map(({ index, conflict }) => (
                    <ConflictsTreeViewItemChild
                        key={`conflict-${index}`}
                        index={index}
                        conflict={conflict}
                        fieldOverwrite={fieldOverwrite}
                        updateConflict={updateConflict}
                        fieldNouNom={fieldNouNom}
                        group={group}
                    />
                ))}
            </TreeItem>
        );
    }
);

const ConflictsTreeView = React.memo(
    ({
        groups,
        selectedItems,
        setSelectedItems,
        fieldOverwrite,
        fieldNouNom,
        updateConflict,
    }: {
        groups: ConflictGroupMeta[];
        selectedItems: string[];
        setSelectedItems: (items: string[]) => void;
        fieldOverwrite: string;
        fieldNouNom: string;
        updateConflict: (index: number, changes: Partial<Conflicte>) => void;
    }) => {
        const defaultExpandedItems = React.useMemo(() => {
            return groups.map(g => `group-${g.tipo}`);
        }, [groups]);

        return (
            <SimpleTreeView
                checkboxSelection
                multiSelect
                selectionPropagation={{ parents: true, descendants: true }}
                selectedItems={selectedItems}
                onSelectedItemsChange={(_event, itemIds) => {
                    setSelectedItems(Array.isArray(itemIds) ? itemIds : itemIds ? [itemIds] : []);
                }}
                defaultExpandedItems={defaultExpandedItems}
                sx={{
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1,
                    p: 1,
                    maxHeight: 400,
                    overflow: 'auto',
                    '& .MuiTreeItem-content': {
                        py: 0.5,
                    },
                }}
            >
                {groups.map(group => (
                    <ConflictsTreeViewItemGroup
                        key={`group-${group.tipo}`}
                        group={group}
                        fieldOverwrite={fieldOverwrite}
                        fieldNouNom={fieldNouNom}
                        updateConflict={updateConflict}
                    />
                ))}
            </SimpleTreeView>
        );
    }
);

const DashboardImportConflictsForm: React.FC<{ isAnalyzing: boolean }> = ({ isAnalyzing }) => {
    const { t } = useTranslation();
    const { data, apiRef, fields } = useFormContext();
    const conflicts: Conflicte[] | undefined = data?.conflicts;
    const hasFile = data?.file != null || (conflicts != null && conflicts.length > 0);

    const [selectedItems, setSelectedItems] = React.useState<string[]>([]);

    const groups = React.useMemo(() => {
        if (!conflicts || conflicts.length === 0) return [];
        return groupConflicts(conflicts, t);
    }, [conflicts, t]);

    const selectedIndices = React.useMemo(() => {
        if (!conflicts) return [];
        return conflicts
            .map((_, i) => i)
            .filter((i) => selectedItems.includes(`conflict-${i}`));
    }, [conflicts, selectedItems]);

    React.useEffect(() => {
        if (!conflicts || conflicts.length === 0) {
            setSelectedItems([]);
        }
    }, [conflicts]);

    const updateConflict = React.useCallback((index: number, changes: Partial<Conflicte>) => {
        if (!conflicts) return;
        const updated = conflicts.map((c, i) => (i === index ? { ...c, ...changes } : c));
        apiRef.current?.setFieldValue('conflicts', updated);
    }, [apiRef, conflicts]);

    const handleSelectAll = () => {
        if (!conflicts) return;
        const allIds: string[] = [];
        groups.forEach((g) => {
            allIds.push(`group-${g.tipo}`);
            g.items.forEach((item) => allIds.push(`conflict-${item.index}`));
        });
        setSelectedItems(allIds);
    };

    const handleDeselectAll = () => {
        setSelectedItems([]);
    };

    const handleBulkUseExisting = () => {
        if (selectedIndices.length === 0 || !conflicts) return;
        const updated = conflicts.map((c, i) => {
            if (!selectedIndices.includes(i)) return c;
            return {
                ...c,
                overwrite: 'EMPRAR_EXISTENT',
            };
        });
        apiRef.current?.setFieldValue('conflicts', updated);
    };

    const handleBulkCreateWithAnotherName = () => {
        if (selectedIndices.length === 0 || !conflicts) return;
        const updated = conflicts.map((c, i) => {
            if (!selectedIndices.includes(i)) return c;
            return {
                ...c,
                overwrite: 'CREAR_AMB_ALTRE_NOM',
            };
        });
        apiRef.current?.setFieldValue('conflicts', updated);
    };

    const fieldOverwrite = fields?.filter((i: any) => i.name === 'overwrite')[0];
    const fieldNouNom = fields?.filter((i: any) => i.name === 'nouNom')[0];

    if (!hasFile && !isAnalyzing) {
        return null;
    }

    if (isAnalyzing || (hasFile && conflicts === undefined)) {
        return (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, my: 2 }}>
                <CircularProgress size={24} />
                <Typography variant="body2" color="text.secondary">
                    {t($ => $.page.dashboards.action.import.analyzing)}
                </Typography>
            </Box>
        );
    }

    if (!conflicts || conflicts.length === 0) {
        return (
            <Alert severity="info" sx={{ mt: 2 }}>
                {t($ => $.page.dashboards.action.import.noConflicts)}
            </Alert>
        );
    }

    return (
        <Box sx={{ mt: 1 }}>
            <Typography variant="subtitle2" sx={{ mb: 1.5 }}>
                {t($ => $.page.dashboards.action.import.dashboardConflicts)}
            </Typography>

            <Box
                sx={{
                    display: 'flex',
                    flexWrap: 'wrap',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    gap: 1,
                    mb: 1.5,
                    p: 1,
                    borderRadius: 1,
                    backgroundColor: 'action.hover',
                }}
            >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography variant="body2" color="text.secondary">
                        {t($ => $.page.dashboards.action.import.bulkActions.selectedCount, {
                            count: selectedIndices.length,
                        })}
                    </Typography>
                    <Button
                        size="small"
                        variant="text"
                        onClick={handleSelectAll}
                        disabled={selectedIndices.length === conflicts.length}
                    >
                        {t($ => $.page.dashboards.action.import.bulkActions.selectAll)}
                    </Button>
                    <Button
                        size="small"
                        variant="text"
                        onClick={handleDeselectAll}
                        disabled={selectedIndices.length === 0}
                    >
                        {t($ => $.page.dashboards.action.import.bulkActions.deselectAll)}
                    </Button>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Button
                        size="small"
                        variant="outlined"
                        onClick={handleBulkUseExisting}
                        disabled={selectedIndices.length === 0}
                        startIcon={<Icon>check</Icon>}
                    >
                        {t($ => $.page.dashboards.action.import.bulkActions.useExisting)}
                    </Button>
                    <Button
                        size="small"
                        variant="outlined"
                        onClick={handleBulkCreateWithAnotherName}
                        disabled={selectedIndices.length === 0}
                        startIcon={<Icon>edit</Icon>}
                    >
                        {t($ => $.page.dashboards.action.import.bulkActions.createWithAnotherName)}
                    </Button>
                </Box>
            </Box>

            <ConflictsTreeView
                groups={groups}
                selectedItems={selectedItems}
                setSelectedItems={setSelectedItems}
                fieldOverwrite={fieldOverwrite}
                fieldNouNom={fieldNouNom}
                updateConflict={updateConflict}
            />
        </Box>
    );
};

const DashboardImportFormContent: React.FC = () => {
    const [isAnalyzing, setIsAnalyzing] = React.useState(false);
    const { apiRef, data } = useFormContext();

    const handleFileChange = (fileValue: any) => {
        if (!fileValue) {
            setIsAnalyzing(false);
            apiRef.current?.setFieldValue('conflicts', undefined);
        } else {
            setIsAnalyzing(true);
            apiRef.current?.setFieldValue('conflicts', undefined);
        }
    };

    React.useEffect(() => {
        if (data?.conflicts !== undefined) {
            setIsAnalyzing(false);
        }
    }, [data?.conflicts]);

    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="file" type={"file"} onChange={handleFileChange} />
            </Grid>
            <Grid size={12}>
                <DashboardImportConflictsForm isAnalyzing={isAnalyzing} />
            </Grid>
        </Grid>
    );
};

const useImportDashboardAction = (refresh?: () => void) => {
    const { t } = useTranslation();
    const apiRef = React.useRef<MuiFormDialogApi>(null);
    const { temporalMessageShow } = useBaseAppContext();
    const handleShow = (): void => {
        apiRef.current?.show?.(undefined);
    };
    const onSuccess = (): void => {
        refresh?.();
        temporalMessageShow(null, t($ => $.page.dashboards.action.import.success), 'success');
    };

    const importLoading = (
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 2, my: 4 }}>
            <CircularProgress size={48} />
            <Typography variant="h6" color="text.primary">
                {t($ => $.page.dashboards.action.import.importing)}
            </Typography>
        </Box>
    );

    const formulario = (
        <FormActionDialog
            resourceName={"dashboard"}
            action={"dashboard_import"}
            apiRef={apiRef}
            title={t($ => $.page.dashboards.action.import.title)}
            onSuccess={onSuccess}
            initialOnChange={false}
            formDialogLoading={importLoading}
        >
            <DashboardImportFormContent />
        </FormActionDialog>
    );

    return {
        handleShow,
        content: formulario,
    };
};

const useActions = () => {
    const { artifactReport: apiReport } = useResourceApiService('dashboard');
    const { temporalMessageShow } = useBaseAppContext();
    const { t } = useTranslation();
    const [exporting, setExporting] = React.useState(false);

    const report = (id:any, code:any, mssg:any, fileType:any) => {
        setExporting(true);
        apiReport(id, {code, fileType})
            .then((result) => {
                iniciaDescargaJSON(result);
                temporalMessageShow(null, mssg, 'success');
            })
            .catch((error) => {
                temporalMessageShow(null, error.message, 'error');
            })
            .finally(() => {
                setExporting(false);
            });
    }
    const dashboardExport = (id:any) => report(id, 'dashboard_export', t($ => $.page.dashboards.action.export), 'JSON')

    return {
        dashboardExport,
        exporting,
    };
};

const columns = [
    {
        field: 'titol',
        flex: 1,
    },
    {
        field: 'descripcio',
        flex: 3,
    },
    {
        field: 'aplicacio',
        flex: 1,
    },
    {
        field: 'entorn',
        flex: 1,
    },
];

const AclEntryForm: React.FC = () => {
    const { t } = useTranslation();
    return <Grid container spacing={2}>
        <Grid size={4}>
            <FormField name="subjectType" />
        </Grid>
        <Grid size={8}>
            <FormField name="subjectValue" />
        </Grid>
        <Grid size={12}>
            <FormField name="readAllowed" label={t($ => $.page.dashboards.acl.readAllowed)} />
        </Grid>
        <Grid size={12}>
            <FormField name="writeAllowed" label={t($ => $.page.dashboards.acl.writeAllowed)} />
        </Grid>
    </Grid>;
}

const EstadisticaDashboards: React.FC = () => {
    const { t } = useTranslation();
    const gridApiRef = useMuiDataGridApiRef();
    const isUserAdmin =  useIsUserAdmin()
    const isUserUsuari =  useIsUserUsuari()

    const additionalColumns:any = useMemo(() => [
        ...columns,
        ...(!isUserUsuari ?[{
            field: 'numPermisos',
            headerName: '',
            sortable: false,
            flex: 0.5,
            renderCell: (params:any) => <IconButton
                title={t($ => $.components.permisos.title)}
                onClick={() => permissionShow(params.id, params.row?.titol ?? '')}
            >
                <Badge badgeContent={params.row.numPermisos} color={'primary'}><Icon>lock</Icon></Badge>
            </IconButton>
        }] :[])
    ], [t, columns, isUserUsuari])

    const aclColumns = useMemo(() => [{
        field: 'subjectType',
        sortable: false,
        flex: 2
    }, {
        field: 'subjectValue',
        sortable: false,
        flex: 4
    }, {
        field: 'readAllowed',
        headerName: t($ => $.page.dashboards.acl.readAllowed),
        sortable: false,
        flex: 1
    }, {
        field: 'writeAllowed',
        headerName: t($ => $.page.dashboards.acl.writeAllowed),
        sortable: false,
        flex: 1
    }], [t]);

    const {
        show: permissionShow,
        component: permissionComponent
    } = useAclCustomPermissionManager({
        resourceType: 'DASHBOARD',
        columns: aclColumns,
        formContent: <AclEntryForm/>
    });
    const refresh = () => {
        gridApiRef?.current?.refresh?.();
    }
    const { dashboardExport, exporting } = useActions();
    const {handleShow: showCloneDashboard, content: contentCloneDashboard} = useCloneDashboardAction(refresh);
    const {handleShow: showImport, content: contentImport} = useImportDashboardAction(refresh);
    return (
        <>
            <PageTitle title={t($ => $.page.dashboards.title)} />
            {exporting && (
                <Backdrop
                    open={exporting}
                    sx={{
                        color: 'common.white',
                        zIndex: (theme) => theme.zIndex.drawer + 1,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 2
                    }}
                >
                    <CircularProgress color="inherit" />
                    <Typography variant="h6">{t($ => $.page.dashboards.action.export)}</Typography>
                </Backdrop>
            )}
            <MuiDataGrid
                title={t($ => $.page.dashboards.title)}
                resourceName="dashboard"
                columns={additionalColumns}
                apiRef={gridApiRef}
                perspectives={['PERMIS_NUM']}
                toolbarType="upper"
                namedQueries={['WRITE']}
                paginationActive
                rowHideUpdateButton
                popupEditActive
                popupEditFormContent={<EstadisticaDashboardForm/>}
                toolbarElementsWithPositions={isUserAdmin ?[
                    {
                        position: 2,
                        element: <IconButton
                            title={t($ => $.page.dashboards.action.import.label)}
                            onClick={showImport}
                        >
                            <Icon>upload</Icon>
                        </IconButton>
                    }
                ] :[]}
                rowAdditionalActions={[
                    {
                        label: t($ => $.page.dashboards.edit),
                        icon: 'edit',
                        clickShowUpdateDialog: true,
                    },
                    {
                        label: t($ => $.page.dashboards.dashboardView),
                        icon: 'dashboard',
                        showInMenu: false,
                        linkTo: '{{id}}',
                    },
                    {
                        label: t($ => $.page.dashboards.action.export),
                        icon: 'download',
                        showInMenu: true,
                        report: 'dashboard_export',
                        onClick: dashboardExport,
                    },
                    {
                        label: t($ => $.page.dashboards.cloneDashboard.title),
                        icon: 'file_copy',
                        showInMenu: true,
                        action: "clone_dashboard",
                        onClick: showCloneDashboard,
                    },
                ]}
            />
            {contentCloneDashboard}
            {contentImport}
            {permissionComponent}
        </>
    );
};

export default EstadisticaDashboards;
