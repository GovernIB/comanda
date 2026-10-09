import * as React from 'react';
import { useTranslation } from 'react-i18next';
import ActionProgressDialog from './ActionProgressDialog.tsx';

const FET_CONS_PROGRESS_EVENT_TYPE = 'dimensio.fetCons.progress';

export type DimensioFetConsProgressDialogProps = {
    /** Indica si la modal de progrés s'ha de mostrar */
    open: boolean;
    /** Identificador de la dimensió que s'està actualitzant, per a filtrar els events SSE rebuts */
    dimensioId?: string | number | null;
    /**
     * Event que es llença quan el procés real ha acabat (progrés al 100% o error), sigui quina sigui la
     * crida que hagi obert aquesta modal: si ja hi havia una execució en curs, la crida d'aquest client no
     * ha fet cap feina i la seva pròpia resposta HTTP no reflecteix quan acaba el procés real.
     */
    onComplete?: (error: boolean) => void;
    /** Event quant es vol ocultar el diàleg */
    onHide?: () => void;
};

const DimensioFetConsProgressDialog: React.FC<DimensioFetConsProgressDialogProps> = ({
    open,
    dimensioId,
    onComplete,
    onHide,
}) => {
    const { t } = useTranslation();

    return (
        <ActionProgressDialog
            open={open}
            itemId={dimensioId}
            eventType={FET_CONS_PROGRESS_EVENT_TYPE}
            getItemId={payload => payload.dimensioId}
            title={t($ => $.page.dimensions.action.refreshCons.progress.title)}
            waitingMessage={t($ => $.page.dimensions.action.refreshCons.progress.waiting)}
            processedMessage={(processats, total) =>
                t($ => $.page.dimensions.action.refreshCons.progress.processed, { processats, total })
            }
            hideButtonLabel={t($ => $.page.dimensions.action.refreshCons.progress.hideInBackground)}
            onComplete={onComplete}
            onHide={onHide}
        />
    );
};

export default DimensioFetConsProgressDialog;
