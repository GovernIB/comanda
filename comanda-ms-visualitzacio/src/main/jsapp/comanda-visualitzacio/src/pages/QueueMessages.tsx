import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, useNavigate } from 'react-router-dom';
import {
    GridPage,
    MuiGrid,
    useMuiContentDialog,
    useCloseDialogButtons,
} from 'reactlib';
import { ContentDetail } from '../components/ContentDetail';
import { 
    Box, 
    Button, 
    Icon, 
    Typography, 
    Paper, 
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Breadcrumbs,
    Link
} from '@mui/material';
import { useResourceApiContext } from '../../lib/components/ResourceApiContext';

// Types for queue and message data
interface QueueInfo {
    name: string;
    address: string;
    routingType: string;
    durable: boolean;
    messageCount: number;
    consumerCount: number;
    deliveringCount: number;
    messagesAdded: number;
    messagesAcknowledged: number;
    filter: string;
    temporary: boolean;
    autoCreated: boolean;
    purgeOnNoConsumers: boolean;
    maxConsumers: number;
}

interface MessageInfo {
    messageID: string;
    queueName: string;
    timestamp: string;
    type: string;
    durable: boolean;
    priority: number;
    size: number;
    properties: Record<string, any>;
    content: string;
    redelivered: boolean;
    deliveryCount: number;
    expirationTime: string;
}

// Queue details component
const QueueDetails: React.FC<{ data: QueueInfo }> = ({ data }) => {
    const { t } = useTranslation();

    const elementsDetail = [
        {
            label: t('page.queue.detail.name'),
            value: data?.name
        },
        {
            label: t('page.queue.detail.address'),
            value: data?.address
        },
        {
            label: t('page.queue.detail.routingType'),
            value: data?.routingType
        },
        {
            label: t('page.queue.detail.durable'),
            value: data?.durable ? t('common.yes') : t('common.no')
        },
        {
            label: t('page.queue.detail.messageCount'),
            value: data?.messageCount
        },
        {
            label: t('page.queue.detail.consumerCount'),
            value: data?.consumerCount
        },
        {
            label: t('page.queue.detail.deliveringCount'),
            value: data?.deliveringCount
        },
        {
            label: t('page.queue.detail.messagesAdded'),
            value: data?.messagesAdded
        },
        {
            label: t('page.queue.detail.messagesAcknowledged'),
            value: data?.messagesAcknowledged
        },
        {
            label: t('page.queue.detail.filter'),
            value: data?.filter || t('common.none')
        },
        {
            label: t('page.queue.detail.temporary'),
            value: data?.temporary ? t('common.yes') : t('common.no')
        },
        {
            label: t('page.queue.detail.autoCreated'),
            value: data?.autoCreated ? t('common.yes') : t('common.no')
        },
        {
            label: t('page.queue.detail.purgeOnNoConsumers'),
            value: data?.purgeOnNoConsumers ? t('common.yes') : t('common.no')
        },
        {
            label: t('page.queue.detail.maxConsumers'),
            value: data?.maxConsumers
        }
    ];

    return <ContentDetail title={t('page.queue.detail.title')} elements={elementsDetail} />;
};

// Message details component
const MessageDetails: React.FC<{ data: MessageInfo }> = ({ data }) => {
    const { t } = useTranslation();

    const formatDate = (dateString: string): string => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleString();
    };

    const formatProperties = (properties: Record<string, any>): string => {
        if (!properties) return '';
        return Object.entries(properties)
            .map(([key, value]) => `${key}: ${value}`)
            .join('\n');
    };

    const elementsDetail = [
        {
            label: t('page.message.detail.messageID'),
            value: data?.messageID
        },
        {
            label: t('page.message.detail.queueName'),
            value: data?.queueName
        },
        {
            label: t('page.message.detail.timestamp'),
            value: formatDate(data?.timestamp)
        },
        {
            label: t('page.message.detail.type'),
            value: data?.type || t('common.none')
        },
        {
            label: t('page.message.detail.durable'),
            value: data?.durable ? t('common.yes') : t('common.no')
        },
        {
            label: t('page.message.detail.priority'),
            value: data?.priority
        },
        {
            label: t('page.message.detail.size'),
            value: data?.size + ' bytes'
        },
        {
            label: t('page.message.detail.redelivered'),
            value: data?.redelivered ? t('common.yes') : t('common.no')
        },
        {
            label: t('page.message.detail.deliveryCount'),
            value: data?.deliveryCount
        },
        {
            label: t('page.message.detail.expirationTime'),
            value: data?.expirationTime ? formatDate(data?.expirationTime) : t('common.none')
        },
        {
            label: t('page.message.detail.properties'),
            value: formatProperties(data?.properties)
        },
        {
            label: t('page.message.detail.content'),
            value: data?.content
        }
    ];

    return <ContentDetail title={t('page.message.detail.title')} elements={elementsDetail} />;
};

// Main queue messages page component
const QueueMessages: React.FC = () => {
    const { t } = useTranslation();
    const { queueName } = useParams<{ queueName: string }>();
    const navigate = useNavigate();
    const closeDialogButton = useCloseDialogButtons();
    const [showMessageDialog, messageDialogComponent] = useMuiContentDialog(closeDialogButton);

    const [queueInfo, setQueueInfo] = React.useState<QueueInfo | null>(null);
    const [messages, setMessages] = React.useState<MessageInfo[]>([]);
    const [loading, setLoading] = React.useState<boolean>(true);
    const [error, setError] = React.useState<string | null>(null);
    const [purgeDialogOpen, setPurgeDialogOpen] = React.useState<boolean>(false);

    // Fetch queue info and messages
    const { requestHref } = useResourceApiContext();
    const fetchQueueData = React.useCallback(async () => {
        try {
            setLoading(true);
            const queueResponse = await requestHref(`/api/broker/queues/${queueName}`);
            setQueueInfo(queueResponse.data);

            const messagesResponse = await requestHref(`/api/broker/queues/${queueName}/messages`);
            setMessages(messagesResponse.data);

            setLoading(false);
        } catch (err) {
            console.error('Error fetching queue data:', err);
            setError(t('page.queue.error.fetchFailed'));
            setLoading(false);
        }
    }, [queueName, t, requestHref]);

    React.useEffect(() => {
        fetchQueueData();
    }, [fetchQueueData]);

    // Show message details
    const handleShowMessageDetails = (message: MessageInfo) => {
        showMessageDialog(
            t('page.message.detail.title'),
            <MessageDetails data={message} />,
            closeDialogButton,
            { maxWidth: 'lg', fullWidth: true }
        );
    };

    // Delete a message
    const handleDeleteMessage = async (messageID: string) => {
        try {
            const response = await fetch(`/api/broker/queues/${queueName}/messages/${messageID}`, {
                method: 'DELETE',
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // Refresh messages after deletion
            fetchQueueData();
        } catch (err) {
            console.error('Error deleting message:', err);
            setError(t('page.message.error.deleteFailed'));
        }
    };

    // Purge all messages from the queue
    const handlePurgeQueue = async () => {
        try {
            const response = await fetch(`/api/broker/queues/${queueName}/messages`, {
                method: 'DELETE',
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            setPurgeDialogOpen(false);
            // Refresh queue data after purge
            fetchQueueData();
        } catch (err) {
            console.error('Error purging queue:', err);
            setError(t('page.queue.error.purgeFailed'));
            setPurgeDialogOpen(false);
        }
    };

    // Navigate back to broker page
    const handleBackToBroker = () => {
        navigate('/broker');
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <Typography>{t('common.loading')}</Typography>
            </Box>
        );
    }

    if (error) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <Typography color="error">{error}</Typography>
            </Box>
        );
    }

    // Define columns for the messages grid
    const columns = [
        {
            field: 'messageID',
            headerName: t('page.message.grid.messageID'),
            flex: 2,
        },
        {
            field: 'timestamp',
            headerName: t('page.message.grid.timestamp'),
            flex: 1,
            valueFormatter: (params: any) => {
                return new Date(params.value).toLocaleString();
            },
        },
        {
            field: 'type',
            headerName: t('page.message.grid.type'),
            flex: 1,
        },
        {
            field: 'priority',
            headerName: t('page.message.grid.priority'),
            flex: 0.5,
        },
        {
            field: 'size',
            headerName: t('page.message.grid.size'),
            flex: 0.5,
            valueFormatter: (params: any) => {
                return `${params.value} bytes`;
            },
        },
        {
            field: 'actions',
            headerName: t('page.message.grid.actions'),
            flex: 1,
            renderCell: (params: any) => (
                <Button
                    variant="outlined"
                    color="error"
                    size="small"
                    onClick={() => handleDeleteMessage(params.row.messageID)}
                >
                    {t('common.delete')}
                </Button>
            ),
        },
    ];

    return (
        <GridPage disableMargins>
            <Box sx={{ p: 2 }}>
                {/* Breadcrumbs navigation */}
                <Breadcrumbs aria-label="breadcrumb" sx={{ mb: 2 }}>
                    <Link color="inherit" onClick={handleBackToBroker} sx={{ cursor: 'pointer' }}>
                        {t('page.broker.title')}
                    </Link>
                    <Typography color="text.primary">{queueName}</Typography>
                </Breadcrumbs>

                <Typography variant="h4" component="h1" gutterBottom>
                    {t('page.queue.title', { name: queueName })}
                </Typography>

                {/* Queue details */}
                {queueInfo && (
                    <Paper sx={{ p: 2, mb: 4 }}>
                        <QueueDetails data={queueInfo} />
                    </Paper>
                )}

                {/* Actions */}
                <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="h5" component="h2">
                        {t('page.message.title')}
                    </Typography>

                    <Button
                        variant="contained"
                        color="error"
                        startIcon={<Icon>delete_sweep</Icon>}
                        onClick={() => setPurgeDialogOpen(true)}
                        disabled={messages.length === 0}
                    >
                        {t('page.queue.actions.purge')}
                    </Button>
                </Box>

                {/* Messages grid */}
                <MuiGrid
                    rows={messages}
                    columns={columns}
                    getRowId={(row) => row.messageID}
                    onRowClick={(params) => handleShowMessageDetails(params.row)}
                    autoHeight
                    disableColumnMenu
                    disableSelectionOnClick
                    hideFooter={messages.length <= 10}
                    pageSize={10}
                    rowsPerPageOptions={[10]}
                    sx={{ height: 400 }}
                />

                {/* Purge confirmation dialog */}
                <Dialog
                    open={purgeDialogOpen}
                    onClose={() => setPurgeDialogOpen(false)}
                >
                    <DialogTitle>{t('page.queue.purge.title')}</DialogTitle>
                    <DialogContent>
                        <DialogContentText>
                            {t('page.queue.purge.confirmation', { name: queueName })}
                        </DialogContentText>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setPurgeDialogOpen(false)}>
                            {t('common.cancel')}
                        </Button>
                        <Button onClick={handlePurgeQueue} color="error" autoFocus>
                            {t('common.confirm')}
                        </Button>
                    </DialogActions>
                </Dialog>

                {/* Message details dialog */}
                {messageDialogComponent}
            </Box>
        </GridPage>
    );
};

export default QueueMessages;
