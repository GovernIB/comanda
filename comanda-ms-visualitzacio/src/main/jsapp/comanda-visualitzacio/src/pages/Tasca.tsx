import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    GridPage,
    MuiGrid,
    MuiFilter,
    FormField,
    springFilterBuilder,
    useFormApiRef,
    useFilterApiRef,
} from 'reactlib';
import {
    Box,
    Grid,
    Typography,
    Icon,
    Button,
    IconButton,
    FormGroup,
    FormControlLabel,
    Switch
} from '@mui/material';
import { formatEndOfDay, formatStartOfDay } from '../util/dateUtils.ts';

const perspectives = ['PATH', 'EXPIRATION'];
const sortModel: any = [{field: 'dataInici', sort: 'asc'}];

export const StyledPrioritat = (props: any) => {
    const {entity, children} = props;
    let style: any = {};
    switch (entity?.prioritat) {
        case 'MAXIMA':
            style = { backgroundColor: '#d99b9d' }
            break;
        case 'ALTA':
            style = { backgroundColor: '#ffebae' }
            break;
        case 'NORMAL':
            style = { border: '1px dashed #AAA' }
            break;
        case 'BAIXA':
            style = { backgroundColor: '#c3e8d1' }
            break;
    }
    return <Typography
        variant="caption"
        sx={{
            padding: '1px 4px',
            fontSize: '11px',
            fontWeight: '500',
            borderRadius: '2px',
            display: 'flex',
            alignItems: 'center',
            width: 'max-content',
            ...style
        }}>{children}</Typography>;
}

const columns = [{
    field: 'descripcio',
    flex: 1,
}, {
    field: 'tipus',
    flex: 1,
}, {
    field: 'prioritat',
    flex: 0.5,
    renderCell: (param:any) => <StyledPrioritat entity={param.row}>{param?.formattedValue}</StyledPrioritat>,
}, {
    field: 'dataInici',
    flex: 0.5,
}, {
    field: 'dataFi',
    flex: 0.5,
}, {
    field: 'dataCaducitat',
    flex: 0.5,
    renderCell: (param:any) => {
        const style = param?.row?.diesPerCaducar == null ? {} :
        param?.row?.diesPerCaducar <= 0 ? { color: 'white', backgroundColor: '#6b0707' } :
        param?.row?.diesPerCaducar <= 3 ? { color: 'white', backgroundColor: 'error.main' } :
        param?.row?.diesPerCaducar <= 5 ? { color: 'white', backgroundColor: 'warning.main' } :
        {};
        return <Typography variant={'inherit'} sx={{p: 1, borderRadius: '4px', ...style}}>
            {param?.formattedValue}
        </Typography>
    },
}];

/*const GridButtonField = (props:any) => {
    const { name, icon, size, ...other } = props;
    const { data, apiRef, fields } = useFormContext();
    return <Grid title={fields?.find?.(item => item?.name === name)?.label || ''} size={size}>
        <Button
            onClick={()=>{
                apiRef?.current?.setFieldValue(name, !data?.[name])
            }}
            variant={ data?.[name] ? 'contained' : 'outlined' }
            sx={{ borderRadius: '4px', width: '100%', height: '100%' }}
            style={{ margin: 0 }}
            {...other}>
            <Icon sx={{ m: 0 }}>{icon}</Icon>
        </Button>
    </Grid>;
}*/

const TascaFilter = (props:any) => {
    const { onSpringFilterChange } = props;
    const { t } = useTranslation();
    const [finishedOnly, setFinishedOnly] = React.useState<boolean>(true);
    const [moreFields, setMoreFields] = React.useState<boolean>(false);
    const appEntornFilterApiRef = useFilterApiRef();
    const moreFilterApiRef = useFilterApiRef();
    const moreFormApiRef = useFormApiRef();
    const netejar = () => {
        appEntornFilterApiRef?.current?.clear?.();
        moreFilterApiRef?.current?.clear?.();
    }
    React.useEffect(() => {
        moreFormApiRef.current?.setFieldValue('finalitzada', finishedOnly);
    }, [finishedOnly]);
    return <>
        <MuiFilter
            apiRef={appEntornFilterApiRef}
            resourceName={'entornApp'}
            code={'salut_entornApp_filter'}
            commonFieldComponentProps={{ size: 'small' }}
            springFilterBuilder={data => {
                moreFormApiRef.current?.setFieldValue('appId', data.app);
                moreFormApiRef.current?.setFieldValue('entornId', data.entorn);
                return '';
            }}>
            <Box sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={6}><FormField name={'app'} /></Grid>
                    <Grid size={6}><FormField name={'entorn'} /></Grid>
                </Grid>
                <Button
                    onClick={() => setFinishedOnly(fo => !fo)}
                    variant={finishedOnly ? 'contained' : 'outlined'}
                    title="Només finalitzades"
                    sx={{ mr: 2 }}>
                    <Icon>done_all</Icon>
                </Button>
                <IconButton
                    onClick={netejar}
                    title={t('components.clear')}
                    sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
                <IconButton
                    onClick={() => setMoreFields((mf) => !mf)}
                    title="Més camps">
                    <Icon>filter_list</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
        <MuiFilter
            apiRef={moreFilterApiRef}
            formApiRef={moreFormApiRef}
            resourceName={'tasca'}
            code={'FILTER'}
            initialData={{ finalitzada: finishedOnly }}
            springFilterBuilder={data => springFilterBuilder.and(
                springFilterBuilder.eq('appId', data?.appId?.id),
                springFilterBuilder.eq('entornId', data?.entornId?.id),
                springFilterBuilder.like('nom', data?.nom),
                springFilterBuilder.like('descripcio', data?.descripcio),
                springFilterBuilder.eq('prioritat', `'${data?.prioritat}'`),
                data?.finalitzada && springFilterBuilder.eq('dataFi', null),
                springFilterBuilder.gte('dataInici', `'${formatStartOfDay(data?.dataInici)}'`),
                springFilterBuilder.lte('dataInici', `'${formatEndOfDay(data?.dataFi)}'`),
            )}
            onSpringFilterChange={onSpringFilterChange}
            commonFieldComponentProps={{ size: 'small' }}>
            <Grid container spacing={1} sx={{ display: moreFields ? undefined : 'none', mt: 1 }}>
                <Grid size={3}><FormField name={'nom'}/></Grid>
                <Grid size={3}><FormField name={'descripcio'}/></Grid>
                <Grid size={3}><FormField name={'tipus'}/></Grid>
                <Grid size={3}><FormField name={'prioritat'}/></Grid>
                <Grid size={4}><FormField name={'dataInici'}/></Grid>
                <Grid size={4}><FormField name={'dataFi'}/></Grid>
                <Grid size={4}><FormField name={'dataCaducitat'}/></Grid>
            </Grid>
        </MuiFilter>
    </>;
}

const Tasca = () => {
    const { t } = useTranslation();
    const [treeView, setTreeView] = React.useState<boolean>(true);
    const [filter, setFilter] = React.useState<string>();
    const actions = [{
        title: '',
        icon: 'open_in_new',
        //title: 'Obrir tasca',
        showInMenu: false,
        onClick: (_id: any, row: any) => window.location.href = row?.url,
        disabled: (row: any) => !row?.url,
        hidden: (row: any) => !row?.id,
    }];
    const treeDataProps = treeView ? {
        treeData: true as true,
        autoHeight: true as true,
        isGroupExpandedByDefault: () => true,
        getTreeDataPath: (row: any) => row?.treePath,
        groupingColDef: {
            headerName: 'Nom',
            flex: 1.5,
            valueFormatter: (value: any, row: any) => {
                if (row?.id) {
                    return <>{row?.nom}</>
                }
                return value;
            },
        }
    } : {
        paginationActive: true as true,
    };
    const filterElement =<TascaFilter onSpringFilterChange={setFilter}/>;
    const treeViewSwitch = <FormGroup sx={{ ml: 2 }}>
        <FormControlLabel
            label="Vista en arbre"
            control={
                <Switch
                    checked={treeView}
                    onChange={event => setTreeView(event.target.checked)}/>
            }/>
    </FormGroup>;
    return <GridPage>
        <MuiGrid
            title={t('menu.tasca')}
            resourceName={'tasca'}
            perspectives={perspectives}
            sortModel={sortModel}
            findDisabled={filter == null}
            filter={filter}
            columns={columns}
            readOnly
            toolbarType="upper"
            toolbarHideQuickFilter
            toolbarElementsWithPositions={[{ position: 1, element: treeViewSwitch }]}
            toolbarAdditionalRow={filterElement}
            rowAdditionalActions={actions}
            {...treeDataProps}
        />
    </GridPage>;
}

export default Tasca;