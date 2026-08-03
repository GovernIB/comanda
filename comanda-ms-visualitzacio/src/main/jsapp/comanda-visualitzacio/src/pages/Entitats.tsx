import PageTitle from "../components/PageTitle.tsx";
import {
    FormField,
    MuiDataGrid,
    MuiDataGridColDef, MuiDialog, springFilterBuilder,
    useBaseAppContext,
    useFormContext, useMuiDataGridApiRef,
    useResourceApiService
} from "reactlib";
import {useTranslation} from "react-i18next";
import { Grid } from "@mui/material";
import {useEffect, useMemo, useState} from "react";
import {SimpleTreeView, TreeItem} from "@mui/x-tree-view";
import Icon from "@mui/material/Icon";
import Box from "@mui/material/Box";
import {useAclCustomPermissionManager} from "../components/AclPermissionManager.tsx";
import * as React from "react";
import IconButton from "@mui/material/IconButton";

const UoAclEntryForm: React.FC = () => {
    const { t } = useTranslation();
    return <Grid container spacing={2}>
        <Grid size={4}>
            <FormField name="subjectType" />
        </Grid>
        <Grid size={8}>
            <FormField name="subjectValue" />
        </Grid>
        <Grid size={12}>
            <FormField name="perm0Allowed" label={t($ => $.page.unitatOrganitzativa.acl.perm0Allowed)} />
        </Grid>
    </Grid>;
}

const EntitatOrganigrama = ({codi, permisos}:{codi:string, permisos?: (id:any, row:any) => void}) => {
    const { t } = useTranslation();
    const [unitats, setUnitats] = useState<any[]>();

    const {isReady: apiIsReady, find: apiFind} = useResourceApiService('unitatOrganitzativa');
    const {temporalMessageShow} = useBaseAppContext();

    useEffect(() => {
        if (apiIsReady && codi) {
            apiFind({filter: springFilterBuilder.eq("codiUnitatArrel", `'${codi}'`), unpaged: true})
                .then(response => {
                    if (response.rows.length > 0) {
                        setUnitats(response.rows)
                    } else {
                        temporalMessageShow(null, t($ => $.page.entitats.action.organigrama.ko), 'error')
                    }
                })
                .catch(error => temporalMessageShow(null, error.message, 'error'))
        }
    }, [apiIsReady, codi, apiFind]);

    const renderTree = (node:any) => (
        <TreeItem key={node.codi} itemId={node.codi} label={
            <Box display={'flex'} alignItems={'center'} gap={1}>
                <Icon>{node.codi == codi ?'home' :'folder'}</Icon>
                {node.codiNom}

                {permisos != null &&
                    <IconButton title={t($ => $.components.permisos.title)} onClick={(ev) => {
                        ev.stopPropagation()
                        permisos?.(node.id, node)
                    }}>
                        <Icon>lock</Icon>
                    </IconButton>
                }
            </Box>
        }>
            {Array.isArray(node.children) ? node.children.map((node:any) => renderTree(node)) : null}
        </TreeItem>
    );

    const constructData = (codi:string, list:any[]) :any => {
        const e = list.find(c => c.codi == codi);
        if (e == null) return;
        return {
            ...e,
            children: list.filter(c => c.codiUnitatSuperior == codi)
                .map(c => constructData(c.codi, list))
        }
    }

    return <>
        <SimpleTreeView defaultExpandedItems={[codi]}>
            {unitats && renderTree( constructData(codi, unitats) )}
        </SimpleTreeView>
    </>
}

const useOrganigramaDialog = () => {
    const { t } = useTranslation();
    const [open, setOpen] = useState<boolean>(false);
    const [codi, setCodi] = useState<string>();

    const uoAclColumns = useMemo(() => [{
        field: 'subjectType',
        sortable: false,
        flex: 2
    }, {
        field: 'subjectValue',
        sortable: false,
        flex: 4
    }, {
        field: 'perm0Allowed',
        headerName: t($ => $.page.unitatOrganitzativa.acl.perm0Allowed),
        sortable: false,
        flex: 1
    }], [t]);

    const {
        show: permissionShow,
        component: permissionComponent
    } = useAclCustomPermissionManager({
        resourceType: 'UNITAT',
        columns: uoAclColumns,
        formContent: <UoAclEntryForm/>
    });

    const handleOpen = (codi:string) => {
        setCodi(codi);
        setOpen(true);
    };

    const handleClose = () => {
        setCodi(undefined);
        setOpen(false);
    };

    const dialog = (<>
        <MuiDialog
            open={open}
            closeCallback={handleClose}
            title={t($ => $.page.entitats.action.organigrama.title)}
            componentProps={{ fullWidth: true, maxWidth: 'lg' }}
        >
            {codi && <EntitatOrganigrama codi={codi} permisos={(id, row) => permissionShow(id, row?.codiNom)}/>}
        </MuiDialog>
        {permissionComponent}
    </>);

    return {
        handleOpen,
        handleClose,
        dialog,
    };
}

const EntitatsFrom = () => {
    const {data} = useFormContext()
    return <Grid container gap={1}>
        <Grid size={12}><FormField name={'codi'} disabled={data?.id}/></Grid>
        <Grid size={12}><FormField name={'nom'} /></Grid>
        <Grid size={12}><FormField name={'codiDir3'} /></Grid>
    </Grid>
}

const EntitatAclEntryForm: React.FC = () => {
    const { t } = useTranslation();
    return <Grid container spacing={2}>
        <Grid size={4}>
            <FormField name="subjectType" />
        </Grid>
        <Grid size={8}>
            <FormField name="subjectValue" />
        </Grid>
        <Grid size={12}>
            <FormField name="perm0Allowed" label={t($ => $.page.entitats.acl.perm0Allowed)} />
        </Grid>
    </Grid>;
}

const columns: MuiDataGridColDef[] = [
    { field: 'codi', flex: 1 },
    { field: 'nom', flex: 2 },
    { field: 'codiDir3', flex: 4 },
];

const Entitats = () => {
    const { t } = useTranslation();

    const entitatAclColumns = useMemo(() => [{
        field: 'subjectType',
        sortable: false,
        flex: 2
    }, {
        field: 'subjectValue',
        sortable: false,
        flex: 4
    }, {
        field: 'perm0Allowed',
        headerName: t($ => $.page.entitats.acl.perm0Allowed),
        sortable: false,
        flex: 1
    }], [t]);

    const {
        show: permissionShow,
        component: permissionComponent
    } = useAclCustomPermissionManager({
        resourceType: 'ENTITAT',
        columns: entitatAclColumns,
        formContent: <EntitatAclEntryForm/>
    });

    const { artifactAction: apiAction } = useResourceApiService('entitat');
    const { temporalMessageShow } = useBaseAppContext();

    const gridApiRef = useMuiDataGridApiRef();
    const refresh = () => {
        gridApiRef?.current?.refresh?.();
    }

    const {handleOpen, dialog} = useOrganigramaDialog();

    const refreshUO = (id:any) => {
        apiAction(id, {code: 'REFRESH_UO'})
            .then(() => {
                refresh()
                temporalMessageShow(null, t($ => $.page.entitats.action.refreshUO.ok), 'success')
            })
            .catch(error => temporalMessageShow(null, error.message, 'error'))
    }

    return (
        <>
            <PageTitle title={t($ => $.page.entitats.title)} />
            <MuiDataGrid
                apiRef={gridApiRef}
                title={t($ => $.page.entitats.title)}
                resourceName="entitat"
                columns={columns}
                toolbarType="upper"
                popupEditCreateActive
                popupEditActive
                popupEditFormContent={<EntitatsFrom/>}
                paginationActive
                rowAdditionalActions={[
                    {
                        label: t($ => $.components.permisos.title),
                        icon: "lock",
                        onClick: (id: string | number, row: { codi?: string }) => permissionShow(id, row?.codi ?? '')
                    },
                    {
                        label: t($ => $.page.entitats.action.refreshUO.label),
                        icon: 'refresh',
                        showInMenu: true,
                        action: 'REFRESH_UO',
                        onClick: refreshUO,
                    },
                    {
                        label: t($ => $.page.entitats.action.organigrama.label),
                        icon: 'list',
                        showInMenu: true,
                        onClick: (_id, row) => handleOpen(row.codiDir3),
                    },
                ]}
            />
            {dialog}
            {permissionComponent}
        </>
    )
}
export default Entitats;
