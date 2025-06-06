import React, { useState, useCallback } from 'react';
import {
    Box,
    Typography,
    TextField,
    Button,
    Chip,
    IconButton,
    Stack,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';

// ColorPaletteSelector.jsx
const ColorPaletteSelector = ({ initialColors = [], onPaletteChange }) => {
    const [colors, setColors] = useState(initialColors);
    const [newColor, setNewColor] = useState('#000000'); // Valor per defecte negre per al nou color

    // Maneja l'afegit d'un nou color a la paleta
    const handleAddColor = useCallback(() => {
        // Evita duplicats si ja hi ha un color igual (opcional)
        if (!colors.includes(newColor)) {
            const updatedColors = [...colors, newColor];
            setColors(updatedColors);
            // Notifica el canvi al component pare
            if (onPaletteChange) {
                onPaletteChange(updatedColors);
            }
            setNewColor('#000000'); // Reseteja el selector de color per al següent afegit
        } else {
            alert('Aquest color ja existeix a la paleta!'); // O mostra un missatge més amigable
        }
    }, [colors, newColor, onPaletteChange]);

    // Maneja l'eliminació d'un color de la paleta
    const handleDeleteColor = useCallback((colorToDelete) => {
        const updatedColors = colors.filter(color => color !== colorToDelete);
        setColors(updatedColors);
        // Notifica el canvi al component pare
        if (onPaletteChange) {
            onPaletteChange(updatedColors);
        }
    }, [colors, onPaletteChange]);

    // Renderitzat del component
    return (
        <Box sx={{ p: 3, border: '1px solid #ccc', borderRadius: 2, bgcolor: 'background.paper' }}>
            <Typography variant="h6" gutterBottom>
                Editor de Paleta de Colors
            </Typography>

            {/* Secció per afegir nous colors */}
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
                <TextField
                    type="color"
                    value={newColor}
                    onChange={(e) => setNewColor(e.target.value)}
                    sx={{ width: 80, height: 40, p: 0, '& input': { p: 0, m: 0 } }} // Estils per fer l'input de color més compacte
                    inputProps={{ style: { padding: 0, border: 'none' } }}
                />
                <TextField
                    type="text"
                    value={newColor}
                    onChange={(e) => setNewColor(e.target.value)}
                    size="small"
                    label="Codi HEX"
                    sx={{ flexGrow: 1 }}
                />
                <Button
                    variant="contained"
                    onClick={handleAddColor}
                    startIcon={<AddCircleOutlineIcon />}
                    disabled={!newColor || colors.includes(newColor)} // Deshabilita si el color està buit o duplicat
                >
                    Afegir Color
                </Button>
            </Stack>

            {/* Visualització de la paleta de colors actual */}
            <Typography variant="subtitle1" sx={{ mt: 3, mb: 1 }}>
                Paleta actual:
            </Typography>
            {colors.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                    No hi ha colors a la paleta.
                </Typography>
            ) : (
                <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                    {colors.map((color: string, index: number) => (
                        <Chip
                            key={color} // Utilitzem el color com a key (assumint que són únics)
                            label={color.toUpperCase()}
                            sx={{ bgcolor: color, color: (theme) => theme.palette.getContrastText(color) }}
                            deleteIcon={<DeleteIcon />}
                            onDelete={() => handleDeleteColor(color)}
                        />
                    ))}
                </Stack>
            )}
        </Box>
    );
};

export default ColorPaletteSelector;