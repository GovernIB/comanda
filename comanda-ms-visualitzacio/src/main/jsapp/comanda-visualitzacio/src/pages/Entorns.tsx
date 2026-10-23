import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import { FormField, MuiDataGrid } from 'reactlib';
import useReordering from '../hooks/reordering.tsx';
import PageTitle from '../components/PageTitle.tsx';
import useReadOnlyGestor from '../hooks/useReadOnlyGestor.ts';
import { ScreenFrame } from '../lot2';
import useScreenGridProps from '../hooks/useScreenGridProps.tsx';
import { screenIcons } from '../util/screenIcons.ts';

const columns = [
    {
        field: 'codi',
        flex: 1,
    },
    {
        field: 'nom',
        flex: 3,
    },
];

const Entorns: React.FC = () => {
    const { t } = useTranslation();
    const screenGridProps = useScreenGridProps(screenIcons.entorn, t(($) => $.page.entorns.title));
    const { dataGridProps, loadingElement } = useReordering('entorn');
    const gestorReadOnly = useReadOnlyGestor();

    return (
        <>
            <PageTitle title={t(($) => $.page.entorns.title)} />
            <ScreenFrame>
                <MuiDataGrid
                    {...screenGridProps}
                    resourceName="entorn"
                    columns={columns}
                    paginationActive
                    popupEditActive
                    popupEditFormContent={
                        <Grid container spacing={2}>
                            <Grid size={4}>
                                <FormField name="codi" />
                            </Grid>
                            <Grid size={8}></Grid>
                            <Grid size={12}>
                                <FormField name="nom" />
                            </Grid>
                        </Grid>
                    }
                    toolbarElementsWithPositions={[
                        {
                            position: 1,
                            element: loadingElement,
                        },
                    ]}
                    rowHideDeleteButton={gestorReadOnly}
                    {...(!gestorReadOnly ? dataGridProps : {})}
                />
            </ScreenFrame>
        </>
    );
};

export default Entorns;
