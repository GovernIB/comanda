import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { ParametresGrups } from './ParametresGrups';
import { parseItemId, isSelectionValid, buildGrupTree } from './ParametresGrups';

const findMock = vi.fn();

vi.mock('reactlib', () => ({
    useResourceApiService: vi.fn(() => ({
        isReady: true,
        find: findMock,
    })),
}));

vi.mock('@mui/x-tree-view/SimpleTreeView', () => ({
    SimpleTreeView: ({ children, onSelectedItemsChange, selectedItems }: any) => (
        <div data-testid="simple-tree-view" data-selected={selectedItems}>
            <button
                data-testid="mock-select-comptabilitat"
                onClick={() => onSelectedItemsChange(null, 'sg:Finances:Comptabilitat')}
            >
                Seleccionar Comptabilitat
            </button>
            {children}
        </div>
    ),
}));

vi.mock('@mui/x-tree-view/TreeItem', () => ({
    TreeItem: ({ itemId, label, children }: any) => (
        <div data-testid={`tree-item-${itemId}`}>
            {label}
            {children}
        </div>
    ),
}));

beforeEach(() => {
    vi.clearAllMocks();
    findMock.mockResolvedValue({ rows: [] });
});

afterEach(() => {
    vi.clearAllMocks();
});

describe('Funcions auxiliars de ParametresGrups', () => {
    it('parseItemId identifica correctament grups i subgrups', () => {
        expect(parseItemId('g:Finances')).toEqual({ grup: 'Finances', subGrup: null });
        expect(parseItemId('sg:Finances:Comptabilitat')).toEqual({ grup: 'Finances', subGrup: 'Comptabilitat' });
        expect(parseItemId('invalid')).toBeNull();
        expect(parseItemId('sg:incomplet')).toBeNull();
    });

    it('buildGrupTree construeix el mapa correctament sense duplicats', () => {
        const rows = [
            { grup: 'G1', subGrup: 'S1' },
            { grup: 'G1', subGrup: 'S1' },
            { grup: 'G1', subGrup: 'S2' },
            { grup: 'G2' },
            { grup: undefined, subGrup: 'S3' },
        ];

        const tree = buildGrupTree(rows);
        expect(tree.size).toBe(2);
        expect(tree.get('G1')).toEqual(['S1', 'S2']);
        expect(tree.get('G2')).toEqual([]);
    });

    it('isSelectionValid valida correctament els IDs', () => {
        const tree = buildGrupTree([{ grup: 'G1', subGrup: 'S1' }]);
        expect(isSelectionValid('g:G1', tree)).toBe(true);
        expect(isSelectionValid('sg:G1:S1', tree)).toBe(true);
        expect(isSelectionValid('sg:G1:S2', tree)).toBe(false);
        expect(isSelectionValid('g:G2', tree)).toBe(false);
    });
});

describe('ParametresGrups Component', () => {
    it('crida a find amb els paràmetres correctes quan està ready', async () => {
        render(<ParametresGrups quickFilter="test" onChange={vi.fn()} />);

        await waitFor(() => {
            expect(findMock).toHaveBeenCalledWith({
                quickFilter: 'test',
                sorts: ['grup,asc', 'subGrup,asc'],
                unpaged: true,
            });
        });
    });

    it('renderitza l\'arbre correctament passant les dades als TreeItem', async () => {
        findMock.mockResolvedValue({
            rows: [
                { grup: 'Finances', subGrup: 'Comptabilitat' },
                { grup: 'Finances', subGrup: 'Facturació' },
                { grup: 'RRHH' },
            ],
        });

        render(<ParametresGrups onChange={vi.fn()} />);

        expect(await screen.findByTestId('tree-item-g:Finances')).toBeInTheDocument();
        expect(screen.getByTestId('tree-item-sg:Finances:Comptabilitat')).toBeInTheDocument();
        expect(screen.getByTestId('tree-item-sg:Finances:Facturació')).toBeInTheDocument();
        expect(screen.getByTestId('tree-item-g:RRHH')).toBeInTheDocument();
    });

    it('auto-selecciona el primer grup i crida onChange quan l\'arbre carrega', async () => {
        const onChangeMock = vi.fn();
        findMock.mockResolvedValue({
            rows: [{ grup: 'Finances', subGrup: 'Comptabilitat' }],
        });

        render(<ParametresGrups onChange={onChangeMock} />);

        await waitFor(() => {
            expect(onChangeMock).toHaveBeenCalledWith('Finances', null);
        });

        expect(screen.getByTestId('simple-tree-view')).toHaveAttribute('data-selected', 'g:Finances');
    });

    it('quan onSelectedItemsChange es dispara, crida onChange amb les dades parsejades correctes', async () => {
        const onChangeMock = vi.fn();
        findMock.mockResolvedValue({
            rows: [{ grup: 'Finances', subGrup: 'Comptabilitat' }],
        });

        render(<ParametresGrups onChange={onChangeMock} />);

        const user = userEvent.setup();
        await user.click(screen.getByTestId('mock-select-comptabilitat'));

        await waitFor(() => {
            expect(onChangeMock).toHaveBeenCalledWith('Finances', 'Comptabilitat');
        });

        expect(screen.getByTestId('simple-tree-view')).toHaveAttribute('data-selected', 'sg:Finances:Comptabilitat');
    });
});
