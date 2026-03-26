import React, { useEffect, useState } from 'react';
import { IconButton, Icon, CircularProgress } from '@mui/material';
import {ContentDialogShowFn} from "../../lib/components/BaseAppContext.tsx";
import ContentDetail from "./ContentDetail.tsx";
import {useTranslation} from "react-i18next";

interface UrlPingAdornmentProps {
    url?: string;
    formData: any;
    onClick: ({ endpoint, formData }: { endpoint: string, formData: any }) => Promise<any>;
    dialogShow: ContentDialogShowFn
}

type PingStatus = 'idle' | 'loading' | 'success' | 'warning' | 'error';

const StatusIcon = ({ status, dialogShow }: { status: PingStatus, dialogShow: () => void }) => {
    const {t} = useTranslation()
    switch (status) {
        case 'success':
            return <Icon fontSize="small" color="success">check_circle</Icon>;
        case 'error':
            return <Icon fontSize="small" color="error">cancel</Icon>;
        case 'warning':
            return <IconButton
                title={t($ => $.page.apps.ping.validationTrace)}
                onClick={dialogShow}
            >
                <Icon fontSize="small" color="error">warning</Icon>
            </IconButton>;
        default:
            return null;
    }
};

const UrlPingAdornment: React.FC<UrlPingAdornmentProps> = ({ url, formData, onClick, dialogShow }) => {
  const {t} = useTranslation()
  const [status, setStatus] = useState<PingStatus>('idle');
  const [elementsDetail, setElementsDetail] = useState<any[]>();
  useEffect(() => {//Si cambian el valor en el URL vaciaremos el Status
    setStatus('idle');
  }, [url]);

  const handleOnClick = async () => {
    if (!url) return;
    setStatus('loading');

    try {
      const response = await onClick({ endpoint: url, formData });
      setStatus(response.status);
      setElementsDetail(response.elements);
    } catch (error) {
      setStatus('error');
      setElementsDetail(undefined)
    }
  };

  return (
    <React.Fragment>
      {status === 'loading' ? (
        <CircularProgress size={20} />
      ) : (
        <StatusIcon status={status} dialogShow={() => dialogShow(
            t($ => $.page.apps.ping.validationError),
            <ContentDetail title={""} elements={elementsDetail} />,
            undefined,
            { maxWidth: 'lg', fullWidth: true, }
        )} />
      )}
      <IconButton disabled={!url || status === 'loading'} onClick={handleOnClick}>
        <Icon fontSize="small">network_check</Icon>
      </IconButton>
    </React.Fragment>
  );
};

export default UrlPingAdornment;
