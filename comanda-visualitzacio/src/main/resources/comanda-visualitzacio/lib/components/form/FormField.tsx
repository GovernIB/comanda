import React from 'react';
//import { useWhatChanged } from '@simbathesailor/use-what-changed';
import { useBaseAppContext } from '../BaseAppContext';
import useLogConsole from '../../util/useLogConsole';
import {
    useFormContext,
    FormFieldDataActionType,
    FormFieldError,
} from './FormContext';
import { useOptionalFilterContext } from './FilterContext';

const LOG_PREFIX = 'FIELD';

export type FormFieldComponent = {
    type: string;
    component: React.FC<FormFieldCustomProps>;
};

export type FormFieldCommonProps = {
    name: string;
    label?: string;
    inline?: boolean;
    required?: boolean;
    disabled?: boolean;
    readOnly?: boolean;
    onChange?: (value: any) => void;
    componentProps?: any;
};

export type FormFieldProps = FormFieldCommonProps & {
    type?: string;
    debug?: boolean;
    [x: string | number | symbol]: unknown;
};

type FormFieldRendererProps = FormFieldCommonProps & {
    value?: any;
    field?: any;
    fieldError?: FormFieldError;
    fieldTypeMap?: Map<string, string>;
    onFieldValueChange: (value: any) => void;
    type?: string;
    debug?: boolean;
};

export type FormFieldCustomProps = FormFieldCommonProps & {
    value: any;
    field: any;
    type?: string;
    fieldError?: FormFieldError;
    onChange: (value: any) => void;
};

const FormFieldRenderer: React.FC<FormFieldRendererProps> = (props) => {
    const {
        name,
        label: labelProp,
        value,
        field,
        fieldError,
        fieldTypeMap,
        inline,
        required,
        disabled,
        readOnly,
        onFieldValueChange,
        componentProps,
        type,
        debug,
        ...otherProps
    } = props;
    /*useWhatChanged([
        name,
        labelProp,
        value,
        field,
        fieldError,
        fieldTypeMap,
        inline,
        required,
        disabled,
        readOnly,
        onFieldValueChange,
        componentProps,
        type,
        debug,
        otherProps]);*/
    const { getFormFieldComponent } = useBaseAppContext();
    const logConsole = useLogConsole(LOG_PREFIX);
    const label = labelProp ?? field?.label ?? name;
    debug && logConsole.debug('Field', name, 'rendered', (value ? 'with value: ' + value : 'empty'));
    const fieldType = type ?? field?.type;
    const mappedFieldType = fieldTypeMap?.get(fieldType) ?? fieldType;
    const FormFieldComponent: React.FC<FormFieldCustomProps> | undefined = field ? getFormFieldComponent(mappedFieldType) : undefined;
    return FormFieldComponent ? <FormFieldComponent
        name={name}
        label={label}
        value={value}
        type={fieldType}
        field={field}
        fieldError={fieldError}
        inline={inline}
        required={required}
        disabled={disabled}
        readOnly={readOnly}
        onChange={onFieldValueChange}
        componentProps={componentProps}
        {...otherProps} /> : <span>[&nbsp;Unknown field: {name}&nbsp;]</span>;
}

const Renderer = React.memo(FormFieldRenderer);
export const FormField: React.FC<FormFieldProps> = (props) => {
    const {
        name,
        inline: inlineProp,
        required,
        disabled,
        readOnly,
        onChange,
        componentProps,
        type,
        debug,
        ...otherProps
    } = props;
    const [field, setField] = React.useState<any>();
    const [fieldError, setFieldError] = React.useState<FormFieldError | undefined>();
    const {
        isReady: isFormReady,
        isSaveActionPresent,
        fields,
        fieldErrors,
        fieldTypeMap,
        inline: inlineCtx,
        dataGetFieldValue,
        dataDispatchAction,
        commonFieldComponentProps,
    } = useFormContext();
    const filterContext = useOptionalFilterContext();
    React.useEffect(() => {
        if (fields) {
            const field = fields.find(f => f.name === name);
            setField(field ?? null);
        }
    }, [fields]);
    React.useEffect(() => {
        if (fieldErrors) {
            const fieldError = fieldErrors.find(e => e.field === name);
            setFieldError(fieldError ?? undefined);
        } else {
            setFieldError(undefined);
        }
    }, [fieldErrors]);
    const isReady = isFormReady && field !== undefined;
    const value = dataGetFieldValue(name);
    const handleFieldValueChange = React.useCallback((value: any) => {
        dataDispatchAction({
            type: FormFieldDataActionType.FIELD_CHANGE,
            payload: { fieldName: name, field, value }
        });
        onChange?.(value);
    }, [dataDispatchAction, name, field, onChange]);
    const inline = inlineProp ?? inlineCtx;
    const forceDisabledAndReadonly = filterContext == null && !isSaveActionPresent;
    const joinedComponentProps = React.useMemo(() => ({
        ...commonFieldComponentProps,
        ...componentProps,
    }), [commonFieldComponentProps, componentProps]);
    return isReady ? <Renderer
        name={name}
        value={value}
        field={field}
        fieldError={fieldError}
        fieldTypeMap={fieldTypeMap}
        inline={inline}
        required={required}
        disabled={forceDisabledAndReadonly || disabled}
        readOnly={forceDisabledAndReadonly || readOnly}
        componentProps={joinedComponentProps}
        type={type}
        debug={debug}
        onFieldValueChange={handleFieldValueChange}
        {...otherProps} /> : null;
}

export default FormField;
