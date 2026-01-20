import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiGrid,
    FormField, useResourceApiService, useBaseAppContext, useConfirmDialogButtons, MuiDataGridApiRef,
} from 'reactlib';
import {ResourceApiError} from "../../lib/components/ResourceApiProvider.tsx";
import Icon from "@mui/material/Icon";
import IconButton from "@mui/material/IconButton";
import {ReactElementWithPosition} from "../../lib/util/reactNodePosition.ts";
import PageTitle from '../components/PageTitle.tsx';

const Caches: React.FC = () => {
    const { t } = useTranslation();
    const { isReady: apiIsReady, delete: apiDelete } =
        useResourceApiService('comandaCache');
    const gridApiRef: MuiDataGridApiRef = React.useRef({});
    const {
        messageDialogShow,
        temporalMessageShow,
        t: tLib,
    } = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const confirmDialogComponentProps = { maxWidth: 'sm', fullWidth: true };
    const onDeleteClick = (id: any) =>  {
        messageDialogShow(
            t('page.caches.buidar.titol'),
            t('page.caches.buidar.confirm'),
            confirmDialogButtons,
            confirmDialogComponentProps).
        then((value: any) => {
            if (value && apiIsReady) {
                apiDelete(id)
                    .then(() => {
                        gridApiRef.current?.refresh();
                        temporalMessageShow(null, t('page.caches.buidar.success'), 'success');
                    })
                    .catch((error: ResourceApiError) => {
                        temporalMessageShow(t('page.caches.buidar.error'), error.message, 'error');
                    });
            }
        }).
        catch(() => {});
    }

    const onDeleteAllClick = () =>  {
        messageDialogShow(
            t('page.caches.buidar.totes.titol'),
            t('page.caches.buidar.totes.confirm'),
            confirmDialogButtons,
            confirmDialogComponentProps).
        then((value: any) => {
            if (value && apiIsReady) {
                apiDelete('TOTES')
                    .then(() => {
                        gridApiRef.current?.refresh();
                        temporalMessageShow(null, t('page.caches.buidar.totes.success'), 'success');
                    })
                    .catch((error: ResourceApiError) => {
                        temporalMessageShow(t('page.caches.buidar.totes.error'), error.message, 'error');
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
                  title={t('page.caches.buidar.totes.titol')}
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
    return <GridPage>
        <PageTitle title={t('page.caches.title')} />
        <MuiGrid
            apiRef={gridApiRef}
            title={t('page.caches.title')}
            resourceName="comandaCache"
            columns={columns}
            toolbarType="upper"
            paginationActive
            toolbarHideCreate
            rowHideUpdateButton
            rowHideDeleteButton
            rowAdditionalActions={[
                {
                    label: t('page.caches.buidar.label'),
                    icon: 'delete',
                    onClick: onDeleteClick,
                },
            ]}
            rowActionsColumnProps={{ width: 10 }}
            toolbarElementsWithPositions={toolbarElementsWithPositions}
        />
    </GridPage>;
}

export default Caches;
