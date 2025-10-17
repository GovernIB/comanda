import * as React from 'react';
import { useTranslation } from 'react-i18next';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import FormGroup from '@mui/material/FormGroup';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Box from '@mui/material/Box';
import {
    GridColumnHeaderTitle,
    GridGroupNode,
    GridTreeDataGroupingCell,
} from '@mui/x-data-grid-pro';
import { useResourceApiService } from 'reactlib';

export const useTreeData = (
    getTreeDataPath: (row: any) => string[],
    headerName?: string,
    headerFlex?: number,
    expandedByDefault?: boolean,
    enabledByDefault?: boolean,
    groupingColDefAdditionalProps?: any) => {
    const { t } = useTranslation();
    const [treeView, setTreeView] = React.useState<boolean>(enabledByDefault ?? true);
    const [expandAll, setExpandAll] = React.useState<boolean>(expandedByDefault ?? false);
    const [expansionState, setExpansionState] = React.useState<any>({});
    const treeViewSwitch = <FormGroup sx={{ ml: 2 }}>
        <FormControlLabel
            label={t('treeData.treeView')}
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
    const isGroupExpandedByDefault = React.useCallback(
        (node: GridGroupNode) =>
            expansionState[node.id] != null ? expansionState[node.id] : expandAll,
        [expandAll, expansionState]
    );
    const groupingColDef = React.useMemo(() => ({
            headerName,
            flex: headerFlex,
            renderHeader: (params: any) => (<Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    width: '100%',
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
    ), [expansionState, groupingColDefAdditionalProps, headerFlex, headerName]);
    const dataGridProps = treeView ? {
        treeData: true as true,
        autoHeight: true as true,
        isGroupExpandedByDefault,
        getTreeDataPath,
        groupingColDef,
    } : {
        paginationActive: true as true,
    };
    return {
        treeView,
        treeViewSwitch,
        dataGridProps,
    };
}

export const useTreeDataEntornAppRenderCell = () => {
    const { isReady: appApiIsReady, find: appApiFind } = useResourceApiService('app');
    const [apps, setApps] = React.useState<any[]>();
    React.useEffect(() => {
        if (appApiIsReady) {
            appApiFind({ unpaged: true, filter: 'activa:true' })
                .then((response) => setApps(response.rows))
                .catch(() => setApps([]));
        }
    }, [appApiIsReady]);
    return React.useCallback((params: any) => {
        const app = apps?.find((app) => app.id === parseInt(params.formattedValue));
        if (typeof params.id === 'number' || app == null) {
            return <GridTreeDataGroupingCell {...params} />;
        }
        return <GridTreeDataGroupingCell
            {...params}
            formattedValue={
                <Box
                    sx={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                    }}>
                    {app.logo && <img
                        src={'data:image/png;base64,' + app.logo}
                        alt="Logo"
                        style={{ height: '48px' }}/>}
                    {app.nom}
                </Box>
            }
        />;
    }, [apps]);
}
