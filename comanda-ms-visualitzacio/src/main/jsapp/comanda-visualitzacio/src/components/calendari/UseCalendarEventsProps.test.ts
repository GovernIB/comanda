import { renderHook } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { useCalendarEvents } from './UseCalendarEventsProps';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            selector({
                calendari: {
                    carregant: 'Carregant',
                    dades_buides: 'Dades buides',
                    sense_dades: 'Sense dades',
                    obtenir_dades: 'Obtenir dades',
                    error_dades: 'Error dades',
                    dades_disponibles: 'Dades disponibles',
                    processant_segon_pla: 'En segon pla...',
                },
            }),
    }),
}));

describe('useCalendarEvents', () => {
    it('useCalendarEvents_quanNoHiHaEntornSeleccionat_noRetornaEsdeveniments', () => {
        // Comprova que el hook no crea esdeveniments si encara no hi ha entorn seleccionat.
        const { result } = renderHook(() =>
            useCalendarEvents({
                currentViewMonth: 0,
                currentViewYear: 2026,
                entornAppId: '',
                datesAmbDades: [],
                emptyDates: [],
                loadingDates: [],
                errors: [],
                datesDisponiblesError: false,
            })
        );

        expect(result.current).toEqual([]);
    });

    it('useCalendarEvents_quanUnaDataCarrega_retornaElsDosEventsDeLoading', () => {
        // Verifica que una data en càrrega genera tant el fons com el contingut visual de loading.
        const { result } = renderHook(() =>
            useCalendarEvents({
                currentViewMonth: 0,
                currentViewYear: 2025,
                entornAppId: 7,
                datesAmbDades: [],
                emptyDates: [],
                loadingDates: ['2025-01-10'],
                errors: [],
                datesDisponiblesError: false,
            })
        );

        const events = result.current.filter(event => event.date === '2025-01-10');
        expect(events).toHaveLength(2);
        expect(events[0]).toEqual(expect.objectContaining({ title: 'Carregant', display: 'background' }));
        expect(events[1]).toEqual(expect.objectContaining({ classNames: ['cal-event-loading'] }));
    });

    it('useCalendarEvents_quanUnaDataTeDades_retornaElsEventsDisponibles', () => {
        // Comprova que una data amb dades disponibles es marca com a disponible i sense error.
        const { result } = renderHook(() =>
            useCalendarEvents({
                currentViewMonth: 0,
                currentViewYear: 2025,
                entornAppId: 7,
                datesAmbDades: ['2025-01-12'],
                emptyDates: [],
                loadingDates: [],
                errors: [],
                datesDisponiblesError: false,
            })
        );

        const events = result.current.filter(event => event.date === '2025-01-12');
        expect(events).toHaveLength(2);
        expect(events[0]).toEqual(
            expect.objectContaining({
                extendedProps: expect.objectContaining({ esDisponible: true, hasError: false }),
            })
        );
        expect(events[1]).toEqual(expect.objectContaining({ title: 'Dades disponibles' }));
    });

    it('useCalendarEvents_quanUnaDataNoTeDadesNiError_mostraLOpcioDObtenirDades', () => {
        // Verifica que una data sense dades genera l'estat buit i l'acció de descàrrega.
        const { result } = renderHook(() =>
            useCalendarEvents({
                currentViewMonth: 0,
                currentViewYear: 2025,
                entornAppId: 7,
                datesAmbDades: [],
                emptyDates: [],
                loadingDates: [],
                errors: [],
                datesDisponiblesError: false,
            })
        );

        const events = result.current.filter(event => event.date === '2025-01-05');
        expect(events).toHaveLength(2);
        expect(events[0]).toEqual(expect.objectContaining({ title: 'Sense dades' }));
        expect(events[1]).toEqual(expect.objectContaining({ title: 'Obtenir dades' }));
    });

    it('useCalendarEvents_quanUnaDataEstaEnProcessamentEnSegonPla_retornaElsEventsDeSegonPla', () => {
        // Verifica que una data en procés de baixa prioritat genera l'estat visual en segon pla.
        const { result } = renderHook(() =>
            useCalendarEvents({
                currentViewMonth: 0,
                currentViewYear: 2025,
                entornAppId: 7,
                datesAmbDades: [],
                emptyDates: [],
                loadingDates: [],
                backgroundProcessingDates: ['2025-01-15'],
                errors: [],
                datesDisponiblesError: false,
            })
        );

        const events = result.current.filter(event => event.date === '2025-01-15');
        expect(events).toHaveLength(2);
        expect(events[0]).toEqual(
            expect.objectContaining({
                title: 'En segon pla...',
                display: 'background',
                backgroundColor: '#fff8e1',
                extendedProps: expect.objectContaining({ isBackgroundProcessing: true }),
            })
        );
        expect(events[1]).toEqual(
            expect.objectContaining({
                title: 'En segon pla...',
                classNames: ['cal-event-background-processing'],
                textColor: '#ed6c02',
                extendedProps: expect.objectContaining({ isBackgroundProcessing: true, hasContent: true }),
            })
        );
    });

    it('useCalendarEvents_quanUnaDataTeDadesEncaraQueEstiguiEnSegonPla_prioritzaDadesDisponibles', () => {
        // Verifica que si ja té dades disponibles, manté l'estat de disponible per poder consultar-les.
        const { result } = renderHook(() =>
            useCalendarEvents({
                currentViewMonth: 0,
                currentViewYear: 2025,
                entornAppId: 7,
                datesAmbDades: ['2025-01-15'],
                emptyDates: [],
                loadingDates: [],
                backgroundProcessingDates: ['2025-01-15'],
                errors: [],
                datesDisponiblesError: false,
            })
        );

        const events = result.current.filter(event => event.date === '2025-01-15');
        expect(events).toHaveLength(2);
        expect(events[0]).toEqual(
            expect.objectContaining({
                extendedProps: expect.objectContaining({ esDisponible: true, isBackgroundProcessing: false }),
            })
        );
        expect(events[1]).toEqual(
            expect.objectContaining({
                title: 'Dades disponibles',
                extendedProps: expect.objectContaining({ esDisponible: true, isBackgroundProcessing: false }),
            })
        );
    });
});
