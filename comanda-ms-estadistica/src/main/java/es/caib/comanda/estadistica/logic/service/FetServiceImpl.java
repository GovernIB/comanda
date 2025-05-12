package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.estadistica.logic.intf.service.FetService;
import es.caib.comanda.estadistica.persist.entity.FetEntity;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Implementació de la interfície FetService que ofereix funcionalitats per consultar estadístiques i gestionar dades
 * relacionades amb els objectes Fet.
 * Aquesta classe interactua amb clients externs per obtenir informació aplicativa i estadística, garantint la seguretat
 * mitjançant l'ús de Keycloak per generar tokens d'autorització.
 * Gestiona consultes personalitzades d'estadístiques segons períodes i dimensions.
 *
 * Principals responsabilitats:
 * - Consultar les estadístiques d'aplicacions periòdiques i gestionar errors possibles durant el procés.
 * - Migrar dades relacionades amb l'estadística d'un entorn específic.
 * - Obtenir i filtrar objectes estatístics (Fet) segons períodes i dimensions aplicades.
 *
 * La classe estèn BaseReadonlyResourceService per oferir funcionalitats bàsiques de gestió de recursos en mode
 * "només lectura" i implementa el servei específic FetService.
 *
 * Dependències destacades:
 * - AppServiceClient i EntornAppServiceClient per consultar entitats remotes de tipus App i EntornApp.
 * - EstadisticaHelper per accedir als serveis auxiliars relacionats amb la gestió estadística.
 * - KeycloakHelper per autenticació, assegurant l'accés autoritzat als serveis.
 *
 * Requereix configuració de propietats per a l'autenticació de Keycloak: `keycloak.username` i `keycloak.password`.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class FetServiceImpl extends BaseReadonlyResourceService<Fet, Long, FetEntity> implements FetService {

//    @Autowired
//    private AppServiceClient appServiceClient;
    @Autowired
    private KeycloakHelper keycloakHelper;
    @Autowired
    private EstadisticaHelper estadisticaHelper;
    @Autowired
    private EntornAppServiceClient entornAppServiceClient;

    private EntornApp entornAppFindById(Long entornAppId) {
        EntityModel<EntornApp> entornApp = entornAppServiceClient.getOne(
                entornAppId,
                null,
                keycloakHelper.getAuthorizationHeader());
        if (entornApp != null) {
            return entornApp.getContent();
        }
        return null;
    }

    @Override
    public void migrarDades(Long entornAppId) { //, LocalDate data) {
        try {
            log.info("Migració de dades manual de ahir per entornAppId: {}", entornAppId);
            EntornApp entornApp = entornAppFindById(entornAppId);
            estadisticaHelper.getEstadisticaInfoDades(entornApp);
        } catch (Exception e) {
            log.error("Error en la migració de dades", e);
            throw e;
        }
    }

    /**
     * Obté una llista d'objectes de tipus Fet que representen les estadístiques de l'aplicació per a un període determinat.
     *
     * @param entornAppId l'identificador de l'entorn aplicatiu per al qual es desitja obtenir les estadístiques
     * @param dataInici la data d'inici del període per al qual es realitza la consulta
     * @param dataFi la data de finalització del període per al qual es realitza la consulta
     * @return una llista d'objectes Fet que contenen les estadístiques recopilades per al període especificat
     */
    @Override
    public List<Fet> getEstadistiquesPeriode(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi) {
//            NivellAgrupacio nivellAgrupacio) {

        return estadisticaHelper.getEstadistiquesPeriode(
                entornAppId,
                dataInici,
                dataFi);
    }

    /**
     * Retorna una llista de fets estadístics per un període de temps especificat i aplicant filtres de dimensions si cal.
     *
     * @param entornAppId Identificador de l'entorn de l'aplicació per al qual es volen obtenir estadístiques.
     * @param dataInici Data d'inici del període de les estadístiques.
     * @param dataFi Data de finalització del període de les estadístiques.
     * @param dimensionsFiltre Map amb les dimensions i els seus valors a utilitzar com a filtre.
     * @return Una llista de fets (List<Fet>) que corresponen a les estadístiques sol·licitades.
     */
    @Override
    public List<Fet> getEstadistiquesPeriodeAmbDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre) {
//            NivellAgrupacio nivellAgrupacio) {

        return estadisticaHelper.getEstadistiquesPeriodeAmbDimensions(
                entornAppId,
                dataInici,
                dataFi,
                dimensionsFiltre);
    }

}
