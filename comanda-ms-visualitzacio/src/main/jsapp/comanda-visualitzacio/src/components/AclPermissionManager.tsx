import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    FormField, MuiDataGridColDef,
    MuiDataGridDialog,
    MuiDataGridDialogApi,
} from 'reactlib';
import useReadOnlyGestor from '../hooks/useReadOnlyGestor.ts';

const AclEntryForm: React.FC = () => {
    return <Grid container spacing={2}>
        <Grid size={4}>
            <FormField name="subjectType" />
        </Grid>
        <Grid size={8}>
            <FormField name="subjectValue" />
        </Grid>
        <Grid size={12}>
            <FormField name="readAllowed" />
        </Grid>
    </Grid>;
}

export const useAclPermissionManager = (resourceType: string) => useAclCustomPermissionManager({resourceType, additionalData: { readAllowed: true }})
export const useAclCustomPermissionManager = (
    {
        resourceType,
        columns = [{
            field: 'subjectType',
            sortable: false,
            flex: 2
        }, {
            field: 'subjectValue',
            sortable: false,
            flex: 5
        }, {
            field: 'readAllowed',
            sortable: false,
            flex: 1
        }],
        formContent = <AclEntryForm />,
        additionalData,
        onEntryChanged
    }: {resourceType: string, columns?:MuiDataGridColDef[], formContent?: any, additionalData?: any, onEntryChanged?: (resourceId: any) => void }
) => {
    const { t } = useTranslation();
    const gestorReadOnly = useReadOnlyGestor();
    const dataGridDialogApiRef = React.useRef<MuiDataGridDialogApi | any>({});
    const currentResourceIdRef = React.useRef<any>(undefined);
    const show = (id: any, description: string) => {
        currentResourceIdRef.current = id;
        dataGridDialogApiRef.current.show({
            title: description,
            dataGridComponentProps: {
                title: t($ => $.components.permisos.title),
                toolbarHideQuickFilter: true,
                fixedFilter: "resourceType:'" + resourceType + "' and resourceId:" + id,
                staticSortModel: [{ field: 'subjectType', sort: 'asc' }, { field: 'subjectValue', sort: 'asc' }],
                formAdditionalData: (_row: any, action: string) => ({
                    resourceType,
                    resourceId: id,
                    ...(action === 'create'
                        ? {
                            subjectType: 'ROLE',
                            ...additionalData
                        }
                        : {}),
                }),
                popupEditActive: true,
                popupEditFormContent: formContent,
                popupEditFormDialogResourceTitle: t($ => $.components.permisos.resourceTitle),
                rowHideDeleteButton: gestorReadOnly,
                // Permet als consumidors del hook saber que s'ha creat/editat/eliminat un permís (p.ex. per
                // refrescar un comptador de permisos mostrat en una altra pantalla, com l'organigrama d'entitats).
                onRowCreate: () => onEntryChanged?.(currentResourceIdRef.current),
                onRowUpdate: () => onEntryChanged?.(currentResourceIdRef.current),
                onRowDelete: () => onEntryChanged?.(currentResourceIdRef.current),
            }
        });
    }
    const close = () => dataGridDialogApiRef.current.close();
    const component = <MuiDataGridDialog
        resourceName="aclEntry"
        columns={columns}
        apiRef={dataGridDialogApiRef}
        dialogComponentProps={{
            fullWidth: true,
            maxWidth: 'lg'
        }}/>;
    return {
        show,
        close,
        component
    };
}
