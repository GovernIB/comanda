import { useEffect, useMemo, useState } from 'react';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Tooltip from '@mui/material/Tooltip';
import EventRepeatIcon from '@mui/icons-material/EventRepeat';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs, { Dayjs } from 'dayjs';
import { useResourceApiService } from 'reactlib';
import { DashboardFiltre, DashboardFiltreSeleccio, Periode } from '../../types/dashboardFiltre.model.ts';

type QuickRange = { label: string; compute: () => { inici: Dayjs; fi: Dayjs } };

/** "Darrer mes/trimestre/any" = darrer període complet i tancat (no l'actual, encara en curs). */
const QUICK_RANGES: QuickRange[] = [
    {
        label: 'Darrer mes',
        compute: () => {
            const referencia = dayjs().subtract(1, 'month');
            return { inici: referencia.startOf('month'), fi: referencia.endOf('month') };
        },
    },
    {
        label: 'Darrer trimestre',
        compute: () => {
            const referencia = dayjs().subtract(3, 'month');
            const inici = referencia.month(Math.floor(referencia.month() / 3) * 3).startOf('month');
            return { inici, fi: inici.add(3, 'month').subtract(1, 'day') };
        },
    },
    {
        label: 'Darrer any',
        compute: () => {
            const referencia = dayjs().subtract(1, 'year');
            return { inici: referencia.startOf('year'), fi: referencia.endOf('year') };
        },
    },
];

type PeriodeFiltreControlProps = {
    filtre: DashboardFiltre;
    periode: Periode | undefined;
    onChange: (periode: Periode | undefined) => void;
};

const PeriodeFiltreControl = ({ filtre, periode, onChange }: PeriodeFiltreControlProps) => {
    const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);

    const handleDateChange = (field: 'absolutDataInici' | 'absolutDataFi', value: Dayjs | null) => {
        const next: Periode = {
            periodeMode: 'ABSOLUT',
            absolutTipus: 'DATE_RANGE',
            ...periode,
            [field]: value?.format('YYYY-MM-DD'),
        };
        onChange(next.absolutDataInici || next.absolutDataFi ? next : undefined);
    };

    const handleQuickRange = (range: QuickRange) => {
        const { inici, fi } = range.compute();
        onChange({
            periodeMode: 'ABSOLUT',
            absolutTipus: 'DATE_RANGE',
            absolutDataInici: inici.format('YYYY-MM-DD'),
            absolutDataFi: fi.format('YYYY-MM-DD'),
        });
        setMenuAnchor(null);
    };

    return (
        <Box sx={{ display: 'flex', gap: 0.5, alignItems: 'center' }}>
            <DatePicker
                label={filtre.titol || 'Des de'}
                value={periode?.absolutDataInici ? dayjs(periode.absolutDataInici) : null}
                onChange={(value) => handleDateChange('absolutDataInici', value)}
                slotProps={{ textField: { size: 'small', sx: { width: 150 } } }}
            />
            <DatePicker
                label="Fins a"
                value={periode?.absolutDataFi ? dayjs(periode.absolutDataFi) : null}
                onChange={(value) => handleDateChange('absolutDataFi', value)}
                slotProps={{ textField: { size: 'small', sx: { width: 150 } } }}
            />
            <Tooltip title="Períodes predefinits">
                <IconButton size="small" onClick={(e) => setMenuAnchor(e.currentTarget)}>
                    <EventRepeatIcon fontSize="small" />
                </IconButton>
            </Tooltip>
            <Menu anchorEl={menuAnchor} open={menuAnchor != null} onClose={() => setMenuAnchor(null)}>
                {QUICK_RANGES.map((range) => (
                    <MenuItem key={range.label} onClick={() => handleQuickRange(range)}>
                        {range.label}
                    </MenuItem>
                ))}
                <MenuItem onClick={() => { onChange(undefined); setMenuAnchor(null); }}>
                    <em>Netejar (període propi de cada widget)</em>
                </MenuItem>
            </Menu>
        </Box>
    );
};

type DimensioFiltreControlProps = {
    filtre: DashboardFiltre;
    values: string[];
    onChange: (values: string[]) => void;
    aplicacioId: number | string | undefined;
};

type DimensioValorOption = { valor: string; label: string };

const DIMENSIO_VALOR_OPTIONS_SIZE = 200;

const DimensioFiltreControl = ({ filtre, values, onChange, aplicacioId }: DimensioFiltreControlProps) => {
    const { isReady, find } = useResourceApiService('dimensioValor');
    const [options, setOptions] = useState<DimensioValorOption[]>([]);

    useEffect(() => {
        let cancelled = false;
        if (isReady && filtre.dimensioCodi && aplicacioId != null) {
            find({
                filter: `dimensio.codi:'${filtre.dimensioCodi}'`,
                size: DIMENSIO_VALOR_OPTIONS_SIZE,
                namedQueries: [`filterByAppGroupByValor:${aplicacioId}`],
            }).then((response) => {
                if (cancelled) return;
                // `codiNom` ja combina el valor amb la denominació de la unitat organitzativa/entitat quan la
                // dimensió és ORGAN_GESTOR, CONSELLERIA o ENTITAT (DimensioValor.getCodiNom() al backend); per a
                // qualsevol altra dimensió és idèntic al valor, així que sempre és segur fer-lo servir com a etiqueta.
                const byValor = new Map<string, string>();
                (response.rows ?? []).forEach((row: { valor?: unknown; codiNom?: unknown }) => {
                    if (typeof row.valor === 'string' && row.valor.length > 0) {
                        byValor.set(row.valor, typeof row.codiNom === 'string' ? row.codiNom : row.valor);
                    }
                });
                const uniqueOptions = Array.from(byValor.entries())
                    .map(([valor, label]) => ({ valor, label }))
                    .sort((a, b) => a.label.localeCompare(b.label));
                setOptions(uniqueOptions);
            });
        }
        return () => {
            cancelled = true;
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isReady, filtre.dimensioCodi, aplicacioId]);

    const selectedOptions = options.filter((o) => values.includes(o.valor));

    return (
        <Autocomplete
            multiple={filtre.multiple !== false}
            size="small"
            sx={{ minWidth: 220 }}
            options={options}
            getOptionLabel={(option) => option.label}
            isOptionEqualToValue={(option, val) => option.valor === val.valor}
            value={filtre.multiple !== false ? selectedOptions : (selectedOptions[0] ?? null)}
            onChange={(_event, newValue) => {
                const selected = newValue == null ? [] : Array.isArray(newValue) ? newValue : [newValue];
                onChange(selected.map((o) => o.valor));
            }}
            renderInput={(params) => <TextField {...params} label={filtre.titol || filtre.dimensioCodi} />}
        />
    );
};

export type DashboardFiltreBarProps = {
    filtres: DashboardFiltre[] | undefined | null;
    value: DashboardFiltreSeleccio;
    onChange: (value: DashboardFiltreSeleccio) => void;
    /** Id de l'aplicació del dashboard (Dashboard.aplicacio.id); restringeix les opcions dels filtres DIMENSIO
     * als valors del mateix app-entorn que el dashboard (vegeu filterByAppGroupByValor a DimensioValorServiceImpl). */
    aplicacioId: number | string | undefined;
};

/**
 * Barra de filtres de capçalera d'un dashboard. Es mostra dins la barra d'eines superior (a la dreta del selector
 * de dashboard, vegeu EstadisticaDashboardView.tsx) quan el dashboard té filtres configurats (Dashboard.filtres);
 * la selecció de l'usuari s'envia com a `filtreSeleccio` a cada crida de l'informe `widget_data` i s'aplica a tots
 * els widgets (vegeu useDashboardWidgets a dashboardRequests.ts). No té padding propi: qui l'incrusta (el
 * contenidor flex de la capçalera) és qui decideix l'espaiat respecte a la resta d'elements.
 */
const DashboardFiltreBar = ({ filtres, value, onChange, aplicacioId }: DashboardFiltreBarProps) => {
    const orderedFiltres = useMemo(
        () => [...(filtres ?? [])].sort((a, b) => a.ordre - b.ordre),
        [filtres]
    );

    if (orderedFiltres.length === 0) {
        return null;
    }

    const handleDimensioChange = (dimensioCodi: string, values: string[]) => {
        const dimensions = { ...(value.dimensions ?? {}) };
        if (values.length === 0) {
            delete dimensions[dimensioCodi];
        } else {
            dimensions[dimensioCodi] = values;
        }
        onChange({ ...value, dimensions });
    };

    const handlePeriodeChange = (periode: Periode | undefined) => {
        onChange({ ...value, periode });
    };

    return (
        <Box
            sx={{
                display: 'flex',
                flexWrap: 'wrap',
                gap: 2,
                alignItems: 'center',
            }}
        >
            {orderedFiltres.map((filtre) =>
                filtre.tipus === 'PERIODE' ? (
                    <PeriodeFiltreControl
                        key={filtre.id}
                        filtre={filtre}
                        periode={value.periode}
                        onChange={handlePeriodeChange}
                    />
                ) : filtre.dimensioCodi ? (
                    <DimensioFiltreControl
                        key={filtre.id}
                        filtre={filtre}
                        values={value.dimensions?.[filtre.dimensioCodi] ?? []}
                        onChange={(values) => handleDimensioChange(filtre.dimensioCodi as string, values)}
                        aplicacioId={aplicacioId}
                    />
                ) : null
            )}
        </Box>
    );
};

export default DashboardFiltreBar;
