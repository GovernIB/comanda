import {GridPage, MuiFilter, MuiGrid, springFilterBuilder, Toolbar, useFilterApiRef, useFormContext, FormField} from "reactlib";
import {Box, Grid, Typography, Icon, Button} from "@mui/material";
import {useState} from "react";
import {useTranslation} from "react-i18next";

const labelStyle = {padding: '1px 4px', fontSize: '11px', fontWeight: '500', borderRadius: '2px', display: 'flex', alignItems: 'center', width: 'max-content'}
const obertStyle = {border: '1px dashed #AAA'}

export const StyledPrioritat = (props: any) => {
    const {entity, children} = props;

    let style: any = {};

    switch (entity?.prioritat) {
        case "MAXIMA":
            style = {backgroundColor: '#d99b9d'}
            break;
        case "ALTA":
            style = {backgroundColor: '#ffebae'}
            break;
        case "NORMAL":
            style = obertStyle
            break;
        case "BAIXA":
            style = {backgroundColor: '#c3e8d1'}
            break;
    }

    return <Typography variant="caption" sx={{...labelStyle, ...style}}>{children}</Typography>
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
        flex: 1,
    },
    {
        field: 'prioritat',
        flex: 0.5,
        renderCell: (param:any) => <StyledPrioritat entity={param.row}>{param?.formattedValue}</StyledPrioritat>,
    },
    {
        field: 'dataInici',
        flex: 0.5,
    },
    {
        field: 'dataFi',
        flex: 0.5,
    },
    {
        field: 'dataCaducitat',
        flex: 0.5,
        renderCell: (param:any) => {
            const style = param?.row?.diesPerCaducar == null ?{}
                :param?.row?.diesPerCaducar <= 0 ? { color: 'white', backgroundColor: '#6b0707' }
                :param?.row?.diesPerCaducar <= 3 ? { color: 'white', backgroundColor: 'error.main' }
                :param?.row?.diesPerCaducar <= 5 ? { color: 'white', backgroundColor: 'warning.main' }
                :{}

            return <Typography variant={'inherit'} sx={{p: 1, borderRadius: '4px', ...style}}>
                {param?.formattedValue}
            </Typography>
        },
    },
]

const tascaFilterBuilder = (data:any) :string => {
    return springFilterBuilder.and(
        springFilterBuilder.eq('appId', data?.app?.id),
        springFilterBuilder.eq('entornId', data?.entorn?.id),

        springFilterBuilder.like('nom', data?.nom),
        springFilterBuilder.like('descripcio', data?.descripcio),
        springFilterBuilder.eq('prioritat', `'${data?.prioritat}'`),
        data?.acabat && springFilterBuilder.eq('dataFi', null),
    )
}

const GridButtonField = (props:any) => {
    const {name, icon, size, ...other} = props;
    const {data, apiRef, fields} = useFormContext()

    return <Grid title={fields?.find?.(item => item?.name === name)?.label || ''} size={size}>
        <Button
            onClick={()=>{
                apiRef?.current?.setFieldValue(name, !data?.[name])
            }}
            variant={ data?.[name] ?"contained":"outlined" }
            sx={{ borderRadius: '4px', width: '100%', height: '100%'}}
            style={{margin: 0}}
            {...other}
        >
            <Icon sx={{m: 0}}>{icon}</Icon>
        </Button>
    </Grid>
}

const TascaFilterForm = (props:any) => {
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
                <Grid container sx={{ minWidth: 250 }} spacing={1}>
                    <Grid size={6}><FormField name={'app'}/></Grid>
                    <Grid size={6}><FormField name={'entorn'}/></Grid>
                </Grid>
            </MuiFilter>
        </Grid>

        <Grid size={{xs:6, sm:3}}><FormField name={'nom'}/></Grid>
        <Grid size={{xs:6, sm:3}}><FormField name={'descripcio'}/></Grid>
        <Grid size={{xs:6, sm:3}}><FormField name={'prioritat'}/></Grid>

        <GridButtonField size={{xs:2, sm:1}} name={'acabat'} icon={"update"}/>
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
        resourceName={'tasca'}
        code={'FILTER'}
        springFilterBuilder={tascaFilterBuilder}
        onSpringFilterChange={onSpringFilterChange}
        buttonControlled
    >
        <Grid container spacing={1}>
            <TascaFilterForm apiRef={apiRef}/>

            <Grid size={{xs:4, sm:2}} display={'flex'} alignItems={'center'} justifyContent={'space-around'}>
                <Button onClick={netejar}>{t('components.clear')}</Button>
                <Button onClick={cercar} variant={'contained'}>{t('components.search')}</Button>
            </Grid>
        </Grid>
    </MuiFilter>
}

const perspectives = ["PATH", "EXPIRATION"]
const sortModel:any = [{field: 'dataCaducitat', sort: 'asc'}]
const Tasca = () => {
    const [filter, setFilter] = useState<string>('')

    return <GridPage disableMargins>
        <Toolbar title={"Tasca"} upperToolbar/>

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
                resourceName={'tasca'}
                // namedQueries={['USER']}
                perspectives={perspectives}
                sortModel={sortModel}
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
                // onRowClick={(params:any) => {if (params?.row?.url) window.location.href = params?.row?.url} }

                readOnly
            />
        </Box>
    </GridPage>
}
export default Tasca;