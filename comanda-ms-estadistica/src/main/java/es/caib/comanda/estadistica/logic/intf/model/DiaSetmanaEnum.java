package es.caib.comanda.estadistica.logic.intf.model;

import java.time.DayOfWeek;

/**
 * Enum que representa els dies de la setmana en format abreujat en català.
 *
 * Aquesta enumeració defineix els següents valors:
 * - DL: Dilluns
 * - DM: Dimarts
 * - DC: Dimecres
 * - DJ: Dijous
 * - DV: Divendres
 * - DS: Dissabte
 * - DG: Diumenge
 *
 * També proporciona una funcionalitat per obtenir el valor corresponent de l'enumeració basant-se en un objecte
 * de tipus DayOfWeek utilitzant el mètode estàtic `valueOfDayOfWeek(DayOfWeek)`.
 *
 * @author Límit Tecnologies
 */
public enum DiaSetmanaEnum {
    DL,
    DM,
    DC,
    DJ,
    DV,
    DS,
    DG;

    public static DiaSetmanaEnum valueOfDayOfWeek(DayOfWeek dayOfWeek) {
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
