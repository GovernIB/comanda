import { createTheme } from '@mui/material/styles';
import { describe, expect, it } from 'vitest';
import estils from './WidgetEstils';

describe('WidgetEstils', () => {
    it('WidgetEstils_paperContainer_quanHiHaClick_activaCursorHoverIBorder', () => {
        // Comprova que l'estil del contenidor activa la vora i el hover quan hi ha acció de clic.
        const theme = createTheme();
        const style = estils.paperContainer(
            '#ffffff',
            'linear-gradient(#fff, #eee)',
            '#111111',
            true,
            2,
            '#999999',
            () => undefined,
            theme
        );

        expect(style).toEqual(
            expect.objectContaining({
                backgroundColor: '#ffffff',
                border: '2px solid #999999',
                cursor: 'pointer',
            })
        );
        expect(style['&:hover']).toEqual(expect.objectContaining({ boxShadow: theme.shadows[4] }));
    });

    it('WidgetEstils_tableHeaderITableRow_configurenElsSeparadorsSegonsLesOpcions', () => {
        // Verifica que els estils de capçalera i fila apliquen els separadors horitzontals i verticals esperats.
        const headerStyle = estils.tableHeader('#111', '#eee', '#ccc', 2, true, '#ddd', 3);
        const rowStyle = estils.tableRow('#222', '#fafafa', true, '#aaa', 1, false, '#ddd', 2);

        expect(headerStyle['& > *']).toEqual(
            expect.objectContaining({
                borderRight: '3px solid #ddd',
                borderBottom: '2px solid #ccc',
            })
        );
        expect(rowStyle['& > *']).toEqual(
            expect.objectContaining({
                borderRight: 'none',
                borderBottom: '1px solid #aaa',
            })
        );
    });
});
