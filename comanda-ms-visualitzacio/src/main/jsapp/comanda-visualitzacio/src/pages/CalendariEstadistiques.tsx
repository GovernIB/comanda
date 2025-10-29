import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import {useBaseAppContext, GridPage, useResourceApiService, Toolbar, springFilterBuilder as builder} from 'reactlib';
import {useState, useEffect, useCallback, useMemo} from "react";
import dayjs from 'dayjs';
import '../fullcalendar-custom.css';
import {
    Button,
    Dialog,
    FormControl,
    InputLabel,
    MenuItem,
    Select,
    Typography,
    CircularProgress,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Icon,
    useTheme,
    useMediaQuery,
    Tooltip,
    OutlinedInput,
    IconButton,
    Chip
} from "@mui/material";
import DialogTitle from "@mui/material/DialogTitle";
import {DatePicker} from "@mui/x-date-pickers/DatePicker";
import DialogContent from "@mui/material/DialogContent";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import DialogActions from "@mui/material/DialogActions";
import ErrorIcon from '@mui/icons-material/Error';
import {useTranslation} from "react-i18next";
import * as React from "react";
import ReactDOM from 'react-dom/client';
import {useMessage} from "../components/MessageShow.tsx";
import Grid from "@mui/material/Grid";
import Checkbox from "@mui/material/Checkbox";
import FormGroup from "@mui/material/FormGroup";
import ListItemText from "@mui/material/ListItemText";

interface ErrorInfo {
    date: string;
    message: string;
    trace?: string;
}

interface PerData {
    entornAppId: number;
    dataInici: string;
}

interface PerInterval {
    entornAppId: number;
    dataInici: string;
    dataFi: string;
}

interface Temps {
    data: string;
    anualitat: number;
    trimestre: number;
    mes: number;
    setmana: number;
    dia: number;
    diaSetmana: string;
}

interface DadesDia {
    temps: Temps;
    dimensionsJson: Record<string, string>;
    indicadorsJson: Record<string, number>;
    entornAppId: number;
}

interface CalendarStatusButtonProps {
  hasError: boolean;
  isLoading: boolean;
  esDisponible: boolean;
}

export const CalendarStatusButton: React.FC<CalendarStatusButtonProps> = ({
  hasError,
  isLoading,
  esDisponible
}) => {
  const { t } = useTranslation();
  const theme = useTheme();
  const isSmallScreen = useMediaQuery(theme.breakpoints.down('lg'));

  const icon = hasError
    ? <Icon>error</Icon>
    : esDisponible
    ? <Icon>check_circle</Icon>
    : <Icon>download</Icon>;

  const label = hasError
    ? t($ => $.calendari.error_dades)
    : esDisponible
    ? t($ => $.calendari.dades_disponibles)
    : t($ => $.calendari.obtenir_dades);

  const tooltip = hasError
    ? t($ => $.calendari.error_dades_tooltip)
    : esDisponible
    ? t($ => $.calendari.dades_disponibles_tooltip)
    : t($ => $.calendari.obtenir_dades_tooltip);

  const color: 'primary' | 'error' | 'success' = hasError
    ? 'error'
    : esDisponible
    ? 'success'
    : 'primary';

  return (
    <>
        <Tooltip title={tooltip}>
        <Button
            variant="contained"
            color={color}
            size="small"
            startIcon={ isLoading ? ( <CircularProgress size={16} color="inherit" /> ) : ( icon ) }
            disabled={isLoading}
            sx={{
            whiteSpace: 'nowrap',
            textTransform: 'none',
            width: '100%',
            minWidth: 0,
            }}
        >
            {!isSmallScreen && label}
        </Button>
        </Tooltip>
    </>
  );

};

const CalendariEstadistiques: React.FC = () => {
    const { t } = useTranslation();
    const { currentLanguage } = useBaseAppContext();
    const [dataInici, setDataInici] = useState<Date | null>(null);
    const [dataFi, setDataFi] = useState<Date | null>(null);
    const [obrirDialog, setObrirDialog] = useState(false);
    const [entornAppId, setEntornAppId] = useState<number | ''>('');
    const [datesAmbDades, setDatesAmbDades] = useState<string[]>([]);
    const [globalLoading, setGlobalLoading] = useState(false);
    const [loadingDates, setLoadingDates] = useState<string[]>([]);
    const [emptyDates, setEmptyDates] = useState<string[]>([]);
    const [errors, setErrors] = useState<ErrorInfo[]>([]);
    const [errorDialogOpen, setErrorDialogOpen] = useState(false);
    const [currentError, setCurrentError] = useState<ErrorInfo | null>(null);
    const [datesDisponiblesError, setDatesDisponiblesError] = useState<boolean>(true);
    const [currentViewMonth, setCurrentViewMonth] = useState(dayjs().month());
    const [currentViewYear, setCurrentViewYear] = useState(dayjs().year());
    
    // State for the day data modal
    const [dadesDiaModalOpen, setDadesDiaModalOpen] = useState(false);
    const [currentDadesDia, setCurrentDadesDia] = useState<DadesDia[]>([]);
    const [currentDataDia, setCurrentDataDia] = useState<string>('');

    // Obtenir els ResourceServices
    const { isReady: entornAppApiIsReady, find: entornAppGetAll } = useResourceApiService('entornApp');

    // Al obrir la pàgina carreguem el llistat de EntornApp actius
    const [entornApps, setEntornApps] = useState<any[]>([]);
    useEffect(() => {
        if (entornAppApiIsReady) {
            console.log('EntornApp API ready');
            entornAppGetAll({
                unpaged: true,
                filter: 'activa : true AND app.activa : true',
            }).then(response => {
                console.log('EntornApp API response received:', response.rows.length, 'items');
                setEntornApps(response.rows);
            });
        }
    }, [entornAppApiIsReady, entornAppGetAll]);

    // Obtenir les accions
    const { isReady: apiFetIsReady, artifactAction: apiAction, artifactReport: apiReport } = useResourceApiService('fet');
    const { temporalMessageShow } = useBaseAppContext();
    const { show: showMessage, component } = useMessage();

    // Definir les accions directament al component
    // Obtenir dades estadístiques per un dia concret
    const obtenirPerData = React.useCallback(async (additionalData: PerData): Promise<boolean> => {
        try {
            // Add the date to the loading dates array
            setLoadingDates(prev => [...prev, additionalData.dataInici]);
            
            const data = await apiAction(null, { code: 'obtenir_per_data', data: additionalData });
            if (data.success)
                temporalMessageShow(null, t($ => $.calendari.success_obtenir_dades), 'success');
            else
                temporalMessageShow(null, t($ => $.calendari.error_obtenir_dades) + ": " + data.message, 'error');


            if (data.diesAmbDades) {
                const dataFormatada = additionalData.dataInici;
                if (data.diesAmbDades[dataFormatada] === false) {
                    console.log('No hi ha dades per a aquest dia', dataFormatada);
                    setEmptyDates(prev => [...prev, additionalData.dataInici]);
                } else {
                    setEmptyDates(prev => prev.filter(date => date !== additionalData.dataInici));
                }
            } else {
                setEmptyDates(prev => [...prev, additionalData.dataInici]);
            }
            console.log('Dades buides:', emptyDates);
            
            // Remove the date from loading dates array
            setLoadingDates(prev => prev.filter(date => date !== additionalData.dataInici));
            return data.success;
        } catch (error: any) {
            temporalMessageShow(null, error.message, 'error');
            // Remove the date from loading dates array in case of error
            setLoadingDates(prev => prev.filter(date => date !== additionalData.dataInici));
            return false;
        }
    }, [apiAction, temporalMessageShow, t]);

    // Obtenir dades estadístiques per un interval de dies
    const obtenirPerInterval = React.useCallback(async (additionalData: PerInterval): Promise<boolean> => {
        try {
            // Set global loading to true to show blocking overlay
            setGlobalLoading(true);
            
            const data = await apiAction(null, { code: 'obtenir_per_interval', data: additionalData });
            if (data.success) {
                showMessage(null, t($ => $.calendari.success_obtenir_dades), 'success');
                // Actualitzar les dates disponibles després d'obtenir dades per interval
                obtenirDatesDisponibles(additionalData.entornAppId);
            } else {
                showMessage(null, t($ => $.calendari.error_obtenir_dades) + ": " + data.message, 'error');
            }
            
            // Set global loading to false when done
            setGlobalLoading(false);
            return data.success;
        } catch (error: any) {
            showMessage(null, error.message, 'error');
            // Set global loading to false in case of error
            setGlobalLoading(false);
            return false;
        }
    }, [apiAction, temporalMessageShow, t]);

    // Obtenir els dies en que es disposa de dades estadístiques
    const obtenirDatesDisponibles = React.useCallback(async (entornAppId: any): Promise<boolean> => {
        console.log('Obtenir dates disponibles per entornApp:', entornAppId);
        try {
            const data = (await apiReport(
                null,
                {code: 'dates_disponibles', data: entornAppId}
            )) as any[];
            console.log('Consulta de dates correcta:', data);
            const dates = data.map((date: Temps) => dayjs(date.data).format('YYYY-MM-DD'));
            setDatesAmbDades(dates);
            setDatesDisponiblesError(false);
            console.log('Dates disponibles:', dates);
        } catch (error: any) {
            console.error('Error en obtenir dates disponibles:', error);
            temporalMessageShow(null, t($ => $.calendari.error_dades_disponibles) + ": " + error.message, 'error');
            setDatesAmbDades([]);
            setDatesDisponiblesError(true);
            console.log('Dates disponibles:', []);
        }
        return true;
    }, [apiReport, temporalMessageShow, t, setDatesDisponiblesError]);

    // Carregar les dades estadístiques de que disposem per un dia concret
    const obtenirDadesDia = React.useCallback(async (entornAppId: number, dataFormatada: string): Promise<boolean> => {
        console.log('Obtenir dades del dia per entornApp:', entornAppId, 'data:', dataFormatada);
        try {
            const additionalData: PerData = {
                entornAppId: entornAppId as number,
                dataInici: dataFormatada
            };
            const data = (await apiReport(
                null,
                {code: 'dades_dia', data: additionalData}
            )) as DadesDia[];
            
            console.log('Consulta de dades correcta:', data);
            
            // Store the fetched data and open the modal
            setCurrentDadesDia(data);
            setCurrentDataDia(dataFormatada);
            setDadesDiaModalOpen(true);
            
            return true;
        } catch (error: any) {
            console.error('Error en obtenir dades per dia:', error);
            temporalMessageShow(null, t($ => $.calendari.error_dades_dia) + ": " + error.message, 'error');
            return false;
        }
    }, [apiReport, temporalMessageShow, t, setCurrentDadesDia, setCurrentDataDia, setDadesDiaModalOpen]);

    // No actualitzem les dates disponibles quan canvia l'entorn d'aplicació
    // Això es farà només quan es carreguin les dates disponibles

    const esDiaAmbDades = useCallback((data: string) => datesAmbDades.includes(data), [datesAmbDades]);
    const esDiaAmbEmptyDades = useCallback((data: string) => emptyDates.includes(data), [emptyDates]);
    const esDiaAmbError = useCallback((data: string) => errors.some(error => error.date === data), [errors]);

    const getErrorForDate = useCallback((data: string) => {
        return errors.find(error => error.date === data);
    }, [errors]);

    const obtenirDadesEstadistiques = useCallback(async (data: string) => {
        if (entornAppId === '') return;

        const dataFormatada = dayjs(data).format('YYYY-MM-DD');
        const additionalData: PerData = {
            entornAppId: entornAppId as number,
            dataInici: dataFormatada
        };

        try {
            const success = await obtenirPerData(additionalData);
            if (success) {
                // Actualitzar les dates disponibles després d'obtenir dades
                await obtenirDatesDisponibles(entornAppId);
                // Eliminar qualsevol error associat a aquesta data
                setErrors(prev => prev.filter(error => error.date !== data));
            } else {
                const errorInfo: ErrorInfo = {
                    date: data,
                    message: ''
                };
                setErrors(prev => {
                    // Eliminar errors anteriors per a aquesta data
                    const filtered = prev.filter(e => e.date !== data);
                    return [...filtered, errorInfo];
                });
            }
        } catch (error: any) {
            console.error('Error en obtenir dades estadístiques:', error);
            // Guardar l'error per mostrar-lo després
            const errorInfo: ErrorInfo = {
                date: data,
                message: error.message || t($ => $.calendari.error_obtenir_dades),
                trace: error.stack
            };
            setErrors(prev => {
                // Eliminar errors anteriors per a aquesta data
                const filtered = prev.filter(e => e.date !== data);
                return [...filtered, errorInfo];
            });
        }
    }, [entornAppId, obtenirPerData, obtenirDatesDisponibles, t]);

    const carregarIntervalDades = useCallback(async (inici: string, fi: string) => {
        if (entornAppId === '') return;

        const additionalData: PerInterval = {
            entornAppId: entornAppId as number,
            dataInici: inici,
            dataFi: fi
        };

        try {
            await obtenirPerInterval(additionalData);
            // La funció obtenirPerInterval ja actualitza les dates disponibles si té èxit
        } catch (error: any) {
            console.error('Error en carregar interval de dades:', error);
            alert(t($ => $.calendari.error_carregar_interval));
        }
    }, [entornAppId, obtenirPerInterval, t]);

    // Obtenir el primer i darrer dia del mes actual del calendari
    const getFirstAndLastDayOfMonth = useCallback(() => {
        // Use currentViewMonth and currentViewYear directly
        console.log('Using currentViewMonth and currentViewYear for date calculation');
        
        // Calculate first and last day of month
        const firstDay = dayjs(new Date(currentViewYear, currentViewMonth, 1)).format('YYYY-MM-DD');
        const lastDay = dayjs(new Date(currentViewYear, currentViewMonth + 1, 0)).format('YYYY-MM-DD');
        
        console.log('First day:', firstDay, 'Last day:', lastDay);
        return {
            firstDay,
            lastDay
        };
    }, [currentViewMonth, currentViewYear]);

    const handleErrorClick = (errorInfo: ErrorInfo) => {
        setCurrentError(errorInfo);
        setErrorDialogOpen(true);
    };

    // Generar els events per al calendari
    const daysInMonth = dayjs(`${currentViewYear}-${currentViewMonth + 1}-01`).daysInMonth();

    const events = Array.from({ length: daysInMonth }, (_, i) => {
        console.log('currentViewMonth', currentViewMonth);
        const date = dayjs(`${currentViewYear}-${String(currentViewMonth + 1).padStart(2, '0')}-${String(i + 1).padStart(2, '0')}`).format('YYYY-MM-DD');
        const hasError = esDiaAmbError(date);
        const hasDades = esDiaAmbDades(date);
        const hasEmptyDades = esDiaAmbEmptyDades(date);
        const isLoading = loadingDates.includes(date);
        console.log('Es disposa de dades:', hasDades, datesAmbDades);
        console.log('Es disposa de dades buides:', hasEmptyDades, emptyDates);

        // Si no hi ha un entornApp seleccionat, o hi ha hagut un error en obtenir dates disponibles, o la data és anterior a avui, no mostrem cap informació
        if (entornAppId === '' || datesDisponiblesError || !dayjs(date).isBefore(dayjs(), 'day')) {
            console.log('No mostrar event per a', date);
            return null; // Return null instead of an empty event to completely hide it
        }
        
        console.log('Mostrar event per a', date);
        console.log('Es disposa de dades:', hasDades);
        console.log('Hi ha error:', hasError);
        console.log('Està carregant:', isLoading);
        
        // Si la data està carregant, mostrem un indicador de càrrega
        if (isLoading) {
            return [
                // Event de background per donar color a la cel·la
                {
                    title: t($ => $.calendari.carregant),
                    date,
                    backgroundColor: '#f5f5f5',
                    extendedProps: {
                        esDisponible: false,
                        hasError: false,
                        isLoading: true
                    },
                    allDay: true,
                    display: 'background'
                },
                // Event de block amb l'indicador de càrrega
                {
                    title: t($ => $.calendari.carregant),
                    date,
                    classNames: ['cal-event-loading'],
                    textColor: '#888',
                    backgroundColor: '#fff',
                    extendedProps: {
                        esDisponible: false,
                        hasError: false,
                        isLoading: true,
                        hasContent: true,
                        content: `<div class="material-button-container">
                            <div class="loading-indicator">
                                <div class="spinner"></div>
                            </div>
                        </div>`
                    },
                    allDay: true
                }
            ];
        }

        // Per a dies sense dades i que no siguin anteriors a avui, generem dos events: un de background i un de block
        else if (!hasDades && !hasError) {
            return [
                // Event de background per donar color a la cel·la
                {
                    title: hasEmptyDades ? t($ => $.calendari.dades_buides) : t($ => $.calendari.sense_dades),
                    date,
                    backgroundColor: hasEmptyDades ? '#f6af2a' : '#79b2ef',
                    extendedProps: {
                        esDisponible: false,
                        hasError: false,
                        isLoading: false
                    },
                    allDay: true,
                    display: 'background'
                },
                // Event de block amb l'enllaç per obtenir dades
                {
                    title: t($ => $.calendari.obtenir_dades),
                    date,
                    classNames: ['cal-event-download'],
                    textColor: '#888',
                    backgroundColor: '#fff',
                    extendedProps: {
                        esDisponible: false,
                        hasError: false,
                        isLoading: false,
                        hasContent: true,
                        content: `<div class="material-button-container">
                            <button class="material-download-button" title="${t($ => $.calendari.obtenir_dades_tooltip)}">
                                <span class="material-icon">download</span>
                                <span>${t($ => $.calendari.obtenir_dades)}</span>
                            </button>
                        </div>`
                    },
                    allDay: true
                }
            ];
        } else {
            // Per a dies amb dades o amb errors, mantenim un sol event
            return [
                // Event de background per donar color a la cel·la
                {
                    title: hasDades ? '' : t($ => $.calendari.error_dades),
                    date,
                    backgroundColor: hasDades ? '#b7ecaf' : '#dc7352',
                    extendedProps: {
                        esDisponible: hasDades,
                        hasError: hasError,
                        isLoading: false
                    },
                    allDay: true,
                    display: 'background'
                },
                {
                    title: hasDades ? t($ => $.calendari.dades_disponibles) : t($ => $.calendari.error_dades),
                    date,
                    backgroundColor: hasDades ? '#e9f9e6' : '#dc7352',
                    borerColor: hasDades ? '#b7ecaf' : '#dc7352',
                    textColor: '#fff',
                    extendedProps: {
                        esDisponible: hasDades,
                        hasError: hasError,
                        isLoading: false,
                        hasContent: true,
                        content: hasError
                            ? `<div class="material-button-container">
                                <button class="material-error-button" title="${t($ => $.calendari.error_dades_tooltip)}">
                                    <span class="material-icon">error</span>
                                    <span>${t($ => $.calendari.error_dades)}</span>
                                </button>
                              </div>`
                            : `<div class="material-button-container">
                                <button class="material-success-button" title="${t($ => $.calendari.dades_disponibles_tooltip)}">
                                    <span class="material-icon">check_circle</span>
                                    <span>${t($ => $.calendari.dades_disponibles)}</span>
                                </button>
                              </div>`
                    },
                    allDay: true,
                }
            ];
        }
    }).flat();

    const handleEventClick = (info: any) => {
        // Si no hi ha un entornApp seleccionat, no fem res
        if (entornAppId === '') {
            return;
        }
        
        const data = info.event.startStr;
        
        // Si l'event té display 'background', no fem res (només pels events sense dades)
        // Això permet que només l'event de tipus block respongui als clics
        if (info.event.display === 'background' && !info.event.extendedProps.esDisponible && !info.event.extendedProps.hasError) {
            return;
        }
        
        // Si la cel·la ja està carregant, no fem res
        if (info.event.extendedProps.isLoading) {
            console.log('La cel·la ja està carregant, no fem res');
            return;
        }
        
        // Si ja hi ha una càrrega global en curs, no fem res
        if (globalLoading) {
            console.log('Hi ha una càrrega global en curs, no fem res');
            return;
        }

        if (info.event.extendedProps.hasError) {
            // Mostrar l'error
            const errorInfo = getErrorForDate(data);
            if (errorInfo) {
                handleErrorClick(errorInfo);
            }
        } else if (info.event.extendedProps.esDisponible) {
            // Mostrar les dades disponibles del dia
            const dataFormatada = dayjs(data).format('YYYY-MM-DD');
            obtenirDadesDia(entornAppId as number, dataFormatada);
        } else if (!info.event.extendedProps.esDisponible) {
            // Obtenir dades estadístiques
            obtenirDadesEstadistiques(data);
        }
    };

    const {isReady: dimensioIsReady, find: dimensioFind} = useResourceApiService('dimensio')
    const [dimensions, setDimensions] = useState<any[]>([])
    useEffect(()=>{
        if (dimensioIsReady && entornAppId) {
            dimensioFind({unpaged: true, filter: builder.eq("entornAppId", entornAppId)})
                .then((data) => setDimensions(data.rows))
                .catch(() => setDimensions([]))
        }
    },[dimensioIsReady, entornAppId])

    const {isReady: indicadorIsReady, find: indicadorFind} = useResourceApiService('indicador')
    const [indicadors, setIndicadors] = useState<any[]>([])
    useEffect(()=>{
        if (indicadorIsReady && entornAppId) {
            indicadorFind({unpaged: true, filter: builder.eq("entornAppId", entornAppId)})
                .then((data) => setIndicadors(data.rows))
                .catch(() => setIndicadors([]))
        }
    },[indicadorIsReady, entornAppId])

    return (
        <GridPage disableMargins>
            {/* Global loading overlay */}
            {globalLoading && (
                <Box className="global-loading-overlay">
                    <Box className="global-loading-container">
                        <CircularProgress size={60} />
                        <Typography sx={{ mt: 2 }}>{t($ => $.calendari.carregant_dades)}</Typography>
                    </Box>
                </Box>
            )}
            <Toolbar
                title={t($ => $.menu.calendari)} upperToolbar
                elementsWithPositions={[
                    {
                        position: 2,
                        element: <FormControl sx={{ minWidth: 250 }}>
                            <InputLabel size={"small"} id="entorn-app-select-label">{t($ => $.calendari.seleccionar_entorn_app)}</InputLabel>
                            <Select
                                labelId="entorn-app-select-label"
                                value={entornAppId}
                                size={"small"}
                                label={t($ => $.calendari.seleccionar_entorn_app)}
                                onChange={(e) => {
                                    const newEntornAppId = e.target.value as number | '';
                                    setEntornAppId(newEntornAppId);
                                    // Netejar les dates disponibles quan canvia l'entorn
                                    setDatesAmbDades([]);
                                    // Si s'ha seleccionat un entorn, carregar les dates disponibles
                                    if (newEntornAppId !== '') {
                                        obtenirDatesDisponibles(newEntornAppId);
                                    }
                                }}
                            >
                                <MenuItem value="">{t($ => $.calendari.seleccionar)}</MenuItem>
                                {entornApps.map((entornApp) => (
                                    <MenuItem key={entornApp.id} value={entornApp.id}>
                                        {entornApp.app.description} - {entornApp.entorn.description}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    }
                ]}
            />
            {/* Sempre mostrem el calendari, però amb un missatge d'ajuda si no hi ha entorn seleccionat */}
            <Box
                sx={{
                    margin: '16px 24px',
                    height: '100%',
                    '& .fc-header-toolbar': {
                        display: 'flex !important',
                        justifyContent: 'space-between !important',
                        alignItems: 'center',
                    },
                    /* Alinea el texto del mes en el centro */
                    '& .fc-toolbar-title': {
                        textAlign: 'center',
                    },
                    /* Asegurar que cada chunk se alinee correctamente */
                    '& .fc-toolbar-chunk': {
                        display: 'flex',
                        alignItems: 'center',
                        alignSelf: 'center',
                    },
                    /* Cuando la pantalla sea estrecha */
                    '@media (max-width: 768px)': {
                        '.fc-header-toolbar': {
                            flexDirection: 'column !important',
                            alignItems: 'stretch', /* para que ocupen todo el ancho */
                            gap: '8px', /* separación vertical */
                        },
                        '.fc-toolbar-chunk': {
                            justifyContent: 'center', /* opcional, centra cada bloque */
                        }
                    }
                }}
            >
                <FullCalendar
                    locale={currentLanguage}
                    firstDay={1}
                    plugins={[dayGridPlugin, interactionPlugin]}
                    themeSystem={'cosmo'}
                    initialView="dayGridMonth"
                    height={'100%'}
                    selectable
                    dayMaxEventRows={1}
                    // events={(entornAppId !== '' && !datesDisponiblesError && datesAmbDades.length > 0) ? events.filter(event => event !== null) : []}
                    events={(entornAppId !== '' && !datesDisponiblesError) ? events.filter(event => event !== null) : []}
                    eventClick={handleEventClick}
                    eventContent={(arg) => {
                        //If the event has hasContent true, render StatusButton
                        if (arg.event.extendedProps.hasContent) {
                            const container = document.createElement('div');
                            container.style.position = "absolute";
                            container.style.bottom = "-20px";
                            container.style.width = "100%";
                            container.style.height = "100%";

                            const root = ReactDOM.createRoot(container);
                            root.render(<CalendarStatusButton
                                hasError={arg.event.extendedProps.hasError}
                                esDisponible={arg.event.extendedProps.esDisponible}
                                isLoading={arg.event.extendedProps.isLoading}
                            />);
                            return { domNodes: [container] };
                        }
                        //If the event has HTML content, render it
                        if (arg.event.extendedProps.content) {
                            const htmlContent = document.createElement('div');
                            htmlContent.innerHTML = arg.event.extendedProps.content;
                            return { domNodes: [htmlContent] };
                        }
                        // For background events without content, display the title
                        if (arg.event.display === 'background') {
                            const titleElement = document.createElement('div');
                            titleElement.innerHTML = arg.event.title;
                            titleElement.className = 'fc-event-title';
                            return { domNodes: [titleElement] };
                        }
                        // Otherwise, use the default rendering
                        return null;
                    }}
                    dayCellDidMount={(info) => {
                        // Only apply background colors if an entornApp is selected and no error in dates
                        if (entornAppId !== '' && !datesDisponiblesError) {
                            const date = dayjs(info.date).format('YYYY-MM-DD');
                            const hasError = esDiaAmbError(date);
                            const hasDades = esDiaAmbDades(date);
                        }
                    }}
                    datesSet={(dateInfo) => {
                        // When month changes, update the current view month and year
                        const startDate = dayjs(dateInfo.start);
                        setCurrentViewMonth(startDate.date() == 1 ? startDate.month() : startDate.month() + 1);
                        setCurrentViewYear(startDate.month() == 11 && startDate.date() > 1 ? startDate.year() + 1 : startDate.year());
                        console.log('Current view month:', currentViewMonth, 'Current view year:', currentViewYear);
                        console.log('Start date:', startDate);
                        console.log(dateInfo);
                        
                        // // Refresh the dates with data if an entornApp is selected
                        // if (entornAppId !== '') {
                        //     obtenirDatesDisponibles(entornAppId);
                        // }
                    }}
                    customButtons={{
                        intervalButton: {
                            text: t($ => $.calendari.carregar_interval),
                            click: () => {
                                if (entornAppId === '') {
                                    alert(t($ => $.calendari.seleccionar_entorn_app_primer));
                                    return;
                                }

                                setObrirDialog(true);
                            }
                        },
                        monthButton: {
                            text: t($ => $.calendari.carregar_mes_actual),
                            click: () => {
                                if (entornAppId === '') {
                                    alert(t($ => $.calendari.seleccionar_entorn_app_primer));
                                    return;
                                }
                                
                                // Get the dates using currentViewMonth and currentViewYear
                                const dates = getFirstAndLastDayOfMonth();
                                carregarIntervalDades(dates.firstDay, dates.lastDay);
                            }
                        }
                    }}
                    buttonText={{
                        today: t($ => $.calendari.today),
                    }}
                    headerToolbar={{
                        start: 'prev,next today',
                        center: 'title',
                        end: 'monthButton intervalButton'
                    }}
                />
            </Box>
            {/* Dialog per carregar dades per interval */}
            <Dialog open={obrirDialog} onClose={() => setObrirDialog(false)}>
                <DialogTitle>{t($ => $.calendari.carregar_interval)}</DialogTitle>
                <DialogContent>
                    <Box className="date-picker-container">
                        <DatePicker
                            label={t($ => $.calendari.data_inici)}
                            value={dataInici}
                            onChange={(newValue) => setDataInici(newValue)}
                            renderInput={(params) => <TextField {...params} fullWidth />}
                        />
                        <DatePicker
                            label={t($ => $.calendari.data_fi)}
                            value={dataFi}
                            onChange={(newValue) => setDataFi(newValue)}
                            renderInput={(params) => <TextField {...params} fullWidth />}
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setObrirDialog(false)}>{t($ => $.calendari.cancelar)}</Button>
                    <Button
                        onClick={() => {
                            if (dataInici && dataFi) {
                                const iniciStr = dayjs(dataInici).format('YYYY-MM-DD');
                                const fiStr = dayjs(dataFi).format('YYYY-MM-DD');
                                carregarIntervalDades(iniciStr, fiStr);
                            }
                            setObrirDialog(false);
                        }}
                        variant="contained"
                        disabled={!dataInici || !dataFi}
                    >
                        {t($ => $.calendari.carregar)}
                    </Button>
                </DialogActions>
            </Dialog>
            {/* Dialog per mostrar errors */}
            <Dialog open={errorDialogOpen} onClose={() => setErrorDialogOpen(false)} maxWidth="md" fullWidth>
                <DialogTitle className="error-dialog-title">
                    <ErrorIcon color="error" />
                    {t($ => $.calendari.error_titol)}
                </DialogTitle>
                <DialogContent>
                    {currentError && (
                        <>
                            <Typography variant="subtitle1" gutterBottom>
                                <strong>{t($ => $.calendari.data)}:</strong> {dayjs(currentError.date).format('DD/MM/YYYY')}
                            </Typography>
                            <Typography variant="subtitle1" gutterBottom>
                                <strong>{t($ => $.calendari.missatge)}:</strong> {currentError.message}
                            </Typography>
                            {currentError.trace && (
                                <Box sx={{ mt: 2 }}>
                                    <Typography variant="subtitle1" gutterBottom>
                                        <strong>{t($ => $.calendari.traca)}:</strong>
                                    </Typography>
                                    <Box className="error-trace-container">
                                        <pre>{currentError.trace}</pre>
                                    </Box>
                                </Box>
                            )}
                        </>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setErrorDialogOpen(false)}>{t($ => $.calendari.tancar)}</Button>
                </DialogActions>
            </Dialog>
            <CaliendariDadesDialog
                dimensions={dimensions}
                indicadors={indicadors}
                currentDadesDia={currentDadesDia}
                currentDataDia={currentDataDia}
                dadesDiaModalOpen={dadesDiaModalOpen}
                setDadesDiaModalOpen={setDadesDiaModalOpen}
            />
            {component}
        </GridPage>
    );
};

const CaliendariDadesDialog = (props:any) => {
    const { dimensions, indicadors, currentDadesDia, currentDataDia, dadesDiaModalOpen, setDadesDiaModalOpen } = props;
    const { t } = useTranslation();

    const dimensionsCodis = dimensions.map((i:any)=>i.codi);
    const indicadorsCodis = indicadors.map((i:any)=>i.codi);

    const [indicadorsShow, setIndicadorsShow] = useState<any[]>(indicadorsCodis)
    const [open, setOpen] = useState<boolean>(true)
    const [filterForm, setFilterForm] = useState<any>({})
    const currentDadesDiaFiltered = useMemo<DadesDia[]>(()=>{
        if (!filterForm) return currentDadesDia
        return currentDadesDia.filter((currentDada: DadesDia) =>
            Object.entries(filterForm).every(([key, value]) =>
                currentDada.dimensionsJson?.[key]?.toLowerCase?.().includes?.(value?.toLowerCase?.())
            )
        );
    },[filterForm])

    useEffect(() => {
        setFilterForm({})
        if (currentDadesDia[0]?.indicadorsJson)
            setIndicadorsShow(Object.keys(currentDadesDia[0]?.indicadorsJson))
    }, [currentDadesDia]);

    {/* Dialog per mostrar les dades del dia */}
    return (
        <Dialog
            open={dadesDiaModalOpen}
            onClose={() => setDadesDiaModalOpen(false)}
            maxWidth="xl"
            fullWidth
            fullScreen
        >
            <DialogTitle>
                {t($ => $.calendari.modal_dades_dia)} - {dayjs(currentDataDia).format('DD/MM/YYYY')}
            </DialogTitle>
            <DialogContent>
                {currentDadesDia.length > 0 ? (<>
                    <FormGroup>
                        <Grid container spacing={1} p={1} sx={{ maxWidth: '100%' }}>
                            <Grid size={11}>
                                <FormControl sx={{ width: '100%' }} size={'small'}>
                                    <InputLabel>{t($ => $.calendari.indicadors)}</InputLabel>
                                    <Select
                                        multiple
                                        value={indicadorsShow}
                                        onChange={(event) => {
                                            const value = event.target.value
                                            if (value.includes("all")) {
                                                if (indicadorsShow.length === indicadorsCodis.length) {
                                                    // Si ya todos están seleccionados → desmarcar todos
                                                    setIndicadorsShow([]);
                                                } else {
                                                    // Seleccionar todos
                                                    setIndicadorsShow(indicadorsCodis);
                                                }
                                            } else {
                                                setIndicadorsShow(value);
                                            }
                                        }}
                                        input={<OutlinedInput label={t($ => $.calendari.indicadors)}/>}
                                        renderValue={(selected) => selected.join(', ')}
                                    >
                                        {/* Opción select all */}
                                        <MenuItem key="all" value="all">
                                            <Checkbox
                                                checked={indicadorsShow.length === indicadorsCodis.length}
                                                indeterminate={
                                                    indicadorsShow.length > 0 &&
                                                    indicadorsShow.length < indicadorsCodis.length
                                                }
                                            />
                                            <ListItemText primary="Seleccionar todo"/>
                                        </MenuItem>

                                        {indicadors.map((indicador:any) => (
                                            <MenuItem key={indicador.codi} value={indicador.codi}>
                                                <Checkbox checked={indicadorsShow.includes(indicador.codi)}/>
                                                <ListItemText primary={indicador.nom}/>
                                            </MenuItem>
                                        ))}
                                    </Select>
                                </FormControl>
                            </Grid>

                            <Grid size={1} display={'flex'} justifyContent={'center'}>
                                <IconButton
                                    title={t($ => $.components.clear)}
                                    onClick={() => {
                                        setIndicadorsShow([]);
                                        setFilterForm({});
                                    }}
                                ><Icon>filter_alt_off</Icon></IconButton>
                                <IconButton
                                    title={t($ => $.page.avisos.filter.more)}
                                    onClick={() => setOpen((prev) => !prev)}
                                ><Icon>filter_list</Icon></IconButton>
                            </Grid>

                            {dimensions.map((dimension:any) => (
                                <Grid size={3} hidden={open}>
                                    <TextField id={`textField-${dimension.codi}`}
                                               label={dimension.nom}
                                               variant="outlined"
                                               value={filterForm[dimension.codi] || ""}
                                               size={'small'}
                                               fullWidth
                                               onChange={(event)=>{
                                                   setFilterForm({
                                                       ...filterForm,
                                                       [dimension.codi]: event.target.value
                                                   })
                                               }}/>
                                </Grid>
                            ))}
                        </Grid>
                    </FormGroup>

                    <TableContainer component={Paper} sx={{ maxHeight: 'calc(95vh - 200px)' }}>
                        <Table stickyHeader aria-label={t($ => $.calendari.modal_dades_dia)}>
                            <TableHead>
                                <TableRow>
                                    <TableCell colSpan={dimensionsCodis.length}>
                                        <Typography variant="subtitle1" fontWeight="bold">
                                            {t($ => $.calendari.dimensions)}
                                        </Typography>
                                    </TableCell>
                                    {!!indicadorsShow.length && <TableCell colSpan={indicadorsShow.length}>
                                        <Typography variant="subtitle1" fontWeight="bold">
                                            {t($ => $.calendari.indicadors)}
                                        </Typography>
                                    </TableCell>}
                                </TableRow>
                                <TableRow>
                                    {/* Dimensions column headers */}
                                    {dimensions.map((dimensio:any) => (
                                        <TableCell key={`dim-${dimensio.codi}`} title={dimensio.descripcio}>
                                            {dimensio.nom}
                                        </TableCell>
                                    ))}

                                    {/* Indicators column headers */}
                                    {indicadors.map((indicator:any) => {
                                        if (indicadorsShow.includes(indicator.codi)) {
                                            return <TableCell key={`ind-${indicator.codi}`}
                                                              align="right"
                                                              title={indicator.descripcio}>
                                                {indicator.nom}
                                            </TableCell>
                                        }
                                    })}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {currentDadesDiaFiltered.map((fet, index) => (
                                    <TableRow key={index} sx={{ backgroundColor: index % 2 === 0 ? "background.default" : "grey.50" }}>
                                        {/* Dimensions values */}
                                        {dimensionsCodis.map((key:any, i:number) => (
                                            <TableCell key={`dim-val-${index}-${i}`}>
                                                {fet.dimensionsJson[key]}
                                            </TableCell>
                                        ))}

                                        {/* Indicators values */}
                                        {indicadorsCodis.map((key:any, i:number) => {
                                            if (indicadorsShow.includes(key)) {
                                                return <TableCell key={`ind-val-${index}-${i}`} align="right">
                                                    {fet.indicadorsJson[key]}
                                                </TableCell>
                                            }})
                                        }
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </>) : (
                    <Typography variant="body1">
                        {t($ => $.calendari.sense_dades)}
                    </Typography>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={() => setDadesDiaModalOpen(false)}>{t($ => $.calendari.tancar)}</Button>
            </DialogActions>
        </Dialog>
    );
}

export default CalendariEstadistiques;
