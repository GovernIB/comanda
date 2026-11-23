package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.client.model.ParamTipus;
import es.caib.comanda.client.model.Parametre;
import es.caib.comanda.ms.logic.intf.exception.ParametreTipusException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ParametresHelper {

    private final ParametreClientHelper parametreClientHelper;

    public Parametre perametreFindByCodi(String codi) { //, String defaultValue) {
        return parametreClientHelper.findByCodi(codi);
    }

    public Parametre perametreFindByCodi(String codi, String defaultValue) {
        Parametre parametre = perametreFindByCodiSafe(codi);
        if (parametre == null) {
            return Parametre.builder().codi(codi).valor(defaultValue).build();
        }
        return parametre;
    }

    private Parametre perametreFindByCodiSafe(String codi) {
        try {
            return parametreClientHelper.findByCodi(codi);
        } catch (FeignException e) {
            log.warn("No s'ha pogut llegir el paràmetre '{}' del servei de configuració: {}", codi, e.getMessage());
            return null;
        }
    }

    public Double getParametreNumeric(String codi) {
        Parametre parametre = perametreFindByCodiSafe(codi);
        if (parametre == null) {
            return null;
        }
        if (!ParamTipus.NUMERIC.equals(parametre.getTipus())) {
            throw new ParametreTipusException(parametre, ParamTipus.NUMERIC);
        }

        String valor = parametre.getValor() != null ? parametre.getValor().trim() : null;
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            log.error("Error convertint el valor del parametre a numero. Code: {}, Value: {}", codi, valor);
            throw new ParametreTipusException(parametre, ParamTipus.NUMERIC, e);
        }
    }

    public Double getParametreNumeric(String codi, Double defaultValue) {
        Double valor = getParametreNumeric(codi);
        if (valor == null) {
            return defaultValue;
        }
        return valor;
    }

    public Integer getParametreEnter(String codi) {
        Parametre parametre = perametreFindByCodiSafe(codi);
        if (parametre == null) {
            return null;
        }
        if (!ParamTipus.NUMERIC.equals(parametre.getTipus())) {
            throw new ParametreTipusException(parametre, ParamTipus.NUMERIC);
        }

        String valor = parametre.getValor() != null ? parametre.getValor().trim() : null;
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            log.error("Error convertint el valor del parametre a numero. Code: {}, Value: {}", codi, valor);
            throw new ParametreTipusException(parametre, ParamTipus.NUMERIC, e);
        }
    }

    public Integer getParametreEnter(String codi, Integer defaultValue) {
        Integer valor = getParametreEnter(codi);
        if (valor == null) {
            return defaultValue;
        }
        return valor;
    }

    public Boolean getParametreBoolean(String codi) {
        Parametre parametre = perametreFindByCodiSafe(codi);
        if (parametre == null) {
            return null;
        }
        if (!ParamTipus.BOOLEAN.equals(parametre.getTipus())) {
            throw new ParametreTipusException(parametre, ParamTipus.BOOLEAN);
        }

        String valor = parametre.getValor() != null ? parametre.getValor().trim() : null;
        if (valor == null || valor.isEmpty()) {
            return null;
        }
        if (!valor.equalsIgnoreCase("true") && !valor.equalsIgnoreCase("false")) {
            throw new ParametreTipusException(parametre, ParamTipus.BOOLEAN);
        }
        return Boolean.parseBoolean(valor);
    }

    public Boolean getParametreBoolean(String codi, Boolean defaultValue) {
        Boolean valor = getParametreBoolean(codi);
        if (valor == null) {
            return defaultValue;
        }
        return valor;
    }

    public String getParametreText(String codi) {
        Parametre parametre = perametreFindByCodiSafe(codi);
        if (parametre == null || parametre.getValor() == null || parametre.getValor().trim().isEmpty()) {
            return null;
        }
        return parametre.getValor().trim();
    }

    public String getParametreText(String codi, String defaultValue) {
        String valor = getParametreText(codi);
        if (valor == null) {
            return defaultValue;
        }
        return valor;
    }

}
