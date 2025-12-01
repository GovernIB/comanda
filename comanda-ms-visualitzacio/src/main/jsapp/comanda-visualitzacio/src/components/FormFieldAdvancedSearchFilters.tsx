import { DialogProps, Icon, IconButton } from '@mui/material';
import React, { useState } from 'react';
import {
    FormField,
    FormFieldProps,
    MuiDataGrid,
    MuiDialog,
    MuiFilter,
    useBaseAppContext,
    useFormContext,
} from 'reactlib';
import { FormFieldDataActionType } from '../../lib/components/form/FormContext.tsx';

type AdvancedSearchWithFiltersParams = {
    advancedSearchFilterCode: string;
    advancedSearchFilterResourceName?: string;
    advancedSearchFilterContent: React.ReactNode;
    advancedSearchFilterBuilder: (data: any) => string | undefined;
    advancedSearchDataGridProps?: any;
    fieldName: FormFieldProps['name'];
    advancedSearchColumns: FormFieldProps['advancedSearchColumns'];
    advancedSearchDialogHeight?: FormFieldProps['advancedSearchDialogHeight'];
    advancedSearchDialogComponentProps?: Partial<DialogProps>;
    filter?: FormFieldProps['filter'];
    namedQueries?: FormFieldProps['namedQueries'];
    perspectives?: FormFieldProps['perspectives'];
    sortModel?: FormFieldProps['sortModel'];
    multiple?: FormFieldProps['multiple'];
};

const useAdvancedSearchWithFilters = ({
    advancedSearchFilterCode,
    advancedSearchFilterResourceName,
    advancedSearchFilterContent,
    advancedSearchFilterBuilder,
    advancedSearchDataGridProps = {},
    fieldName,
    advancedSearchColumns,
    advancedSearchDialogHeight,
    advancedSearchDialogComponentProps,
    filter,
    namedQueries,
    perspectives,
    sortModel,
    multiple,
}: AdvancedSearchWithFiltersParams) => {
    const { t: tLib } = useBaseAppContext();
    const { resourceName, resourceType, resourceTypeCode, dataDispatchAction, fields, data } =
        useFormContext();
    const [open, setOpen] = useState(false);
    const closeDialog = () => setOpen(false);
    const openDialog = () => setOpen(true);

    const toolbarAdditionalRow = (
        <MuiFilter
            resourceName={advancedSearchFilterResourceName ?? resourceName}
            code={advancedSearchFilterCode}
            springFilterBuilder={advancedSearchFilterBuilder}
        >
            {advancedSearchFilterContent}
        </MuiFilter>
    );

    const handleRowClick = ({ row }: { row: any }) => {
        closeDialog();
        const field = fields?.find((f) => f.name === fieldName);
        const value = data?.[fieldName];
        const changeValue = (newValue: any) => {
            dataDispatchAction({
                type: FormFieldDataActionType.FIELD_CHANGE,
                payload: { fieldName, value: newValue, field },
            });
        };

        const valueField = field.dataSource.valueField;
        const labelField = field.dataSource.labelField;
        const valueReference = {
            id: row[valueField],
            description: row[labelField],
        };
        const valueReferenceWithData = valueReference ? { ...valueReference, data: row } : null;
        if (multiple) {
            const currentValues = Array.isArray(value) ? value : value != null ? [value] : [];
            const currentValueFound = currentValues.find(
                (v) => v.id === valueReferenceWithData?.id
            );
            changeValue(
                currentValueFound ? currentValues : [...currentValues, valueReferenceWithData]
            );
        } else {
            changeValue(valueReferenceWithData);
        }
    };

    const dialog = (
        <MuiDialog
            open={open}
            closeCallback={closeDialog}
            title={tLib('form.field.reference.advanced.title')}
            componentProps={{
                fullWidth: true,
                maxWidth: 'md',
                ...advancedSearchDialogComponentProps,
            }}
        >
            <MuiDataGrid
                toolbarAdditionalRow={toolbarAdditionalRow}
                columns={advancedSearchColumns}
                resourceName={resourceName}
                resourceType={resourceType}
                resourceTypeCode={resourceTypeCode}
                resourceFieldName={fieldName}
                paginationActive
                titleDisabled
                quickFilterSetFocus
                quickFilterFullWidth
                toolbarHideRefresh
                readOnly
                staticFilter={filter}
                staticSortModel={sortModel}
                namedQueries={namedQueries}
                perspectives={perspectives}
                onRowClick={handleRowClick}
                height={advancedSearchDialogHeight ?? 370}
                {...advancedSearchDataGridProps}
            />
        </MuiDialog>
    );
    return {
        openDialog,
        dialog,
    };
};

type FormFieldAdvancedSearchFiltersProps = FormFieldProps & {
    advancedSearchFilterCode: string;
    advancedSearchFilterResourceName?: string;
    advancedSearchFilterContent: React.ReactNode;
    advancedSearchFilterBuilder: (data: any) => string | undefined;
    advancedSearchDataGridProps?: any;
    advancedSearchDialogComponentProps?: Partial<DialogProps>;
};

const FormFieldAdvancedSearchFilters: React.FC<FormFieldAdvancedSearchFiltersProps> = (props) => {
    const {
        advancedSearchFilterCode,
        advancedSearchFilterResourceName,
        advancedSearchFilterContent,
        advancedSearchFilterBuilder,
        advancedSearchDataGridProps,
        name,
        advancedSearchColumns,
        advancedSearchDialogHeight,
        advancedSearchDialogComponentProps,
        filter,
        namedQueries,
        perspectives,
        sortModel,
        multiple,
    } = props;
    const { openDialog, dialog } = useAdvancedSearchWithFilters({
        advancedSearchFilterCode,
        advancedSearchFilterResourceName,
        advancedSearchFilterContent,
        advancedSearchFilterBuilder,
        advancedSearchDataGridProps,
        fieldName: name,
        advancedSearchColumns,
        advancedSearchDialogHeight,
        advancedSearchDialogComponentProps,
        filter,
        namedQueries,
        perspectives,
        sortModel,
        multiple,
    });
    return (
        <>
            {dialog}
            <FormField
                {...props}
                advancedSearchColumns={undefined}
                componentProps={{
                    slotProps: {
                        input: {
                            startAdornment: (
                                <IconButton onClick={openDialog} size="small" tabIndex={-1}>
                                    <Icon fontSize="small">manage_search</Icon>
                                </IconButton>
                            ),
                        },
                    },
                }}
            >
                {props.children}
            </FormField>
        </>
    );
};

export default FormFieldAdvancedSearchFilters;
