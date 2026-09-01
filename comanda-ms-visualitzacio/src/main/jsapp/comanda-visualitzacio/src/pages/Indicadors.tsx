import * as React from 'react';
import { useState, useEffect } from "react";
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import Button from '@mui/material/Button';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import Autocomplete from '@mui/material/Autocomplete';
import {
    MuiDataGrid,
    MuiDataGridColDef,
    springFilterBuilder,
    MuiFilter,
    FormField,
    useFilterApiRef,
    useResourceApiService,
    useFormContext,
    useMuiDataGridApiRef,
    useMuiFormDialogApiRef,
    useFormDialogButtons,
    useBaseAppContext,
    MuiFormDialog,
} from 'reactlib';
import { FormFieldDataActionType } from '../../lib/components/form/FormContext.tsx';
import { columnesIndicador } from '../components/sharedAdvancedSearch/advancedSearchColumns';
import FormFieldCustomAdvancedSearch from '../components/FormFieldCustomAdvancedSearch';
import FormActionDialog from '../components/FormActionDialog.tsx';
import PageTitle from '../components/PageTitle.tsx';
import useReadOnlyGestor from '../hooks/useReadOnlyGestor.ts';
import { findOptions } from '../util/requestUtils.ts';
import type { MuiFormDialogApi } from 'reactlib';

const IndicadorsFilter = (props: { onSpringFilterChange: (springFilter?: string) => void }) => {
    const { onSpringFilterChange } = props;
    const { t } = useTranslation();
    const { isReady: entornAppApiIsReady, find: entornAppGetAll } = useResourceApiService('entornApp');
    const filterApiRef = useFilterApiRef();
    const [entornApp, setEntornApp] = useState<Array<{ id?: number | string; entornAppDescription?: string }> | null>([]);

    // Al obrir la pàgina carreguem el llistat de EntornApp actius
    useEffect(() => {
        if (entornAppApiIsReady) {
            console.log('EntornApp API ready');
            entornAppGetAll({
                unpaged: true,
                filter: 'activa : true AND app.activa : true',
            }).then(response => {
                console.log('EntornApp API response received:', response.rows.length, 'items');
                setEntornApp(response.rows);
            });
        }
    }, [entornAppApiIsReady, entornAppGetAll]);

    const netejar = () => {
        filterApiRef?.current?.clear();
    }

    return (
        <MuiFilter
            apiRef={filterApiRef}
            resourceName="indicador"
            code="indicadorFilter"
            commonFieldComponentProps={{ size: 'small' }}
            onSpringFilterChange={onSpringFilterChange}
            springFilterBuilder={data => {
                // Build Spring filter based on available fields in the artifact
                // Fallback to empty if no values provided
                return springFilterBuilder.and(
                    data?.entornApp && springFilterBuilder.eq('entornAppId', data?.entornApp?.id ?? data?.entornApp),
                    data?.codi && springFilterBuilder.like('codi', data?.codi),
                    data?.nom && springFilterBuilder.like('nom', data?.nom),
                ) || '';
            }}>
            <Box sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={4}>
                        <FormField
                            name={'entornApp'}
                            type={'reference'}
                            label={t($ => $.page.indicadors.column.entornApp)}
                            required={false}
                            optionsRequest={(q: string) => {
                                const opts = (entornApp ?? []).map((ea: { id?: number | string; entornAppDescription?: string }) => ({
                                    id: ea?.id,
                                    description: ea.entornAppDescription,
                                }));
                                const filtered = q
                                    ? opts.filter(o => o.description?.toLowerCase().includes(q.toLowerCase()))
                                    : opts;
                                return Promise.resolve({ options: filtered });
                            }}
                            componentProps={{ disabled: (entornApp ?? []).length === 0 }}
                        />
                    </Grid>
                    <Grid size={4}><FormField name={'codi'} /></Grid>
                    <Grid size={4}><FormField name={'nom'} /></Grid>
                </Grid>
                <IconButton
                    onClick={netejar}
                    title={t($ => $.components.clear)}
                    sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
    );
};

type FormulaTerme = { key: string; indicadorId: any; operador: 'SUMA' | 'RESTA' };

let formulaTermeKeyCounter = 0;
const nextFormulaTermeKey = () => `terme-${formulaTermeKeyCounter++}`;
export const buildDefaultFormulaTermes = (): FormulaTerme[] => [
    { key: nextFormulaTermeKey(), indicadorId: null, operador: 'SUMA' },
];

/** Converteix les files carregades del recurs `indicadorFormulaTerme` a l'estat intern de l'editor, ordenades per `ordre`. */
export const mapTermeRowsToFormulaTermes = (rows: any[]): FormulaTerme[] =>
    (rows ?? [])
        .slice()
        .sort((a, b) => (a?.ordre ?? 0) - (b?.ordre ?? 0))
        .map(row => ({
            key: nextFormulaTermeKey(),
            indicadorId: row?.indicadorComponent?.id ?? row?.indicadorComponent,
            operador: row?.operador,
        }));

/** Payloads per crear els termes d'una fórmula: descarta files sense indicador triat i reassigna `ordre`. */
export const computeFormulaTermePayloads = (termes: FormulaTerme[], indicadorId: any) =>
    termes
        .filter(terme => terme.indicadorId != null)
        .map((terme, index) => ({
            indicadorFormula: { id: indicadorId },
            indicadorComponent: { id: terme.indicadorId },
            operador: terme.operador,
            ordre: index,
        }));

/**
 * Selector de l'entornApp de l'indicador de fórmula (camp `entornAppId`, un `Long` senzill al backend, no
 * una `ResourceReference`). Es implementat a mà (en lloc d'un `<FormField type="reference">`, que espera un
 * camp de tipus referència) seguint el mateix patró que `FiltreDimensioCodiField` a DashboardEditorSidePanel.
 */
const IndicadorFormulaEntornAppField: React.FC<{
    label: string;
    disabled?: boolean;
    entornAppOptions: Array<{ id?: number | string; entornAppDescription?: string }>;
}> = ({ label, disabled, entornAppOptions }) => {
    const { dataGetFieldValue, dataDispatchAction, fields, fieldErrors } = useFormContext();
    const value = dataGetFieldValue('entornAppId');
    const field = fields?.find((f: any) => f.name === 'entornAppId');
    const fieldError = fieldErrors?.find((e: any) => e.field === 'entornAppId');
    const options = (entornAppOptions ?? []).map(ea => ({ id: ea.id, description: ea.entornAppDescription ?? '' }));
    const selected = options.find(option => option.id === value) ?? null;

    return (
        <Autocomplete
            size="small"
            disabled={disabled}
            options={options}
            value={selected}
            getOptionLabel={(option) => option.description}
            isOptionEqualToValue={(option, val) => option.id === val.id}
            onChange={(_event, newValue) => {
                dataDispatchAction({
                    type: FormFieldDataActionType.FIELD_CHANGE,
                    payload: { fieldName: 'entornAppId', field, value: newValue?.id ?? null },
                });
            }}
            renderInput={(params) => (
                <TextField {...params} label={label} required error={fieldError != null} helperText={fieldError?.message} />
            )}
        />
    );
};

/**
 * Editor dels termes (indicador component + operador +/-) d'un indicador de fórmula. `IndicadorFormulaTerme`
 * no és un camp del recurs `indicador`, sinó una col·lecció d'un recurs fill independent que es desa per
 * separat (vegeu saveFormulaTermes), per això l'estat es gestiona amb un `useState` propi (no via `FormField`).
 *
 * L'estat NO es guarda al component `Indicadors` (el pare): el contingut del diàleg de `MuiFormDialog` es
 * "congela" quan es crida `show()` i no es torna a renderitzar encara que el pare canviï d'estat i li passi
 * uns `children` nous — només es refresca quan es torna a cridar `show()`. Per això `termesRef` és una
 * referència mutable i estable (mateix objecte a cada render) que fa de canal entre aquest component (que
 * gestiona el seu propi `useState` per refrescar-se ell mateix en afegir/eliminar termes) i `Indicadors`
 * (que llegeix `termesRef.current` a `saveFormulaTermes` i l'escriu abans de cridar `show()`).
 */
const FormulaTermesEditor: React.FC<{
    termesRef: React.MutableRefObject<FormulaTerme[]>;
}> = ({ termesRef }) => {
    const { t } = useTranslation();
    const { data } = useFormContext();
    const { isReady, find } = useResourceApiService('indicador');
    const [termes, setTermesState] = React.useState<FormulaTerme[]>(() => termesRef.current);
    const [options, setOptions] = React.useState<{ id: any; codi: string; nom: string }[]>([]);
    const entornAppId = data?.entornAppId;

    useEffect(() => {
        let cancelled = false;
        if (isReady && entornAppId != null) {
            find({
                filter: springFilterBuilder.and(
                    springFilterBuilder.eq('entornAppId', entornAppId),
                    springFilterBuilder.eq('tipus', "'SIMPLE'"),
                ),
                unpaged: true,
            }).then((response: any) => {
                if (cancelled) return;
                const rows = (response.rows ?? []) as Array<{ id?: any; codi?: string; nom?: string }>;
                setOptions(rows.map(row => ({ id: row.id, codi: row.codi ?? '', nom: row.nom ?? row.codi ?? '' })));
            });
        } else {
            setOptions([]);
        }
        return () => {
            cancelled = true;
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isReady, entornAppId]);

    const updateTermes = (next: FormulaTerme[]) => {
        termesRef.current = next;
        setTermesState(next);
    };
    const addTerme = () => updateTermes([...termes, { key: nextFormulaTermeKey(), indicadorId: null, operador: 'SUMA' }]);
    const removeTerme = (key: string) => updateTermes(termes.filter(terme => terme.key !== key));
    const updateTerme = (key: string, changes: Partial<FormulaTerme>) =>
        updateTermes(termes.map(terme => (terme.key === key ? { ...terme, ...changes } : terme)));

    return (
        <Box>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
                {t($ => $.page.indicadors.formulaForm.termesTitle)}
            </Typography>
            {termes.map((terme, index) => {
                const selected = options.find(option => option.id === terme.indicadorId) ?? null;
                const usedElsewhere = termes.filter(t => t.key !== terme.key).map(t => t.indicadorId);
                const visibleOptions = options.filter(option => !usedElsewhere.includes(option.id));
                return (
                    <Box key={terme.key} sx={{ display: 'flex', gap: 1, alignItems: 'center', mb: 1 }}>
                        {index === 0 ? (
                            <Typography sx={{ width: 40, textAlign: 'center' }}>+</Typography>
                        ) : (
                            <Select
                                size="small"
                                value={terme.operador}
                                onChange={(event) => updateTerme(terme.key, { operador: event.target.value as 'SUMA' | 'RESTA' })}
                                sx={{ width: 70 }}
                                inputProps={{ 'aria-label': t($ => $.page.indicadors.formulaForm.termeIndicador) }}
                            >
                                <MenuItem value="SUMA">+</MenuItem>
                                <MenuItem value="RESTA">-</MenuItem>
                            </Select>
                        )}
                        <Autocomplete
                            size="small"
                            sx={{ flexGrow: 1 }}
                            options={visibleOptions}
                            value={selected}
                            getOptionLabel={(option) => `${option.codi} - ${option.nom}`}
                            isOptionEqualToValue={(option, val) => option.id === val.id}
                            onChange={(_event, newValue) => updateTerme(terme.key, { indicadorId: newValue?.id ?? null })}
                            renderInput={(params) => (
                                <TextField {...params} label={t($ => $.page.indicadors.formulaForm.termeIndicador)} required />
                            )}
                        />
                        <IconButton
                            onClick={() => removeTerme(terme.key)}
                            disabled={termes.length <= 1}
                            title={t($ => $.page.indicadors.formulaForm.removeTerme)}>
                            <Icon>delete</Icon>
                        </IconButton>
                    </Box>
                );
            })}
            <Button startIcon={<Icon>add</Icon>} onClick={addTerme} disabled={entornAppId == null}>
                {t($ => $.page.indicadors.formulaForm.addTerme)}
            </Button>
        </Box>
    );
};

/**
 * Referència mutable amb l'App i l'entorn de l'indicador d'origen que s'està copiant, emprada per filtrar les
 * opcions del selector d'entorn destí a IndicadorCopiarEntornForm (mateix patró que termesRef, vegeu el
 * comentari de FormulaTermesEditor: el diàleg no es tornar a renderitzar amb noves props en cridar `show()`,
 * per això cal aquest canal en lloc de passar-ho com a prop).
 */
const copiarEntornOrigenRef: React.MutableRefObject<{ appId: any; entornId: any }> = { current: { appId: null, entornId: null } };

/** Filtre del selector d'entorn destí: entorns amb la mateixa App que l'indicador d'origen, excloent-ne el propi entorn. */
export const buildCopiarEntornDestiFilter = (appId: any, entornId: any): string =>
    springFilterBuilder.and(
        springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornAppEntities.app.id', appId))),
        springFilterBuilder.neq('id', entornId)
    );

/**
 * Formulari de l'acció "Copiar a un altre entorn": selector de l'entorn destí, restringit als entorns on ja hi
 * ha configurada la mateixa App que la de l'indicador d'origen, excloent l'entorn al qual ja pertany l'indicador.
 */
const IndicadorCopiarEntornForm: React.FC = () => {
    const { t } = useTranslation();
    const { isReady: entornIsReady, find: entornFind } = useResourceApiService('entorn');
    const { appId, entornId } = copiarEntornOrigenRef.current;
    const filterEntorn = buildCopiarEntornDestiFilter(appId, entornId);

    if (!entornIsReady) return null;
    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField
                    name="entornDesti"
                    label={t($ => $.page.indicadors.copiarEntorn.entornDesti)}
                    optionsRequest={(quickFilter: string) => findOptions(entornFind, 'nom', quickFilter, filterEntorn)}
                />
            </Grid>
        </Grid>
    );
};

/** Acció per copiar un indicador de tipus FORMULA a un altre entorn de la mateixa App (vegeu IndicadorServiceImpl.CopiarIndicadorEntornAction). */
const useCopiarIndicadorEntornAction = (refresh?: () => void) => {
    const { t } = useTranslation();
    const apiRef = React.useRef<MuiFormDialogApi>(null);
    const { temporalMessageShow } = useBaseAppContext();
    const handleShow = (id: any, row: any, appId: any, entornId: any): void => {
        copiarEntornOrigenRef.current = { appId, entornId };
        apiRef.current?.show?.(id, row);
    };
    const onSuccess = (): void => {
        refresh?.();
        temporalMessageShow(null, t($ => $.page.indicadors.copiarEntorn.success), 'success');
    };
    const formulario =
        <FormActionDialog
            resourceName={"indicador"}
            action={"copiar_indicador_entorn"}
            apiRef={apiRef}
            title={t($ => $.page.indicadors.copiarEntorn.title)}
            onSuccess={onSuccess}
            initialOnChange={false}
        >
            <IndicadorCopiarEntornForm />
        </FormActionDialog>;
    return {
        handleShow,
        content: formulario,
    };
};

const Indicadors: React.FC = () => {
    const { t } = useTranslation();
    const gestorReadOnly = useReadOnlyGestor();
    const { temporalMessageShow } = useBaseAppContext();
    const [filter, setFilter] = React.useState<string | undefined>(springFilterBuilder.eq('entornAppId', 0));

    const gridApiRef = useMuiDataGridApiRef();
    const formulaDialogApiRef = useMuiFormDialogApiRef();
    const formulaDialogButtons = useFormDialogButtons();
    const { isReady: entornAppApiIsReady, find: entornAppGetAll, getOne: entornAppGetOne } = useResourceApiService('entornApp');
    const [entornAppOptions, setEntornAppOptions] = useState<Array<{ id?: number | string; entornAppDescription?: string }>>([]);
    const termesRef = React.useRef<FormulaTerme[]>(buildDefaultFormulaTermes());
    const {
        isReady: termeApiIsReady,
        find: findTerme,
        create: createTerme,
        delete: deleteTerme,
    } = useResourceApiService('indicadorFormulaTerme');
    const { delete: deleteIndicador } = useResourceApiService('indicador');
    const { handleShow: showCopiarEntorn, content: contentCopiarEntorn } =
        useCopiarIndicadorEntornAction(() => gridApiRef.current?.refresh?.());

    useEffect(() => {
        if (entornAppApiIsReady) {
            entornAppGetAll({
                unpaged: true,
                filter: 'activa : true AND app.activa : true',
            }).then(response => {
                setEntornAppOptions(response.rows);
            });
        }
    }, [entornAppApiIsReady, entornAppGetAll]);

    const columns: MuiDataGridColDef[] = [
        { field: 'codi', flex: 1 },
        { field: 'nom', flex: 2 },
        { field: 'descripcio', flex: 3 },
        { field: 'format', flex: 1 },
        { field: 'tipus', headerName: t($ => $.page.indicadors.column.tipus), flex: 0.8 },
        { field: 'compactable', flex: 0.6 },
        { field: 'tipusCompactacio', flex: 1.2 },
        { field: 'indicadorComptadorPerMitjana.description', headerName: t($ => $.page.indicadors.column.indicadorMitjana), flex: 2 },
    ];

    const openCreateFormula = () => {
        termesRef.current = buildDefaultFormulaTermes();
        // compactable és NOT NULL a BD (per defecte true als indicadors SIMPLE); els de fórmula no es
        // compacten (el seu valor es calcula a partir dels termes, no de fets JSON), per això es força a false.
        formulaDialogApiRef.current?.show(
            undefined,
            { tipus: 'FORMULA', compactable: false },
            t($ => $.page.indicadors.formulaForm.createTitle)
        );
    };

    const openEditFormula = async (id: any) => {
        if (!termeApiIsReady) return;
        const response = await findTerme({
            filter: springFilterBuilder.eq('indicadorFormula.id', id),
            unpaged: true,
        });
        const loadedTermes = mapTermeRowsToFormulaTermes(response.rows ?? []);
        termesRef.current = loadedTermes.length > 0 ? loadedTermes : buildDefaultFormulaTermes();
        formulaDialogApiRef.current?.show(id, undefined, t($ => $.page.indicadors.formulaForm.editTitle));
    };

    // Cal l'App i l'entorn de l'entornApp d'origen per restringir el selector d'entorn destí a
    // IndicadorCopiarEntornForm (vegeu copiarEntornOrigenRef) als entorns amb la mateixa App, excloent el
    // propi entorn de l'indicador.
    const openCopiarEntorn = async (id: any, row: any) => {
        if (row?.entornAppId == null) return;
        const entornApp = await entornAppGetOne(row.entornAppId);
        showCopiarEntorn(id, row, entornApp?.app?.id, entornApp?.entorn?.id);
    };

    const saveFormulaTermes = async (indicadorId: any) => {
        const existing = await findTerme({
            filter: springFilterBuilder.eq('indicadorFormula.id', indicadorId),
            unpaged: true,
        });
        for (const existingTerme of existing.rows ?? []) {
            await deleteTerme(existingTerme.id);
        }
        for (const payload of computeFormulaTermePayloads(termesRef.current, indicadorId)) {
            await createTerme({ data: payload });
        }
    };

    // L'indicador i els seus termes es desen amb dues crides HTTP independents (l'indicador és un recurs
    // "pare" normal i els termes són un recurs fill propi, vegeu el comentari de FormulaTermesEditor), per
    // tant no hi ha una transacció única que ho cobreixi tot. En creació, si el desat dels termes falla just
    // després de crear l'indicador, s'esborra l'indicador acabat de crear per no deixar-lo "orfe" sense
    // termes (en edició no es desfà res, ja que hi havia dades prèvies vàlides que no s'han de perdre).
    const handleFormulaCreated = async (savedIndicador: any) => {
        const indicadorId = savedIndicador?.id;
        try {
            await saveFormulaTermes(indicadorId);
            temporalMessageShow(null, t($ => $.page.indicadors.formulaForm.success), 'success');
            gridApiRef.current?.refresh?.();
        } catch (error: any) {
            try {
                await deleteIndicador(indicadorId);
            } catch {
                // Si tampoc es pot desfer la creació, es prioritza mostrar l'error original dels termes.
            }
            temporalMessageShow(null, error?.message ?? t($ => $.common.error), 'error');
        }
    };

    const handleFormulaUpdated = async (savedIndicador: any) => {
        const indicadorId = savedIndicador?.id;
        try {
            await saveFormulaTermes(indicadorId);
            temporalMessageShow(null, t($ => $.page.indicadors.formulaForm.success), 'success');
            gridApiRef.current?.refresh?.();
        } catch (error: any) {
            temporalMessageShow(null, error?.message ?? t($ => $.common.error), 'error');
        }
    };

    const filterElement = <IndicadorsFilter onSpringFilterChange={setFilter}/>;

    const IndicadorForm: React.FC = () => {
        const { data } = useFormContext();
        return (
        <Grid container spacing={2}>
            <Grid size={6}><FormField name="codi" readOnly disabled /></Grid>
            <Grid size={6}><FormField name="nom" readOnly disabled /></Grid>
            <Grid size={12}><FormField name="descripcio" readOnly disabled /></Grid>
            <Grid size={6}><FormField name="format" readOnly disabled /></Grid>
            <Grid size={12}><FormField name="compactable" /></Grid>
            {data?.compactable === true && (
                <>
                    <Grid size={6}><FormField name="tipusCompactacio" /></Grid>
                    {data?.tipusCompactacio === "MITJANA" && (
                        <Grid size={12}>
                            <FormFieldCustomAdvancedSearch
                                name="indicadorComptadorPerMitjana"
                                // namedQueries={[`filterByAppGroupByNom:${data?.aplicacio?.id}`]} TODO S'ha de filtrar per tots els indicadors de la mateixa app?
                                advancedSearchColumns={columnesIndicador}
                                advancedSearchDataGridProps={{ rowHeight: 30 }}
                                advancedSearchDialogHeight={500}
                            />
                        </Grid>
                    )}
                </>
            )}
        </Grid>
        );
    };

    const IndicadorFormulaForm: React.FC = () => {
        const { data } = useFormContext();
        const isEdit = data?.id != null;
        return (
            <Grid container spacing={2}>
                <Grid size={6}><FormField name="codi" readOnly={isEdit} disabled={isEdit} required /></Grid>
                <Grid size={6}><FormField name="nom" required /></Grid>
                <Grid size={12}><FormField name="descripcio" required={false} /></Grid>
                <Grid size={6}><FormField name="format" required={false} /></Grid>
                <Grid size={6}>
                    <IndicadorFormulaEntornAppField
                        label={t($ => $.page.indicadors.column.entornApp)}
                        disabled={isEdit}
                        entornAppOptions={entornAppOptions}
                    />
                </Grid>
                <Grid size={12}>
                    <FormulaTermesEditor termesRef={termesRef} />
                </Grid>
            </Grid>
        );
    };

    return (
        <>
            <PageTitle title={t($ => $.page.indicadors.title)} />
            <MuiDataGrid
                apiRef={gridApiRef}
                title={t($ => $.page.indicadors.title)}
                resourceName="indicador"
                columns={columns}
                toolbarType="upper"
                paginationActive
                toolbarHideQuickFilter
                toolbarAdditionalRow={filterElement}
                filter={filter}
                popupEditActive
                toolbarHideCreate
                rowHideDeleteButton={gestorReadOnly}
                popupEditFormContent={<IndicadorForm />}
                toolbarElementsWithPositions={[
                    {
                        position: 2,
                        element: (
                            <IconButton
                                title={t($ => $.page.indicadors.action.createFormula)}
                                onClick={openCreateFormula}>
                                <Icon>add</Icon>
                            </IconButton>
                        ),
                    },
                ]}
                rowAdditionalActions={[
                    {
                        label: t($ => $.page.indicadors.action.editFormula),
                        icon: 'functions',
                        showInMenu: true,
                        onClick: (id: any) => openEditFormula(id),
                        hidden: (row: any) => row?.tipus !== 'FORMULA',
                    },
                    {
                        label: t($ => $.page.indicadors.action.copiarEntorn),
                        icon: 'content_copy',
                        showInMenu: true,
                        onClick: (id: any, row: any) => openCopiarEntorn(id, row),
                        hidden: (row: any) => row?.tipus !== 'FORMULA',
                    },
                ]}
            />
            <MuiFormDialog
                resourceName="indicador"
                apiRef={formulaDialogApiRef}
                dialogButtons={formulaDialogButtons}
                dialogComponentProps={{ fullWidth: true, maxWidth: 'md' }}
                formComponentProps={{
                    onCreateSuccess: handleFormulaCreated,
                    onUpdateSuccess: handleFormulaUpdated,
                }}
            >
                <IndicadorFormulaForm />
            </MuiFormDialog>
            {contentCopiarEntorn}
        </>
    );
};

export default Indicadors;
