import React, {useState, useEffect} from 'react';
import {useFormContext, FormFieldDataActionType, FormFieldError} from '../../../lib/components/form/FormContext';
import {FormField} from 'reactlib';
import {columnesIndicador} from '../sharedAdvancedSearch/advancedSearchColumns';
import {Box, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, IconButton, Typography} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import DragIndicatorIcon from '@mui/icons-material/DragIndicator';
import {useTranslation} from 'react-i18next';

interface ColumnaItem {
    indicador: any;
    titol: string;
    agregacio?: string;
    unitatAgregacio?: string;
}

interface ColumnesTableProps {
    name: string;
    label?: string;
    value?: ColumnaItem[];
    mostrarUnitat: boolean;
    hiddenAgregacioValues?: string[];
    onChange?: (value: ColumnaItem[]) => void;
}

const ColumnesTable: React.FC<ColumnesTableProps> = ({name, label, mostrarUnitat = true, hiddenAgregacioValues, value = [], onChange}) => {
    const {data, dataDispatchAction, fields, fieldErrors = []} = useFormContext();
    const {t} = useTranslation();

    // Initialize with at least one empty row if none provided
    const emptyRow = {indicador: null, titol: ''};
    const initialColumnes = value && value.length > 0
        ? value
        : [emptyRow];

    // State to track which rows have been touched (had focus and then lost it)
    const [touchedFields, setTouchedFields] = useState<Record<string, boolean>>({});

    // State to track which rows are currently in focus
    const [focusedRowIndex, setFocusedRowIndex] = useState<number | null>(null);

    // State to store field validation errors
    const [validationErrors, setValidationErrors] = useState<FormFieldError[]>([]);

    const [columnes, setColumnes] = useState<ColumnaItem[]>(initialColumnes);
    const [draggedIndex, setDraggedIndex] = useState<number | null>(null);
    const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);

    // Use a ref to store the previous value for comparison
    const prevValueRef = React.useRef<ColumnaItem[] | undefined>(value);

    // Validate fields and create error objects
    const validateFields = React.useCallback(() => {
        const errors: FormFieldError[] = [];

        // Validate each row
        columnes.forEach((columna, index) => {
            // Check if indicador has a value
            if (columna.indicador) {
                // Validate titol - should not be empty if indicador has a value
                const titolFieldName = `${name}.${index}.titol`;
                if (touchedFields[titolFieldName] && (!columna.titol || columna.titol.trim() === '')) {
                    errors.push({
                        code: 'REQUIRED',
                        field: titolFieldName,
                        message: '',
                    });
                }

                // Validate agregacio - should not be empty if indicador has a value
                const agregacioFieldName = `${name}.${index}.agregacio`;
                if (touchedFields[agregacioFieldName] && (!columna.agregacio || columna.agregacio.trim() === '')) {
                    errors.push({
                        code: 'REQUIRED',
                        field: agregacioFieldName,
                        message: '',
                    });
                }

                // Validate unitatAgregacio - should not be empty if tipusIndicador is 'AVERAGE'
                const unitatAgregacioFieldName = `${name}.${index}.unitatAgregacio`;
                if (data.columnes?.[index]?.agregacio === 'AVERAGE' &&
                    touchedFields[unitatAgregacioFieldName] &&
                    (!columna.unitatAgregacio || columna.unitatAgregacio.trim() === '')) {
                    errors.push({
                        code: 'REQUIRED',
                        field: unitatAgregacioFieldName,
                        message: '',
                    });
                }
            }
        });

        return errors;
    }, [columnes, touchedFields, data.columnes, name, t]);

    // Update local state when value changes from outside, but only if it's different
    useEffect(() => {
        // Only update if value is different from previous value to prevent infinite loop
        const valueToUse = value && value.length > 0 ? value : [emptyRow];
        const prevValue = prevValueRef.current;

        // Check if the new value is different from the previous value
        const isDifferent = JSON.stringify(valueToUse) !== JSON.stringify(prevValue);

        if (isDifferent) {
            setColumnes(valueToUse);
            prevValueRef.current = valueToUse;
        }
    }, [value]);

    // Update validation errors when fields are touched or values change
    useEffect(() => {
        const errors = validateFields();
        setValidationErrors(errors);
    }, [validateFields]);

    // Add a new empty row
    const handleAddRow = () => {
        const newColumnes = [
            ...columnes,
            {indicador: null, titol: ''}
        ];
        setColumnes(newColumnes);
        updateFormData(newColumnes);
    };

    // Remove a row by index
    const handleRemoveRow = (index: number) => {
        // Prevent removing the last row
        if (columnes.length <= 1) {
            return;
        }

        const newColumnes = columnes.filter((_, i) => i !== index);
        setColumnes(newColumnes);
        updateFormData(newColumnes);
    };

    // Update form data
    const updateFormData = (newColumnes: ColumnaItem[]) => {
        dataDispatchAction({
            type: FormFieldDataActionType.FIELD_CHANGE,
            payload: {fieldName: name, value: newColumnes}
        });

        if (onChange) {
            onChange(newColumnes);
        }
    };

    // Handle field blur event
    const handleFieldBlur = (fieldName: string) => {
        // Mark the field as touched
        setTouchedFields(prev => ({
            ...prev,
            [fieldName]: true
        }));
    };

    // Mark all fields in a row as touched
    const markRowFieldsAsTouched = (rowIndex: number) => {
        const fieldsToTouch = {
            [`${name}.${rowIndex}.indicador`]: true,
            [`${name}.${rowIndex}.titol`]: true,
            [`${name}.${rowIndex}.agregacio`]: true,
            [`${name}.${rowIndex}.unitatAgregacio`]: true
        };

        setTouchedFields(prev => ({
            ...prev,
            ...fieldsToTouch
        }));
    };

    // Handle row focus
    const handleRowFocus = (index: number) => {
        setFocusedRowIndex(index);
    };

    // Handle row blur
    const handleRowBlur = (index: number) => {
        // When a row loses focus, mark all its fields as touched to trigger validation
        markRowFieldsAsTouched(index);
        setFocusedRowIndex(null);
    };

    // Find error for a specific field
    const getFieldError = (fieldName: string): FormFieldError | undefined => {
        // Replace first '.' with '[' and second '.' with '].'
        const formattedFieldName = fieldName.replace(/\./, '[').replace(/\./, '].');
        const error = fieldErrors.find(error => error.field === formattedFieldName);
        return error ? error : validationErrors.find(error => error.field === fieldName);
    };

    // Handle field change for a specific row and field
    const handleFieldChange = (index: number, fieldName: keyof ColumnaItem, value: any) => {
        const newColumnes = [...columnes];

        // Create a new object for the row
        const newRow = {...newColumnes[index]};

        // If the field is 'agregacio' or 'unitatAgregacio' and the value is empty,
        // don't include it in the object
        if ((fieldName === 'agregacio' || fieldName === 'unitatAgregacio') && (value === '' || value === null)) {
            delete newRow[fieldName];
        } else {
            // Otherwise, set the value normally
            newRow[fieldName] = value;
        }

        newColumnes[index] = newRow;
        setColumnes(newColumnes);
        updateFormData(newColumnes);
    };

    // Drag and drop handlers
    const handleDragStart = (e: React.DragEvent<HTMLTableRowElement>, index: number) => {
        setDraggedIndex(index);
        // Set the drag effect and make the ghost image semi-transparent
        e.dataTransfer.effectAllowed = 'move';
        if (e.currentTarget.firstChild) {
            const originalRect = e.currentTarget.getBoundingClientRect();
            const ghostElement = e.currentTarget.cloneNode(true) as HTMLElement;
            ghostElement.style.opacity = '0.8';
            ghostElement.style.width = `${originalRect.width}px`;
            ghostElement.style.tableLayout = 'fixed';

            // Set the width of each cell to match the original
            const originalCells = e.currentTarget.querySelectorAll('td');
            const ghostCells = ghostElement.querySelectorAll('td');
            originalCells.forEach((cell, i) => {
                const cellRect = cell.getBoundingClientRect();
                if (ghostCells[i]) {
                    ghostCells[i].style.width = `${cellRect.width}px`;
                }
            });

            document.body.appendChild(ghostElement);
            e.dataTransfer.setDragImage(ghostElement, 0, 0);
            // Remove the ghost element after it's no longer needed
            setTimeout(() => {
                document.body.removeChild(ghostElement);
            }, 0);
        }
    };

    const handleDragOver = (e: React.DragEvent<HTMLTableRowElement>, index: number) => {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        setDragOverIndex(index);
    };

    const handleDrop = (e: React.DragEvent<HTMLTableRowElement>, dropIndex: number) => {
        e.preventDefault();

        if (draggedIndex === null || draggedIndex === dropIndex) {
            return;
        }

        // Create a new array with the reordered items
        const newColumnes = [...columnes];
        const draggedItem = newColumnes[draggedIndex];

        // Remove the dragged item
        newColumnes.splice(draggedIndex, 1);

        // Insert the dragged item at the drop position
        newColumnes.splice(dropIndex, 0, draggedItem);

        // Update state and form data
        setColumnes(newColumnes);
        updateFormData(newColumnes);
        setDraggedIndex(null);
        setDragOverIndex(null);
    };

    const handleDragEnd = () => {
        setDraggedIndex(null);
        setDragOverIndex(null);
    };

    return (
        <Box sx={{width: '100%', mb: 2}}>
            {label && <Typography variant="subtitle1" sx={{mb: 1}}>{label}</Typography>}

            <TableContainer component={Paper} sx={{mb: 2}}>
                <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell width="2%" sx={{px: 0}}></TableCell>
                            <TableCell width="33%">{t('page.widget.taula.columna.indicador')}</TableCell>
                            <TableCell width="33%">{t('page.widget.taula.columna.titolIndicador')}</TableCell>
                            <TableCell width="15%">{t('page.widget.taula.columna.tipusIndicador')}</TableCell>
                            { mostrarUnitat && (<TableCell width="15%">{t('page.widget.taula.columna.periodeIndicador')}</TableCell>) }
                            <TableCell width="2%" align="center">
                                {/*{t('page.widget.taula.columna.accions')}*/}
                            </TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {columnes.map((columna, index) => (
                            <TableRow
                                key={index}
                                draggable
                                tabIndex={0}
                                onFocus={() => handleRowFocus(index)}
                                onBlur={() => handleRowBlur(index)}
                                onDragStart={(e) => handleDragStart(e, index)}
                                onDragOver={(e) => handleDragOver(e, index)}
                                onDrop={(e) => handleDrop(e, index)}
                                onDragEnd={handleDragEnd}
                                sx={{
                                    cursor: 'grab',
                                    opacity: draggedIndex === index ? 0.8 : 1,
                                    '&:hover .drag-handle': {
                                        opacity: 1
                                    },
                                    // Highlight the drop target
                                    ...(dragOverIndex === index && draggedIndex !== index ? {
                                        borderTop: '2px solid #1976d2',
                                        backgroundColor: 'rgba(25, 118, 210, 0.08)'
                                    } : {}),
                                    // Highlight the focused row
                                    ...(focusedRowIndex === index ? {
                                        outline: '2px solid #1976d2',
                                        outlineOffset: '-2px'
                                    } : {})
                                }}
                            >
                                <TableCell sx={{px: 1, width: '2%'}}>
                                    <IconButton
                                        className="drag-handle"
                                        size="small"
                                        title={t('page.widget.taula.columna.arrossega')}
                                        aria-label={t('page.widget.taula.columna.arrossega')}
                                        sx={{
                                            px: 0,
                                            cursor: 'grab',
                                            opacity: 0.8,
                                            '&:hover': {
                                                opacity: 1
                                            }
                                        }}
                                    >
                                        <DragIndicatorIcon fontSize="small"/>
                                    </IconButton>
                                </TableCell>
                                <TableCell sx={{pl: 0, pr: 1}}>
                                    <FormField
                                        name={`${name}.${index}.indicador`}
                                        label={t('page.widget.taula.columna.indicador')}
                                        advancedSearchColumns={columnesIndicador}
                                        value={columna.indicador}
                                        onChange={(value) => handleFieldChange(index, 'indicador', value)}
                                        field={fields?.find((field) => field.name === "indicador")}
                                        fieldError={getFieldError(`${name}.${index}.indicador`)}
                                        componentProps={{
                                            onBlur: () => handleFieldBlur(`${name}.${index}.indicador`)
                                        }}
                                    />
                                </TableCell>
                                <TableCell sx={{pl: 0, pr: 1}}>
                                    <FormField
                                        name={`${name}.${index}.titol`}
                                        label={t('page.widget.taula.columna.titolIndicador')}
                                        value={columna.titol}
                                        onChange={(value) => handleFieldChange(index, 'titol', value)}
                                        field={fields?.find((field) => field.name === "titol")}
                                        fieldError={getFieldError(`${name}.${index}.titol`)}
                                        componentProps={{
                                            onBlur: () => handleFieldBlur(`${name}.${index}.titol`)
                                        }}
                                    />
                                </TableCell>
                                <TableCell sx={{pl: 0, pr: 1}}>
                                    <FormField
                                        name={`${name}.${index}.agregacio`}
                                        label={t('generic.tipus')}
                                        value={columna.agregacio}
                                        hiddenEnumValues={hiddenAgregacioValues}
                                        onChange={(value) => handleFieldChange(index, 'agregacio', value)}
                                        field={fields?.find((field) => field.name === "agregacio")}
                                        fieldError={getFieldError(`${name}.${index}.agregacio`)}
                                        componentProps={{
                                            onBlur: () => handleFieldBlur(`${name}.${index}.agregacio`)
                                        }}
                                    />
                                </TableCell>
                                { mostrarUnitat && (
                                    <TableCell sx={{pl: 0, pr: 1}}>
                                        <FormField
                                            name={`${name}.${index}.unitatAgregacio`}
                                            label={t('generic.periode')}
                                            value={columna.unitatAgregacio}
                                            onChange={(value) => handleFieldChange(index, 'unitatAgregacio', value)}
                                            field={fields?.find((field) => field.name === "unitatAgregacio")}
                                            fieldError={getFieldError(`${name}.${index}.unitatAgregacio`)}
                                            componentProps={{
                                                onBlur: () => handleFieldBlur(`${name}.${index}.unitatAgregacio`)
                                            }}
                                            disabled={columnes?.[index]?.agregacio !== 'AVERAGE'}
                                        />
                                    </TableCell>
                                )}
                                <TableCell sx={{pl: 0, pr: 1}} align="center">
                                    <IconButton
                                        size="small"
                                        color="error"
                                        onClick={() => handleRemoveRow(index)}
                                        aria-label="delete"
                                        disabled={columnes.length <= 1}
                                    >
                                        <DeleteIcon fontSize="small"/>
                                    </IconButton>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>

            <Button
                variant="outlined"
                startIcon={<AddIcon/>}
                onClick={handleAddRow}
                size="small"
            >
                {t('Afegir columna')}
            </Button>
        </Box>
    );
};

export default ColumnesTable;
