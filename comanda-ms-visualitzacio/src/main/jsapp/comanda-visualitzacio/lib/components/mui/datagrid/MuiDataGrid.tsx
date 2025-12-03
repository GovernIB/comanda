import React from 'react';
import {
    DataGridProProps as DataGridProps,
    GridRowsProp,
    GridRowParams,
    GridRowClassNameParams,
    GridColDef,
    GridFilterModel,
    GridSortModel,
    GridSortDirection,
    GridPaginationModel,
    GridRowSelectionModel,
    GridSlots,
    GridRowModes,
    GridRowModesModel,
    GridApiPro,
    GridEventListener,
    GridCallbackDetails,
    GridInitialState,
    MuiEvent,
    useGridApiRef as useMuiDatagridApiRef,
} from '@mui/x-data-grid-pro';
import Box from '@mui/material/Box';
import { capitalize } from '../../../util/text';
import useLogConsole from '../../../util/useLogConsole';
import { formattedFieldValue, isFieldNumericType } from '../../../util/fields';
import { useSessionComponentPersistentState } from '../../../util/useComponentPersistentState';
import {
    ReactElementWithPosition,
    joinReactElementsWithPositionWithReactElementsWithPositions,
} from '../../../util/reactNodePosition';
import { FormI18nKeys } from '../../form/Form';
import { DialogButton } from '../../BaseAppContext';
import { useMuiBaseAppContext } from '../MuiBaseAppContext';
import { useResourceApiService } from '../../ResourceApiProvider';
import { useResourceApiContext, ResourceType, ExportFileType } from '../../ResourceApiContext';
import { toDataGridActionItem, DataGridActionItemOnClickFn } from './DataGridActionItem';
import {
    useApiDataCommon,
    useDataCommonEditable,
    DataCommonAdditionalAction,
    DataCommonShowCreateDialogFn,
    DataCommonShowUpdateDialogFn,
    DataCommonTriggerDeleteFn,
} from '../datacommon/MuiDataCommon';
import { useDataToolbar, DataToolbarType } from '../datacommon/DataToolbar';
import DataGridRow from './DataGridRow';
import DataGridFooter from './DataGridFooter';
import DataGridNoRowsOverlay from './DataGridNoRowsOverlay';
import DataGridCustomStyle from './DataGridCustomStyle';
import DataGridContext, {
    MuiDataGridApi,
    MuiDataGridApiRef,
    useDataGridContext,
    DEFAULT_ROW_SELECTION,
} from './DataGridContext';

export const LOG_PREFIX = 'GRID';

/**
 * Propietats de les columnes del component MuiDataGrid (també conté totes les propietats de les columnes del DataGrid de MUI).
 */
export type MuiDataGridColDef = GridColDef & {
    /** Nom del camp */
    field: string;
    /** Tipus del camp (sobreescriu el tipus del camp retornat pel backend) */
    fieldType?: string;
    /** Indica si el camp és de tipus divisa */
    currencyType?: boolean;
    /** Codi ISO per a mostrar el símbol de la moneda */
    currencyCode?: string | ((row: any) => string);
    /** Indica el nombre de llocs decimals que s'han de mostrar (només per a tipus divisa) */
    currencyDecimalPlaces?: number | ((row: any) => number);
    /** Indica el codi ISO del locale per a mostrar la divisa */
    currencyLocale?: string | ((row: any) => string);
    /** Indica el nombre de llocs decimals que s'han de mostrar (només per a tipus numèrics) */
    decimalPlaces?: number | ((row: any) => number);
    /** Indica que no s'ha de mostrar l'hora (només per a tipus data) */
    noTime?: boolean;
    /** Indica que no s'ha de mostrar els segons (només per a tipus data) */
    noSeconds?: boolean;
    /** Indica que aquesta columna no s'ha d'incloure a l'exportació */
    exportExcluded?: boolean;
    /** Processa i canvia l'ordenació dels camps (si es retorna undefined vol dir que l'ordenació no canvia) */
    sortProcessor?: (field: string, sort: GridSortDirection) => GridSortModel | undefined;
} & Omit<GridColDef, 'field'>;

/**
 * Propietats del component MuiDataGrid (també conté totes les propietats del DataGrid de MUI).
 */
export type MuiDataGridProps = {
    /** Títol que es mostrarà a la barra d'eines */
    title?: string;
    /** Indica si s'ha de mostrar o no el títol a la barra d'eines */
    titleDisabled?: true;
    /** Subtítol que es mostrarà a la barra d'eines */
    subtitle?: string;
    /** Nom del recurs de l'API REST que es consultarà per a obtenir la informació que es mostrarà a la graella */
    resourceName: string;
    /** Tipus de l'artefacte associat al recurs (aquest atribut només s'ha d'emplenar si volem consultar informació del camp associat al recurs d'un artefacte) */
    resourceType?: ResourceType;
    /** Codi de l'artefacte associat al recurs (aquest atribut només s'ha d'emplenar si volem consultar informació del camp associat al recurs d'un artefacte) */
    resourceTypeCode?: string;
    /** Nom del camp de l'artefacte associat al recurs (aquest atribut només s'ha d'emplenar si volem consultar informació del camp associat al recurs d'un artefacte) */
    resourceFieldName?: string;
    /** Configuració de les columnes de la graella  */
    columns: MuiDataGridColDef[];
    /** Indica si la graella és de només lectura (no es permeten modificacions) */
    readOnly?: true;
    /** Desactiva les peticions automàtiques al backend per a obtenir la informació a mostrar a la graella */
    findDisabled?: boolean;
    /** Text pel missatge de que no hi ha resultats */
    noRowsText?: string;
    /** Activa la persistència de l'estat (paginació, ordenació, selecció, ...) */
    persistentState?: true;
    /** Activa la selecció de files */
    selectionActive?: true;
    /** Activa la paginació */
    paginationActive?: true;
    /** Model de paginació inicial */
    paginationModel?: GridPaginationModel;
    /** Model d'ordenació inicial */
    sortModel?: GridSortModel;
    /** Model d'ordenació que s'aplicarà sempre (ignorant el valor de sortModel) */
    staticSortModel?: GridSortModel;
    /** Valor inicial pel filtre ràpid */
    quickFilterInitialValue?: string;
    /** Indica si el camp de filtre ràpid ha de tenir el focus quan es crei el component */
    quickFilterSetFocus?: true;
    /** Indica si el camp de filtre ràpid ha d'ocupar el 100% de l'espai horitzontal disponible */
    quickFilterFullWidth?: true;
    /** Filtre en format Spring Filter que s'enviarà en les consultes d'informació al backend */
    filter?: string;
    /** Filtre en format Spring Filter que s'aplicarà sempre en les consultes d'informació al backend (deshabilita filter) */
    staticFilter?: string;
    /** Consultes per nom que s'enviaran en les consultes d'informació al backend */
    namedQueries?: string[];
    /** Perspectives que s'enviaran en les consultes d'informació al backend */
    perspectives?: string[];
    /** Format de fitxer que s'enviarà com a paràmetre al fer la petició d'exportació d'informació al backend */
    exportFileType?: ExportFileType;
    /** Dades addicionals pel formulari de creació o modificació d'una fila de la graella */
    formAdditionalData?: ((row: any, action: string) => any) | any;
    /** Files addicionals per a la vista en arbre (si la vista d'arbre no està activa aquest atribut s'ignorarà) */
    treeDataAdditionalRows?: any[] | ((rows: any[]) => any[]);
    /** Tipus de barra d'eines que es mostrarà a la part superior */
    toolbarType?: DataToolbarType;
    /** Oculta la barra d'eines de la part superior */
    toolbarHide?: true;
    /** Indica si el toolbar ha de mostrar un botó per a tornar enrere */
    toolbarBackButton?: true;
    /** Oculta el botó d'exportació de la barra d'eines */
    toolbarHideExport?: false;
    /** Oculta el botó de creació de la barra d'eines */
    toolbarHideCreate?: true;
    /** Oculta el botó de refresc de la barra d'eines */
    toolbarHideRefresh?: true;
    /** Oculta el camp de filtre ràpid de la barra d'eines */
    toolbarHideQuickFilter?: true;
    /** Adreça que s'ha de mostrar al fer clic sobre el botó de crear una nova fila */
    toolbarCreateLink?: string;
    /** Elements addicionals (amb la seva posició) per a la barra d'eines */
    toolbarElementsWithPositions?: ReactElementWithPosition[];
    /** Element que es col·locarà just a davall la barra d'eines */
    toolbarAdditionalRow?: React.ReactElement;
    /** Adreça que s'ha de mostrar al fer clic sobre una fila de la graella (només es permet fer clic sobre les files si s'especifica algun valor) */
    rowLink?: string;
    /** Adreça que s'ha de mostrar al fer clic sobre el botó per a mostrar els detalls d'una fila (només en mode només lectura) */
    rowDetailLink?: string;
    /** Adreça que s'ha de mostrar al fer clic sobre el botó de modificar una fila */
    rowUpdateLink?: string;
    /** Deshabilita el botó d'actualització de cada fila (por ser també una funció que reb la fila i retorna true/false) */
    rowDisableUpdateButton?: boolean | ((row: any) => boolean);
    /** Deshabilita el botó d'esborrar de cada fila (por ser també una funció que reb la fila i retorna true/false) */
    rowDisableDeleteButton?: boolean | ((row: any) => boolean);
    /** Deshabilita el botó de detalls de cada fila (por ser també una funció que reb la fila i retorna true/false) */
    rowDisableDetailsButton?: boolean | ((row: any) => boolean);
    /** Oculta el botó de modificació de cada fila (por ser també una funció que reb la fila i retorna true/false) */
    rowHideUpdateButton?: boolean | ((row: any) => boolean);
    /** Oculta el botó d'esborrar de cada fila (por ser també una funció que reb la fila i retorna true/false) */
    rowHideDeleteButton?: boolean | ((row: any) => boolean);
    /** Oculta el botó de detalls de cada fila (por ser també una funció que reb la fila i retorna true/false) */
    rowHideDetailsButton?: boolean | ((row: any) => boolean);
    /** Index a dins la llista de columnes a on insertar la columna d'accions (si no s'especifica s'inserta al final) */
    rowActionsColumnIndex?: number;
    /** Propietats addicionals per a la columna d'accions de la graella */
    rowActionsColumnProps?: any;
    /** Accions addicionals per a cada fila */
    rowAdditionalActions?: DataCommonAdditionalAction[];
    /** Model amb les files seleccionades */
    rowSelectionModel?: GridRowSelectionModel;
    /** Indica que la creació i modificació amb finestra emergent està activa */
    popupEditActive?: boolean;
    /** Indica que només la creació amb finestra emergent està activa */
    popupEditCreateActive?: boolean;
    /** Indica que només la modificació amb finestra emergent està activa */
    popupEditUpdateActive?: boolean;
    /** Contingut (camps) del formulari de creació / modificació */
    popupEditFormContent?: React.ReactElement;
    /** Títol per la finestra emergent */
    popupEditFormDialogTitle?: string;
    /** Nom del recurs per la finestra emergent (s'afegeix al títol per defecte) */
    popupEditFormDialogResourceTitle?: string;
    /** Botons pel component Dialog de la finestra emergent */
    popupEditFormDialogButtons?: DialogButton[];
    /** Propietats pel component Dialog de la finestra emergent */
    popupEditFormDialogComponentProps?: any;
    /** Event onClose pel component Dialog de la finestra emergent */
    popupEditFormDialogOnClose?: (reason?: string) => boolean;
    /** Propietats pel component Form de la finestra emergent */
    popupEditFormComponentProps?: any;
    /** Claus de traducció personalitzades pel component Form de la finestra emergent */
    popupEditFormI18nKeys?: FormI18nKeys;
    /** Event que es llença quan es fa clic sobre una fila de la graella */
    onRowClick?: (params: GridRowParams, event: MuiEvent, details: GridCallbackDetails) => void;
    /** Event que es llença quan hi ha canvis en les files que mostra la graella */
    onRowsChange?: (rows: GridRowsProp, pageInfo: any) => void;
    /** Event que es llença quan hi ha canvis en l'ordenació de la graella */
    onRowOrderChange?: GridEventListener<'rowOrderChange'>;
    /** Event que es llença quan hi ha canvis en les files seleccionades de la graella */
    onRowSelectionModelChange?: (
        rowSelectionModel: GridRowSelectionModel,
        details: GridCallbackDetails
    ) => void;
    /** Referència a l'api del component */
    apiRef?: MuiDataGridApiRef;
    /** Referència a l'api interna del component DataGrid de MUI */
    datagridApiRef?: React.RefObject<GridApiPro | null>;
    /** Alçada del component en píxels */
    height?: number;
    /**
     * Indica si l'alçada del component s'ha d'ajustar al nombre de files que s'han de mostrar
     * @warning Canviar aquest valor dinàmicament fa que el DataGrid de MUI es torni a montar de nou (l'estat intern i subscripcions a events es perden).
     */
    autoHeight?: true;
    /** Indica que les files parells s'han de mostrar d'un color més oscur per a facilitar la seva lectura */
    striped?: true;
    /** Indica que només s'han de mostrar les vores horitzontals de la graella */
    semiBordered?: true;
    /** Estils addicionals pel contenidor de la graella */
    sx?: any;
    /** Indica si s'han d'imprimir a la consola missatges de depuració */
    debug?: true;
} & Omit<DataGridProps, 'apiRef'>;

const processFindSortModel = (sortModel: GridSortModel, columns: MuiDataGridColDef[]) => {
    const result: any[] = [];
    sortModel.forEach(({ field, sort }) => {
        const columnForCurrentField = columns.find((c) => c.field === field);
        const mappedFields = columnForCurrentField?.sortProcessor
            ? columnForCurrentField.sortProcessor(field, sort)
            : undefined;
        if (mappedFields) {
            mappedFields.forEach((mappedField) => result.push(mappedField));
        } else {
            result.push({ field, sort });
        }
    });
    return result as GridSortModel;
};

const rowLinkFind = (rowLink: string | undefined, rowLinks: any[] | undefined) => {
    if (rowLink != null) {
        const isNegative = rowLink != null && rowLink.startsWith('!');
        return isNegative ? rowLinks?.[rowLink.substring(1) as any] : rowLinks?.[rowLink as any];
    }
};
const rowLinkShowCheck = (rowLink: string | undefined, rowLinks: any[] | undefined) => {
    const found = rowLinkFind(rowLink, rowLinks);
    if (found) {
        const isNegative = rowLink != null && rowLink.startsWith('!');
        return isNegative ? found == null : found != null;
    } else {
        return true;
    }
};
const rowArtifactShowCheck = (
    action: string | undefined,
    report: string | undefined,
    artifacts: any[] | undefined
) => {
    if (action != null) {
        return artifacts?.find((a) => a.type === 'ACTION' && a.code === action) != null;
    } else if (report != null) {
        return artifacts?.find((a) => a.type === 'REPORT' && a.code === report) != null;
    } else {
        return true;
    }
};
const getRowActionOnClick = (
    rowAction: DataCommonAdditionalAction,
    showCreateDialog: DataCommonShowCreateDialogFn,
    showUpdateDialog: DataCommonShowUpdateDialogFn,
    triggerDelete: DataCommonTriggerDeleteFn
): DataGridActionItemOnClickFn | undefined => {
    if (rowAction.clickShowCreateDialog) {
        return (_id, row) => showCreateDialog(row);
    } else if (rowAction.clickShowUpdateDialog) {
        return (id, row) => showUpdateDialog(id, row);
    } else if (rowAction.clickTriggerDelete) {
        return (id) => triggerDelete(id);
    } else {
        return rowAction.onClick;
    }
};

const rowActionsToGridActionsCellItems = (
    rowActions: DataCommonAdditionalAction[],
    params: GridRowParams,
    showCreateDialog: DataCommonShowCreateDialogFn,
    showUpdateDialog: DataCommonShowUpdateDialogFn,
    triggerDelete: DataCommonTriggerDeleteFn,
    artifacts: any[] | undefined,
    forceDisabled?: boolean
): React.ReactElement[] => {
    const actions: React.ReactElement[] = [];
    rowActions.forEach((rowAction: DataCommonAdditionalAction) => {
        const rowLink = rowLinkFind(rowAction.rowLink, params.row['_actions']);
        const rowLinkShow = rowLinkShowCheck(rowAction.rowLink, params.row['_actions']);
        const rowArtifactShow = rowArtifactShowCheck(rowAction.action, rowAction.report, artifacts);
        const rowActionLinkTo =
            typeof rowAction.linkTo === 'function'
                ? rowAction.linkTo?.(params.row)
                : rowAction.linkTo?.replace('{{id}}', '' + params.id);
        const rowActionLinkState =
            typeof rowAction.linkState === 'function'
                ? rowAction.linkState?.(params.row)
                : rowAction.linkState;
        const rowActionLinkTarget =
            typeof rowAction.linkTarget === 'function'
                ? rowAction.linkTarget?.(params.row)
                : rowAction.linkTarget;
        const rowActionOnClick = getRowActionOnClick(
            rowAction,
            showCreateDialog,
            showUpdateDialog,
            triggerDelete
        );
        const label =
            typeof rowAction.label === 'function' ? rowAction.label(params.row) : rowAction.label;
        const title =
            typeof rowAction.title === 'function' ? rowAction.title(params.row) : rowAction.title;
        const icon =
            typeof rowAction.icon === 'function' ? rowAction.icon(params.row) : rowAction.icon;
        const showInMenu =
            typeof rowAction.showInMenu === 'function'
                ? rowAction.showInMenu(params.row)
                : rowAction.showInMenu;
        const disabled =
            forceDisabled ||
            (typeof rowAction.disabled === 'function'
                ? rowAction.disabled(params.row)
                : rowAction.disabled);
        const hidden =
            typeof rowAction.hidden === 'function'
                ? rowAction.hidden(params.row)
                : rowAction.hidden;
        rowLinkShow &&
            rowArtifactShow &&
            !hidden &&
            actions.push(
                toDataGridActionItem(
                    params.id,
                    label ?? (rowLink != null ? rowLink?.title : rowAction),
                    title,
                    icon,
                    params.row,
                    rowActionLinkTo,
                    rowActionLinkState,
                    rowActionLinkTarget,
                    rowActionOnClick,
                    showInMenu,
                    disabled
                )
            );
    });
    return actions;
};

const useGridColumns = (
    columns: MuiDataGridColDef[],
    rowActionsColumnIndex: number | undefined,
    rowActionsColumnProps: any,
    rowActions: DataCommonAdditionalAction[],
    rowEditActions: DataCommonAdditionalAction[],
    fields: any[] | undefined,
    showCreateDialog: DataCommonShowCreateDialogFn,
    showUpdateDialog: DataCommonShowUpdateDialogFn,
    triggerDelete: DataCommonTriggerDeleteFn,
    artifacts: any[] | undefined,
    rowModesModel?: GridRowModesModel
) => {
    const { currentLanguage } = useResourceApiContext();
    const processedColumns = React.useMemo(() => {
        const processedColumns: MuiDataGridColDef[] = columns.map((c) => {
            const field = fields?.find((f) => f.name === c.field);
            const isNumericType = isFieldNumericType(field, c.fieldType);
            const isCurrencyType = c.currencyType;
            return {
                valueGetter: (value: any, row: any, column: GridColDef) => {
                    if (column.field?.includes('.')) {
                        const value = column.field
                            .split('.')
                            .reduce(
                                (o: any, x: string) =>
                                    typeof o == 'undefined' || o === null ? o : o[x],
                                row
                            );
                        return value;
                    } else {
                        return value;
                    }
                },
                valueFormatter: (value: never, row: any) => {
                    const cany: any = c;
                    const formattedValue = formattedFieldValue(value, field, {
                        type: isCurrencyType ? 'currency' : c.fieldType,
                        currentLanguage,
                        currencyCode: cany['currencyCode'],
                        currencyDecimalPlaces: cany['currencyDecimalPlaces'],
                        currencyLocale: cany['currencyLocale'],
                        decimalPlaces: cany['decimalPlaces'],
                        noTime: cany['noTime'],
                        noSeconds: cany['noSeconds'],
                        formatterParams: row,
                    });
                    return formattedValue;
                },
                headerName: field ? field?.label : '',
                headerAlign: isNumericType ? 'right' : undefined,
                align: isNumericType ? 'right' : undefined,
                display: 'flex',
                exportable: field != null,
                ...c,
            };
        });
        if (rowActions && rowActions.length) {
            const actionsColumn = {
                field: ' ',
                type: 'actions',
                getActions: (params: GridRowParams) => {
                    const anyRowInEditMode =
                        rowModesModel &&
                        Object.keys(rowModesModel).filter(
                            (m) => rowModesModel[m].mode === GridRowModes.Edit
                        ).length > 0;
                    const isEditMode =
                        rowModesModel && rowModesModel[params.id]?.mode === GridRowModes.Edit;
                    return rowActionsToGridActionsCellItems(
                        isEditMode ? rowEditActions : rowActions,
                        params,
                        showCreateDialog,
                        showUpdateDialog,
                        triggerDelete,
                        artifacts,
                        anyRowInEditMode && !isEditMode
                    );
                },
                ...rowActionsColumnProps,
            };
            processedColumns.splice(
                rowActionsColumnIndex ?? processedColumns.length,
                0,
                actionsColumn
            );
        }
        return processedColumns;
    }, [columns, fields, rowModesModel, artifacts]);
    return processedColumns;
};

/**
 * Hook per a accedir a l'API de MuiDataGrid des de fora del context del component.
 *
 * @returns referència a l'API del component MuiDataGrid.
 */
export const useMuiDataGridApiRef: () => React.RefObject<MuiDataGridApi> = () => {
    const gridApiRef = React.useRef<MuiDataGridApi | any>({});
    return gridApiRef;
};

/**
 * Hook per a accedir a l'API de MuiDataGrid des de dins el context del component.
 *
 * @returns referència a l'API del component MuiDataGrid.
 */
export const useMuiDataGridApiContext: () => MuiDataGridApiRef = () => {
    const gridContext = useDataGridContext();
    return gridContext.apiRef;
};

/**
 * Graella per a visualitzar dades provinents d'una API REST basada en Base-Boot.
 *
 * @param props - Propietats del component.
 * @returns Element JSX de la graella.
 */
export const MuiDataGrid: React.FC<MuiDataGridProps> = (props) => {
    const { defaultMuiComponentProps } = useMuiBaseAppContext();
    const {
        title,
        titleDisabled,
        subtitle,
        resourceName,
        resourceType,
        resourceTypeCode,
        resourceFieldName,
        columns,
        readOnly,
        findDisabled,
        noRowsText,
        persistentState,
        selectionActive,
        paginationActive,
        paginationModel: paginationModelProp,
        sortModel,
        staticSortModel,
        quickFilterInitialValue,
        quickFilterSetFocus,
        quickFilterFullWidth,
        filter: filterProp,
        staticFilter,
        namedQueries,
        perspectives,
        exportFileType = 'PDF',
        formAdditionalData,
        treeDataAdditionalRows,
        toolbarType = 'default',
        toolbarHide,
        toolbarBackButton,
        toolbarHideExport = true,
        toolbarHideCreate,
        toolbarHideRefresh,
        toolbarHideQuickFilter,
        toolbarCreateLink,
        toolbarElementsWithPositions,
        toolbarAdditionalRow,
        rowLink,
        rowDetailLink,
        rowUpdateLink,
        rowDisableUpdateButton,
        rowDisableDeleteButton,
        rowDisableDetailsButton,
        rowHideUpdateButton,
        rowHideDeleteButton,
        rowHideDetailsButton,
        rowActionsColumnIndex,
        rowActionsColumnProps,
        rowAdditionalActions = [],
        rowSelectionModel: rowSelectionModelProp = DEFAULT_ROW_SELECTION,
        popupEditActive,
        popupEditCreateActive,
        popupEditUpdateActive,
        popupEditFormContent,
        popupEditFormDialogTitle,
        popupEditFormDialogResourceTitle,
        popupEditFormDialogButtons,
        popupEditFormDialogComponentProps,
        popupEditFormDialogOnClose,
        popupEditFormComponentProps,
        popupEditFormI18nKeys,
        onRowClick,
        onRowsChange,
        onRowOrderChange,
        onRowSelectionModelChange,
        apiRef: apiRefProp,
        datagridApiRef: datagridApiRefProp,
        height,
        autoHeight,
        striped,
        semiBordered,
        sx,
        debug = false,
        ...otherProps
    } = { ...defaultMuiComponentProps.dataGrid, ...props };
    const logConsole = useLogConsole(LOG_PREFIX);
    const datagridApiRefInternal = useMuiDatagridApiRef();
    const datagridApiRef = datagridApiRefProp ?? datagridApiRefInternal;
    const anyArtifactRowAction =
        rowAdditionalActions?.find((a) => a.action != null || a.report != null) != null;
    const treeDataAdditionalRowsIsFunction = treeDataAdditionalRows
        ? typeof treeDataAdditionalRows === 'function'
        : false;
    const [_filterModel, setFilterModel] = React.useState<GridFilterModel>();
    const [internalSortModel, setInternalSortModel] = React.useState<GridSortModel>(
        sortModel ?? []
    );
    const [internalFilter, setInternalFilter] = React.useState<string | undefined>(filterProp);
    const [paginationModel, setPaginationModel] = React.useState<GridPaginationModel | undefined>(
        paginationModelProp
    );
    const [footerAutoPageSize, setFooterAutoPageSize] = React.useState<boolean>(
        !(otherProps.pageSizeOptions != null && paginationModelProp != null)
    );
    const [rowSelectionModel, setRowSelectionModel] =
        React.useState<GridRowSelectionModel>(rowSelectionModelProp);
    const [additionalRows, setAdditionalRows] = React.useState<any[]>(
        !treeDataAdditionalRowsIsFunction ? [] : (treeDataAdditionalRows as any[])
    );
    const [initialState, setInitialState] = React.useState<GridInitialState | null>();
    const {
        currentActions: apiCurrentActions,
        currentError: apiCurrentError,
        delete: apiDelete,
    } = useResourceApiService(resourceName);
    const findArgs = React.useMemo(() => {
        const filter = staticFilter
            ? internalFilter
                ? '(' + staticFilter + ') and (' + internalFilter + ')'
                : staticFilter
            : internalFilter;
        const findSortModel = staticSortModel ?? internalSortModel;
        const processedFindSortModel = processFindSortModel(findSortModel, columns);
        const sorts = processedFindSortModel?.length
            ? processedFindSortModel.map(({ field, sort }) => `${field},${sort}`)
            : undefined;
        const paginationArgs = paginationActive
            ? {
                  page: paginationModel?.page,
                  size: paginationModel?.pageSize,
              }
            : { unpaged: true };
        return {
            ...paginationArgs,
            sorts,
            filter,
            namedQueries,
            perspectives,
        };
    }, [
        paginationActive,
        paginationModel,
        staticSortModel,
        internalSortModel,
        internalFilter,
        staticFilter,
        namedQueries,
        perspectives,
        columns,
    ]);
    const {
        loading,
        fields,
        rows,
        pageInfo,
        artifacts,
        error: apiDataCommonError,
        refresh,
        export: exportt,
        quickFilterComponent,
    } = useApiDataCommon(
        resourceName,
        resourceType,
        resourceTypeCode,
        resourceFieldName,
        findDisabled,
        findArgs,
        quickFilterInitialValue,
        quickFilterSetFocus,
        {
            fullWidth: quickFilterFullWidth,
            sx: { ml: quickFilterFullWidth ? 0 : 1 },
        },
        anyArtifactRowAction
    );
    const isUpperToolbarType = toolbarType === 'upper';
    const gridMargins = isUpperToolbarType ? { m: 2 } : null;
    React.useEffect(() => {
        onRowsChange?.(rows, pageInfo);
        if (treeDataAdditionalRowsIsFunction) {
            setAdditionalRows((treeDataAdditionalRows as (rows: any[]) => any[])(rows));
        }
    }, [rows]);
    React.useEffect(() => {
        setInternalFilter(filterProp);
    }, [filterProp]);
    const { state, isReady: stateIsReady } = persistentState
        ? useSessionComponentPersistentState('mui_datagrid_state', resourceName, () =>
              datagridApiRef.current?.exportState()
          )
        : { isReady: false };
    React.useEffect(() => {
        if (stateIsReady) {
            setInitialState(state);
            if (state?.pagination?.paginationModel) {
                setPaginationModel(state.pagination.paginationModel);
            }
        }
    }, [state, stateIsReady]);
    const {
        toolbarAddElement,
        rowEditActions,
        formDialogComponent,
        showCreateDialog,
        showUpdateDialog,
        triggerDelete,
    } = useDataCommonEditable(
        resourceName,
        readOnly ?? false,
        formAdditionalData,
        toolbarCreateLink,
        rowDetailLink,
        rowUpdateLink,
        rowDisableUpdateButton,
        rowDisableDeleteButton,
        rowDisableDetailsButton,
        rowHideUpdateButton,
        rowHideDeleteButton,
        rowHideDetailsButton,
        popupEditActive,
        popupEditCreateActive,
        popupEditUpdateActive,
        popupEditFormContent,
        popupEditFormDialogTitle,
        popupEditFormDialogResourceTitle,
        popupEditFormDialogButtons,
        popupEditFormDialogComponentProps,
        popupEditFormDialogOnClose,
        popupEditFormComponentProps,
        popupEditFormI18nKeys,
        apiCurrentActions,
        apiDelete,
        refresh
    );
    const toolbarNodesPosition = 2;
    const toolbarGridElementsWithPositions: ReactElementWithPosition[] = [];
    toolbarAddElement != null &&
        toolbarGridElementsWithPositions.push({
            position: toolbarNodesPosition,
            element: !toolbarHideCreate ? toolbarAddElement : <span />,
        });
    const toolbarNumElements =
        toolbarNodesPosition +
        (toolbarHideExport ? 0 : 1) +
        (toolbarHideRefresh ? 0 : 1) +
        (toolbarHideQuickFilter ? 0 : 1);
    const joinedToolbarElementsWithPositions =
        joinReactElementsWithPositionWithReactElementsWithPositions(
            toolbarNumElements,
            toolbarGridElementsWithPositions,
            toolbarElementsWithPositions
        );
    const gridExport = () => {
        const exportFields: string[] = columns
            .filter((c) => {
                const field = fields?.find((f) => f.name === c.field);
                return field != null;
            })
            .map((c) => c.field);
        exportt(exportFields, exportFileType, true);
    };
    const toolbar = useDataToolbar(
        title ?? capitalize(resourceName) ?? '<unknown>',
        titleDisabled ?? false,
        subtitle,
        toolbarType,
        apiCurrentError || apiDataCommonError,
        quickFilterComponent,
        refresh,
        gridExport,
        toolbarBackButton,
        toolbarHideExport,
        toolbarHideRefresh,
        toolbarHideQuickFilter,
        joinedToolbarElementsWithPositions
    );
    const processedColumns = useGridColumns(
        columns,
        rowActionsColumnIndex,
        rowActionsColumnProps,
        [...rowAdditionalActions, ...rowEditActions],
        rowEditActions,
        fields,
        showCreateDialog,
        showUpdateDialog,
        triggerDelete,
        artifacts,
        otherProps.rowModesModel
    );
    const apiRef = React.useRef<MuiDataGridApi>({
        refresh,
        export: gridExport,
        showCreateDialog,
        showUpdateDialog,
        setFilter: (filter) => setInternalFilter(filter ?? undefined),
    });
    if (apiRefProp) {
        if (apiRefProp.current) {
            apiRefProp.current.refresh = refresh;
            apiRefProp.current.export = gridExport;
            apiRefProp.current.showCreateDialog = showCreateDialog;
            apiRefProp.current.showUpdateDialog = showUpdateDialog;
            apiRefProp.current.setFilter = (filter) => setInternalFilter(filter ?? undefined);
        } else {
            logConsole.warn('apiRef prop must be initialized with an empty object');
        }
    }
    const filteringProps: any = {
        filterMode: 'server',
        onFilterModelChange: setFilterModel,
    };
    const sortingProps: any = {
        sortingMode: 'server',
        sortModel: staticSortModel ?? internalSortModel,
        onSortModelChange: setInternalSortModel,
    };
    const paginationProps: any = paginationActive
        ? {
              paginationMode: 'server',
              pagination: true,
              autoPageSize: !autoHeight && footerAutoPageSize,
              paginationModel: paginationModel,
              onPaginationModelChange: setPaginationModel,
              rowCount: pageInfo?.totalElements ?? 0,
          }
        : null;
    const selectionProps: any = selectionActive
        ? {
              checkboxSelection: true,
              disableRowSelectionOnClick: true,
              onRowSelectionModelChange: (
                  rowSelectionModel: GridRowSelectionModel,
                  details: GridCallbackDetails
              ) => {
                  setRowSelectionModel(rowSelectionModel);
                  onRowSelectionModelChange?.(rowSelectionModel, details);
              },
              rowSelectionModel,
              keepNonExistentRowsSelected: true,
              checkboxSelectionVisibleOnly: true,
          }
        : {
              disableRowSelectionOnClick: true,
          };
    const stripedProps: any = striped
        ? {
              getRowClassName: (params: GridRowClassNameParams) =>
                  params.indexRelativeToCurrentPage % 2 === 0 ? 'even' : 'odd',
          }
        : null;
    const processedRows = React.useMemo(() => [...additionalRows, ...rows], [additionalRows, rows]);
    const content = (
        <>
            {!toolbarHide && toolbar}
            {toolbarAdditionalRow ? (
                <Box sx={{ ...gridMargins, mb: 0 }}>{toolbarAdditionalRow}</Box>
            ) : null}
            {formDialogComponent}
            <DataGridCustomStyle
                {...otherProps}
                loading={otherProps?.loading ?? loading}
                rows={otherProps?.rows ?? processedRows}
                columns={processedColumns}
                onRowClick={onRowClick}
                onRowOrderChange={onRowOrderChange}
                initialState={persistentState && initialState ? initialState : undefined}
                apiRef={datagridApiRef}
                {...filteringProps}
                {...sortingProps}
                {...paginationProps}
                {...selectionProps}
                {...stripedProps}
                slots={{
                    row: DataGridRow as GridSlots['row'],
                    footer: DataGridFooter as GridSlots['footer'],
                    noRowsOverlay: DataGridNoRowsOverlay,
                }}
                slotProps={{
                    row: { linkTo: rowLink, cursorPointer: onRowClick != null },
                    footer: {
                        paginationActive,
                        selectionActive,
                        paginationModel,
                        pageInfo,
                        setRowSelectionModel,
                        pageSizeOptions: otherProps?.pageSizeOptions,
                        enableAutoPageSizeOption: !autoHeight,
                        autoPageSize: footerAutoPageSize,
                        setAutoPageSize: setFooterAutoPageSize,
                    },
                    noRowsOverlay: {
                        requestPending: findDisabled && !('rows' in otherProps),
                        noRowsText: noRowsText,
                    },
                }}
                semiBordered={semiBordered}
                autoHeight={autoHeight}
                sx={{
                    height: autoHeight ? 'auto' : undefined,
                    ...gridMargins,
                    ...sx,
                }}
            />
        </>
    );
    // Workaround for bug in MUI-X v6 related to the DataGrid height https://github.com/mui/mui-x/issues/10520
    const virtualScrollerStyles = {
        [`& .MuiDataGrid-main`]: {
            flex: '1 1 0px',
        },
    };
    const context = {
        resourceName,
        loading,
        findArgs,
        rows: processedRows,
        selection: rowSelectionModel,
        apiRef,
    };
    return (
        <DataGridContext.Provider value={context}>
            {autoHeight ? (
                content
            ) : (
                <Box
                    sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        height: height ? height : '100%',
                        ...virtualScrollerStyles,
                    }}>
                    {content}
                </Box>
            )}
        </DataGridContext.Provider>
    );
};

export default MuiDataGrid;
