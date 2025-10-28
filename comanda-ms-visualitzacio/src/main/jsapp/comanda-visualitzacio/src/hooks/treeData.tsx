import * as React from 'react';
import { useTranslation } from 'react-i18next';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Box from '@mui/material/Box';
import {
    GRID_ROOT_GROUP_ID,
    GridApiPro,
    GridColumnHeaderTitle,
    GridEventListener,
    GridGroupingColDefOverride,
    GridRowId,
    gridRowTreeSelector,
} from '@mui/x-data-grid-pro';
import { MuiDataGridProps } from 'reactlib';

export const useTreeData = (
    getTreeDataPath: (row: any) => string[],
    gridApiRef: React.RefObject<GridApiPro | null>,
    headerName?: string,
    headerFlex?: number,
    expandedByDefault?: boolean,
    enabledByDefault?: boolean,
    groupingColDefAdditionalProps?: any
) => {
    const { t } = useTranslation();
    const [treeView, setTreeView] = React.useState<boolean>(enabledByDefault ?? true);
    const treeViewSwitch = (
        <FormGroup sx={{ ml: 2 }}>
            <FormControlLabel
                label={t('treeData.treeView')}
                control={
                    <Switch
                        checked={treeView}
                        onChange={(event) => setTreeView(event.target.checked)}
                    />
                }
            />
        </FormGroup>
    );

    // WORKAROUND to maintain the expansion state when isGroupExpandedByDefault is used (https://github.com/mui/mui-x/issues/20049)
    const expandedParents = React.useRef<{ [key: string]: boolean }>({});
    const isGroupExpandedByDefault = React.useCallback(
        (node: { id: string | number }) => {
            return expandedParents.current[node.id] ?? expandedByDefault ?? false;
        },
        [expandedByDefault]
    );
    React.useEffect(() => {
        const onRowExpansionChange: GridEventListener<'rowExpansionChange'> = (node) => {
            // If node.id is GRID_ROOT_GROUP_ID, expandAllRows or collapseAllRows has been called
            if (node.id === GRID_ROOT_GROUP_ID) {
                // node.childrenExpanded should always be true or false
                const newExpansionState = node.childrenExpanded ?? false;
                const tree = { ...gridRowTreeSelector(gridApiRef) };

                const traverse = (nodeId: GridRowId) => {
                    const node = tree[nodeId];
                    if (node?.type === 'group') {
                        expandedParents.current[node.id] = newExpansionState;
                        node.children.forEach(traverse);
                    }
                };
                traverse(GRID_ROOT_GROUP_ID);

                return;
            }

            expandedParents.current[node.id] = !!node.childrenExpanded;
        };

        return gridApiRef.current?.subscribeEvent('rowExpansionChange', onRowExpansionChange);
    }, [gridApiRef]);
    // WORKAROUND ends here

    const groupingColDef = React.useMemo<GridGroupingColDefOverride>(
        () => ({
            headerName,
            flex: headerFlex,
            hideDescendantCount: true,
            renderHeader: (params) => (
                <Box
                    sx={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        width: '100%',
                        flex: 1,
                    }}
                >
                    <GridColumnHeaderTitle
                        label={params?.colDef?.headerName ?? ""}
                        columnWidth={params.colDef.computedWidth}
                    />
                    <Box sx={{ display: 'flex', gap: 1, ml: 1 }}>
                        <IconButton
                            size="small"
                            onClick={() => {
                                gridApiRef.current?.expandAllRows();
                            }}
                        >
                            <Icon fontSize="small">unfold_more</Icon>
                        </IconButton>
                        <IconButton
                            size="small"
                            onClick={() => {
                                gridApiRef.current?.collapseAllRows();
                            }}
                        >
                            <Icon fontSize="small">unfold_less</Icon>
                        </IconButton>
                    </Box>
                </Box>
            ),
            ...groupingColDefAdditionalProps,
        }),
        [gridApiRef, groupingColDefAdditionalProps, headerFlex, headerName]
    );
    const dataGridProps: Partial<MuiDataGridProps> = treeView
        ? {
              treeData: true,
              isGroupExpandedByDefault,
              getTreeDataPath,
              groupingColDef,
          }
        : {
            // If getTreeDataPath is not provided when disabling treeData, an error will be thrown
            getTreeDataPath,
        };
    return {
        treeView,
        treeViewSwitch,
        dataGridProps,
    };
};
