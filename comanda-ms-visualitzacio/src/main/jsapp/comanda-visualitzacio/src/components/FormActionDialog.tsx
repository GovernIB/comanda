import React, {MutableRefObject} from "react";
import {DialogButton, useBaseAppContext, useMuiActionReportLogic} from "reactlib";

type CommonProps = {
    title?: string | ((data:any) => string),
    resourceName: string,
    formDialogButtons?: DialogButton[],
    formDialogComponentProps?: any,
    formDialogLoading?: React.ReactElement,
    initialOnChange?: boolean,
    children: React.ReactElement,
    apiRef?: MutableRefObject<any>,
    formDialogResultProcessor?: (result?: any) => React.ReactElement,
    onSuccess?: (result?: any) => void,
    onError?: (error?: any) => void
}
type FormActionDialogProp = CommonProps & {
    action: string,
}

type FormReportDialogProp = CommonProps & {
    report: string,
    reportFileType: any,
}

const FormActionDialog = (props:FormActionDialogProp) => {
    const {temporalMessageShow} = useBaseAppContext();
    const {
        title,
        resourceName,
        action,
        formDialogButtons,
        formDialogComponentProps,
        formDialogLoading,
        initialOnChange = false,
        children,
        apiRef,
        formDialogResultProcessor,
        onSuccess = () => temporalMessageShow(null, '', 'success'),
        onError = (error: any) => {
            if (error?.message) {
                temporalMessageShow(null, error?.description, 'error');
            }
        },
    } = props;
    const {
        available,
        formDialogComponent,
        exec: actionExecutor,
        close,
    } = useMuiActionReportLogic(
        resourceName,
        action,
        undefined,
        undefined,
        false,
        undefined,
        undefined,
        undefined,
        initialOnChange,
        children,
        undefined,
        formDialogLoading,
        formDialogButtons,
        formDialogComponentProps,
        formDialogResultProcessor,
        onSuccess,
        onError,
        undefined,
        undefined,
        (reason?: string) => reason !== 'backdropClick'
    )

    const exec = (id: any, formAdditionalData?: any) :void => {
        if(available) {
            const customTitle = (typeof title === 'function') ? title?.(formAdditionalData) : title;
            actionExecutor(id, customTitle, formAdditionalData)
        }
    }

    if (apiRef != null) {
        apiRef.current = { show: exec, close };
    }

    return formDialogComponent;
}
export const FormReportDialog = (props:FormReportDialogProp) => {
    const {temporalMessageShow} = useBaseAppContext();
    const {
        title,
        resourceName,
        report,
        reportFileType = 'PDF',
        formDialogComponentProps,
        formDialogLoading,
        initialOnChange,
        children,
        apiRef,
        formDialogResultProcessor,
        onSuccess = () => temporalMessageShow(null, '', 'info'),
        onError = (error: any) => {
            if (error?.message) {
                temporalMessageShow(null, error?.message, 'error');
            }
        },
    } = props;

    const {
        available,
        formDialogComponent,
        exec: reportExecutor,
        close,
    } = useMuiActionReportLogic(
        resourceName,
        undefined,
        report,
        reportFileType,
        false,
        undefined,
        undefined,
        undefined,
        initialOnChange,
        children,
        undefined,
        formDialogLoading,
        undefined,
        formDialogComponentProps,
        formDialogResultProcessor,
        onSuccess,
        onError,
        undefined,
        undefined,
        (reason?: string) => reason !== 'backdropClick'
    )

    const exec = (id: any, formAdditionalData?: any) :void => {
        if(available) {
            const customTitle = (typeof title === 'function') ? title?.(formAdditionalData) : title;
            reportExecutor(id, customTitle, formAdditionalData)
        }
    }

    if (apiRef != null) {
        apiRef.current = { show: exec, close };
    }

    return formDialogComponent;
}
export default FormActionDialog;
