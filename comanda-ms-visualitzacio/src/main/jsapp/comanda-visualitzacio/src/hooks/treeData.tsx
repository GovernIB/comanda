import * as React from 'react';
import { useTranslation } from 'react-i18next';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Box from '@mui/material/Box';
import {
    GridApiPro,
    GridColumnHeaderTitle,
    GridGroupingColDefOverride,
} from '@mui/x-data-grid-pro';
import { MuiDataGridProps } from 'reactlib';

export const useTreeDataWithoutSwitch = (
    getTreeDataPath: (row: any) => string[],
    gridApiRef: React.RefObject<GridApiPro | null>,
    headerName?: string,
    headerFlex?: number,
    expandedByDefault?: boolean,
    // By default, treeView is enabled
    enabled: boolean = true,
    groupingColDefAdditionalProps?: any
) => {
    const { t } = useTranslation();
    const isGroupExpandedByDefault = React.useCallback(() => {
        return expandedByDefault ?? false;
    }, [expandedByDefault]);

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
                        label={params?.colDef?.headerName ?? ''}
                        columnWidth={params.colDef.computedWidth}
                    />
                    <Box sx={{ display: 'flex', gap: 1, ml: 1 }}>
                        <IconButton
                            size="small"
                            onClick={() => {
                                gridApiRef.current?.expandAllRows();
                            }}
                            title={t($ => $.treeData.expandAll)}
                        >
                            <Icon fontSize="small">unfold_more</Icon>
                        </IconButton>
                        <IconButton
                            size="small"
                            onClick={() => {
                                gridApiRef.current?.collapseAllRows();
                            }}
                            title={t($ => $.treeData.collapseAll)}
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
    const dataGridProps: Partial<
        Pick<
            MuiDataGridProps,
            'treeData' | 'isGroupExpandedByDefault' | 'getTreeDataPath' | 'groupingColDef'
        >
    > = enabled
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
        dataGridProps,
    };
};

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
                label={t($ => $.treeData.treeView)}
                control={
                    <Switch
                        checked={treeView}
                        onChange={(event) => setTreeView(event.target.checked)}
                    />
                }
            />
        </FormGroup>
    );

    const { dataGridProps } = useTreeDataWithoutSwitch(
        getTreeDataPath,
        gridApiRef,
        headerName,
        headerFlex,
        expandedByDefault,
        treeView,
        groupingColDefAdditionalProps
    );
    return {
        treeView,
        treeViewSwitch,
        dataGridProps,
    };
};
