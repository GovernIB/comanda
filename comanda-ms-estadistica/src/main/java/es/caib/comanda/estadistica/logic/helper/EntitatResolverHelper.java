package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPlugin;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesRestClient;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.EntitatValorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.EntitatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Resol l'entitat (EntitatEntity) associada a un fet i, a partir d'aquesta, la conselleria d'un òrgan gestor.
 * Centralitza la lògica descrita per l'usuari: si el fet té una dimensió de tipus ENTITAT amb valor, es fa servir
 * el codiDir3 d'aquesta entitat com a arrel de l'arbre d'unitats organitzatives; si no, es fa servir l'arrel per
 * defecte (paràmetre {@code es.caib.comanda.estadistica.dir3.govern.codi.arrel}, o el fallback intern si no està
 * configurat - vegeu {@link UnitatsOrganitzativesRestClient#CODI_ARREL_PER_DEFECTE}).
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntitatResolverHelper {

    private final DimensioRepository dimensioRepository;
    private final DimensioValorRepository dimensioValorRepository;
    private final EntitatRepository entitatRepository;
    private final UnitatsOrganitzativesRestClient unitatsOrganitzativesRestClient;
    private final UnitatsOrganitzativesPlugin unitatsOrganitzativesPlugin;

    /** Troba la dimensió de tipus ENTITAT de l'entorn d'aplicació indicat, si n'hi ha. */
    public Optional<DimensioEntity> findDimensioEntitat(Long entornAppId) {
        return dimensioRepository.findByEntornAppIdAndTipus(entornAppId, TipusDimensioEnum.ENTITAT);
    }

    /**
     * Resol l'EntitatEntity corresponent a un valor concret d'una dimensió de tipus ENTITAT.
     * Primer comprova si hi ha una sobreescriptura manual (DimensioValorEntity.entitatMapejada, per als casos en
     * què el valor rebut no es correspon exactament amb cap Entitat definida a Comanda); si no n'hi ha, compara
     * `valor` amb Entitat.codi o Entitat.codiDir3 segons {@code dimensioEntitat.getEntitatValorTipus()}.
     */
    public Optional<EntitatEntity> resolveEntitat(DimensioEntity dimensioEntitat, String valor) {
        if (dimensioEntitat == null || valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        Optional<DimensioValorEntity> dimensioValor = dimensioValorRepository.findByDimensioAndValor(dimensioEntitat, valor);
        if (dimensioValor.isPresent() && dimensioValor.get().getEntitatMapejada() != null) {
            return Optional.of(dimensioValor.get().getEntitatMapejada());
        }
        return EntitatValorTipus.CODI_DIR3.equals(dimensioEntitat.getEntitatValorTipus())
            ? entitatRepository.findByCodiDir3(valor)
            : entitatRepository.findByCodi(valor);
    }

    /**
     * Codi Dir3 que s'ha d'usar com a arrel de l'arbre d'unitats organitzatives: el codiDir3 de l'entitat indicada,
     * o l'arrel per defecte (paràmetre de configuració, o el fallback intern) si no hi ha entitat.
     */
    public String resolveArrelCodi(Optional<EntitatEntity> entitat) {
        return entitat.map(EntitatEntity::getCodiDir3).orElseGet(unitatsOrganitzativesRestClient::getCodiArrel);
    }

    /**
     * Calcula la conselleria d'un òrgan gestor per a un fet concret, tenint en compte la seva entitat (si en té).
     *
     * @param entornAppId l'entorn d'aplicació del fet
     * @param organValor el valor de la dimensió ORGAN_GESTOR en aquest fet
     * @param dimensionsValors totes les dimensions del fet (codi de dimensió -> valor), per poder localitzar el
     *                         valor de la dimensió ENTITAT si n'hi ha
     * @return el codi de conselleria, o null si no s'ha pogut determinar
     */
    public String resolveConselleria(Long entornAppId, String organValor, Map<String, String> dimensionsValors) {
        if (organValor == null) {
            return null;
        }
        Optional<DimensioEntity> dimensioEntitat = findDimensioEntitat(entornAppId);
        Optional<EntitatEntity> entitat = dimensioEntitat
            .map(d -> dimensionsValors != null ? dimensionsValors.get(d.getCodi()) : null)
            .flatMap(valorEntitat -> resolveEntitat(dimensioEntitat.get(), valorEntitat));
        String arrelCodi = resolveArrelCodi(entitat);
        try {
            return unitatsOrganitzativesPlugin.getConselleria(organValor, arrelCodi);
        } catch (Exception e) {
            log.warn("Error calculant la conselleria per a l'òrgan {} amb arrel {}: {}", organValor, arrelCodi, e.getMessage());
            return null;
        }
    }

}
