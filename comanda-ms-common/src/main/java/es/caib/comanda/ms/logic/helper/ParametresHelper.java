package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.client.ParametreServiceClient;
import es.caib.comanda.client.model.ParamTipus;
import es.caib.comanda.client.model.Parametre;
import es.caib.comanda.ms.logic.intf.exception.ParametreTipusException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import static es.caib.comanda.ms.back.config.HazelCastCacheConfig.PARAMETRE_CACHE;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParametresHelper {

    private final ParametreServiceClient parametreServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Lazy
    private final ParametresHelper self = this;



    @Cacheable(value = PARAMETRE_CACHE, key = "#codi")
    public Parametre perametreFindByCodi(String codi) { //, String defaultValue) {
        PagedModel<EntityModel<Parametre>> parametres = parametreServiceClient.find(
                null,
                "codi:\'" + codi + "\'",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        if (parametres == null || parametres.getContent().isEmpty()) {
            return null;
        }
        return parametres.getContent().stream()
                .findFirst().orElseThrow(() -> new ResourceNotFoundException(Parametre.class, "codi:" + codi)).getContent();
    }

    public Parametre perametreFindByCodi(String codi, String defaultValue) {
        // Utilitzam self per que passi pel proxy y funcioni la caché
        Parametre parametre = self.perametreFindByCodi(codi);
        if (parametre == null) {
            return Parametre.builder().codi(codi).valor(defaultValue).build();
        }
        return parametre;
    }

    public Double getParametreNumeric(String codi) {
        Parametre parametre = self.perametreFindByCodi(codi);
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
        Parametre parametre = self.perametreFindByCodi(codi);
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
        Parametre parametre = self.perametreFindByCodi(codi);
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
        Parametre parametre = self.perametreFindByCodi(codi);
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
