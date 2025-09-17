import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import {
    FormPage,
    GridPage,
    MuiDataGrid,
    MuiForm,
    FormField,
} from 'reactlib';

const dataGridColumns = [{
    field: 'nom',
    flex: 1,
}];

export const AlarmaForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    return <FormPage>
        <MuiForm
            id={id}
            title={id ? t('page.alarmaConfig.update') : t('page.alarmaConfig.create')}
            resourceName="app"
            goBackLink="/app"
            createLink="form/{{id}}">
            <FormField name="nom" />
        </MuiForm>
    </FormPage>;
}

const Alarma = () => {
    const { t } = useTranslation();
    return <GridPage>
        <MuiDataGrid
            title={t('page.alarmaConfig.title')}
            resourceName="alarmaConfig"
            columns={dataGridColumns}
            toolbarType="upper"
            toolbarCreateLink="form"
            rowUpdateLink="form/{{id}}"
        />
    </GridPage>;
}

export default Alarma;