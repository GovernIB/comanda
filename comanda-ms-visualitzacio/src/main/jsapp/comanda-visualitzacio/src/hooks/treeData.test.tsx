import { fireEvent, render, renderHook, screen } from '@testing-library/react';
import { createRef } from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { useTreeData, useTreeDataWithoutSwitch } from './treeData';
import type { GridGroupingColDefOverride } from '@mui/x-data-grid-pro';

const useTranslationMock = vi.fn(() => ({
    t: (selector: (input: { treeData: { expandAll: string; collapseAll: string; treeView: string } }) => string) =>
        selector({
            treeData: {
                expandAll: 'Expandir tot',
                collapseAll: 'Col·lapsar tot',
                treeView: 'Vista arbre',
            },
        }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => useTranslationMock(),
}));

vi.mock('@mui/x-data-grid-pro', () => ({
    GridColumnHeaderTitle: ({ label }: { label: string }) => <span>{label}</span>,
}));

describe('useTreeDataWithoutSwitch', () => {
    afterEach(() => {
        useTranslationMock.mockClear();
    });

    it('useTreeDataWithoutSwitch_quanEstaHabilitat_retornaLesPropsDeTreeData', () => {
        // Comprova que el hook exposa la configuració completa de treeData quan està activat.
        const getTreeDataPath = (row: { path: string[] }) => row.path;
        const gridApiRef = createRef<any>();
        const { result } = renderHook(() =>
            useTreeDataWithoutSwitch(getTreeDataPath, gridApiRef, 'Jerarquia', 2, true, true)
        );
        const groupingColDef = result.current.dataGridProps.groupingColDef as GridGroupingColDefOverride;

        expect(result.current.dataGridProps.treeData).toBe(true);
        expect(result.current.dataGridProps.getTreeDataPath).toBe(getTreeDataPath);
        expect(result.current.dataGridProps.isGroupExpandedByDefault?.({} as any)).toBe(true);
        expect(groupingColDef.headerName).toBe('Jerarquia');
    });

    it('useTreeDataWithoutSwitch_quanEstaDeshabilitat_ometeixLaBanderaTreeData', () => {
        // Verifica que el hook no activa treeData quan es demana explícitament la vista plana.
        const getTreeDataPath = (row: { path: string[] }) => row.path;
        const gridApiRef = createRef<any>();
        const { result } = renderHook(() =>
            useTreeDataWithoutSwitch(getTreeDataPath, gridApiRef, 'Jerarquia', 2, false, false)
        );

        expect(result.current.dataGridProps.treeData).toBeUndefined();
        expect(result.current.dataGridProps.getTreeDataPath).toBe(getTreeDataPath);
    });

    it('useTreeDataWithoutSwitch_quanEsPremenElsBotonsDeCapcalera_cridaExpandICollapse', () => {
        // Comprova que els botons de la capçalera deleguen correctament l'expansió i col·lapse de files.
        const expandAllRows = vi.fn();
        const collapseAllRows = vi.fn();
        const gridApiRef = {
            current: {
                expandAllRows,
                collapseAllRows,
            },
        } as any;
        const { result } = renderHook(() =>
            useTreeDataWithoutSwitch((row: { path: string[] }) => row.path, gridApiRef, 'Jerarquia', 2)
        );
        const groupingColDef = result.current.dataGridProps.groupingColDef as GridGroupingColDefOverride;
        const header = groupingColDef.renderHeader?.({
            colDef: {
                headerName: 'Jerarquia',
                computedWidth: 240,
            },
        } as any);

        render(<>{header}</>);

        fireEvent.click(screen.getByTitle('Expandir tot'));
        fireEvent.click(screen.getByTitle('Col·lapsar tot'));

        expect(expandAllRows).toHaveBeenCalledTimes(1);
        expect(collapseAllRows).toHaveBeenCalledTimes(1);
    });
});

describe('useTreeData', () => {
    it('useTreeData_quanEsCanviaLSwitch_actualitzaLEstatIDesactivaTreeData', () => {
        // Verifica que l'switch de vista arbre modifica l'estat intern del hook.
        const getTreeDataPath = (row: { path: string[] }) => row.path;
        const gridApiRef = createRef<any>();
        const { result } = renderHook(() =>
            useTreeData(getTreeDataPath, gridApiRef, 'Jerarquia', 2, false, true)
        );
        const view = render(result.current.treeViewSwitch);

        expect(result.current.treeView).toBe(true);

        fireEvent.click(screen.getByRole('switch'));
        view.rerender(result.current.treeViewSwitch);

        expect(result.current.treeView).toBe(false);
        expect(result.current.dataGridProps.treeData).toBeUndefined();
    });
});
