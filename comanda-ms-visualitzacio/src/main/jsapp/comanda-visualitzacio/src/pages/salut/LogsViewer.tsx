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
}: {
    entornAppId: number;
    onDownload: (nom: string) => void;
    onPreview: (nom: string) => void;
}) => {
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
                flex: 1,
            },
            {
                field: 'dataCreacio',
            },
            {
                field: 'dataModificacio',
            },
            {
                field: 'mida',
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
                                    label="Preview"
                                />
                            )}
                            <GridActionsCellItem
                                icon={<DownloadIcon />}
                                onClick={() => onDownload(params.row.nom)}
                                label="Download"
                            />
                        </GridActionsCell>
                    );
                },
            },
        ],
        [onDownload, onPreview]
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
            loading={!logs.length}
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

const Virtualizer = ({ lines, scrollToBottom }: { lines: string[]; scrollToBottom: boolean }) => {
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
                contain: 'strict',
                '& p': {
                    pl: 3,
                    position: 'relative',
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
    entornAppId,
    fileName,
    scrollToBottom,
}: {
    scrollToBottom: boolean;
    fileName: string;
    entornAppId: number;
}) => {
    const { isReady, artifactReport } = useResourceApiService('entornApp');
    const [lines, setLines] = useState<string[] | null>(null);
    useEffect(() => {
        if (!isReady) {
            return;
        }
        async function requests() {
            const list = await artifactReport(entornAppId, {
                code: 'previsualitzar_log',
                data: {
                    fileName,
                    lineCount: 1000,
                },
            });
            setLines((list as any[]).map(liniaDto => liniaDto.linia) as string[]);
        }
        requests();
    }, [isReady, artifactReport, entornAppId, fileName]);

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
    return <Virtualizer lines={lines} scrollToBottom={scrollToBottom} />;
};

const LogsViewer = ({ entornAppId }: { entornAppId: number }) => {
    const [selected, setSelected] = useState<string>();
    const closeDialogButtons = useCloseDialogButtons();
    const [dialogOpen, setDialogOpen] = useState(false);
    const { isReady, artifactReport } = useResourceApiService('entornApp');

    const download = useCallback(
        async (name: string) => {
            if (!isReady) return;
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
                    textWrap: softWrap ? 'wrap' : 'nowrap',
                },
            }}
        >
            <Dialog
                title="TRAD"
                open={dialogOpen}
                closeCallback={() => setDialogOpen(false)}
                buttonCallback={() => setDialogOpen(false)}
                componentProps={{ fullWidth: true, maxWidth: 'md' }}
                buttons={closeDialogButtons}
            >
                <Box sx={{ height: '500px' }}>
                    <LogList entornAppId={entornAppId} onDownload={download} onPreview={preview} />
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
                            {selected ?? 'Seleccionar un fitxer'}
                        </Typography>
                    </Button>
                    {selected && (
                        <>
                            <IconButton>
                                <RefreshIcon />
                            </IconButton>
                            <IconButton onClick={() => download(selected)}>
                                <DownloadIcon />
                            </IconButton>
                        </>
                    )}
                </Box>
                <Box sx={{ display: 'flex', gap: 1}}>
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
                    entornAppId={entornAppId}
                    fileName={selected}
                    scrollToBottom={scrollToBottom}
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
                        No hi ha previsualització
                    </Typography>
                </Box>
            )}
        </Box>
    );
};

export default LogsViewer;
