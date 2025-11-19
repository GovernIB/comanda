import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    FormField,
    MuiDataGridDialog,
    MuiDataGridDialogApi,
} from 'reactlib';

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
        <Grid size={12}>
            <FormField name="writeAllowed" />
        </Grid>
    </Grid>;
}

export const useAclPermissionManager = (resourceType: string) => {
    const { t } = useTranslation();
    const dataGridDialogApiRef = React.useRef<MuiDataGridDialogApi | any>({});
    const show = (id: any, description: string) => {
        dataGridDialogApiRef.current.show({
            title: description,
            dataGridComponentProps: {
                title: t($ => $.components.permisos.title),
                toolbarHideQuickFilter: true,
                staticFilter: "resourceType:'" + resourceType + "' and resourceId:" + id,
                staticSortModel: [{ field: 'subjectType', sort: 'asc' }, { field: 'subjectValue', sort: 'asc' }],
                formAdditionalData: {
                    resourceType,
                    resourceId: id,
                },
                popupEditActive: true,
                popupEditFormContent: <AclEntryForm />,
                popupEditFormDialogResourceTitle: t($ => $.components.permisos.resourceTitle)
            }
        });
    }
    const close = () => dataGridDialogApiRef.current.close();
    const component = <MuiDataGridDialog
        resourceName="aclEntry"
        columns={[{
            field: 'subjectType',
            sortable: false,
            flex: 2
        }, {
            field: 'subjectValue',
            sortable: false,
            flex: 3
        }, {
            field: 'readAllowed',
            sortable: false,
            flex: 1
        }, {
            field: 'writeAllowed',
            sortable: false,
            flex: 1
        }]}
        apiRef={dataGridDialogApiRef} />;
    return {
        show,
        close,
        component
    };
}