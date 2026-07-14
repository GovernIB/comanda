import React, { useEffect, useState } from 'react';
import { IconButton, Icon, CircularProgress } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { ExistsParameterResult } from '../pages/Apps';

interface ParameterExistsAdornmentProps {
    value?: string;
    onClick: (value: string) => Promise<ExistsParameterResult>;
    disabled?: boolean;
}

type VerificationStatus = 'idle' | 'loading' | 'exists' | 'notExists';

const StatusIcon = ({ status }: { status: VerificationStatus }) => {
    const { t } = useTranslation();
    switch (status) {
        case 'exists':
            return <Icon title={t($ => $.page.apps.fields.parameterExists)} fontSize="small" color="success">check_circle</Icon>;
        case 'notExists':
            return <Icon title={t($ => $.page.apps.fields.parameterNotExists)} fontSize="small" color="error">cancel</Icon>;
        default:
            return null;
    }
};

const ParameterExistsAdornment: React.FC<ParameterExistsAdornmentProps> = ({ value, onClick, disabled }) => {
    const { t } = useTranslation();
    const [status, setStatus] = useState<VerificationStatus>('idle');

    useEffect(() => {
        setStatus('idle');
    }, [value, disabled]);

    const handleOnClick = async () => {
        if (!value) return;
        setStatus('loading');
        try {
            const exists = await onClick(value);
            setStatus(exists ? 'exists' : 'notExists');
        } catch {
            setStatus('notExists');
        }
    };

    return (
        <React.Fragment>
            {status === 'loading' ? (
                <CircularProgress size={20} />
            ) : (
                <StatusIcon status={status} />
            )}
            <IconButton
                disabled={!value || status === 'loading' || disabled}
                onClick={handleOnClick}
                title={t($ => $.page.apps.fields.checkParameter)}
            >
                <Icon fontSize="small">fact_check</Icon>
            </IconButton>
        </React.Fragment>
    );
};

export default ParameterExistsAdornment;
