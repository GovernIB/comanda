import React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListSubheader from '@mui/material/ListSubheader';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import TextField from '@mui/material/TextField';
import Checkbox from '@mui/material/Checkbox';
import CircularProgress from '@mui/material/CircularProgress';
import Tooltip from '@mui/material/Tooltip';
import Cancel from '@mui/icons-material/Cancel';
import CheckCircle from '@mui/icons-material/CheckCircle';
import RemoveCircle from '@mui/icons-material/RemoveCircle';
import { TextHighlight, useResourceApiService, useBaseAppContext, springFilterBuilder as builder } from 'reactlib';

export enum ParamTipus {
    NUMERIC = 'NUMERIC',
    TEXT = 'TEXT',
    BOOLEAN = 'BOOLEAN',
    PASSWORD = 'PASSWORD',
    CRON = 'CRON',
    SELECT = 'SELECT',
}

const ParametreValue: React.FC<{ tipus: ParamTipus; valor: React.ReactNode }> = ({ tipus, valor }) => {
    const { t } = useTranslation();
    if (valor === null || valor === undefined) {
        return (
            <Tooltip title={t($ => $.page.parametres.detail.valuesTootip.null)}>
                <RemoveCircle color="disabled" />
            </Tooltip>
        );
    }
    if (tipus === ParamTipus.BOOLEAN) {
        return valor === true ? (
            <Tooltip title={t($ => $.page.parametres.detail.valuesTootip.true)}>
                <CheckCircle color="success" />
            </Tooltip>
        ) : (
            <Tooltip title={t($ => $.page.parametres.detail.valuesTootip.false)}>
                <Cancel color="error" />
            </Tooltip>
        );
    }
    if (tipus === ParamTipus.PASSWORD) {
        return <Typography variant="body2">••••••••</Typography>;
    }
    return <Typography variant="body2">{valor}</Typography>;
};

const ParametreRow: React.FC<{
    item: Record<string, any>;
    highlight?: string;
    apiPatch: (id: string | number, args: { data: Record<string, unknown> }) => Promise<unknown>;
}> = ({ item, highlight, apiPatch }) => {
    const { t } = useTranslation();
    const { temporalMessageShow } = useBaseAppContext();
    const isBoolean = item.tipus === ParamTipus.BOOLEAN;
    const isNumeric = item.tipus === ParamTipus.NUMERIC;
    const isPassword = item.tipus === ParamTipus.PASSWORD;

    const [savedValue, setSavedValue] = React.useState<any>(isBoolean ? item.valorBoolean : item.valor);
    const [changedValue, setChangedValue] = React.useState<any>(undefined);
    const [saving, setSaving] = React.useState(false);

    const currentValue = changedValue !== undefined ? changedValue : savedValue;
    const hasChanged = changedValue !== undefined && changedValue !== savedValue;

    const handleSave = async () => {
        setSaving(true);
        try {
            const data = isBoolean ? { valorBoolean: changedValue } : { valor: changedValue };
            await apiPatch(item.id, { data });
            setSavedValue(changedValue);
            setChangedValue(undefined);
            temporalMessageShow(null, t($ => $.page.parametres.save.success), 'success');
        } catch (err: any) {
            temporalMessageShow(t($ => $.page.parametres.save.error), err?.message ?? '', 'error');
        } finally {
            setSaving(false);
        }
    };

    return (
        <Grid container spacing={2} alignItems="flex-start" sx={{ width: '100%', py: 0.5 }}>
            <Grid size={{ xs: 12, sm: 5 }}>
                <Typography variant="body2" fontWeight={500} component="div">
                    <TextHighlight text={item.nom} match={highlight} ignoreCase />
                </Typography>
                {item.descripcio && (
                    <Typography variant="caption" color="text.secondary" display="block" component="div">
                        <TextHighlight text={item.descripcio} match={highlight} ignoreCase />
                    </Typography>
                )}
                <Typography variant="caption" color="text.disabled">{item.codi}</Typography>
            </Grid>
            <Grid size={{ xs: 12, sm: 7 }}>
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                    {item.editable ? (
                        <>
                            {isBoolean ? (
                                <Checkbox
                                    checked={currentValue === true}
                                    indeterminate={currentValue === null || currentValue === undefined}
                                    onChange={(e) => setChangedValue(e.target.checked)}
                                    size="small"
                                    sx={{ mt: -0.5 }}
                                />
                            ) : (
                                <TextField
                                    value={currentValue ?? ''}
                                    type={isNumeric ? 'number' : isPassword ? 'password' : 'text'}
                                    size="small"
                                    fullWidth
                                    onChange={(e) => setChangedValue(e.target.value)}
                                    slotProps={{ input: { style: { fontSize: '14px' } } }}
                                />
                            )}
                            {hasChanged && (
                                <IconButton
                                    color="primary"
                                    size="small"
                                    onClick={handleSave}
                                    disabled={saving}
                                    title={t($ => $.page.parametres.save.success)}
                                >
                                    {saving
                                        ? <CircularProgress size={18} color="inherit" />
                                        : <Icon fontSize="small">save</Icon>}
                                </IconButton>
                            )}
                        </>
                    ) : (
                        <ParametreValue tipus={item.tipus as ParamTipus} valor={item.valorBoolean ?? item.valor} />
                    )}
                </Box>
            </Grid>
        </Grid>
    );
};

const groupBySubGrup = (items: Array<Record<string, unknown>>): Map<string, Array<Record<string, unknown>>> => {
    const map = new Map<string, Array<Record<string, unknown>>>();
    items.forEach((item) => {
        const key = (item.subGrup as string) ?? '';
        if (!map.has(key)) map.set(key, []);
        map.get(key)!.push(item);
    });
    return map;
};

export const ParametresItems: React.FC<{
    grup: string | null;
    subGrup: string | null;
    quickFilter?: string;
}> = ({ grup, subGrup, quickFilter }) => {
    const { t } = useTranslation();
    const { isReady, find, patch } = useResourceApiService('parametre');
    const [parametres, setParametres] = React.useState<Array<Record<string, unknown>>>([]);
    const [loading, setLoading] = React.useState(false);

    React.useEffect(() => {
        if (!isReady || !grup) {
            setParametres([]);
            return;
        }
        setLoading(true);
        const filterParts: string[] = [builder.eq('grup', `'${grup}'`)];
        if (subGrup) {
            filterParts.push(builder.eq('subGrup', `'${subGrup}'`));
        }
        find({
            filter: builder.and(...filterParts),
            quickFilter: quickFilter || undefined,
            sorts: ['subGrup,asc', 'codi,asc'],
            unpaged: true,
        })
            .then((response) => setParametres(response.rows as Array<Record<string, unknown>>))
            .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isReady, grup, subGrup, quickFilter]);

    if (!grup) {
        return (
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
                <Typography color="text.secondary">{t($ => $.page.parametres.noGroup)}</Typography>
            </Box>
        );
    }

    const title = subGrup ?? grup;
    const grouped = subGrup ? null : groupBySubGrup(parametres);
    const isEmpty = parametres.length === 0 && !loading;

    return (
        <Box sx={{ px: 3 }}>
            <Typography variant="h6" sx={{ mb: 1 }}>{title}</Typography>
            <List component={Paper} disablePadding>
                {isEmpty && (
                    <Box sx={{ textAlign: 'center', py: 4 }}>
                        <Icon fontSize="large" color="disabled">block</Icon>
                        <Typography variant="h6" color="text.secondary">
                            {t($ => $.page.parametres.empty)}
                        </Typography>
                    </Box>
                )}
                {grouped
                    ? Array.from(grouped.entries()).map(([sg, items]) => (
                        <React.Fragment key={sg}>
                            {sg && (
                                <ListSubheader sx={{ lineHeight: '36px', bgcolor: 'background.default' }}>
                                    {sg}
                                </ListSubheader>
                            )}
                            {items.map((item) => (
                                <ListItem key={String(item.id)} disablePadding divider>
                                    <ListItemButton disableRipple sx={{ py: 1.5 }}>
                                        <ParametreRow
                                            key={String(item.id)}
                                            item={item}
                                            highlight={quickFilter}
                                            apiPatch={patch}
                                        />
                                    </ListItemButton>
                                </ListItem>
                            ))}
                        </React.Fragment>
                    ))
                    : parametres.map((item) => (
                        <ListItem key={String(item.id)} disablePadding divider>
                            <ListItemButton disableRipple sx={{ py: 1.5 }}>
                                <ParametreRow
                                    key={String(item.id)}
                                    item={item}
                                    highlight={quickFilter}
                                    apiPatch={patch}
                                />
                            </ListItemButton>
                        </ListItem>
                    ))}
            </List>
        </Box>
    );
};
