import { dateFormatLocale, useCloseDialogButtons, useResourceApiService } from 'reactlib';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Box, Button, ToggleButton, Tooltip, Typography } from '@mui/material';
import { useVirtualizer } from '@tanstack/react-virtual';
import WrapTextIcon from '@mui/icons-material/WrapText';
import VerticalAlignBottomIcon from '@mui/icons-material/VerticalAlignBottom';
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
import { ResourceApiBlobResponse } from '../../../lib/components/ResourceApiProvider';
import CircularProgress from '@mui/material/CircularProgress';
import { mergeSequentialStringArrays } from '../../util/stringUtils';
import { useTranslation } from 'react-i18next';
import useDataGridLocale from '../../hooks/useDataGridLocale';

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
    const dataGridLocale = useDataGridLocale();
    const [logs, setLogs] = useState<FitxerInfo[]>([]);
    useEffect(() => {
        if (!isReady) {
            return;
        }
        async function requests() {
            const list = await artifactReport(entornAppId, {
                code: 'llistar_logs',
            });
            setLogs(list as FitxerInfo[]);
        }
        requests();
    }, [isReady, artifactReport, entornAppId]);
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
        <DataGridPro
            loading={loading}
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
        ></DataGridPro>
    );
};

const getLineNumber = (virtualizerLineIndex: number) =>
    (virtualizerLineIndex + 1).toString().slice(-4);

/**
 * Component per visualitzar el text del log de forma eficient utilitzant virtualització.
 * Permet navegar per milers de línies sense penalització de rendiment.
 */
const Virtualizer = ({
    lines,
    scrollToBottom,
    onScrollToBottomChange,
    softWrap,
}: {
    lines: string[];
    scrollToBottom: boolean;
    onScrollToBottomChange: (value: boolean) => void;
    softWrap: boolean;
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

    // Manejador del desplaçament per detectar si l'usuari surt del final del fitxer.
    const handleScroll = useCallback(() => {
        if (!scrollToBottom || !containerRef.current) return;

        const { scrollTop, scrollHeight, clientHeight } = containerRef.current;
        const isAtBottom = Math.abs(scrollHeight - clientHeight - scrollTop) < 10;

        if (!isAtBottom) {
            onScrollToBottomChange(false);
        }
    }, [scrollToBottom, onScrollToBottomChange]);

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
                            {lines[virtualRow.index]}
                        </p>
                    ))}
                </div>
            </div>
        </Box>
    );
};

/**
 * Component intermediari per carregar o visualitzar la previsualització en directe.
 */
const LivePreview = ({
    lines,
    scrollToBottom,
    onScrollToBottomChange,
    softWrap,
}: {
    lines?: string[] | null;
    scrollToBottom: boolean;
    onScrollToBottomChange: (value: boolean) => void;
    softWrap: boolean;
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
        />
    );
};

/**
 * Component principal de la pestanya de logs de salut.
 * Gestiona la selecció de fitxers, la càrrega de dades i la previsualització en directe.
 */
const LogsViewer = ({ entornAppId }: { entornAppId: number }) => {
    const { t } = useTranslation();
    const [selected, setSelected] = useState<string>();
    const [lines, setLines] = useState<string[] | null>(null);
    const [isRefreshLoading, setIsRefreshLoading] = useState<boolean>(false);
    const [isDownloadLoading, setIsDownloadLoading] = useState<boolean>(false);
    const closeDialogButtons = useCloseDialogButtons();
    const [dialogOpen, setDialogOpen] = useState(false);
    const { isReady, artifactReport } = useResourceApiService('entornApp');

    /**
     * Actualitza el contingut del log actualment seleccionat.
     * Només recupera les últimes 1000 línies i les fusiona amb les ja existents.
     */
    const refreshPreview = useCallback(async () => {
        if (!selected) {
            return;
        }
        setIsRefreshLoading(true);
        const list = await artifactReport(entornAppId, {
            code: 'previsualitzar_log',
            data: {
                fileName: selected,
                lineCount: 1000,
            },
        });
        setLines(prevState => {
            const newLines = (list as any[]).map(liniaDto => liniaDto.linia) as string[];
            return mergeSequentialStringArrays(prevState ?? [], newLines);
        });
        setIsRefreshLoading(false);
    }, [artifactReport, entornAppId, selected]);

    useEffect(() => {
        if (!isReady) {
            return;
        }
        setLines(null);
        if (selected) {
            refreshPreview();
        }
    }, [isReady, artifactReport, entornAppId, selected, refreshPreview]);

    /**
     * Funció per descarregar un fitxer de log directament des de l'API.
     */
    const download = useCallback(
        async (name: string) => {
            if (!isReady) return;
            setIsDownloadLoading(true);
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
            setIsDownloadLoading(false);
        },
        [artifactReport, entornAppId, isReady]
    );

    /**
     * Selecciona un fitxer per a la seva previsualització.
     */
    const preview = useCallback((name: string) => {
        setSelected(name);
        setDialogOpen(false);
    }, []);

    const [softWrap, setSoftWrap] = useState(false);
    const [scrollToBottom, setScrollToBottom] = useState(false);

    return (
        <Box
            sx={{
                height: '100%',
                width: '100%',
                display: 'flex',
                flexDirection: 'column',
                gap: 2,
                '& p': {
                    m: 0,
                },
            }}
        >
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
                            <Tooltip title={t($ => $.page.salut.logs.refresh)}>
                                <IconButton loading={isRefreshLoading} onClick={() => refreshPreview()}>
                                    <RefreshIcon />
                                </IconButton>
                            </Tooltip>
                            <Tooltip title={t($ => $.page.salut.logs.download)}>
                                <IconButton
                                    loading={isDownloadLoading}
                                    onClick={() => download(selected)}
                                >
                                    <DownloadIcon />
                                </IconButton>
                            </Tooltip>
                        </>
                    )}
                </Box>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <Tooltip title={t($ => $.page.salut.logs.softWrap)}>
                        <ToggleButton
                            value="wrapText"
                            size="small"
                            selected={softWrap}
                            color="primary"
                            onChange={() => setSoftWrap(prevSelected => !prevSelected)}
                            disabled={!selected}
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
                        >
                            <VerticalAlignBottomIcon />
                        </ToggleButton>
                    </Tooltip>
                </Box>
            </Box>
            <Divider sx={{ mb: 2 }} />
            {selected ? (
                <LivePreview
                    key={selected + entornAppId}
                    lines={lines}
                    scrollToBottom={scrollToBottom}
                    onScrollToBottomChange={setScrollToBottom}
                    softWrap={softWrap}
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
