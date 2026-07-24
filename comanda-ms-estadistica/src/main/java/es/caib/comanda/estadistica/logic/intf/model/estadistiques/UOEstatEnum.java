package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UOEstatEnum {
    VIGENT("V"),
    EXTINGIT("E"),
    ANULAT("A"),
    TRANSITORI("T");

    private String value;

    UOEstatEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static UOEstatEnum fromValue(String value) {
        for (UOEstatEnum b : UOEstatEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
