import React from 'react';
import { Box, Typography, Icon, Button, Snackbar, Alert } from '@mui/material';
import { useTranslation } from 'react-i18next';

type StacktraceBlockProps = {
    title: string;
    value?: string;
};

export const StacktraceBlock: React.FC<StacktraceBlockProps> = ({ title, value }) => {
    const { t } = useTranslation();
    const [copied, setCopied] = React.useState(false);
    const handleCopy = async () => {
        if (value) {
            await navigator.clipboard.writeText(value);
            setCopied(true);
        }
    };
    const handleClose = ( _event?: React.SyntheticEvent | Event, reason?: string ) => {
        if (reason === 'clickaway') return;
        setCopied(false);
    };

    return (
        <>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                <Typography variant="subtitle1" fontWeight="bold">{title}</Typography>
                <Button onClick={handleCopy} size="small" title={t('components.copiarContingutTitle')}>
                    <Icon fontSize="small" sx={{mr:1}}>content_copy</Icon>
                    {t('components.copiarContingut')}
                </Button>
            </Box>

            <Box sx={{
                border: '1px solid #ccc',
                borderRadius: 2,
                padding: 2,
                width: '100%',
                minHeight: 150,
                maxHeight: 300,
                overflow: 'auto',
                whiteSpace: 'pre',
                fontFamily: 'monospace',
                backgroundColor: '#f9f9f9'
            }}>
                <Typography variant="body2" component="pre">
                    {value}
                </Typography>
            </Box>

            <Snackbar
                open={copied}
                autoHideDuration={2000}
                onClose={handleClose}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }} >
                <Alert
                    onClose={handleClose}
                    severity="info"
                    variant="filled"
                    sx={{ width: '100%' }} >
                    {t('components.copiarContingutSuccess')}
                </Alert>
            </Snackbar>
        </>
    );
};
