package es.caib.comanda.ms.estadistica.model;

import java.time.DayOfWeek;

public enum DiaSetmanaEnum {
    DL,
    DM,
    DC,
    DJ,
    DV,
    DS,
    DG;

    public static DiaSetmanaEnum valueOf(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return DL;
            case TUESDAY: return DM;
            case WEDNESDAY: return DC;
            case THURSDAY: return DJ;
            case FRIDAY: return DV;
            case SATURDAY: return DS;
            case SUNDAY: return DG;
            default: return null;
        }
    }
}
