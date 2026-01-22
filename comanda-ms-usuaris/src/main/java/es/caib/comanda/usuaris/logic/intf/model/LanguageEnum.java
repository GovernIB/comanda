package es.caib.comanda.usuaris.logic.intf.model;

import lombok.Getter;

public enum LanguageEnum {
    ES("es"),
    CA("ca");

    @Getter
    private final String code;

    LanguageEnum(String languageCode) {
        this.code = languageCode;
    }
}
