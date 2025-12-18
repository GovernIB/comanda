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
import { ResourceApiBlobResponse } from '../../../lib/components/ResourceApiProvider';

const sampleText = (
    'Lorem ipsum dolor sit amet, consectetur adipiscing elit. In iaculis facilisis maximus. Nam dictum quam tellus, in elementum libero tincidunt in. Nulla sed facilisis felis. Morbi imperdiet condimentum est, sit amet varius ante dictum sed. Integer at pharetra eros, ac molestie elit. Aliquam malesuada felis metus, at venenatis nunc condimentum in. Donec mi orci, luctus non massa ut, blandit sollicitudin magna. Nullam mollis nulla nulla, et convallis arcu faucibus eu. Nam vitae pretium nulla. Donec pharetra leo non molestie rutrum.\n' +
    'Duis lobortis quam lectus, ac porta lectus venenatis ac. Etiam hendrerit euismod purus sed aliquam. Vivamus dictum mattis eleifend. Maecenas sem quam, dapibus id facilisis at, commodo at ipsum. Aliquam efficitur lacus ex, a congue massa malesuada a. Curabitur quam magna, pretium vel placerat scelerisque, lacinia quis arcu. Nam congue rutrum justo vitae molestie. Quisque faucibus odio et enim semper, vel accumsan nulla congue. Sed ut velit a odio placerat dignissim sed vitae lectus. Quisque sodales feugiat massa, eget fringilla lectus auctor sit amet.\n' +
    'Donec ornare fringilla enim nec rhoncus. Mauris malesuada erat a neque vestibulum condimentum. Donec dapibus, mauris et rhoncus rutrum, lorem sapien dapibus sapien, quis fermentum neque nulla sed urna. Vivamus pellentesque cursus quam ut commodo. Nulla elementum lacus ac nisl scelerisque, a pharetra diam elementum. Aliquam ultricies velit mattis neque rhoncus finibus. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Sed id mattis tellus. Suspendisse vehicula, dolor eget auctor commodo, augue neque gravida leo, nec vulputate est risus at justo. Vivamus finibus in sem ut pretium. Vivamus consequat ut arcu sed volutpat. Cras vestibulum, ex ac dignissim fringilla, metus magna faucibus magna, at commodo justo ipsum ac sem. Nulla justo urna, finibus id quam quis, tristique dapibus neque. Fusce rhoncus urna sed massa euismod porta. Sed accumsan, felis non ultrices sagittis, est libero elementum enim, ac laoreet lacus ligula sed erat. Praesent massa metus, efficitur eu lorem aliquam, porttitor elementum lorem.\n' +
    'Quisque venenatis in lorem vel fringilla. Mauris tincidunt interdum eros, eu imperdiet ante vestibulum quis. Suspendisse pretium malesuada neque, at sagittis dui dapibus id. Vivamus mattis nisi at leo consectetur, vel feugiat enim mollis. Aliquam erat volutpat. Donec sollicitudin nisl magna, sed accumsan sapien condimentum et. Pellentesque consequat nisl in nisl convallis laoreet.\n' +
    'Quisque sem felis, hendrerit egestas nisi sit amet, dignissim egestas risus. Nulla condimentum lacus id tristique mattis. Aenean purus tellus, cursus sed nibh eget, elementum aliquet risus. Aenean maximus diam nec arcu consequat, ullamcorper gravida dolor suscipit. Duis convallis lacus nisi, nec luctus tellus porta ac. Quisque scelerisque tortor quis aliquet efficitur. Nunc gravida erat erat, sed gravida magna volutpat sit amet. Curabitur scelerisque pharetra diam sed sollicitudin. Integer commodo nulla sed enim rutrum, quis suscipit velit suscipit. Quisque pharetra tristique orci id efficitur.\n' +
    'Nulla bibendum turpis eget enim placerat ultricies. Cras velit est, aliquet quis ligula sed, volutpat sodales elit. Sed pharetra dolor ac eleifend vulputate. Proin ante leo, pretium eu tortor sit amet, lobortis aliquet eros. Quisque purus augue, feugiat efficitur finibus sit amet, mattis sed tellus. Nulla aliquam convallis felis, sed luctus urna lobortis id. Cras blandit aliquam massa et gravida. Vestibulum massa est, tempus a venenatis sit amet, aliquam vitae nisl.\n' +
    'Pellentesque ipsum arcu, fringilla sed placerat at, eleifend fringilla eros. Nulla bibendum leo vel lorem iaculis vehicula. Praesent fermentum felis tortor, sit amet aliquam ante vulputate non. Fusce tincidunt eros vel interdum mattis. Nulla lacinia dolor tortor, at consectetur ipsum auctor sit amet. Mauris est ipsum, tristique non odio ut, imperdiet porttitor ipsum. Aliquam auctor erat velit, sit amet lacinia est pharetra id. Integer dictum sodales ex non placerat.\n' +
    'Nullam aliquam quam ut orci feugiat pharetra. Etiam volutpat diam a est ultrices congue. Quisque sed sapien sed sapien ultricies tristique. Suspendisse pulvinar odio eget sapien dapibus pulvinar sed bibendum urna. Aliquam luctus vel mi ut tristique. Duis neque nisi, vestibulum at est id, iaculis malesuada ipsum. Cras arcu odio, ornare porta finibus iaculis, pellentesque et ipsum. Aliquam sodales metus augue, sit amet ullamcorper dolor tincidunt sit amet. Ut sagittis congue leo et tincidunt. Morbi consequat ante a diam venenatis sodales. Integer maximus iaculis odio placerat pretium. Sed imperdiet magna ex, vel placerat sapien pellentesque a. Nulla facilisi. Sed fermentum sapien non orci consequat mollis. Suspendisse in posuere enim. Quisque laoreet, ligula in scelerisque maximus, metus urna commodo purus, a dignissim magna nulla non lectus.\n' +
    'Proin nec justo sagittis, ullamcorper enim a, suscipit lectus. Maecenas nec lacus diam. Sed iaculis dignissim magna at lobortis. Maecenas feugiat cursus dictum. Pellentesque consequat, nisl sed sodales condimentum, orci mi facilisis eros, ut interdum erat urna id eros. Donec faucibus felis quis leo varius ullamcorper. Integer magna leo, elementum in dolor ut, ultricies cursus neque. Curabitur in neque dolor. Donec maximus, lorem eget vulputate placerat, arcu quam facilisis ligula, fringilla sollicitudin lorem dolor eu ex. Cras egestas massa nec felis porta, id posuere tortor commodo. Aenean finibus eleifend lacus, suscipit faucibus magna pellentesque consequat. Morbi tincidunt dictum sollicitudin. Pellentesque a blandit tortor, sed ullamcorper est. Integer vulputate aliquet leo sit amet ornare. Aliquam dictum ipsum a sem dapibus, in facilisis nisl venenatis.\n' +
    'Vestibulum tincidunt vehicula mi sit amet congue. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis in vestibulum ligula, sit amet imperdiet massa. Donec mollis eros et magna euismod volutpat. Donec risus ex, congue et tellus ac, lobortis accumsan dolor. In auctor, neque vel porta pretium, est sapien elementum lorem, a bibendum mauris dui non enim. In tristique, velit quis suscipit viverra, metus libero mattis ligula, non varius augue nisl in ipsum. Aenean quis feugiat velit. Nullam tristique dui vitae purus convallis sodales. Maecenas ullamcorper efficitur neque sit amet fringilla. Nullam pellentesque nisi sit amet massa varius, ac pretium velit lobortis. Mauris vel enim consequat, gravida tellus in, ullamcorper arcu. Nullam blandit mi felis, tristique finibus eros mattis ut. Nulla scelerisque imperdiet lacus, a semper purus rhoncus ut.\n' +
    'Nulla risus magna, sollicitudin nec rutrum vel, consequat cursus leo. Praesent in mauris dui. Curabitur sem ipsum, porttitor ut congue ut, malesuada et tellus. Aenean iaculis mi eu porta mattis. Donec vitae risus dictum, egestas ante eget, dignissim ex. In imperdiet est id tempor consequat. Sed ut lorem vitae velit fermentum interdum ac in elit. Cras porttitor felis nisl, sit amet luctus enim vulputate ac. Pellentesque non ex aliquet, mattis lorem sed, faucibus urna. Cras porttitor nulla at luctus sollicitudin. Nulla facilisi. Vivamus vestibulum ante tempus iaculis rhoncus. Fusce ipsum urna, lobortis vel est vel, euismod tincidunt massa.\n' +
    'Aliquam mollis facilisis justo sit amet ultrices. Suspendisse nibh nisi, posuere vel lorem sed, scelerisque laoreet urna. Aenean nec dui vel diam fringilla interdum. Vestibulum massa odio, condimentum nec hendrerit id, tincidunt quis augue. In feugiat purus quis leo congue eleifend. Quisque at lectus in diam mattis laoreet. Quisque scelerisque hendrerit magna sed euismod. In cursus erat in nisi ultrices dictum quis sed eros. Aliquam elit augue, sodales sit amet aliquam nec, malesuada et dolor. Vivamus vitae suscipit leo, ac imperdiet sem. Ut iaculis felis ante, placerat semper neque tincidunt pretium. Sed viverra tellus a elit facilisis, et lobortis velit tincidunt.\n' +
    'Etiam condimentum sem sit amet eros hendrerit pretium. Nulla et porta orci. Sed consectetur commodo lacus, porttitor efficitur enim facilisis eget. Fusce vel vehicula elit, scelerisque mollis ligula. Ut tempus nunc eu blandit elementum. Sed pellentesque facilisis mi non lobortis. Donec purus felis, suscipit euismod sapien at, efficitur varius ex.\n' +
    'Donec eget imperdiet libero. Curabitur nec justo sit amet diam pulvinar ullamcorper. Donec elementum lacus et mollis bibendum. Vivamus ornare tortor quis pellentesque fringilla. Donec aliquam fringilla elit. In consequat ac orci vel consectetur. Sed arcu nulla, consequat ac porttitor non, dignissim at tortor. In metus dolor, finibus et pretium ultricies, elementum et neque.\n'
).repeat(50);

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
            initialState={{
                sorting: {
                    sortModel: [{ field: 'showPreview', sort: 'desc'}],
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

const Virtualizer = ({ lines, scrollToBottom }: { lines: string[]; scrollToBottom?: boolean }) => {
    const containerRef = useRef<HTMLDivElement>(null);
    const rowVirtualizer = useVirtualizer({
        count: lines.length,
        getScrollElement: () => containerRef.current!,
        estimateSize: () => 25,
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
                    }}
                >
                    {virtualRows.map(virtualRow => (
                        <p
                            key={virtualRow.key}
                            data-index={virtualRow.index}
                            ref={rowVirtualizer.measureElement}
                        >
                            {lines[virtualRow.index]}
                        </p>
                    ))}
                </div>
            </div>
        </Box>
    );
};

const LogsViewer = ({ entornAppId }: { entornAppId: number }) => {
    const [selected, setSelected] = useState<string>();
    const closeDialogButtons = useCloseDialogButtons();
    // const [showDialog, dialogElement, dialogRef] = useContentDialog(closeDialogButtons);
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

    const [lines, setLines] = useState<string[]>(sampleText.split('\n'));
    const [softWrap, setSoftWrap] = useState(false);
    const [scrollToBottom, setScrollToBottom] = useState(false);

    const add = () => {
        setLines([...lines, 'New line' + Date.now()]);
    };

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
                    <IconButton disabled={!isReady} onClick={() => setDialogOpen(true)}>
                        <MenuIcon />
                    </IconButton>
                    <Typography component="h3">
                        {selected ?? 'Seleccioni un fitxer per a visualitzar-lo...'}
                    </Typography>
                </Box>
                <Box>
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
                    <Button onClick={add} disabled={!selected}>
                        Add
                    </Button>
                </Box>
            </Box>
            <Divider />
            {selected ? (
                <Virtualizer lines={lines} scrollToBottom={scrollToBottom} />
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
