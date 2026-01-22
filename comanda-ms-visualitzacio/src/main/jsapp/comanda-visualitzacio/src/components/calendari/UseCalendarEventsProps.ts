import { useMemo } from "react";
import dayjs from "dayjs";
import { ErrorInfo } from "./CalendariTypes";
import { useTranslation } from "react-i18next";

interface UseCalendarEventsProps {
  currentViewMonth: number;
  currentViewYear: number;
  entornAppId: number | '';
  datesAmbDades: string[];
  emptyDates: string[];
  loadingDates: string[];
  errors: ErrorInfo[];
  datesDisponiblesError: boolean;
}

export const useCalendarEvents = ({
  currentViewMonth,
  currentViewYear,
  entornAppId,
  datesAmbDades,
  emptyDates,
  loadingDates,
  errors,
  datesDisponiblesError
}: UseCalendarEventsProps) => {
  const { t } = useTranslation();
  return useMemo(() => {
    const daysInMonth = dayjs(`${currentViewYear}-${currentViewMonth + 1}-01`).daysInMonth();

    const events = Array.from({ length: daysInMonth }, (_, i) => {
      const date = dayjs(`${currentViewYear}-${String(currentViewMonth + 1).padStart(2,'0')}-${String(i+1).padStart(2,'0')}`).format('YYYY-MM-DD');
      const hasError = errors.some(e => e.date === date);
      const hasDades = datesAmbDades.includes(date);
      const hasEmptyDades = emptyDates.includes(date);
      const isLoading = loadingDates.includes(date);

      // Si no hi ha un entornApp seleccionat, o hi ha error en dates disponibles, o la data NO és anterior a avui -> no mostrar
      if (entornAppId === '' || datesDisponiblesError || !dayjs(date).isBefore(dayjs(), 'day')) {
        return null;
      }

      // Loading
      if (isLoading) {
        return [
          {
            title: t($ => $.calendari.carregant),
            date,
            backgroundColor: '#f5f5f5',
            extendedProps: { esDisponible: false, hasError: false, isLoading: true },
            allDay: true,
            display: 'background'
          },
          {
            title: t($ => $.calendari.carregant),
            date,
            classNames: ['cal-event-loading'],
            textColor: '#888',
            backgroundColor: '#fff',
            extendedProps: { esDisponible: false, hasError: false, isLoading: true, hasContent: true },
            allDay: true
          }
        ];
      }

      // No data & no error
      if (!hasDades && !hasError) {
        return [
          {
            title: hasEmptyDades ? t($ => $.calendari.dades_buides) : t($ => $.calendari.sense_dades),
            date,
            backgroundColor: hasEmptyDades ? '#f6af2a' : '#79b2ef',
            extendedProps: { esDisponible: false, hasError: false, isLoading: false },
            allDay: true,
            display: 'background'
          },
          {
            title: t($ => $.calendari.obtenir_dades),
            date,
            classNames: ['cal-event-download'],
            textColor: '#888',
            backgroundColor: '#fff',
            extendedProps: { esDisponible: false, hasError: false, isLoading: false, hasContent: true },
            allDay: true
          }
        ];
      }

      // Has data or has error
      return [
        {
          title: hasDades ? '' : t($ => $.calendari.error_dades),
          date,
          backgroundColor: hasDades ? '#b7ecaf' : '#dc7352',
          extendedProps: { esDisponible: hasDades, hasError: hasError, isLoading: false },
          allDay: true,
          display: 'background'
        },
        {
          title: hasDades ? t($ => $.calendari.dades_disponibles) : t($ => $.calendari.error_dades),
          date,
          backgroundColor: hasDades ? '#e9f9e6' : '#dc7352',
          borerColor: hasDades ? '#b7ecaf' : '#dc7352',
          textColor: '#fff',
          extendedProps: { esDisponible: hasDades, hasError: hasError, isLoading: false, hasContent: true },
          allDay: true
        }
      ];
    }).flat();

    // Filtrar nuls para FullCalendar
    return events.filter(e => e !== null);
  }, [
    currentViewMonth,
    currentViewYear,
    entornAppId,
    JSON.stringify(datesAmbDades),
    JSON.stringify(emptyDates),
    JSON.stringify(loadingDates),
    JSON.stringify(errors),
    datesDisponiblesError,
    t
  ]);
};
