package es.caib.comanda.usuaris.logic.intf.model;

public enum NumOfElementsPerPageENum {
    AUTOMATIC,
    _10,
    _20,
    _50,
    _100,
    _200;

    public static NumOfElementsPerPageENum byDefault() {
        return AUTOMATIC;
    }
}
