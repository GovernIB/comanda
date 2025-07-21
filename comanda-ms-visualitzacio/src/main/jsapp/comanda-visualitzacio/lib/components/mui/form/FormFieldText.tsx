import React from 'react';
import TextField from '@mui/material/TextField';
import Icon from '@mui/material/Icon';
import { useDebounce } from '../../../util/useDebounce';
import { FormFieldCustomProps } from '../../form/FormField';
import { FormFieldError } from '../../form/FormContext';
import { TextFieldProps } from '@mui/material/TextField';

type FormFieldTextProps = FormFieldCustomProps & {
    /** Indica si s'ha de fer debounce amb els valors del camp */
    debounce?: true;
};

export const useFormFieldCommon = (
    field: any,
    fieldError: FormFieldError | undefined,
    inline: boolean | undefined,
    componentProps: any,
    startAdornmentIcons?: React.ReactElement[]
) => {
    const helperText = inline ? field?.helperText : (fieldError?.message ?? field?.helperText);
    const title = field?.title ?? (inline ? helperText : undefined);
    const inlineErrorIconElement =
        fieldError && inline ? (
            <Icon fontSize="small" color="error" title={fieldError.message} sx={{ mr: 1 }}>
                warning
            </Icon>
        ) : null;
    const startAdornment =
        inlineErrorIconElement || startAdornmentIcons?.length ? (
            <>
                {inlineErrorIconElement}
                {...startAdornmentIcons ?? []}
                {componentProps?.slotProps?.input?.startAdornment}
            </>
        ) : (
            componentProps?.slotProps?.input?.startAdornment
        );
    return {
        helperText,
        title,
        startAdornment,
    };
};

const InnerFormFieldText: React.FC<
    FormFieldTextProps & {
        overrideTextFieldProps?: TextFieldProps;
    }
> = (props) => {
    const {
        name,
        label,
        value,
        type,
        field,
        fieldError,
        inline,
        required,
        disabled,
        readOnly,
        onChange,
        componentProps,
        overrideTextFieldProps,
    } = props;
    const { helperText, title, startAdornment } = useFormFieldCommon(
        field,
        fieldError,
        inline,
        componentProps
    );
    const inputProps = {
        readOnly,
        ...componentProps?.slotProps?.input,
        startAdornment,
    };
    const htmlInputProps = {
        maxLength: field?.maxLength,
        ...componentProps?.slotProps?.htmlInput,
    };
    const isTextAreaType = type === 'textarea' || field?.type === 'textarea';
    return (
        <TextField
            name={name}
            label={!inline ? label : undefined}
            placeholder={componentProps?.placeholder ?? (inline ? label : undefined)}
            value={value ?? ''}
            required={required ?? field?.required}
            disabled={disabled}
            error={fieldError != null}
            title={title}
            helperText={helperText}
            onChange={(e) => onChange(e.target.value === '' ? null : e.target.value)}
            fullWidth
            multiline={isTextAreaType}
            rows={isTextAreaType ? 4 : undefined}
            {...componentProps}
            slotProps={{
                input: inputProps,
                htmlInput: htmlInputProps,
            }}
            {...overrideTextFieldProps}
        />
    );
};

const useIsUserTypingRef = (delay: number = 250): [React.RefObject<boolean>, () => void] => {
    const isUserTypingRef = React.useRef(false);
    const timeoutIdRef = React.useRef<any>(null);

    const onUserInput = () => {
        isUserTypingRef.current = true;

        if (timeoutIdRef.current != null) {
            clearTimeout(timeoutIdRef.current);
        }
        timeoutIdRef.current = setTimeout(() => {
            isUserTypingRef.current = false;
        }, delay);
    };
    return [isUserTypingRef, onUserInput];
};

const InnerFormFieldTextDebounce: React.FC<FormFieldTextProps> = (props) => {
    const { value, onChange } = props;
    const [localValue, setLocalValue] = React.useState<string | null>(value);
    const changedValue = useDebounce(localValue, undefined, true);
    const [isUserTypingRef, onUserInput] = useIsUserTypingRef();
    React.useEffect(() => {
        if (!isUserTypingRef.current) {
            setLocalValue(value);
        }
    }, [value]);
    React.useEffect(() => {
        onChange?.(changedValue);
    }, [changedValue]);
    return (
        <InnerFormFieldText
            {...props}
            overrideTextFieldProps={{
                value: localValue ?? '',
                onChange: (e) => {
                    onUserInput();
                    setLocalValue(e.target.value === '' ? null : e.target.value);
                },
            }}
        />
    );
};

export const FormFieldText: React.FC<FormFieldTextProps> = (props) => {
    if (props.debounce) {
        return <InnerFormFieldTextDebounce {...props} />;
    } else {
        return <InnerFormFieldText {...props} />;
    }
};

export default FormFieldText;
