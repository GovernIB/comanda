import { useTranslation } from 'react-i18next';
import {
    GridPage,
    MuiDataGrid,
} from 'reactlib';

const dataGridColumns = [{
    field: 'missatge',
    flex: 1,
}, {
    field: 'dataActivacio',
    flex: 0.5,
}, {
    field: 'estat',
    flex: 0.5,
}];

const Alarmes = () => {
    const { t } = useTranslation();
    return <GridPage>
        <MuiDataGrid
            title={t('menu.alarmes')}
            resourceName="alarma"
            columns={dataGridColumns}
            readOnly
            toolbarType="upper"
            filter="estat:'ACTIVA'"
            sortModel={[{ field: 'dataActivacio', sort: 'desc' }]} />
    </GridPage>;
}

export default Alarmes;