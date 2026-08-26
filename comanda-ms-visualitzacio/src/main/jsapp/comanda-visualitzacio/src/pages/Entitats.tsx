import PageTitle from "../components/PageTitle.tsx";
import {
    FormField,
    MuiDataGrid,
    MuiDataGridColDef,
    useBaseAppContext,
    useFormContext, useMuiDataGridApiRef,
    useResourceApiService
} from "reactlib";
import {useTranslation} from "react-i18next";
import { Grid } from "@mui/material";
import {useMemo} from "react";
import {useAclCustomPermissionManager} from "../components/AclPermissionManager.tsx";
import {useOrganigramaDialog} from "../components/EntitatOrganigrama.tsx";
import * as React from "react";
import IconButton from "@mui/material/IconButton";
import Icon from "@mui/material/Icon";
import Badge from "@mui/material/Badge";

const EntitatsFrom = () => {
    const {data} = useFormContext()
    return <Grid container gap={1}>
        <Grid size={12}><FormField name={'codi'} disabled={data?.id}/></Grid>
        <Grid size={12}><FormField name={'nom'} /></Grid>
        <Grid size={12}><FormField name={'codiDir3'} /></Grid>
        <Grid size={12}><FormField name={'cif'} /></Grid>
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
    { field: 'cif', flex: 2 },
];

const Entitats = () => {
    const { t } = useTranslation();

    const additionalColumns:any = useMemo(() => [
        ...columns,
        {
            field: 'numPermisos',
            headerName: '',
            sortable: false,
            flex: 0.5,
            renderCell: (params:any) => <IconButton
                title={t($ => $.components.permisos.title)}
                onClick={() => permissionShow(params.id, params.row?.codi ?? '')}
            >
                <Badge badgeContent={params.row.numPermisos} color={'primary'}><Icon>lock</Icon></Badge>
            </IconButton>
        }
    ], [t, columns])

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
                columns={additionalColumns}
                perspectives={['PERMIS_NUM']}
                toolbarType="upper"
                popupEditCreateActive
                popupEditActive
                popupEditFormContent={<EntitatsFrom/>}
                paginationActive
                rowAdditionalActions={[
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
                        onClick: (id, row) => handleOpen(id, row.codiDir3),
                    },
                ]}
            />
            {dialog}
            {permissionComponent}
        </>
    )
}
export default Entitats;
