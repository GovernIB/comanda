import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import FasesCompactacio from './FasesCompactacio';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            selector({
                page: {
                    apps: {
                        progress: {
                            diaries: 'Diàries',
                            weeklies: 'Setmanals',
                            monthlies: 'Mensuals',
                        },
                    },
                },
            }),
    }),
}));

describe('FasesCompactacio', () => {
    it('FasesCompactacio_quanNoHiHaCompactacio_mostraUnSolSegmentDiari', () => {
        // Comprova que el component representa només la fase diària quan no hi ha setmanal ni mensual.
        render(<FasesCompactacio s={0} m={0} e={12} total={12} />);

        expect(screen.getByTitle('Diàries')).toBeInTheDocument();
        expect(screen.queryByTitle('Setmanals')).not.toBeInTheDocument();
        expect(screen.queryByTitle('Mensuals')).not.toBeInTheDocument();
    });

    it('FasesCompactacio_quanHiHaTresFases_renderitzaTotsElsSegments', () => {
        // Verifica que el component crea els trams diari, setmanal i mensual quan toca.
        render(<FasesCompactacio s={3} m={6} e={12} total={12} />);

        expect(screen.getByTitle('Diàries')).toBeInTheDocument();
        expect(screen.getByTitle('Setmanals')).toBeInTheDocument();
        expect(screen.getByTitle('Mensuals')).toBeInTheDocument();
    });
});
