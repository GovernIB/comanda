import React from "react";
import Snackbar from "@mui/material/Snackbar";
import Alert from "@mui/material/Alert";
import AlertTitle from "@mui/material/AlertTitle";

const TEMPORALMSG_DURATION_DEFAULT = 2000;

export type TemporalMessageSeverity = 'success' | 'info' | 'warning' | 'error';
type TemporalMessageProps = {
    open: boolean;
    setOpen: (open: boolean) => void;
    title?: string;
    message?: string;
    severity?: TemporalMessageSeverity;
    additionalComponents?: React.ReactElement[];
    autoHideDuration?:number;
};

export const useMessage = () => {
    const [open, setOpen] = React.useState<boolean>(false);
    const [title, setTitle] = React.useState<string>();
    const [message, setMessage] = React.useState<string>();
    const [severity, setSeverity] = React.useState<TemporalMessageSeverity>();
    const [additionalComponents, setAdditionalComponents] = React.useState<React.ReactElement[] | undefined>();
    const [autoHideDuration, setAutoHideDuration] = React.useState<number | undefined>(undefined);
    const show = (
        title: string | null,
        message: string,
        severity?: TemporalMessageSeverity,
        additionalComponents?: React.ReactElement[]) => {
        setTitle(title ?? undefined);
        setMessage(message);
        setSeverity(severity)
        setAdditionalComponents(additionalComponents);
        setOpen(true);
        setAutoHideDuration(undefined)
    }
    const showTemporal = (
        title: string | null,
        message: string,
        severity?: TemporalMessageSeverity,
        additionalComponents?: React.ReactElement[],
        autoHideDuration?: number) => {
        setTitle(title ?? undefined);
        setMessage(message);
        setSeverity(severity)
        setAdditionalComponents(additionalComponents);
        setOpen(true);
        setAutoHideDuration(autoHideDuration ?? TEMPORALMSG_DURATION_DEFAULT)
    }
    const component = <Message
        open={open}
        setOpen={setOpen}
        title={title}
        message={message}
        severity={severity}
        additionalComponents={additionalComponents}
        autoHideDuration={autoHideDuration} />;
    return {show, showTemporal, component};
}

export const Message: React.FC<TemporalMessageProps> = (props) => {
    const {
        open,
        setOpen,
        title,
        message,
        severity,
        additionalComponents,
        autoHideDuration,
    } = props;
    return <Snackbar
        open={open}
        onClose={() => setOpen(false)}
        autoHideDuration={autoHideDuration}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        <Alert onClose={() => setOpen(false)} severity={severity ?? 'info'} sx={{ width: '100%' }}>
            {title && <AlertTitle>{title}</AlertTitle>}
            {message}
            {additionalComponents}
        </Alert>
    </Snackbar>;
}

export default Message;