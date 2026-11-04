import React from 'react';
import Chip from '@mui/material/Chip';
import { useTheme, type SxProps, type Theme } from '@mui/material/styles';

export interface AppGrupChipProps {
    nom?: string | null;
    colorClar?: string | null;
    colorFosc?: string | null;
    size?: 'small' | 'medium';
    sx?: SxProps<Theme>;
}

export const AppGrupChip: React.FC<AppGrupChipProps> = ({
    nom,
    colorClar,
    colorFosc,
    size = 'small',
    sx,
}) => {
    const theme = useTheme();

    if (!nom) {
        return null;
    }

    const isDarkMode = theme.palette.mode === 'dark';
    const chosenColor = isDarkMode ? (colorFosc || colorClar) : (colorClar || colorFosc);
    const bgColor = chosenColor || (isDarkMode ? '#424242' : '#e0e0e0');

    let textColor = theme.palette.text.primary;
    if (chosenColor) {
        try {
            textColor = theme.palette.getContrastText(chosenColor);
        } catch {
            textColor = isDarkMode ? '#ffffff' : '#000000';
        }
    }

    return (
        <Chip
            size={size}
            label={nom}
            sx={{
                backgroundColor: bgColor,
                color: textColor,
                fontWeight: 500,
                fontSize: size === 'small' ? '0.75rem' : '0.875rem',
                height: size === 'small' ? 22 : 28,
                borderRadius: '12px',
                border: isDarkMode ? '1px solid rgba(255,255,255,0.12)' : '1px solid rgba(0,0,0,0.08)',
                ...sx,
            }}
        />
    );
};

export default AppGrupChip;
