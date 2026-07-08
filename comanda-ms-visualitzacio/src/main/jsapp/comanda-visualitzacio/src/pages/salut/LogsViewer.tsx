import { debounce } from 'lodash';
import {
    dateFormatLocale,
    useCloseDialogButtons,
    useDebounce,
    useResourceApiService,
} from 'reactlib';
import { memo, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {Box, Button, FormControlLabel, Switch, ToggleButton, Tooltip, Typography, TextField, InputAdornment} from '@mui/material';
import { useVirtualizer } from '@tanstack/react-virtual';
import WrapTextIcon from '@mui/icons-material/WrapText';
import VerticalAlignBottomIcon from '@mui/icons-material/VerticalAlignBottom';
import SearchIcon from '@mui/icons-material/Search';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import CloseIcon from '@mui/icons-material/Close';
import Divider from '@mui/material/Divider';
import MenuIcon from '@mui/icons-material/Menu';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import Dialog from '../../../lib/components/mui/Dialog';
import { DataGridPro, GridActionsCellItem } from '@mui/x-data-grid-pro';
import type { GridColDef } from '@mui/x-data-grid';
import DownloadIcon from '@mui/icons-material/Download';
import PageviewIcon from '@mui/icons-material/Pageview';
import RefreshIcon from '@mui/icons-material/Refresh';
import { ResourceApiBlobResponse } from 'reactlib';
import CircularProgress from '@mui/material/CircularProgress';
import { mergeSequentialStringArrays } from '../../util/stringUtils';
import { useTranslation } from 'react-i18next';
import useDataGridLocale from '../../hooks/useDataGridLocale';
import { useMessage } from '../../components/MessageShow';
import { EntornAppModel } from '../../types/app.model.tsx';
import { DefaultLogsPerspective } from './dataFetching.ts';
import * as z from 'zod';
import { getErrorMessage } from '../../util/exceptionUtils.ts';

const PrevisualitzarLogResponseSchema = z.array(
    z.object({
        linia: z.string(),
    })
);

const MIN_LOG_LINE_COUNT = 100;
const MAX_LOG_LINE_COUNT = 10000;
const DEFAULT_LOG_LINE_COUNT = 1000;

const normalizeLogLineCount = (value: number) => {
    if (!Number.isFinite(value)) return MIN_LOG_LINE_COUNT;
    return Math.min(MAX_LOG_LINE_COUNT, Math.max(MIN_LOG_LINE_COUNT, Math.trunc(value)));
};
/**
 * Informació de la llista de fitxers de log.
 */
interface FitxerInfo {
    nom: string;
    mida: number;
    dataCreacio: string;
    dataModificacio: string;
}

/**
 * Extensió de FitxerInfo per a la taula de logs amb suport de previsualització.
 */
interface LogListRow extends FitxerInfo {
    showPreview: boolean;
}

/**
 * Tipus de fitxers que permeten previsualització.
 */
const allowedFileTypes = ['.log', '.txt'];

/**
 * Component que mostra la llista de logs disponibles en una taula (DataGridPro).
 */
const LogList = ({
    entornAppId,
    onDownload,
    onPreview,
    loading,
}: {
    entornAppId: number;
    onDownload: (nom: string) => void;
    onPreview: (nom: string) => void;
    loading: boolean;
}) => {
    const { t } = useTranslation();
    const { isReady, artifactReport } = useResourceApiService('entornApp');
    const { showTemporal: showMessage, component } = useMessage();
    const dataGridLocale = useDataGridLocale();
    const [logs, setLogs] = useState<FitxerInfo[]>([]);
    const [logsLoading, setLogsLoading] = useState<boolean>(true);
    useEffect(() => {
        if (!isReady) {
            return;
        }
        async function requests() {
            try {
                setLogsLoading(true);
                const list = await artifactReport(entornAppId, {
                    code: 'llistar_logs',
                });
                setLogs(list as FitxerInfo[]);
            } catch (e) {
                showMessage("Error", getErrorMessage(e), 'error');
            } finally {
                setLogsLoading(false);
            }
        }
        requests();
    }, [isReady, artifactReport, entornAppId, showMessage]);
    // Columnes de la taula de logs.
    const logListColumns: GridColDef<LogListRow>[] = useMemo(
        () => [
            {
                field: 'nom',
                headerName: t($ => $.page.salut.logs.logsList.nom),
                flex: 1,
            },
            {
                field: 'dataCreacio',
                headerName: t($ => $.page.salut.logs.logsList.dataCreacio),
                valueFormatter: (value) => dateFormatLocale(value, true),
                flex: 0.5,
            },
            {
                field: 'dataModificacio',
                headerName: t($ => $.page.salut.logs.logsList.dataModificacio),
                valueFormatter: (value) => dateFormatLocale(value, true),
                flex: 0.5,
            },
            {
                field: 'mida',
                headerName: t($ => $.page.salut.logs.logsList.mida),
                // Formata la mida del fitxer per mostrar-la en MB, KB o B.
                valueFormatter: value => {
                    if (value >= 1024 * 1024) {
                        return `${(value / (1024 * 1024)).toFixed(2)} MB`;
                    } else if (value >= 1024) {
                        return `${(value / 1024).toFixed(2)} KB`;
                    }
                    return `${value} B`;
                },
            },
            {
                field: 'showPreview',
                headerName: t($ => $.page.salut.logs.logsList.showPreview),
                type: 'boolean',
            },
            {
                field: 'actions',
                type: 'actions',
                align: 'right',
                width: 90,
                // Accions disponibles per cada fila (previsualització i descàrrega).
                getActions: params => {
                    const actions = [];
                    if (params.row.showPreview) {
                        actions.push(
                            <GridActionsCellItem
                                icon={<PageviewIcon />}
                                onClick={() => onPreview(params.row.nom)}
                                label={t($ => $.page.salut.logs.preview)}
                                showInMenu={false}
                            />
                        );
                    }
                    actions.push(
                        <GridActionsCellItem
                            icon={<DownloadIcon />}
                            onClick={() => onDownload(params.row.nom)}
                            label={t($ => $.page.salut.logs.download)}
                            showInMenu={false}
                        />
                    );
                    return actions;
                },
            },
        ],
        [onDownload, onPreview, t]
    );
    const rows = useMemo(
        () =>
            logs.map(log => ({
                ...log,
                id: log.nom,
                showPreview: allowedFileTypes.some(type => log.nom.endsWith(type)),
            })),
        [logs]
    );
    return (
        <>
            {component}
            <DataGridPro
            loading={loading || logsLoading}
            localeText={dataGridLocale}
            initialState={{
                sorting: {
                    sortModel: [{ field: 'showPreview', sort: 'desc' }],
                },
                columns: {
                    columnVisibilityModel: {
                        showPreview: false,
                    },
                },
            }}
            columns={logListColumns}
            rows={rows}
            // disableColumnSelector
            // disableColumnFilter
            showToolbar
            />
        </>
    );
};

const getLineNumber = (virtualizerLineIndex: number) =>
    (virtualizerLineIndex + 1).toString().slice(-4);

/**
 * Component per visualitzar el text del log de forma eficient utilitzant virtualització.
 * Permet navegar per milers de línies sense penalització de rendiment.
 */
const Virtualizer = memo(({
    lines,
    scrollToBottom,
    onScrollToBottomChange,
    softWrap,
    searchTerm,
    currentMatchIndex,
    matches,
}: {
    lines: string[];
    scrollToBottom: boolean;
    onScrollToBottomChange: (value: boolean) => void;
    softWrap: boolean;
    searchTerm: string;
    currentMatchIndex: number;
    matches: number[];
}) => {
    const containerRef = useRef<HTMLDivElement>(null);
    const rowVirtualizer = useVirtualizer({
        count: lines.length,
        getScrollElement: () => containerRef.current!,
        estimateSize: () => 22,
    });
    useEffect(() => {
        if (scrollToBottom && containerRef.current) {
            containerRef.current.scrollTop = containerRef.current.scrollHeight;
        }
    }, [scrollToBottom, lines]);

    useEffect(() => {
        if (currentMatchIndex !== -1 && matches[currentMatchIndex] !== undefined) {
            onScrollToBottomChange(false);
            rowVirtualizer.scrollToIndex(matches[currentMatchIndex], { align: 'center' });
        }
    }, [currentMatchIndex, matches, rowVirtualizer, onScrollToBottomChange]);

    // Manejador del desplaçament per detectar si l'usuari surt del final del fitxer.
    const handleScroll = useCallback(() => {
        if (!scrollToBottom || !containerRef.current) return;

        const { scrollTop, scrollHeight, clientHeight } = containerRef.current;
        const isAtBottom = Math.abs(scrollHeight - clientHeight - scrollTop) < 10;

        if (!isAtBottom) {
            onScrollToBottomChange(false);
        }
    }, [scrollToBottom, onScrollToBottomChange]);

    const highlight = useCallback((text: string, highlightText: string, isActive: boolean) => {
        if (!highlightText) return text;
        const parts = text.split(new RegExp(`(${highlightText.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi'));
        return (
            <>
                {parts.map((part, i) =>
                    part.toLowerCase() === highlightText.toLowerCase() ? (
                        <mark
                            key={i}
                            style={{
                                backgroundColor: isActive ? '#ff9800' : '#616161',
                                color: 'inherit',
                                borderRadius: '2px',
                            }}
                        >
                            {part}
                        </mark>
                    ) : (
                        part
                    )
                )}
            </>
        );
    }, []);

    const virtualRows = rowVirtualizer.getVirtualItems();
    return (
        <Box
            ref={containerRef}
            onScroll={handleScroll}
            sx={{
                height: '100%',
                flexGrow: 1,
                overflow: 'scroll',
                overflowY: 'scroll',
                overflowX: softWrap ? 'hidden' : 'scroll',
                contain: 'strict',
                // backgroundColor: theme => (theme.palette.mode === 'dark' ? '#363636' : '#1e1e1e'),
                backgroundColor: '#363636',
                color: '#d4d4d4',
                fontFamily: 'monospace',
                fontSize: '0.75rem',
                p: 1,
                border: '1px solid',
                borderColor: 'divider',
                borderRadius: 1,
                '& p': {
                    pl: 6,
                    position: 'relative',
                    textWrap: softWrap ? 'wrap' : 'nowrap',
                    wordBreak: softWrap ? 'break-all' : 'none',
                    minHeight: '1.25rem',
                },
                '& .lineNumber': {
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '40px',
                    display: 'inline-flex',
                    justifyContent: 'flex-end',
                    pr: 1,
                    alignItems: 'baseline',
                    lineHeight: 'inherit',
                    color: '#858585',
                    pointerEvents: 'none',
                    userSelect: 'none',
                    borderRight: '1px solid #444',
                    mr: 1,
                },
            }}
        >
            <div
                style={{
                    height: rowVirtualizer.getTotalSize(),
                    width: '100%',
                    position: 'relative',
                }}
            >
                <div
                    style={{
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        width: '100%',
                        transform: `translateY(${virtualRows[0]?.start ?? 0}px)`,
                        lineHeight: 'normal',
                    }}
                >
                    {virtualRows.map(virtualRow => (
                        <p
                            key={virtualRow.key}
                            data-index={virtualRow.index}
                            ref={rowVirtualizer.measureElement}
                        >
                            <span className="lineNumber">{getLineNumber(virtualRow.index)}</span>
                            {highlight(
                                lines[virtualRow.index],
                                searchTerm,
                                matches[currentMatchIndex] === virtualRow.index
                            )}
                        </p>
                    ))}
                </div>
            </div>
        </Box>
    );
});

/**
 * Component intermediari per carregar o visualitzar la previsualització en directe.
 */
const LivePreview = ({
    lines,
    scrollToBottom,
    onScrollToBottomChange,
    softWrap,
    searchTerm,
    currentMatchIndex,
    matches,
}: {
    lines?: string[] | null;
    scrollToBottom: boolean;
    onScrollToBottomChange: (value: boolean) => void;
    softWrap: boolean;
    searchTerm: string;
    currentMatchIndex: number;
    matches: number[];
}) => {
    if (lines == null) {
        return (
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '100%',
                    backgroundColor: theme => (theme.palette.mode === 'dark' ? '#121212' : '#1e1e1e'),
                    border: '1px solid',
                    borderColor: 'divider',
                    borderRadius: 1,
                }}
            >
                <CircularProgress size={100} />
            </Box>
        );
    }
    return (
        <Virtualizer
            lines={lines}
            scrollToBottom={scrollToBottom}
            onScrollToBottomChange={onScrollToBottomChange}
            softWrap={softWrap}
            searchTerm={searchTerm}
            currentMatchIndex={currentMatchIndex}
            matches={matches}
        />
    );
};

/**
 * Component principal de la pestanya de logs de salut.
 * Gestiona la selecció de fitxers, la càrrega de dades i la previsualització en directe.
 */
const LogsViewer = ({ entornAppId, preselectedLog }: { entornAppId: number, preselectedLog: string | null }) => {
    const { t } = useTranslation();
    const [selected, setSelected] = useState<string | null>(preselectedLog);
    const [lines, setLines] = useState<string[] | null>(null);
    const [lineFetchCount, setLineFetchCount] = useState(DEFAULT_LOG_LINE_COUNT);
    const [lineFetchCountInput, setLineFetchCountInput] = useState(String(DEFAULT_LOG_LINE_COUNT));
    const [isRefreshLoading, setIsRefreshLoading] = useState<boolean>(false);
    const [isDownloadLoading, setIsDownloadLoading] = useState<boolean>(false);
    const closeDialogButtons = useCloseDialogButtons();
    const [dialogOpen, setDialogOpen] = useState(false);
    const { isReady, artifactReport } = useResourceApiService('entornApp');
    const refreshRequestSequence = useRef<number>(0);
    const { showTemporal: showMessage, component } = useMessage();

    /**
     * Actualitza el contingut del log actualment seleccionat.
     * Recupera les últimes línies configurades i les fusiona amb les ja existents.
     */
    const refreshPreview = useCallback(async (lineCount: number) => {
        if (!selected) {
            return;
        }
        setIsRefreshLoading(true);
        const sequence = ++refreshRequestSequence.current;
        try {
            const reportResponse = await artifactReport(entornAppId, {
                code: 'previsualitzar_log',
                data: {
                    fileName: selected,
                    lineCount,
                },
            });
            const list = PrevisualitzarLogResponseSchema.parse(reportResponse);
            if (sequence !== refreshRequestSequence.current) return;
            setLines(prevState => {
                const newLines = list.map(liniaDto => liniaDto.linia);
                return mergeSequentialStringArrays(prevState ?? [], newLines);
            });
        } catch (e) {
            showMessage("Error", getErrorMessage(e), 'error');
        } finally {
            setIsRefreshLoading(false);
        }
    }, [artifactReport, entornAppId, selected, showMessage]);

    useEffect(() => {
        if (!isReady) {
            return;
        }
        setLines(null);
        if (selected) {
            refreshPreview(lineFetchCount);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isReady, entornAppId, selected]);

    /**
     * Funció per descarregar un fitxer de log directament des de l'API.
     */
    const download = useCallback(
        async (name: string) => {
            if (!isReady) return;
            setIsDownloadLoading(true);
            try {
                const file = (await artifactReport(entornAppId, {
                    code: 'descarregar_log',
                    data: name,
                    fileType: 'CSV', // El fileType s'ignora, però és obligatori enviar-lo al backend
                })) as ResourceApiBlobResponse;
                const blob = file?.blob;
                const url = window.URL.createObjectURL(blob);

                const a = document.createElement('a');
                a.href = url;
                a.download = file.fileName;
                document.body.appendChild(a);
                a.click();

                a.remove();
                window.URL.revokeObjectURL(url);
            } catch (e) {
                showMessage("Error", getErrorMessage(e), 'error');
            } finally {
                setIsDownloadLoading(false);
            }
        },
        [artifactReport, entornAppId, isReady, showMessage]
    );

    /**
     * Selecciona un fitxer per a la seva previsualització.
     */
    const preview = useCallback((name: string) => {
        setSelected(name);
        setDialogOpen(false);
        setScrollToBottom(true);
    }, []);

    const [softWrap, setSoftWrap] = useState(false);
    const [scrollToBottom, setScrollToBottom] = useState(true);
    const [autoRefresh, setAutoRefresh] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const debouncedSearchTerm = useDebounce(searchTerm, undefined, true);
    const [currentMatchIndex, setCurrentMatchIndex] = useState(-1);

    const debouncedOnLineFetchCountChange = useMemo(
        () => debounce((value: number) => {
            const normalizedValue = normalizeLogLineCount(value);
            setLineFetchCount(normalizedValue);
            refreshPreview(normalizedValue);
        }, 500),
        [refreshPreview]
    );

    const matches = useMemo(() => {
        if (!debouncedSearchTerm || !lines) return [];
        const lowerSearchTerm = debouncedSearchTerm.toLowerCase();
        const result: number[] = [];
        lines.forEach((line, index) => {
            if (line.toLowerCase().includes(lowerSearchTerm)) {
                result.push(index);
            }
        });
        return result;
    }, [lines, debouncedSearchTerm]);

    useEffect(() => {
        if (debouncedSearchTerm && matches.length > 0) {
            setCurrentMatchIndex(0);
        } else {
            setCurrentMatchIndex(-1);
        }
    }, [matches, debouncedSearchTerm]);

    const handleNextMatch = useCallback(() => {
        if (matches.length === 0) return;
        setCurrentMatchIndex(prev => (prev + 1) % matches.length);
    }, [matches]);

    const handlePrevMatch = useCallback(() => {
        if (matches.length === 0) return;
        setCurrentMatchIndex(prev => (prev - 1 + matches.length) % matches.length);
    }, [matches]);

    useEffect(() => {
        let intervalId: ReturnType<typeof setInterval> | null = null;

        if (autoRefresh) {
            intervalId = setInterval(() => refreshPreview(lineFetchCount), 10000);
        }
        return () => {
            if (intervalId) {
                clearInterval(intervalId);
            }
        };
    }, [autoRefresh, lineFetchCount, refreshPreview]);

    return (
        <Box
            sx={{
                height: '100%',
                width: '100%',
                display: 'flex',
                flexDirection: 'column',
                gap: 1,
                '& p': {
                    m: 0,
                },
            }}
        >
            {component}
            <Dialog
                title={t($ => $.page.salut.logs.logsList.title)}
                open={dialogOpen}
                closeCallback={() => setDialogOpen(false)}
                buttonCallback={() => setDialogOpen(false)}
                componentProps={{ fullWidth: true, maxWidth: 'md' }}
                buttons={closeDialogButtons}
            >
                <Box sx={{ height: '500px' }}>
                    <LogList
                        entornAppId={entornAppId}
                        onDownload={download}
                        onPreview={preview}
                        loading={isRefreshLoading || isDownloadLoading}
                    />
                </Box>
            </Dialog>
            <Box
                sx={{
                    width: '100%',
                    display: 'flex',
                    // flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    // borderRight: '1px solid #ccc',
                    // height: '100%',
                }}
            >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Button
                        color="primary"
                        variant="outlined"
                        size="small"
                        disabled={!isReady}
                        onClick={() => setDialogOpen(true)}
                        startIcon={<MenuIcon />}
                        sx={{
                            borderRadius: 1,
                        }}
                    >
                        <Typography
                            color="textPrimary"
                            sx={{
                                textTransform: 'none',
                            }}
                        >
                            {selected ?? t($ => $.page.salut.logs.noSelected)}
                        </Typography>
                    </Button>
                    {selected && (
                        <>
                            <Tooltip title={t($ => $.page.salut.logs.download)}>
                                <IconButton
                                    loading={isDownloadLoading}
                                    onClick={() => download(selected)}
                                >
                                    <DownloadIcon />
                                </IconButton>
                            </Tooltip>
                            <Tooltip title={t($ => $.page.salut.logs.refresh)}>
                                <IconButton loading={isRefreshLoading} onClick={() => refreshPreview(lineFetchCount)} disabled={autoRefresh}>
                                    <RefreshIcon />
                                </IconButton>
                            </Tooltip>
                            <Tooltip title={t($ => $.page.salut.logs.autoRefresh)}>
                                <FormControlLabel control={<Switch
                                    checked={autoRefresh}
                                    onChange={(_event, checked) => setAutoRefresh(checked)}
                                />} label={t($ => $.page.salut.logs.autoRefresh)} />
                            </Tooltip>
                            <TextField
                                size="small"
                                type="number"
                                label={t($ => $.page.salut.logs.lineFetchCount)}
                                value={lineFetchCountInput}
                                onChange={e => {
                                    setLineFetchCountInput(e.target.value);
                                    debouncedOnLineFetchCountChange(Number(e.target.value));
                                }}
                                sx={{
                                    width: '120px',
                                    '& .MuiInputBase-root': {
                                        height: '32px',
                                        fontSize: '0.875rem',
                                    },
                                    '& .MuiInputLabel-root': {
                                        fontSize: '0.875rem',
                                    },
                                }}
                                slotProps={{
                                    htmlInput: {
                                        min: MIN_LOG_LINE_COUNT,
                                        max: MAX_LOG_LINE_COUNT,
                                        step: 100,
                                    },
                                }}
                            />

                        </>
                    )}
                </Box>
                <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                    {selected && (
                        <TextField
                            size="small"
                            variant="outlined"
                            placeholder={t($ => $.page.salut.logs.search)}
                            value={searchTerm}
                            onChange={e => setSearchTerm(e.target.value)}
                            onKeyDown={e => {
                                if (e.key === 'Enter') {
                                    if (e.shiftKey) {
                                        handlePrevMatch();
                                    } else {
                                        handleNextMatch();
                                    }
                                }
                            }}
                            sx={{
                                '& .MuiInputBase-root': {
                                    height: '32px',
                                    fontSize: '0.875rem',
                                },
                            }}
                            slotProps={{
                                input: {
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <SearchIcon fontSize="small" />
                                        </InputAdornment>
                                    ),
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            {debouncedSearchTerm && (
                                                <>
                                                    <Typography variant="caption" sx={{ mr: 1, whiteSpace: 'nowrap' }}>
                                                        {t($ => $.page.salut.logs.matches, {
                                                            current: matches.length > 0 ? currentMatchIndex + 1 : 0,
                                                            total: matches.length,
                                                        })}
                                                    </Typography>
                                                    <IconButton size="small" onClick={handlePrevMatch} disabled={matches.length === 0}>
                                                        <KeyboardArrowUpIcon fontSize="small" />
                                                    </IconButton>
                                                    <IconButton size="small" onClick={handleNextMatch} disabled={matches.length === 0}>
                                                        <KeyboardArrowDownIcon fontSize="small" />
                                                    </IconButton>
                                                    <IconButton size="small" onClick={() => setSearchTerm('')}>
                                                        <CloseIcon fontSize="small" />
                                                    </IconButton>
                                                </>
                                            )}
                                        </InputAdornment>
                                    ),
                                }
                            }}
                        />
                    )}
                    <Tooltip title={t($ => $.page.salut.logs.softWrap)}>
                        <ToggleButton
                            value="wrapText"
                            size="small"
                            selected={softWrap}
                            color="primary"
                            onChange={() => setSoftWrap(prevSelected => !prevSelected)}
                            disabled={!selected}
                            sx={{
                                height: '32px',
                                fontSize: '0.875rem',
                            }}
                        >
                            <WrapTextIcon />
                        </ToggleButton>
                    </Tooltip>
                    <Tooltip title={t($ => $.page.salut.logs.scrollToBottom)}>
                        <ToggleButton
                            value="alignBottom"
                            size="small"
                            selected={scrollToBottom}
                            color="primary"
                            onChange={() => setScrollToBottom(prevSelected => !prevSelected)}
                            disabled={!selected}
                            sx={{
                                height: '32px',
                                fontSize: '0.875rem',
                            }}
                        >
                            <VerticalAlignBottomIcon />
                        </ToggleButton>
                    </Tooltip>
                </Box>
            </Box>
            <Divider sx={{ mb: 1 }} />
            {selected ? (
                <LivePreview
                    key={selected + entornAppId}
                    lines={lines}
                    scrollToBottom={scrollToBottom}
                    onScrollToBottomChange={setScrollToBottom}
                    softWrap={softWrap}
                    searchTerm={debouncedSearchTerm}
                    currentMatchIndex={currentMatchIndex}
                    matches={matches}
                />
            ) : (
                <Box
                    sx={{
                        height: '100%',
                        flexGrow: 1,
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        flexDirection: 'column',
                    }}
                >
                    <Icon fontSize="large" color="disabled">
                        {'block'}
                    </Icon>
                    <Typography variant="h5" color="text.secondary">
                        {t($ => $.page.salut.logs.noPreview)}
                    </Typography>
                </Box>
            )}
        </Box>
    );
};

export default LogsViewer;

export const PreselectLogsViewer = ({
    entornApp,
}: {
    entornApp: EntornAppModel & DefaultLogsPerspective;
}) => {
    const { isReady, artifactReport } = useResourceApiService('entornApp');
    const [preselectedLog, setPreselectedLog] = useState<string | null>();
    useEffect(() => {
        if (!isReady) return;
        artifactReport(entornApp.id, {
            code: 'llistar_logs',
        })
            .then(response => response as FitxerInfo[])
            .then(fitxerList => {
                for (const defaultLogName of entornApp.defaultLogs) {
                    if (fitxerList.find(log => log.nom === defaultLogName)) {
                        setPreselectedLog(defaultLogName);
                        return;
                    }
                }
                setPreselectedLog(null);
            })
            .catch(error => {
                console.error('Error al procés de selecció automàtica de logs', error);
                setPreselectedLog(null);
            });
    }, [isReady, artifactReport, entornApp]);
    if (preselectedLog === undefined)
        return (
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '100%',
                }}
            >
                <CircularProgress size={50} />
            </Box>
        );
    return <LogsViewer entornAppId={entornApp.id} preselectedLog={preselectedLog} />;
};
