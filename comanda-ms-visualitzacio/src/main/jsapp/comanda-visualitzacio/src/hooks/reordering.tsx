import { CircularProgress } from '@mui/material';
import { GridEventListener } from '@mui/x-data-grid-pro';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useResourceApiService, useBaseAppContext, MuiDataGridProps } from 'reactlib';

const useReordering = (resourceName: string) => {
    const { temporalMessageShow } = useBaseAppContext();
    const { t } = useTranslation();
    const { patch } = useResourceApiService(resourceName);
    const [ongoingRequests, setOngoingRequests] = React.useState<number[]>([]);
    const onRowOrderChange: GridEventListener<'rowOrderChange'> = React.useCallback(
        (params, _event, details) => {
            const requestIdentifier = Date.now();
            setOngoingRequests((prevRequests) => [...prevRequests, requestIdentifier]);
            patch(params.row?.id, {
                data: {
                    ordre: details.api.getRow(details.api.getAllRowIds()[params.targetIndex]).ordre,
                },
            })
                .catch(() => {
                    temporalMessageShow(
                        '',
                        t(($) => $.reordering.errorMessage),
                        'error'
                    );
                })
                .finally(() => {
                    setOngoingRequests((prevRequests) =>
                        prevRequests.filter((identifier) => identifier !== requestIdentifier)
                    );
                });
        },
        [patch, t, temporalMessageShow]
    );

    const dataGridProps: Partial<MuiDataGridProps> = {
        rowReordering: true,
        onRowOrderChange,
    };
    return {
        dataGridProps,
        loadingElement: ongoingRequests.length ? (
            <CircularProgress sx={{ ml: 1 }} size="1.5rem" />
        ) : (
            <></>
        ),
    };
};

export default useReordering;
