import * as React from 'react';
import Box from '@mui/material/Box';
import { FilterClearButton } from '../lot2';

type FilterClearAllButtonProps = {
    /** Altres filtres de la pantalla que també s'han de buidar (el filtre que conté el botó es buida sol). */
    otherFilterApiRefs: Array<React.RefObject<{ clear: () => void } | null>>;
    /** Acció addicional en netejar. */
    onClear?: () => void;
};

/**
 * Botó de netejar de lot2 (`FilterClearButton`) per a pantalles amb més d'un filtre: ha d'anar dins el
 * primer `MuiFilter`, que és el que buida el propi botó; aquest embolcall buida abans els altres.
 */
const FilterClearAllButton: React.FC<FilterClearAllButtonProps> = ({ otherFilterApiRefs, onClear }) => (
    <Box
        sx={{ display: 'contents' }}
        onClickCapture={() => {
            otherFilterApiRefs.forEach((ref) => ref.current?.clear());
            onClear?.();
        }}
    >
        <FilterClearButton />
    </Box>
);

export default FilterClearAllButton;
