import React, { useEffect, useState } from 'react';
import { IconButton, Icon, CircularProgress } from '@mui/material';

interface UrlPingAdornmentProps {
  url?: string;
  onClick: (url?: string) => Promise<boolean>;
}

type PingStatus = 'idle' | 'loading' | 'success' | 'error';

const UrlPingAdornment: React.FC<UrlPingAdornmentProps> = ({ url, onClick }) => {
  const [status, setStatus] = useState<PingStatus>('idle');
  useEffect(() => {//Si cambian el valor en el URL vaciaremos el Status
    setStatus('idle');
  }, [url]);

  const handleOnClick = async () => {
    if (!url) return;
    setStatus('loading');

    try {
      const success = await onClick(url);
      setStatus(success ? 'success' : 'error');
    } catch (error) {
      setStatus('error');
    }
  };

  const StatusIcon = () => {
    switch (status) {
      case 'success':
        return <Icon fontSize="small" color="success">check_circle</Icon>;
      case 'error':
        return <Icon fontSize="small" color="error">cancel</Icon>;
      default:
        return null;
    }
  };

  return (
    <React.Fragment>
      {status === 'loading' ? (
        <CircularProgress size={20} />
      ) : (
        <StatusIcon />
      )}
      <IconButton disabled={!url || status === 'loading'} onClick={handleOnClick}>
        <Icon fontSize="small">network_check</Icon>
      </IconButton>
    </React.Fragment>
  );
};

export default UrlPingAdornment;