package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.client.ParametreServiceClient;
import es.caib.comanda.client.model.Parametre;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.PARAMETRE_CACHE;

/**
 * Client amb caché per obtenir paràmetres del servei de configuració.
 */
@Component
@RequiredArgsConstructor
public class ParametreClientHelper {

    private final ParametreServiceClient parametreServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    /**
     * Cerca un paràmetre pel seu codi i el manté en caché.
     *
     * @param codi codi del paràmetre.
     * @return el paràmetre trobat o {@code null} si no existeix.
     */
    @Cacheable(value = PARAMETRE_CACHE, key = "#codi")
    public Parametre findByCodi(String codi) {
        PagedModel<EntityModel<Parametre>> parametres = parametreServiceClient.find(
                null,
                "codi:'" + codi + "'",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        if (parametres == null || parametres.getContent().isEmpty()) {
            return null;
        }
        return parametres.getContent().stream()
                .findFirst()
                .map(EntityModel::getContent)
                .orElse(null);
    }
}
