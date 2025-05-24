package es.caib.comanda.estadistica.logic.intf.model.periode;

public enum PresetPeriode {
    // Rolling (intervals d’un nombre fix d’unitats acabats “avui”)
    DARRERS_7_DIES,
    DARRERS_14_DIES,
    DARRERS_30_DIES,
    DARRERS_90_DIES,
    DARRERS_180_DIES,
    DARRERS_365_DIES,
    DARRERES_4_SETMANES,
    DARRERES_12_SETMANES,
    DARRERES_52_SETMANES,
    DARRERS_3_MESOS,
    DARRERS_6_MESOS,
    DARRERS_12_MESOS,
    DARRERS_4_TRIMESTRES,
    DARRER_1_ANY,
    DARRERS_2_ANYS,
    DARRERS_5_ANYS,
    // Períodes “complets” (totalment tancats abans de l’actual)
    DARRER_COMPLET_DIA,
    DARRERA_COMPLETA_SETMANA,
    DARRER_COMPLET_MES,
    DARRER_COMPLET_TRIMESTRE,
    DARRER_COMPLET_ANY,
    // “To Date” (fins al moment actual dins del període)
    AVUI,
    AHIR,
    AQUESTA_SETMANA_FINS_ARA,
    AQUEST_MES_FINS_ARA,
    AQUEST_TRIMESTRE_FINS_ARA,
    AQUEST_ANY_FINS_ARA,
    // Genèrics “LAST_N_…” (períodes rolling però amb N a definir per l’usuari)
    DARRERS_N_DIES,
    DARRERES_N_SETMANES,
    DARRERS_N_MESOS,
    DARRERS_N_TRIMESTRES,
    DARRERS_N_ANYS
}
