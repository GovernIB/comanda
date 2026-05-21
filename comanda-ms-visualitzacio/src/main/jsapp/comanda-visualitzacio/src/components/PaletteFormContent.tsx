import React from 'react';
import { Box, Button, Grid, IconButton, Stack, TextField, Tooltip, Typography } from '@mui/material';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import DeleteIcon from '@mui/icons-material/Delete';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import { useTranslation } from 'react-i18next';

export type PaletteColor = { id?: number; posicio?: number; valor?: string; };
export type PaletteData = { id?: number; nom?: string; descripcio?: string; colors: PaletteColor[]; [key: string]: any; };
export type PaletteMode = 'create' | 'edit' | 'duplicate';
export interface PaletteTheme {
    background: string;
    text: string;
    surface: string;
    surfaceText: string;
    fieldBackground: string;
    fieldText: string;
    border: string;
    accent: string;
    accentText: string;
}

export const normalizeColors = (colors?: PaletteColor[]): PaletteColor[] =>
  [...(colors || [])]
    .filter(Boolean)
    .sort((a, b) => (a.posicio ?? Number.MAX_SAFE_INTEGER) - (b.posicio ?? Number.MAX_SAFE_INTEGER))
    .map((color, posicio) => ({ ...color, posicio }));

export const colorInputValue = (value?: string) => /^#[0-9a-f]{6}$/i.test(value || '') ? value || '#000000' : '#000000';

export const useGetPaletteDialogTitle = () => {
    const { t } = useTranslation();

    return (mode: PaletteMode, nom?: string) => {
        const namePart = nom ? ` (${nom})` : '';
        switch (mode) {
            case 'duplicate': return `${t($ => $.page.palette.duplicatePalette)}${namePart}`;
            case 'edit': return `${t($ => $.page.palette.editPalette)}${namePart}`;
            default: return `${t($ => $.page.palette.newPalette)}${namePart}`;
        }
    };
};

const PaletteBar = ({ colors }: { colors?: PaletteColor[] }) => (
  <Box sx={{ display: 'flex', width: '100%', height: 24, border: '1px solid', borderColor: 'divider', overflow: 'hidden', borderRadius: 1 }}>
    {normalizeColors(colors).map((color, index) => (
      <Tooltip key={`${index}-${color.valor}`} title={`${index}: ${color.valor}`}>
        <Box sx={{ flex: 1, minWidth: 16, bgcolor: color.valor || 'transparent' }} />
      </Tooltip>
    ))}
  </Box>
);

interface PaletteFormContentProps {
  palette: PaletteData;
  onChange: (palette: PaletteData) => void;
  mode?: PaletteMode;
  showDuplicateButton?: boolean;
  onDuplicate?: () => void;
  disabled?: boolean;
  paletteTheme?: PaletteTheme;
}

export const PaletteFormContent: React.FC<PaletteFormContentProps> = ({
  palette,
  onChange,
  mode = 'create',
  showDuplicateButton = false,
  onDuplicate,
  disabled = false,
  paletteTheme,
}) => {
  const { t } = useTranslation();
  const colors = normalizeColors(palette.colors);

  const setColors = (nextColors: PaletteColor[]) => {
    onChange({ ...palette, colors: normalizeColors(nextColors) });
  };

  const updateColor = (index: number, valor: string) => {
    const nextColors = [...colors];
    nextColors[index] = { ...nextColors[index], valor };
    setColors(nextColors);
  };

  const moveColor = (index: number, direction: -1 | 1) => {
    const target = index + direction;
    if (target < 0 || target >= colors.length) return;
    const nextColors = [...colors];
    nextColors[index].posicio = target;
    nextColors[target].posicio = index;
    [nextColors[index], nextColors[target]] = [nextColors[target], nextColors[index]];
    setColors(nextColors);
  };

  const addColor = () => setColors([...colors, { posicio: colors.length, valor: '#000000' }]);
  const deleteColor = (index: number) => setColors(colors.filter((_, posicio) => posicio !== index));

  const themeOverrides = paletteTheme ? {
    "& .MuiOutlinedInput-root": {
      bgcolor: paletteTheme.fieldBackground,
      color: paletteTheme.fieldText,
      "& .MuiOutlinedInput-notchedOutline": { borderColor: paletteTheme.border },
      "&:hover .MuiOutlinedInput-notchedOutline": { borderColor: paletteTheme.accent },
      "&.Mui-focused .MuiOutlinedInput-notchedOutline": { borderColor: paletteTheme.accent },
    },
    "& .MuiInputLabel-root": {
      color: paletteTheme.surfaceText,
      "&.Mui-focused": { color: paletteTheme.accent },
    },
    "& .MuiSvgIcon-root": { color: paletteTheme.fieldText },
    "& .MuiButtonBase-root": { color: paletteTheme.text, },
    "& .MuiButton-outlined": { borderColor: paletteTheme.accent, },
    "& .MuiButton-outlined:hover": { borderColor: paletteTheme.accent, },
    "& .MuiButtonBase-root .MuiSvgIcon-root": { color: "inherit", },
  } : {};

  return (
    <Stack spacing={2} sx={{ width: '100%', ...themeOverrides }}>
      <Grid container spacing={2}>
        <Grid size={12}>
          <TextField fullWidth size="small" label={t($ => $.page.palette.nom)} value={palette.nom || ''}
            onChange={(e) => onChange({ ...palette, nom: e.target.value })} disabled={disabled} />
        </Grid>
        <Grid size={12}>
          <TextField fullWidth size="small" label={t($ => $.page.palette.descripcio)} value={palette.descripcio || ''}
            onChange={(e) => onChange({ ...palette, descripcio: e.target.value })} disabled={disabled} />
        </Grid>
      </Grid>

      <Stack spacing={1}>
        <Typography variant="subtitle2">{t($ => $.page.palette.colors)}</Typography>
        <PaletteBar colors={colors} />
        {colors.map((color, index) => (
          <Stack key={`${index}-${color.valor}`} direction="row" spacing={1} alignItems="center">
            <Typography variant="body2" sx={{ width: 28 }}>{index}</Typography>
            <TextField type="color" size="small" value={colorInputValue(color.valor)}
              onChange={(e) => updateColor(index, e.target.value)} sx={{ width: 64 }} disabled={disabled} />
            <TextField size="small" value={color.valor || ''}
              onChange={(e) => updateColor(index, e.target.value)} sx={{ flex: 1 }} disabled={disabled} />
            <Tooltip title={t($ => $.page.palette.upElement)}>
              <span><IconButton size="small" onClick={() => moveColor(index, -1)} disabled={disabled || index === 0}><ArrowUpwardIcon fontSize="small" /></IconButton></span>
            </Tooltip>
            <Tooltip title={t($ => $.page.palette.downElement)}>
              <span><IconButton size="small" onClick={() => moveColor(index, 1)} disabled={disabled || index === colors.length - 1}><ArrowDownwardIcon fontSize="small" /></IconButton></span>
            </Tooltip>
            <Tooltip title={t($ => $.common.delete)}>
              <span><IconButton size="small" onClick={() => deleteColor(index)} disabled={disabled || colors.length <= 1}><DeleteIcon fontSize="small" /></IconButton></span>
            </Tooltip>
          </Stack>
        ))}
        <Button variant="outlined" startIcon={<AddCircleOutlineIcon />} onClick={addColor} disabled={disabled}>{t($ => $.page.palette.addColor)}</Button>
      </Stack>

      {showDuplicateButton && mode === 'edit' && onDuplicate && (
        <Box sx={{ mt: 1, pt: 1, borderTop: '1px solid', borderColor: 'divider' }}>
          <Button onClick={onDuplicate} startIcon={<ContentCopyIcon />} variant="text" size="small" sx={{ width: '100%', justifyContent: 'flex-start' }}>
            {t($ => $.page.palette.duplicatePalette)}
          </Button>
        </Box>
      )}
    </Stack>
  );
};