import React from 'react';
import Box from '@mui/material/Box';
import Link from '@mui/material/Link';
import { FormFieldReference } from '../../lib/components/mui/form/FormFieldReference';
import { FormFieldCustomProps } from '../../lib/components/form/FormField';
import { useTranslation } from 'react-i18next';
import { useParameterContext } from './ParameterContext';

const DEFAULT_PAGE_SIZE = 20;

// Id de l'opció "falsa" que FormFieldReference afegeix a la llista quan hi ha més resultats
// dels que es mostren (text "Mostrant X de Y elements").
const PAGE_LABEL_OPTION_ID = '___pageLabel';

// Contingut de l'opció "Mostrant X de Y elements", amb un enllaç "Mostrar tots..." afegit.
// És una opció desactivada (getOptionDisabled la marca com a tal), de manera que un clic no
// la selecciona; l'stopPropagation evita que arribi al gestor de clics del desplegable.
const PageLabelWithShowAll: React.FC<{ text: string; onShowAll: () => void }> = ({
    text,
    onShowAll,
}) => {
    const { t } = useTranslation();
    return (
        <Box
            component="span"
            sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%', gap: 1 }}
        >
            <span>{text}</span>
            <Link
                component="button"
                type="button"
                underline="hover"
                // L'opció és "disabled" (perquè un clic no la seleccioni com a valor del camp),
                // i MUI hi aplica pointer-events: none; el reactivem només per aquest enllaç.
                sx={{ pointerEvents: 'auto', cursor: 'pointer' }}
                onClick={(event) => {
                    event.stopPropagation();
                    onShowAll();
                }}
            >
                {t($ => $.components.appFormFieldReference.mostrarTots)}
            </Link>
        </Box>
    );
};

const AppFormFieldReference: React.FC<FormFieldCustomProps> = (props) => {
    const { maxResultSelects } = useParameterContext();
    const [showAll, setShowAll] = React.useState(false);
    const otherProps = props as any;
    const optionsPageSize = otherProps.optionsPageSize ?? maxResultSelects ?? DEFAULT_PAGE_SIZE;
    const optionsUnpaged = showAll || otherProps.optionsUnpaged;
    const callerOptionRenderer = otherProps.optionRenderer;
    const optionRenderer = (args: { id: any; description: string }) => {
        if (args.id === PAGE_LABEL_OPTION_ID) {
            return <PageLabelWithShowAll text={args.description} onShowAll={() => setShowAll(true)} />;
        }
        return callerOptionRenderer ? callerOptionRenderer(args) : <>{args.description}</>;
    };
    return (
        <FormFieldReference
            {...otherProps}
            optionsPageSize={optionsPageSize}
            optionsUnpaged={optionsUnpaged}
            optionRenderer={optionRenderer}
        />
    );
};

export default AppFormFieldReference;
