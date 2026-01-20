import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiGrid,
    useMuiContentDialog,
    useCloseDialogButtons,
} from 'reactlib';
import { ContentDetail } from '../components/ContentDetail';
import { Tabs, Tab, Chip, Box, Button, Icon, Typography, Paper, Card, CardContent, CardActions } from '@mui/material';
import { useResourceApiContext } from '../../lib/components/ResourceApiContext';
import { buildHref } from '../util/requestUtils.ts';
import PageTitle from '../components/PageTitle.tsx';

// Types for broker data
interface BrokerInfo {
    version: string;
    name: string;
    status: string;
    uptime: string;
    memoryUsage: number;
    diskUsage: number;
    totalConnections: number;
    totalQueues: number;
    totalMessages: number;
}

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

// Status badge component
type StatusBadgeProps = {
    value: string;
};

const StatusBadge: React.FC<StatusBadgeProps> = ({ value }) => {
    let color: 'success' | 'error' | 'warning' | 'default' = 'default';

    switch (value) {
        case 'STARTED':
            color = 'success';
            break;
        case 'STOPPED':
            color = 'error';
            break;
        default:
            color = 'default';
    }

    return <Chip label={value} color={color} size="small" />;
};

// Broker details component
const BrokerDetails: React.FC<{ data: BrokerInfo }> = ({ data }) => {
    const { t } = useTranslation();

    const formatBytes = (bytes: number): string => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    const elementsDetail = [
        {
            label: t('page.broker.detail.version'),
            value: data?.version
        },
        {
            label: t('page.broker.detail.name'),
            value: data?.name
        },
        {
            label: t('page.broker.detail.status'),
            contentValue: <StatusBadge value={data?.status} />
        },
        {
            label: t('page.broker.detail.uptime'),
            value: data?.uptime
        },
        {
            label: t('page.broker.detail.memoryUsage'),
            value: formatBytes(data?.memoryUsage)
        },
        {
            label: t('page.broker.detail.diskUsage'),
            value: formatBytes(data?.diskUsage)
        },
        {
            label: t('page.broker.detail.totalConnections'),
            value: data?.totalConnections
        },
        {
            label: t('page.broker.detail.totalQueues'),
            value: data?.totalQueues
        },
        {
            label: t('page.broker.detail.totalMessages'),
            value: data?.totalMessages
        }
    ];

    return <ContentDetail title={t('page.broker.detail.title')} elements={elementsDetail} />;
};

// Queue list component
const QueueList: React.FC<{ queues: QueueInfo[] }> = ({ queues }) => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const handleViewMessages = (queueName: string) => {
        navigate(`/broker/queue/${queueName}`);
    };

    return (
        <Grid container spacing={2}>
            {queues.map((queue) => (
                <Grid item xs={12} sm={6} md={4} key={queue.name}>
                    <Card>
                        <CardContent>
                            <Typography variant="h6" component="div">
                                {queue.name}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                {t('page.broker.queue.address')}: {queue.address}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                {t('page.broker.queue.routingType')}: {queue.routingType}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                {t('page.broker.queue.durable')}: {queue.durable ? t('common.yes') : t('common.no')}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                {t('page.broker.queue.messageCount')}: {queue.messageCount}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                {t('page.broker.queue.consumerCount')}: {queue.consumerCount}
                            </Typography>
                        </CardContent>
                        <CardActions>
                            <Button size="small" onClick={() => handleViewMessages(queue.name)}>
                                {t('page.broker.queue.viewMessages')}
                            </Button>
                        </CardActions>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
};

// Main broker page component
const Broker: React.FC = () => {
    const { t } = useTranslation();
    const [brokerInfo, setBrokerInfo] = React.useState<BrokerInfo | null>(null);
    const [queues, setQueues] = React.useState<QueueInfo[]>([]);
    const [loading, setLoading] = React.useState<boolean>(true);
    const [error, setError] = React.useState<string | null>(null);

    // Fetch broker info and queues
    const { requestHref } = useResourceApiContext();
    React.useEffect(() => {
        const fetchBrokerInfo = async () => {
            try {
                setLoading(true);
                const brokerResponse = await requestHref(buildHref("broker"));
                setBrokerInfo(brokerResponse.data);

                const queuesResponse = await requestHref(buildHref("broker/queues"));
                setQueues(queuesResponse.data);

                setLoading(false);
            } catch (err) {
                console.error('Error fetching broker data:', err);
                setError(t('page.broker.error.fetchFailed'));
                setLoading(false);
            }
        };

        fetchBrokerInfo();
    }, [t, requestHref]);

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

    return (
        <GridPage>
            <PageTitle title={t('page.broker.title')} />
            <Box sx={{ p: 2, pb: 6 }}>
                <Typography variant="h4" component="h1" gutterBottom>
                    {t('page.broker.title')}
                </Typography>

                {brokerInfo && (
                    <Paper sx={{ p: 2, mb: 4 }}>
                        <BrokerDetails data={brokerInfo} />
                    </Paper>
                )}

                <Typography variant="h5" component="h2" gutterBottom>
                    {t('page.broker.queues.title')}
                </Typography>

                <QueueList queues={queues} />
            </Box>
        </GridPage>
    );
};

export default Broker;
