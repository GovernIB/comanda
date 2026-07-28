import React from 'react';
import { SimpleTreeView } from '@mui/x-tree-view/SimpleTreeView';
import { TreeItem } from '@mui/x-tree-view/TreeItem';
import { useResourceApiService } from 'reactlib';

type GrupTree = Map<string, string[]>;

const makeGrupId = (grup: string) => `g:${grup}`;
const makeSubGrupId = (grup: string, subGrup: string) => `sg:${grup}:${subGrup}`;

const parseItemId = (id: string): { grup: string; subGrup: string | null } | null => {
    if (id.startsWith('sg:')) {
        const rest = id.substring(3);
        const idx = rest.indexOf(':');
        if (idx < 0) return null;
        return { grup: rest.substring(0, idx), subGrup: rest.substring(idx + 1) };
    }
    if (id.startsWith('g:')) {
        return { grup: id.substring(2), subGrup: null };
    }
    return null;
};

const isSelectionValid = (itemId: string, tree: GrupTree): boolean => {
    const parsed = parseItemId(itemId);
    if (!parsed) return false;
    if (!tree.has(parsed.grup)) return false;
    if (parsed.subGrup !== null) {
        return tree.get(parsed.grup)?.includes(parsed.subGrup) ?? false;
    }
    return true;
};

const buildGrupTree = (rows: Array<{ grup?: string; subGrup?: string }>): GrupTree => {
    const tree: GrupTree = new Map();
    rows.forEach((p) => {
        if (!p.grup) return;
        if (!tree.has(p.grup)) tree.set(p.grup, []);
        if (p.subGrup && !tree.get(p.grup)!.includes(p.subGrup)) {
            tree.get(p.grup)!.push(p.subGrup);
        }
    });
    return tree;
};

export const ParametresGrups: React.FC<{
    quickFilter?: string;
    onChange: (grup: string | null, subGrup: string | null) => void;
}> = ({ quickFilter, onChange }) => {
    const { isReady, find } = useResourceApiService('parametre');
    const [grupTree, setGrupTree] = React.useState<GrupTree>(new Map());
    const [selectedItemId, setSelectedItemId] = React.useState<string>('');
    const selectedItemIdRef = React.useRef(selectedItemId);
    const onChangeRef = React.useRef(onChange);

    React.useEffect(() => { selectedItemIdRef.current = selectedItemId; }, [selectedItemId]);
    React.useEffect(() => { onChangeRef.current = onChange; }, [onChange]);

    React.useEffect(() => {
        if (!isReady) return;
        find({
            quickFilter: quickFilter || undefined,
            sorts: ['grup,asc', 'subGrup,asc'],
            unpaged: true,
        }).then((response) => {
            const tree = buildGrupTree(response.rows as Array<{ grup?: string; subGrup?: string }>);
            setGrupTree(tree);

            if (!isSelectionValid(selectedItemIdRef.current, tree) && tree.size > 0) {
                const firstGrup = tree.keys().next().value!;
                const newId = makeGrupId(firstGrup);
                const newSubGrup = null;
                setSelectedItemId(newId);
                onChangeRef.current(firstGrup, newSubGrup);
            }
        });
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isReady, quickFilter]);

    const handleSelectedItemsChange = (_event: React.SyntheticEvent | null, id: string | string[] | null) => {
        const itemId = Array.isArray(id) ? (id.length > 0 ? id[0] : null) : id;
        if (!itemId) return;
        const parsed = parseItemId(itemId);
        if (!parsed) return;
        setSelectedItemId(itemId);
        onChange(parsed.grup, parsed.subGrup);
    };

    return (
        <SimpleTreeView
            selectedItems={selectedItemId}
            onSelectedItemsChange={handleSelectedItemsChange}
            sx={{
                '& .MuiTreeItem-content': { minHeight: 40, paddingY: 0.5 },
                '& .MuiTreeItem-label': { fontSize: '14px' },
            }}
        >
            {Array.from(grupTree.entries()).map(([grup, subGrups]) => (
                <TreeItem key={makeGrupId(grup)} itemId={makeGrupId(grup)} label={grup}>
                    {subGrups.map((subGrup) => (
                        <TreeItem
                            key={makeSubGrupId(grup, subGrup)}
                            itemId={makeSubGrupId(grup, subGrup)}
                            label={subGrup}
                        />
                    ))}
                </TreeItem>
            ))}
        </SimpleTreeView>
    );
};
