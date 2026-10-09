import * as React from 'react';
import { useTranslation } from 'react-i18next';
import ActionProgressDialog from './ActionProgressDialog.tsx';

const ENTITAT_REFRESH_UO_PROGRESS_EVENT_TYPE = 'entitat.refreshUO.progress';

export type EntitatRefreshUOProgressDialogProps = {
    /** Indica si la modal de progrés s'ha de mostrar */
    open: boolean;
    /** Identificador de l'entitat que s'està actualitzant, per a filtrar els events SSE rebuts */
    entitatId?: string | number | null;
    /**
     * Event que es llença quan el procés real ha acabat (progrés al 100% o error), sigui quina sigui la
     * crida que hagi obert aquesta modal: si ja hi havia una execució en curs, la crida d'aquest client no
     * ha fet cap feina i la seva pròpia resposta HTTP no reflecteix quan acaba el procés real.
     */
    onComplete?: (error: boolean) => void;
    /** Event quant es vol ocultar el diàleg */
    onHide?: () => void;
};

/** Mateix patró que DimensioFetConsProgressDialog (acció FET_CONS de Dimensio), per a l'acció REFRESH_UO d'Entitat. */
const EntitatRefreshUOProgressDialog: React.FC<EntitatRefreshUOProgressDialogProps> = ({
    open,
    entitatId,
    onComplete,
    onHide,
}) => {
    const { t } = useTranslation();

    return (
        <ActionProgressDialog
            open={open}
            itemId={entitatId}
            eventType={ENTITAT_REFRESH_UO_PROGRESS_EVENT_TYPE}
            getItemId={payload => payload.entitatId}
            title={t($ => $.page.entitats.action.refreshUO.progress.title)}
            waitingMessage={t($ => $.page.entitats.action.refreshUO.progress.waiting)}
            processedMessage={(processats, total) =>
                t($ => $.page.entitats.action.refreshUO.progress.processed, { processats, total })
            }
            hideButtonLabel={t($ => $.page.entitats.action.refreshUO.progress.hideInBackground)}
            onComplete={onComplete}
            onHide={onHide}
        />
    );
};

export default EntitatRefreshUOProgressDialog;
