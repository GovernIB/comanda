import Chip from '@mui/material/Chip';
import { SalutGenericTooltip } from './SalutChipTooltip';
import * as React from 'react';
import { ChipColor } from '../../util/colorUtil';

const SalutChip: React.FC<{
    tooltip?: string;
    backgroundColor?: string;
    textColor?: string;
    label: React.ReactNode;
    icon?: React.ReactElement;
}> = ({ tooltip, backgroundColor, textColor = ChipColor.WHITE, label, icon }) => {
    const chipElement = (
        <Chip
            sx={{
                bgcolor: backgroundColor,
                color: textColor,
            }}
            label={label}
            icon={icon}
            size="small"
        />
    );
    if (tooltip === undefined) {
        return chipElement;
    }
    return <SalutGenericTooltip title={tooltip}>{chipElement}</SalutGenericTooltip>;
};

export default SalutChip;
