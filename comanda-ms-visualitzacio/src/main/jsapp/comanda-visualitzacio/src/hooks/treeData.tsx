import * as React from 'react';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Box from '@mui/material/Box';
import {
    GridColumnHeaderTitle,
    GridGroupNode,
} from '@mui/x-data-grid-pro';

export const useTreeData = (
    getTreeDataPath: (row: any) => string[],
    headerName?: string,
    headerFlex?: number,
    groupingColDefAdditionalProps?: any) => {
    const [treeView, setTreeView] = React.useState<boolean>(true);
    const [expandAll, setExpandAll] = React.useState<boolean>(true);
    const [expansionState, setExpansionState] = React.useState<any>({});
    const treeViewSwitch = <FormGroup sx={{ ml: 2 }}>
        <FormControlLabel
            label="Vista en arbre"
            control={
                <Switch
                    checked={treeView}
                    onChange={event => setTreeView(event.target.checked)}/>
            }/>
    </FormGroup>;
    const onRowExpansionChange = (id: any, expanded: boolean) => {
        setExpansionState((prevState: any) => ({
            ...prevState,
            [id]: expanded,
        }));
    };
    const dataGridProps = treeView ? {
        treeData: true as true,
        autoHeight: true as true,
        isGroupExpandedByDefault: (node: GridGroupNode) => expansionState[node.id] != null ? expansionState[node.id] : expandAll,
        getTreeDataPath,
        groupingColDef: {
            headerName,
            flex: headerFlex,
            renderHeader: (params: any) => (<Box
                sx={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    width: "100%",
                    flex: 1,
                }}>
                <GridColumnHeaderTitle label={params?.colDef?.headerName} columnWidth={params.colDef.computedWidth} />
                <Box sx={{ display: 'flex', gap: 1, ml: 1 }}>
                    <IconButton
                        size="small"
                        onClick={() => {
                            setExpandAll(true);
                            if (expansionState != null) {
                                Object.keys(expansionState).map((id) => {
                                    onRowExpansionChange(id, true);
                                });
                            }
                        }}>
                        <Icon fontSize="small">unfold_more</Icon>
                    </IconButton>
                    <IconButton
                        size="small"
                        onClick={() => {
                            setExpandAll(false);
                            if (expansionState != null) {
                                Object.keys(expansionState).map((id) => {
                                    onRowExpansionChange(id, false);
                                });
                            }
                        }}>
                        <Icon fontSize="small">unfold_less</Icon>
                    </IconButton>
                </Box>
            </Box>),
            ...groupingColDefAdditionalProps
        }
    } : {
        paginationActive: true as true,
    };
    return {
        treeView,
        treeViewSwitch,
        dataGridProps,
    };
}