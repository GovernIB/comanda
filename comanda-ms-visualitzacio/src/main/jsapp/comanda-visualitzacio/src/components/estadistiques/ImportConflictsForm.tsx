import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { TFunction } from 'i18next';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import Alert from '@mui/material/Alert';
import AlertTitle from '@mui/material/AlertTitle';
import Collapse from '@mui/material/Collapse';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Tooltip from '@mui/material/Tooltip';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import { SimpleTreeView, TreeItem } from '@mui/x-tree-view';
import { FormField, useFormContext } from 'reactlib';

export type Conflicte = {
    titol: string;
    tipo: string;
    overwrite?: string;
    nouNom?: string;
    appId?: number;
    entornAppId?: number;
    suggerenciaNouNom?: string;
    bloquejant?: boolean;
    missatgeError?: string;
    codi?: string;
};

export interface ConflictGroupMeta {
    tipo: string;
    label: string;
    icon: string;
    items: { index: number; conflict: Conflicte }[];
}

export const supportsOverwrite = (tipo?: string) => {
    return tipo === 'IndicadorExport' || tipo === 'PlantillaExport' || tipo === 'PaletaExport';
};

export const groupConflicts = (conflicts: Conflicte[], t: TFunction): ConflictGroupMeta[] => {
    const knownTypes = [
        { tipo: 'DashboardExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.dashboard), icon: 'dashboard' },
        { tipo: 'EstadisticaWidgetExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.widget), icon: 'widgets' },
        { tipo: 'IndicadorExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.indicador), icon: 'calculate' },
        { tipo: 'DimensioExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.dimensio), icon: 'view_column' },
        { tipo: 'PlantillaExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.plantilla), icon: 'palette' },
        { tipo: 'PaletaExport', getLabel: () => t($ => $.page.dashboards.action.import.groups.paleta), icon: 'format_color_fill' },
    ];

    const map = new Map<string, { index: number; conflict: Conflicte }[]>();
    conflicts.forEach((conflict, index) => {
        let key = conflict.tipo || 'Other';
        if (key === 'DimensioValorExport') {
            key = 'DimensioExport';
        }
        if (!map.has(key)) {
            map.set(key, []);
        }
        map.get(key)!.push({ index, conflict });
    });

    const groups: ConflictGroupMeta[] = [];

    knownTypes.forEach(({ tipo, getLabel, icon }) => {
        if (map.has(tipo)) {
            groups.push({
                tipo,
                label: getLabel(),
                icon,
                items: map.get(tipo)!,
            });
            map.delete(tipo);
        }
    });

    map.forEach((items, tipo) => {
        groups.push({
            tipo,
            label: tipo === 'Other' ? t($ => $.page.dashboards.action.import.groups.other) : tipo,
            icon: 'category',
            items,
        });
    });

    return groups;
};

export const ConflictsTreeViewItemChild = React.memo(
    ({
        index,
        conflict,
        fieldOverwrite,
        updateConflict,
        fieldNouNom,
        group,
    }: {
        index: number;
        conflict: Conflicte;
        fieldOverwrite: string;
        updateConflict: (index: number, changes: Partial<Conflicte>) => void;
        fieldNouNom: string;
        group: ConflictGroupMeta;
    }) => {
        const { t } = useTranslation();
        return (
            <TreeItem
                itemId={`conflict-${index}`}
                data-testid={`conflict-item-${index}`}
                disableSelection={Boolean(conflict.bloquejant)}
                sx={{
                    // Afegit perquè el label del FormField no es talli
                    "& .MuiTreeItem-label": {
                        overflow: 'visible',
                    },
                    "& .MuiTreeItem-content": {
                        alignItems: 'start',
                        ...(conflict.bloquejant && {
                            color: 'error.main',
                        }),
                    },
                }}
                label={
                    <Box
                        sx={{
                            display: 'flex',
                            flexWrap: 'wrap',
                            alignItems: 'start',
                            justifyContent: 'space-between',
                            pr: 1,
                            gap: 1,
                            width: '100%',
                        }}
                    >
                        <Box
                            sx={{
                                display: 'flex',
                                flexDirection: 'column',
                                gap: 0.5,
                                flex: 1,
                                minWidth: 150,
                            }}
                        >
                            <Box
                                sx={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    height: 24, // Aquesta altura ha de ser la mateixa de l'element MuiSimpleTreeView-itemCheckbox
                                    gap: 1,
                                }}
                            >
                                <Icon
                                    fontSize="small"
                                    sx={{ color: conflict.bloquejant ? 'error.main' : 'text.secondary', flexShrink: 0 }}
                                >
                                    {conflict.bloquejant ? 'error_outline' : group.icon}
                                </Icon>
                                <Typography
                                    variant="body2"
                                    noWrap
                                    sx={{ fontWeight: 500, color: conflict.bloquejant ? 'error.main' : 'inherit' }}
                                    title={
                                        conflict.tipo === 'IndicadorExport' && conflict.codi && conflict.codi !== conflict.titol
                                            ? `${conflict.titol} (${conflict.codi})`
                                            : conflict.titol
                                    }
                                >
                                    {conflict.tipo === 'IndicadorExport' && conflict.codi && conflict.codi !== conflict.titol
                                        ? `${conflict.titol} (${conflict.codi})`
                                        : conflict.titol}
                                </Typography>
                                {conflict.bloquejant && (
                                    <Chip
                                        label={t($ => $.page.dashboards.action.import.blockingChip)}
                                        size="small"
                                        color="error"
                                        variant="outlined"
                                        sx={{ height: 20, fontSize: '0.75rem', fontWeight: 600 }}
                                    />
                                )}
                            </Box>
                            {conflict.bloquejant && conflict.missatgeError && (
                                <Typography
                                    variant="caption"
                                    color="error"
                                    sx={{ pl: 3.5, display: 'block', wordBreak: 'break-word', whiteSpace: 'normal' }}
                                >
                                    {conflict.missatgeError}
                                </Typography>
                            )}
                        </Box>
                        {!conflict.bloquejant && (
                            <Box
                                sx={{
                                    display: 'flex',
                                    alignItems: 'start',
                                    gap: 1,
                                    flexShrink: 0,
                                }}
                                onClick={e => e.stopPropagation()}
                                onKeyDown={e => e.stopPropagation()}
                            >
                                <Box sx={{ minWidth: 160 }}>
                                    <FormField
                                        name={`conflicts[${index}].overwrite`}
                                        value={conflict.overwrite}
                                        field={fieldOverwrite}
                                        onChange={value => updateConflict(index, { overwrite: value })}
                                        componentProps={{ size: 'small', variant: 'standard', label: "" }}
                                        hiddenEnumValues={
                                            conflict.tipo === 'IndicadorExport'
                                                ? ['CREAR_AMB_ALTRE_NOM']
                                                : conflict.tipo === 'DashboardExport'
                                                    ? ['EMPRAR_EXISTENT', 'SOBRESCRIURE']
                                                    : conflict.tipo === 'PlantillaExport' || conflict.tipo === 'PaletaExport'
                                                        ? []
                                                        : ['SOBRESCRIURE']
                                        }
                                        required
                                    />
                                </Box>
                                {conflict.overwrite === 'CREAR_AMB_ALTRE_NOM' && conflict.tipo !== 'IndicadorExport' && (
                                    <Box sx={{ minWidth: 160 }}>
                                        <FormField
                                            name={`conflicts[${index}].nouNom`}
                                            field={fieldNouNom}
                                            value={conflict.nouNom}
                                            onChange={value => updateConflict(index, { nouNom: value })}
                                            componentProps={{ size: 'small', variant: 'standard', label: "", placeholder: conflict.suggerenciaNouNom || conflict.titol }}
                                        />
                                    </Box>
                                )}
                            </Box>
                        )}
                    </Box>
                }
            />
        );
    }
);

export const ConflictsTreeViewItemGroup = React.memo(
    ({
        group,
        fieldOverwrite,
        fieldNouNom,
        updateConflict,
    }: {
        group: ConflictGroupMeta;
        fieldOverwrite: string;
        fieldNouNom: string;
        updateConflict: (index: number, changes: Partial<Conflicte>) => void;
    }) => {
        const hasBlockingItems = React.useMemo(() => {
            return group.items.some(item => item.conflict.bloquejant);
        }, [group.items]);

        return (
            <TreeItem
                itemId={`group-${group.tipo}`}
                label={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 0.5 }}>
                        <Icon fontSize="small" color={hasBlockingItems ? 'error' : 'primary'}>
                            {group.icon}
                        </Icon>
                        <Typography variant="subtitle2" sx={{ fontWeight: 600, color: hasBlockingItems ? 'error.main' : 'inherit' }}>
                            {group.label}
                        </Typography>
                        <Chip
                            label={group.items.length}
                            size="small"
                            color={hasBlockingItems ? 'error' : 'default'}
                            variant="outlined"
                            sx={{ height: 20, fontSize: '0.75rem' }}
                        />
                    </Box>
                }
            >
                {group.items.map(({ index, conflict }) => (
                    <ConflictsTreeViewItemChild
                        key={`conflict-${index}`}
                        index={index}
                        conflict={conflict}
                        fieldOverwrite={fieldOverwrite}
                        updateConflict={updateConflict}
                        fieldNouNom={fieldNouNom}
                        group={group}
                    />
                ))}
            </TreeItem>
        );
    }
);

export const ConflictsTreeView = React.memo(
    ({
        groups,
        selectedItems,
        setSelectedItems,
        fieldOverwrite,
        fieldNouNom,
        updateConflict,
        conflicts,
    }: {
        groups: ConflictGroupMeta[];
        selectedItems: string[];
        setSelectedItems: (items: string[]) => void;
        fieldOverwrite: string;
        fieldNouNom: string;
        updateConflict: (index: number, changes: Partial<Conflicte>) => void;
        conflicts?: Conflicte[];
    }) => {
        const [collapsedByUser, setCollapsedByUser] = React.useState<Set<string>>(new Set());

        React.useEffect(() => {
            if (!conflicts || conflicts.length === 0) {
                setCollapsedByUser(new Set());
            }
        }, [conflicts]);

        const expandedItems = React.useMemo(() => {
            return groups
                .map(g => `group-${g.tipo}`)
                .filter(id => !collapsedByUser.has(id));
        }, [groups, collapsedByUser]);

        const handleExpandedItemsChange = (_event: React.SyntheticEvent | null, itemIds: string[]) => {
            const rawIds = new Set(Array.isArray(itemIds) ? itemIds : itemIds ? [itemIds] : []);
            setCollapsedByUser(prev => {
                const next = new Set(prev);
                groups.forEach(g => {
                    const id = `group-${g.tipo}`;
                    if (rawIds.has(id)) {
                        next.delete(id);
                    } else {
                        next.add(id);
                    }
                });
                return next;
            });
        };

        return (
            <SimpleTreeView
                checkboxSelection
                multiSelect
                selectionPropagation={{ parents: true, descendants: true }}
                selectedItems={selectedItems}
                onSelectedItemsChange={(_event, itemIds) => {
                    const rawIds = Array.isArray(itemIds) ? itemIds : itemIds ? [itemIds] : [];
                    const filteredIds = rawIds.filter(id => {
                        if (id.startsWith('conflict-')) {
                            const idx = parseInt(id.replace('conflict-', ''), 10);
                            return !conflicts?.[idx]?.bloquejant;
                        }
                        return true;
                    });
                    setSelectedItems(filteredIds);
                }}
                expandedItems={expandedItems}
                onExpandedItemsChange={handleExpandedItemsChange}
                sx={{
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1,
                    p: 1,
                    maxHeight: 400,
                    overflow: 'auto',
                    '& .MuiTreeItem-content': {
                        py: 0.5,
                    },
                }}
            >
                {groups.map(group => (
                    <ConflictsTreeViewItemGroup
                        key={`group-${group.tipo}`}
                        group={group}
                        fieldOverwrite={fieldOverwrite}
                        fieldNouNom={fieldNouNom}
                        updateConflict={updateConflict}
                    />
                ))}
            </SimpleTreeView>
        );
    }
);

export interface ImportConflictsFormProps {
    isAnalyzing: boolean;
    onReanalyze?: () => void;
    conflictsDescription?: string;
    hideNonApplicablePaletteConflicts?: boolean;
    noConflictsMessage?: string;
}

export const ImportConflictsForm: React.FC<ImportConflictsFormProps> = ({
    isAnalyzing,
    onReanalyze,
    conflictsDescription,
    hideNonApplicablePaletteConflicts = true,
    noConflictsMessage,
}) => {
    const { t } = useTranslation();
    const { data, apiRef, fields } = useFormContext();
    const conflicts: Conflicte[] | undefined = data?.conflicts;
    const hasFile = data?.file != null || (conflicts != null && conflicts.length > 0);

    const [selectedItems, setSelectedItems] = React.useState<string[]>([]);
    const [isWarningExpanded, setIsWarningExpanded] = React.useState(false);

    const hasPlantillaConflict = React.useMemo(() => {
        return conflicts?.some(c => c.tipo === 'PlantillaExport' && !c.bloquejant) ?? false;
    }, [conflicts]);

    const hasPlantillaWithChanges = React.useMemo(() => {
        return conflicts?.some(
            c => c.tipo === 'PlantillaExport' && !c.bloquejant && (c.overwrite === 'CREAR_AMB_ALTRE_NOM' || c.overwrite === 'SOBRESCRIURE')
        ) ?? false;
    }, [conflicts]);

    const showPaletes = !hideNonApplicablePaletteConflicts || !hasPlantillaConflict || hasPlantillaWithChanges;

    const groups = React.useMemo(() => {
        if (!conflicts || conflicts.length === 0) return [];
        return groupConflicts(conflicts, t);
    }, [conflicts, t]);

    const visibleGroups = React.useMemo(() => {
        if (!groups) return [];
        if (!showPaletes) {
            return groups.filter(g => g.tipo !== 'PaletaExport');
        }
        return groups;
    }, [groups, showPaletes]);

    const hasBlockingConflicts = React.useMemo(() => {
        return conflicts?.some(c => c.bloquejant) ?? false;
    }, [conflicts]);

    const nonBlockingConflicts = React.useMemo(() => {
        return (conflicts ?? []).filter(c => !c.bloquejant);
    }, [conflicts]);

    const visibleNonBlockingConflicts = React.useMemo(() => {
        return (conflicts ?? []).filter(c => !c.bloquejant && (showPaletes || c.tipo !== 'PaletaExport'));
    }, [conflicts, showPaletes]);

    const selectedIndices = React.useMemo(() => {
        if (!conflicts) return [];
        return conflicts
            .map((c, i) => ({ c, i }))
            .filter(({ c, i }) => !c.bloquejant && (showPaletes || c.tipo !== 'PaletaExport') && selectedItems.includes(`conflict-${i}`))
            .map(({ i }) => i);
    }, [conflicts, selectedItems, showPaletes]);

    React.useEffect(() => {
        if (!conflicts || conflicts.length === 0) {
            setSelectedItems([]);
        }
    }, [conflicts]);

    React.useEffect(() => {
        if (!showPaletes) {
            setSelectedItems(prev => prev.filter(id => {
                if (id === 'group-PaletaExport') return false;
                if (id.startsWith('conflict-')) {
                    const idx = parseInt(id.replace('conflict-', ''), 10);
                    return conflicts?.[idx]?.tipo !== 'PaletaExport';
                }
                return true;
            }));
        }
    }, [showPaletes, conflicts]);

    const updateConflict = React.useCallback((index: number, changes: Partial<Conflicte>) => {
        if (!conflicts) return;
        const updated = conflicts.map((c, i) => (i === index ? { ...c, ...changes } : c));
        apiRef.current?.setFieldValue('conflicts', updated);
    }, [apiRef, conflicts]);

    const handleSelectAll = () => {
        if (!conflicts) return;
        const allIds: string[] = [];
        visibleGroups.forEach((g) => {
            const hasNonBlocking = g.items.some(item => !item.conflict.bloquejant);
            if (hasNonBlocking) {
                allIds.push(`group-${g.tipo}`);
            }
            g.items.forEach((item) => {
                if (!item.conflict.bloquejant) {
                    allIds.push(`conflict-${item.index}`);
                }
            });
        });
        setSelectedItems(allIds);
    };

    const handleDeselectAll = () => {
        setSelectedItems([]);
    };

    const handleBulkUseExisting = () => {
        if (selectedIndices.length === 0 || !conflicts) return;
        const updated = conflicts.map((c, i) => {
            if (!selectedIndices.includes(i) || c.bloquejant || c.tipo === 'DashboardExport') return c;
            return {
                ...c,
                overwrite: 'EMPRAR_EXISTENT',
            };
        });
        apiRef.current?.setFieldValue('conflicts', updated);
    };

    const handleBulkCreateWithAnotherName = () => {
        if (selectedIndices.length === 0 || !conflicts) return;
        const updated = conflicts.map((c, i) => {
            if (!selectedIndices.includes(i) || c.bloquejant || c.tipo === 'IndicadorExport') return c;
            return {
                ...c,
                overwrite: 'CREAR_AMB_ALTRE_NOM',
            };
        });
        apiRef.current?.setFieldValue('conflicts', updated);
    };

    const handleBulkOverwrite = () => {
        if (selectedIndices.length === 0 || !conflicts) return;
        const updated = conflicts.map((c, i) => {
            if (!selectedIndices.includes(i) || c.bloquejant || !supportsOverwrite(c.tipo)) return c;
            return {
                ...c,
                overwrite: 'SOBRESCRIURE',
            };
        });
        apiRef.current?.setFieldValue('conflicts', updated);
    };

    const fieldOverwrite = fields?.filter((i: any) => i.name === 'overwrite')[0];
    const fieldNouNom = fields?.filter((i: any) => i.name === 'nouNom')[0];

    const selectedConflicts = React.useMemo(() => {
        if (!conflicts) return [];
        return selectedIndices.map(i => conflicts[i]).filter(Boolean);
    }, [conflicts, selectedIndices]);

    const hasEligibleUseExisting = React.useMemo(() => {
        return selectedConflicts.some(c => c.tipo !== 'DashboardExport');
    }, [selectedConflicts]);

    const hasIneligibleUseExisting = React.useMemo(() => {
        return selectedConflicts.some(c => c.tipo === 'DashboardExport');
    }, [selectedConflicts]);

    const isMixedUseExisting = React.useMemo(() => {
        return selectedConflicts.length > 0 && hasEligibleUseExisting && hasIneligibleUseExisting;
    }, [selectedConflicts.length, hasEligibleUseExisting, hasIneligibleUseExisting]);

    const hasEligibleCreateWithAnotherName = React.useMemo(() => {
        return selectedConflicts.some(c => c.tipo !== 'IndicadorExport');
    }, [selectedConflicts]);

    const hasIneligibleCreateWithAnotherName = React.useMemo(() => {
        return selectedConflicts.some(c => c.tipo === 'IndicadorExport');
    }, [selectedConflicts]);

    const isMixedCreateWithAnotherName = React.useMemo(() => {
        return selectedConflicts.length > 0 && hasEligibleCreateWithAnotherName && hasIneligibleCreateWithAnotherName;
    }, [selectedConflicts.length, hasEligibleCreateWithAnotherName, hasIneligibleCreateWithAnotherName]);

    const hasEligibleOverwrite = React.useMemo(() => {
        return selectedConflicts.some(c => supportsOverwrite(c.tipo));
    }, [selectedConflicts]);

    const hasIneligibleOverwrite = React.useMemo(() => {
        return selectedConflicts.some(c => !supportsOverwrite(c.tipo));
    }, [selectedConflicts]);

    const isMixedOverwrite = React.useMemo(() => {
        return selectedConflicts.length > 0 && hasEligibleOverwrite && hasIneligibleOverwrite;
    }, [selectedConflicts.length, hasEligibleOverwrite, hasIneligibleOverwrite]);

    const hasMixedSelection = isMixedUseExisting || isMixedCreateWithAnotherName || isMixedOverwrite;

    const hasExistingItems = React.useMemo(() => {
        return (conflicts ?? []).some((c) => !c.bloquejant && (showPaletes || c.tipo !== 'PaletaExport') && (c.overwrite === 'EMPRAR_EXISTENT' || !c.overwrite));
    }, [conflicts, showPaletes]);

    const hasNonIndicadorConflicts = React.useMemo(() => {
        return visibleNonBlockingConflicts.some(c => c.tipo !== 'IndicadorExport');
    }, [visibleNonBlockingConflicts]);

    const hasOverwriteConflicts = React.useMemo(() => {
        return visibleNonBlockingConflicts.some(c => supportsOverwrite(c.tipo));
    }, [visibleNonBlockingConflicts]);

    if (!hasFile && !isAnalyzing) {
        return null;
    }

    if (isAnalyzing || (hasFile && conflicts === undefined)) {
        return (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, my: 2 }}>
                <CircularProgress size={24} />
                <Typography variant="body2" color="text.secondary">
                    {t($ => $.page.dashboards.action.import.analyzing)}
                </Typography>
            </Box>
        );
    }

    if (!conflicts || conflicts.length === 0) {
        return (
            <Alert severity="info" sx={{ mt: 2 }}>
                {noConflictsMessage ?? t($ => $.page.dashboards.action.import.noConflicts)}
            </Alert>
        );
    }

    return (
        <Box sx={{ mt: 1 }}>
            {hasBlockingConflicts && (
                <Alert
                    severity="error"
                    sx={{ mb: 1.5 }}
                    action={
                        onReanalyze && (
                            <Button
                                color="error"
                                size="small"
                                variant="outlined"
                                onClick={onReanalyze}
                                startIcon={<Icon>refresh</Icon>}
                                disabled={isAnalyzing}
                            >
                                {t($ => $.page.dashboards.action.import.reanalyze)}
                            </Button>
                        )
                    }
                >
                    <AlertTitle sx={{ fontWeight: 600 }}>
                        {t($ => $.page.dashboards.action.import.blockingErrorTitle)}
                    </AlertTitle>
                    <Typography variant="body2">
                        {t($ => $.page.dashboards.action.import.blockingErrorDescription)}
                    </Typography>
                </Alert>
            )}

            {hasExistingItems && (
                <Alert
                    severity="warning"
                    sx={{ mb: 1.5 }}
                    action={
                        <IconButton
                            aria-label={isWarningExpanded ? "expand_less" : "expand_more"}
                            size="small"
                            onClick={() => setIsWarningExpanded((prev) => !prev)}
                        >
                            <Icon>{isWarningExpanded ? 'expand_less' : 'expand_more'}</Icon>
                        </IconButton>
                    }
                >
                    <Box
                        sx={{ cursor: 'pointer', display: 'flex', alignItems: 'center' }}
                        onClick={() => setIsWarningExpanded((prev) => !prev)}
                    >
                        <AlertTitle sx={{ mb: isWarningExpanded ? 0.5 : 0, userSelect: 'none' }}>
                            {t($ => $.page.dashboards.action.import.warningExistingTitle)}
                        </AlertTitle>
                    </Box>
                    <Collapse in={isWarningExpanded}>
                        <Typography variant="body2">
                            {t($ => $.page.dashboards.action.import.warningExistingDescription)}
                        </Typography>
                    </Collapse>
                </Alert>
            )}

            {nonBlockingConflicts.length > 0 ? (
                <Box
                    sx={{
                        display: 'flex',
                        flexWrap: 'wrap',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        gap: 1,
                        mb: 1.5,
                        p: 1,
                        borderRadius: 1,
                        backgroundColor: 'action.hover',
                    }}
                >
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, width: '100%', mb: 1.5 }}>
                        <Typography variant="h6">
                            {t($ => $.page.dashboards.action.import.conflictsTitle)}
                        </Typography>
                        <Tooltip title={conflictsDescription ?? t($ => $.page.dashboards.action.import.dashboardConflicts)} arrow>
                            <IconButton size="small" aria-label="info">
                                <Icon fontSize="small">info_outline</Icon>
                            </IconButton>
                        </Tooltip>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="body2" color="text.secondary">
                            {t($ => $.page.dashboards.action.import.bulkActions.selectedCount, {
                                count: selectedIndices.length,
                            })}
                        </Typography>
                        <Button
                            size="small"
                            variant="text"
                            onClick={handleSelectAll}
                            disabled={selectedIndices.length === visibleNonBlockingConflicts.length || visibleNonBlockingConflicts.length === 0}
                        >
                            {t($ => $.page.dashboards.action.import.bulkActions.selectAll)}
                        </Button>
                        <Button
                            size="small"
                            variant="text"
                            onClick={handleDeselectAll}
                            disabled={selectedIndices.length === 0}
                        >
                            {t($ => $.page.dashboards.action.import.bulkActions.deselectAll)}
                        </Button>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Button
                            size="small"
                            variant="outlined"
                            onClick={handleBulkUseExisting}
                            disabled={selectedIndices.length === 0 || isMixedUseExisting || !hasEligibleUseExisting}
                            startIcon={<Icon>check</Icon>}
                        >
                            {t($ => $.page.dashboards.action.import.bulkActions.useExisting)}
                        </Button>
                        {hasNonIndicadorConflicts && (
                            <Button
                                size="small"
                                variant="outlined"
                                onClick={handleBulkCreateWithAnotherName}
                                disabled={selectedIndices.length === 0 || isMixedCreateWithAnotherName || !hasEligibleCreateWithAnotherName}
                                startIcon={<Icon>edit</Icon>}
                            >
                                {t($ => $.page.dashboards.action.import.bulkActions.createWithAnotherName)}
                            </Button>
                        )}
                        {hasOverwriteConflicts && (
                            <Button
                                size="small"
                                variant="outlined"
                                onClick={handleBulkOverwrite}
                                disabled={selectedIndices.length === 0 || isMixedOverwrite || !hasEligibleOverwrite}
                                startIcon={<Icon>save</Icon>}
                            >
                                {t($ => $.page.dashboards.action.import.bulkActions.overwrite)}
                            </Button>
                        )}
                        {hasMixedSelection && (
                            <Tooltip title={t($ => $.page.dashboards.action.import.bulkActions.mixedSelectionInfo)} arrow>
                                <IconButton size="small" aria-label="info" color="info" data-testid="mixed-selection-info">
                                    <Icon fontSize="small">info_outline</Icon>
                                </IconButton>
                            </Tooltip>
                        )}
                    </Box>
                </Box>
            ) : (
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5, p: 1 }}>
                    <Typography variant="h6">
                        {t($ => $.page.dashboards.action.import.conflictsTitle)}
                    </Typography>
                </Box>
            )}

            <ConflictsTreeView
                groups={visibleGroups}
                selectedItems={selectedItems}
                setSelectedItems={setSelectedItems}
                fieldOverwrite={fieldOverwrite}
                fieldNouNom={fieldNouNom}
                updateConflict={updateConflict}
                conflicts={conflicts}
            />
        </Box>
    );
};

export default ImportConflictsForm;
