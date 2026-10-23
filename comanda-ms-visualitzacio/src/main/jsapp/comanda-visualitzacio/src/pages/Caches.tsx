import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    MuiDataGrid,
    useResourceApiService,
    useBaseAppContext,
    useConfirmDialogButtons,
    useMuiDataGridApiRef,
} from 'reactlib';
import {ResourceApiError} from "../../lib/components/ResourceApiProvider.tsx";
import Icon from "@mui/material/Icon";
import IconButton from "@mui/material/IconButton";
import {ReactElementWithPosition} from "../../lib/util/reactNodePosition.ts";
import PageTitle from '../components/PageTitle.tsx';
import { ScreenFrame } from '../lot2';
import useScreenGridProps from '../hooks/useScreenGridProps.tsx';
import { screenIcons } from '../util/screenIcons.ts';

const Caches: React.FC = () => {
    const { t } = useTranslation();
    const screenGridProps = useScreenGridProps(screenIcons.cache, t($ => $.page.caches.title));
    const { isReady: apiIsReady, delete: apiDelete } =
        useResourceApiService('comandaCache');
    const gridApiRef = useMuiDataGridApiRef();
    const {
        messageDialogShow,
        temporalMessageShow,
    } = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const confirmDialogComponentProps = { maxWidth: 'sm', fullWidth: true };
    const onDeleteClick = (id: string | number) =>  {
        messageDialogShow(
            t($ => $.page.caches.buidar.titol),
            t($ => $.page.caches.buidar.confirm),
            confirmDialogButtons,
            confirmDialogComponentProps).
        then((value: any) => {
            if (value && apiIsReady) {
                apiDelete(id)
                    .then(() => {
                        gridApiRef.current?.refresh();
                        temporalMessageShow(null, t($ => $.page.caches.buidar.success), 'success');
                    })
                    .catch((error: ResourceApiError) => {
                        temporalMessageShow(t($ => $.page.caches.buidar.error), error.message, 'error');
                    });
            }
        }).
        catch(() => {});
    }

    const onDeleteAllClick = () =>  {
        messageDialogShow(
            t($ => $.page.caches.buidar.totes.titol),
            t($ => $.page.caches.buidar.totes.confirm),
            confirmDialogButtons,
            confirmDialogComponentProps).
        then((value: any) => {
            if (value && apiIsReady) {
                apiDelete('TOTES')
                    .then(() => {
                        gridApiRef.current?.refresh();
                        temporalMessageShow(null, t($ => $.page.caches.buidar.totes.success), 'success');
                    })
                    .catch((error: ResourceApiError) => {
                        temporalMessageShow(t($ => $.page.caches.buidar.totes.error), error.message, 'error');
                    });
            }
        }).
        catch(() => {});
    }

    const toolbarElementsWithPositions: ReactElementWithPosition[] = [
        {
          position: 2,
          element: (
              <IconButton
                  onClick={() => onDeleteAllClick()}
                  title={t($ => $.page.caches.buidar.totes.titol)}
              >
                  <Icon>delete</Icon>
              </IconButton>
          )
        },
    ];

    const columns = [
        {
            field: 'id',
            flex: 1,
        },
        {
            field: 'descripcio',
            flex: 3,
        },
        {
            field: 'entrades',
            flex: 1,
        },
        {
            field: 'mida',
            flex: 1,
        },
    ];
    return (
        <>
            <PageTitle title={t($ => $.page.caches.title)} />
            <ScreenFrame>
                <MuiDataGrid
                    apiRef={gridApiRef}
                    {...screenGridProps}
                    resourceName="comandaCache"
                    columns={columns}
                    paginationActive
                    toolbarHideCreate
                    rowHideUpdateButton
                    rowHideDeleteButton
                    rowAdditionalActions={[
                        {
                            label: t($ => $.page.caches.buidar.label),
                            icon: 'delete',
                            onClick: onDeleteClick,
                        },
                    ]}
                    rowActionsColumnProps={{ width: 10 }}
                    toolbarElementsWithPositions={toolbarElementsWithPositions}
                />
            </ScreenFrame>
        </>
    );
}

export default Caches;
