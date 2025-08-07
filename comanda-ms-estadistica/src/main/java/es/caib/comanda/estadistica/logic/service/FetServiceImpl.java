package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirParamAction;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirResponse;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.estadistica.logic.intf.service.FetService;
import es.caib.comanda.estadistica.logic.mapper.FetMapper;
import es.caib.comanda.estadistica.logic.mapper.TempsMapper;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.TempsRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
@RequiredArgsConstructor
@Service
public class FetServiceImpl extends BaseMutableResourceService<Fet, Long, FetEntity> implements FetService {

    private final EstadisticaHelper estadisticaHelper;
    private final ConsultaEstadisticaHelper consultaEstadisticaHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final TempsRepository tempsRepository;
    private final FetRepository fetRepository;
    private final TempsMapper tempsMapper;
    private final FetMapper fetMapper;

    @PostConstruct
    public void init() {
        register(Fet.FET_REPORT_DATES_DISPONIBLES, new DatesDisponiblesReportGenerator(tempsRepository, tempsMapper));
        register(Fet.FET_REPORT_DADES_DIA, new DadesDiaReportGenerator(fetRepository, fetMapper));
        register(Fet.FET_ACTION_OBTENIR_PER_DATA, new ObtenirPerDataAction(estadisticaClientHelper, estadisticaHelper));
        register(Fet.FET_ACTION_OBTENIR_PER_INTERVAL, new ObtenirPerIntervalAction(estadisticaClientHelper, estadisticaHelper));
    }

    @Override
    public void obtenirFets(Long entornAppId) { //, LocalDate data) {
        try {
            log.info("Migració de dades manual de ahir per entornAppId: {}", entornAppId);
            EntornApp entornApp = estadisticaClientHelper.entornAppFindById(entornAppId);
            estadisticaHelper.getEstadisticaInfoDades(entornApp);
        } catch (Exception e) {
            log.error("Error en la migració de dades", e);
            throw e;
        }
    }

    @Override
    public void obtenirFets(Long entornAppId, int dies) {
        try {
            log.info("Migració de dades manual de ahir per entornAppId: {}", entornAppId);
            EntornApp entornApp = estadisticaClientHelper.entornAppFindById(entornAppId);
            estadisticaHelper.getEstadisticaInfoDades(entornApp, dies);
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

        return consultaEstadisticaHelper.getEstadistiquesPeriode(
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

        return consultaEstadisticaHelper.getEstadistiquesPeriodeAmbDimensions(
                entornAppId,
                dataInici,
                dataFi,
                dimensionsFiltre);
    }




    @RequiredArgsConstructor
    public static class DatesDisponiblesReportGenerator implements ReportGenerator<FetEntity, Long, Temps> {
        private final TempsRepository tempsRepository;
        private final TempsMapper tempsMapper;
        private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        @Override
        public List<Temps> generateData(String code, FetEntity entity, Long entornAppId) throws ReportGenerationException {
            log.info("Obtenint dates amb dades estadístiques per a entornAppId: {}", entornAppId);

            try {
                // Obtenir totes les dates (Temps) que tenen dades estadístiques per a aquest entornApp
                List<TempsEntity> tempsEntities = tempsRepository.findByEntornAppId(entornAppId);
                if (tempsEntities == null)
                    return List.of();
                return tempsEntities.stream()
                        .map(t -> tempsMapper.toTemps(t))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Error en obtenir dates amb dades estadístiques", e);
                return List.of();
            }
        }

        @Override
        public void onChange(Serializable id, Long previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Long target) {
        }
    }

    @RequiredArgsConstructor
    public static class DadesDiaReportGenerator implements ReportGenerator<FetEntity, FetObtenirParamAction, Fet> {
        private final FetRepository fetRepository;
        private final FetMapper fetMapper;
        private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        @Override
        public List<Fet> generateData(String code, FetEntity entity, FetObtenirParamAction params) throws ReportGenerationException {
            Long entornAppId = params.getEntornAppId();
            LocalDate dia = params.getDataInici();
            log.info("Obtenint dades estadístiques per a entornAppId: {} i dia: {}", entornAppId, dia);

            try {
                // Obtenir totes les dates (Temps) que tenen dades estadístiques per a aquest entornApp
                List<FetEntity> fetEntities = fetRepository.findByEntornAppIdAndTempsData(entornAppId, dia);
                if (fetEntities == null)
                    return List.of();
                return fetEntities.stream()
                        .map(t -> fetMapper.toFet(t))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Error en obtenir dates amb dades estadístiques", e);
                return List.of();
            }
        }

        @Override
        public void onChange(Serializable id, FetObtenirParamAction previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, FetObtenirParamAction target) {
        }
    }

    @RequiredArgsConstructor
    public static class ObtenirPerDataAction implements ActionExecutor<FetEntity, FetObtenirParamAction, FetObtenirResponse> {
        private final EstadisticaClientHelper estadisticaClientHelper;
        private final EstadisticaHelper estadisticaHelper;

        @Override
        public FetObtenirResponse exec(String code, FetEntity entity, FetObtenirParamAction params) throws ActionExecutionException {
            Long entornAppId = params.getEntornAppId();
            LocalDate data = params.getDataInici();
            try {
                log.info("Obtenint dades estadístiques per a la data {} i entornAppId: {}", data, entornAppId);
                EntornApp entornApp = estadisticaClientHelper.entornAppFindById(params.getEntornAppId());

                // Format de la data: dd-MM-yyyy
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String dataFormatada = data.format(formatter);

                // Construir URL amb la data específica
                String estadisticaUrl = entornApp.getEstadisticaUrl() + "/of/" + dataFormatada;

                // Utilitzar el mètode existent però amb la URL modificada
                return estadisticaHelper.getEstadisticaInfoDadesAmbUrl(entornApp, estadisticaUrl, false);

            } catch (Exception e) {
                log.error("Error en obtenir dades estadístiques per a la data {}", data, e);
                return FetObtenirResponse.builder().success(false).message(e.getMessage()).build();
            }
        }

        @Override
        public void onChange(Serializable id, FetObtenirParamAction previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, FetObtenirParamAction target) {
        }
    }

    @RequiredArgsConstructor
    public static class ObtenirPerIntervalAction implements ActionExecutor<FetEntity, FetObtenirParamAction, FetObtenirResponse> {
        private final EstadisticaClientHelper estadisticaClientHelper;
        private final EstadisticaHelper estadisticaHelper;

        @Override
        public FetObtenirResponse exec(String code, FetEntity entity, FetObtenirParamAction params) throws ActionExecutionException {
            Long entornAppId = params.getEntornAppId();
            LocalDate dataInici = params.getDataInici();
            LocalDate dataFi = params.getDataFi();
            try {
                log.info("Obtenint dades estadístiques per a l'interval {} a {} i entornAppId: {}",
                        dataInici, dataFi, entornAppId);
                EntornApp entornApp = estadisticaClientHelper.entornAppFindById(entornAppId);

                // Format de les dates: dd-MM-yyyy
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String dataIniciFormatada = dataInici.format(formatter);
                String dataFiFormatada = dataFi.format(formatter);

                // Construir URL amb l'interval de dates
                String estadisticaUrl = entornApp.getEstadisticaUrl() + "/from/" + dataIniciFormatada + "/to/" + dataFiFormatada;

                // Utilitzar el mètode existent però amb la URL modificada
                return estadisticaHelper.getEstadisticaInfoDadesAmbUrl(entornApp, estadisticaUrl, true);

            } catch (Exception e) {
                log.error("Error al obtenir dades estadístiques per a l'interval {} a {}", dataInici, dataFi, e);
                return FetObtenirResponse.builder().success(false).message(e.getMessage()).build();
            }

        }

        @Override
        public void onChange(Serializable id, FetObtenirParamAction previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, FetObtenirParamAction target) {
        }
    }

}
