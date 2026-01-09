import { useCloseDialogButtons, useResourceApiService } from 'reactlib';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Box, Button, ToggleButton, Typography } from '@mui/material';
import { useVirtualizer } from '@tanstack/react-virtual';
import WrapTextIcon from '@mui/icons-material/WrapText';
import VerticalAlignBottomIcon from '@mui/icons-material/VerticalAlignBottom';
import Divider from '@mui/material/Divider';
import MenuIcon from '@mui/icons-material/Menu';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import Dialog from '../../../lib/components/mui/Dialog';
import { DataGridPro, GridActionsCell, GridActionsCellItem } from '@mui/x-data-grid-pro';
import type { GridColDef } from '@mui/x-data-grid';
import DownloadIcon from '@mui/icons-material/Download';
import PageviewIcon from '@mui/icons-material/Pageview';
import RefreshIcon from '@mui/icons-material/Refresh';
import { ResourceApiBlobResponse } from '../../../lib/components/ResourceApiProvider';
import CircularProgress from '@mui/material/CircularProgress';
import { mergeSequentialStringArrays } from '../../util/stringUtils';
import { useTranslation } from 'react-i18next';

interface FitxerInfo {
    nom: string;
    mida: number;
    dataCreacio: string;
    dataModificacio: string;
}

interface LogListRow extends FitxerInfo {
    showPreview: boolean;
}

const allowedFileTypes = ['.log', '.txt'];

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
                flex: 0.5,
            },
            {
                field: 'dataModificacio',
                headerName: t($ => $.page.salut.logs.logsList.dataModificacio),
                flex: 0.5,
            },
            {
                field: 'mida',
                headerName: t($ => $.page.salut.logs.logsList.mida),
                // type: 'number',
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
                renderCell: params => {
                    return (
                        <GridActionsCell {...params}>
                            {params.row.showPreview && (
                                <GridActionsCellItem
                                    icon={<PageviewIcon />}
                                    onClick={() => onPreview(params.row.nom)}
                                    label={t($ => $.page.salut.logs.preview)}
                                />
                            )}
                            <GridActionsCellItem
                                icon={<DownloadIcon />}
                                onClick={() => onDownload(params.row.nom)}
                                label={t($ => $.page.salut.logs.download)}
                            />
                        </GridActionsCell>
                    );
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
            loading={!logs.length || loading}
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
    (virtualizerLineIndex + 1).toString().slice(-2);

const Virtualizer = ({
    lines,
    scrollToBottom,
    softWrap,
}: {
    lines: string[];
    scrollToBottom: boolean;
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
    const virtualRows = rowVirtualizer.getVirtualItems();
    return (
        <Box
            ref={containerRef}
            sx={{
                height: '100%',
                flexGrow: 1,
                overflow: 'scroll',
                overflowY: scrollToBottom ? 'hidden' : 'scroll',
                overflowX: softWrap ? 'hidden' : 'scroll',
                contain: 'strict',
                '& p': {
                    pl: 3,
                    position: 'relative',
                    textWrap: softWrap ? 'wrap' : 'nowrap',
                },
                '& .lineNumber': {
                    position: 'absolute',
                    top: 0,
                    left: -1,
                    display: 'inline-flex',
                    alignItems: 'baseline',
                    lineHeight: 'inherit',
                    color: 'text.secondary',
                    pointerEvents: 'none',
                    userSelect: 'none',
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

const LivePreview = ({
    lines,
    scrollToBottom,
    softWrap,
}: {
    lines?: string[] | null;
    scrollToBottom: boolean;
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
                }}
            >
                <CircularProgress size={100} />
            </Box>
        );
    }
    return <Virtualizer lines={lines} scrollToBottom={scrollToBottom} softWrap={softWrap} />;
};

const LogsViewer = ({ entornAppId }: { entornAppId: number }) => {
    const { t } = useTranslation();
    const [selected, setSelected] = useState<string>();
    const [lines, setLines] = useState<string[] | null>(null);
    const [isRefreshLoading, setIsRefreshLoading] = useState<boolean>(false);
    const [isDownloadLoading, setIsDownloadLoading] = useState<boolean>(false);
    const closeDialogButtons = useCloseDialogButtons();
    const [dialogOpen, setDialogOpen] = useState(false);
    const { isReady, artifactReport } = useResourceApiService('entornApp');

    const refreshPreview = useCallback(async () => {
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
        refreshPreview();
    }, [isReady, artifactReport, entornAppId, selected, refreshPreview]);

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
                            <IconButton loading={isRefreshLoading} onClick={() => refreshPreview()}>
                                <RefreshIcon />
                            </IconButton>
                            <IconButton
                                loading={isDownloadLoading}
                                onClick={() => download(selected)}
                            >
                                <DownloadIcon />
                            </IconButton>
                        </>
                    )}
                </Box>
                <Box sx={{ display: 'flex', gap: 1 }}>
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
                </Box>
            </Box>
            <Divider />
            {selected ? (
                <LivePreview
                    key={selected + entornAppId}
                    lines={lines}
                    scrollToBottom={scrollToBottom}
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
