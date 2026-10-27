import React from 'react';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import { useBaseAppContext } from '../../BaseAppContext';

type DataQuickFilterProps = {
    value: string;
    setFocus?: true;
    /** Plegable: buit es mostra com un botó d'icona de cerca que obre el camp (per defecte false) */
    collapsible?: boolean;
    onChange: (value: string) => void;
} & any;

export const DataQuickFilter: React.FC<DataQuickFilterProps> = (props) => {
    const { value, setFocus, collapsible, onChange, ...otherProps } = props;
    const { t } = useBaseAppContext();
    const [opened, setOpened] = React.useState<boolean>(!!setFocus);
    const focusOnOpenRef = React.useRef<boolean>(false);
    const inputElementRef = React.useRef<HTMLInputElement | null>(null);
    // Plegable: es manté obert mentre té valor (també si el valor és persistit)
    if (collapsible && !opened && !value) {
        return (
            <IconButton
                title={t('datacommon.quickfilter.label')}
                aria-label={t('datacommon.quickfilter.label')}
                onClick={() => {
                    focusOnOpenRef.current = true;
                    setOpened(true);
                }}
                sx={otherProps.sx}>
                <Icon>search</Icon>
            </IconButton>
        );
    }
    return (
        <TextField
            title={t('datacommon.quickfilter.label')}
            size="small"
            value={value}
            inputRef={
                collapsible
                    ? (input: HTMLInputElement | null) => {
                          inputElementRef.current = input;
                          if (input && (setFocus || focusOnOpenRef.current)) {
                              focusOnOpenRef.current = false;
                              input.focus();
                          }
                      }
                    : setFocus
                      ? (input) => input && input.focus()
                      : undefined
            }
            onChange={(event) => onChange(event.target.value)}
            onBlur={collapsible ? () => !value && setOpened(false) : undefined}
            slotProps={{
                htmlInput: {
                    'aria-label': t('datacommon.quickfilter.label'),
                },
                input: {
                    endAdornment: (
                        <>
                            {value && (
                                <InputAdornment position="end">
                                    <IconButton
                                        size="small"
                                        onClick={() => {
                                            onChange('');
                                            collapsible && setOpened(true);
                                            collapsible && inputElementRef.current?.focus();
                                        }}>
                                        <Icon fontSize="inherit">clear</Icon>
                                    </IconButton>
                                </InputAdornment>
                            )}
                            <InputAdornment position="end">
                                <Icon>search</Icon>
                            </InputAdornment>
                        </>
                    ),
                },
            }}
            {...otherProps}
        />
    );
};

export default DataQuickFilter;
