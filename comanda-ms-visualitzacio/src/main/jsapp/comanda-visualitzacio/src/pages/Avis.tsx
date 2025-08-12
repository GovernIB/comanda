import {
    FormField,
    GridPage,
    MuiFilter,
    MuiGrid,
    springFilterBuilder,
    Toolbar,
    useFilterApiRef,
    useFormContext
} from "reactlib";
import {Box, Button, Grid, Typography} from "@mui/material";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import {formatEndOfDay, formatStartOfDay} from "../util/dateUtils.ts";

const avisFilterBuilder = (data:any) :string => {
    return springFilterBuilder.and(
        springFilterBuilder.eq('appId', data?.app?.id),
        springFilterBuilder.eq('entornId', data?.entorn?.id),

        springFilterBuilder.like('nom', data?.nom),
        springFilterBuilder.like('descripcio', data?.descripcio),
        springFilterBuilder.eq('tipus', `'${data?.tipus}'`),

        springFilterBuilder.gte('dataInici', `'${formatStartOfDay(data?.dataInici)}'`),
        springFilterBuilder.lte('dataInici', `'${formatEndOfDay(data?.dataFi)}'`),
    )
}

const AvisFilterForm = (props:any) => {
    const { apiRef: filterRef } = props
    const {apiRef} = useFormContext()

    return <>
        <Grid size={12}>
            <MuiFilter
                apiRef={filterRef}
                resourceName={'entornApp'}
                code={'salut_entornApp_filter'}
                springFilterBuilder={(data:any) => {
                    apiRef?.current?.setFieldValue('app', data.app)
                    apiRef?.current?.setFieldValue('entorn', data.entorn)
                    return ''
                }}
            >
                <Grid container spacing={1}>
                    <Grid size={6}><FormField name={'app'}/></Grid>
                    <Grid size={6}><FormField name={'entorn'}/></Grid>
                </Grid>
            </MuiFilter>
        </Grid>

        <Grid size={{xs:6, sm:4}}><FormField name={'nom'}/></Grid>
        <Grid size={{xs:6, sm:4}}><FormField name={'descripcio'}/></Grid>
        <Grid size={{xs:6, sm:4}}><FormField name={'tipus'}/></Grid>
        <Grid size={{xs:6, sm:5}}><FormField name={'dataInici'}/></Grid>
        <Grid size={{xs:6, sm:5}}><FormField name={'dataFi'}/></Grid>
    </>
}

const TascaFilter = (props:any) => {
    const { onSpringFilterChange } = props;
    const { t } = useTranslation()
    const filterRef = useFilterApiRef();
    const apiRef = useFilterApiRef();

    const cercar = ()=> {
        filterRef?.current?.filter?.()
    }
    const netejar = ()=> {
        apiRef?.current?.clear?.()
        filterRef?.current?.clear?.()
    }

    return <MuiFilter
        apiRef={filterRef}
        resourceName={'avis'}
        code={'FILTER'}
        springFilterBuilder={avisFilterBuilder}
        onSpringFilterChange={onSpringFilterChange}
        buttonControlled
    >
        <Grid container spacing={1}>
            <AvisFilterForm apiRef={apiRef}/>

            <Grid size={{xs:4, sm:2}} display={'flex'} alignItems={'center'} justifyContent={'space-around'}>
                <Button onClick={netejar}>{t('components.clear')}</Button>
                <Button onClick={cercar} variant={'contained'}>{t('components.search')}</Button>
            </Grid>
        </Grid>
    </MuiFilter>
}

const columns = [
    // {
    //     field: 'nom',
    //     flex: 1,
    // },
    {
        field: 'descripcio',
        flex: 1,
    },
    {
        field: 'tipus',
        flex: 0.5,
        renderCell: (param:any) => {
            let style: any = {};

            switch(param?.row?.tipus){
                case "NOTICIA":
                    style = {backgroundColor: 'success.main', color: 'white'}
                    break;
                case "INFO":
                    style = {backgroundColor: 'info.main', color: 'white'}
                    break;
                case "ALERTA":
                    style = {backgroundColor: 'warning.main', color: 'white'}
                    break;
                case "ERROR":
                    style = {backgroundColor: 'error.main', color: 'white'}
                    break;
                case "CRITIC":
                    style = {backgroundColor: '#6b0707', color: 'white'}
                    break;
            }

            return <Typography variant={'inherit'} sx={{p: 1, borderRadius: '4px', ...style}}>
                {param?.formattedValue}
            </Typography>
        }
    },
    {
        field: 'dataInici',
        flex: 0.5,
    },
    {
        field: 'dataFi',
        flex: 0.5,
    },
]

const perspectives = ["PATH"]
const Avis = () => {
    const { t } = useTranslation()
    const [filter, setFilter] = useState<string>('')

    return <GridPage disableMargins>
        <Toolbar title={t('menu.avis')} upperToolbar/>

        <Box sx={{
            margin: '16px 24px',
            height: '100%',

            display: 'flex',
            flexDirection: 'column',
            // alignItems: 'center',
            gap: '8px',
        }}>
            <TascaFilter onSpringFilterChange={setFilter}/>

            <MuiGrid
                resourceName={'avis'}
                perspectives={perspectives}
                // sortModel={sortModel}
                filter={filter}
                columns={columns}

                treeData
                isGroupExpandedByDefault={()=>true}
                getTreeDataPath={(row) => row?.treePath}
                groupingColDef={{
                    headerName: 'nom',
                    flex: 1.5,
                    valueFormatter: (value: any, row: any) => {
                        if (row?.id) {
                            return <>{row?.nom}</>
                        }
                        return value;
                    },
                }}

                toolbarHide
                readOnly
            />
        </Box>
    </GridPage>
}
export default Avis;