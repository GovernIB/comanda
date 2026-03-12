package es.caib.comanda.api.controller.v1.salut;

import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.ContextInfo;
import es.caib.comanda.model.v1.salut.EstatSalut;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.model.v1.salut.InformacioSistema;
import es.caib.comanda.model.v1.salut.Manual;
import es.caib.comanda.model.v1.salut.SalutInfo;
import es.caib.comanda.model.v1.salut.SubsistemaInfo;
import es.caib.comanda.ms.salut.helper.MonitorHelper;
import es.caib.comanda.ms.salut.helper.SalutHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static es.caib.comanda.ms.back.config.BaseOpenApiConfig.BASIC_SECURITY_SCHEME;
import static es.caib.comanda.ms.back.config.BaseOpenApiConfig.SECURITY_NAME;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

/**
 * Contracte de l'API de Salut que COMANDA espera que implementin les APPs.
 * Aquesta classe defineix les rutes i els models retornats per generar el contracte OpenAPI.
 * La implementació real ha de ser aportada per cada APP.
 */
@RestController
@RequestMapping("/salut/v1")
@Tag(name = "COMANDA → APP / Salut", description = "Contracte d'API de salut i metadades de l'aplicació que COMANDA pot consultar")
@SecurityScheme(type = SecuritySchemeType.HTTP, name = SECURITY_NAME, scheme = BASIC_SECURITY_SCHEME)
public class SalutApiV1Controller {

//    @PersistenceContext
//    private EntityManager em;

    @GetMapping("/info")
    @PreAuthorize("hasRole(T(es.caib.comanda.base.config.BaseConfig).ROLE_WEBSERVICE)")
    @SecurityRequirement(name = SECURITY_NAME)
    @Operation(operationId = "salutInfo",
            summary = "Obtenir informació de l'aplicació",
            description = "Retorna dades bàsiques de l'aplicació (codi, nom, versió, data de build, etc.) i contextos exposats.",
            tags = {"COMANDA → APP / Salut"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta", content = @Content(schema = @Schema(implementation = AppInfo.class))),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor")
    })
    public AppInfo salutInfo(HttpServletRequest request) throws java.io.IOException {
        SalutHelper.BuildInfo buildInfo = SalutHelper.getBuildInfo();

        List<SubsistemaInfo> subsistemes = List.of(
                SubsistemaInfo.builder().codi("CON").nom("Configuració").build(),
                SubsistemaInfo.builder().codi("SAL").nom("Salut").build(),
                SubsistemaInfo.builder().codi("EST").nom("Estadístiques").build(),
                SubsistemaInfo.builder().codi("MON").nom("Monitor").build(),
                SubsistemaInfo.builder().codi("AVI").nom("Avisos").build(),
                SubsistemaInfo.builder().codi("TAS").nom("Tasques").build(),
                SubsistemaInfo.builder().codi("ALA").nom("Alarmes").build(),
                SubsistemaInfo.builder().codi("ACL").nom("Acl").build()
        );
        
        return AppInfo.builder()
                .codi("COM")
                .nom("Comanda")
                .data(buildInfo.getBuildDate())
                .versio(buildInfo.getVersion())
                .revisio(buildInfo.getCommitId())
                .jdkVersion(buildInfo.getBuildJDK())
                .versioJboss(MonitorHelper.getApplicationServerInfo())
                .contexts(getContexts(getBaseUrl(request)))
                .integracions(Collections.emptyList())
                .subsistemes(subsistemes)
                .build();

    }

    private List<ContextInfo> getContexts(String baseUrl) {
        return List.of(
                ContextInfo.builder()
                        .codi("BACK")
                        .nom("Backoffice")
                        .path(baseUrl + "/comandaback")
                        .manuals(Collections.emptyList())
                        .build(),
                ContextInfo.builder()
                        .codi("INT")
                        .nom("API interna")
                        .path(baseUrl + "/comandaapi/interna")
                        .manuals(List.of(Manual.builder().nom("Manual d'integració").path("https://github.com/GovernIB/comanda/raw/comanda-0.1/doc/pdf/01_COMANDA_integració.pdf").build()))
                        .api(baseUrl + "/comandaapi/interna/swagger-ui/index.html")
                        .build()
        );
    }

    private String getBaseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder
                .fromRequestUri(request)
                .replacePath(null) // elimina el context path "/comandaapi/..."
                .build()
                .toUriString();
    }

    @GetMapping
    @Operation(operationId = "salut",
            summary = "Obtenir informació de l'estat de salut de l'aplicació",
            description = "Retorna l'estat de salut funcional i integracions, amb metadades de versió.",
            tags = {"COMANDA → APP / Salut"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operació correcta",
                    content = @Content(schema = @Schema(implementation = SalutInfo.class))),
            @ApiResponse(responseCode = "500", description = "Error intern del servidor"),
    })
    public SalutInfo salut(
            HttpServletRequest request,
            @DateTimeFormat(iso = DATE_TIME) @Parameter(name = "dataPeriode", description = "Data mínima de la que es demana informació per període", required = false) @RequestParam(required = false) OffsetDateTime dataPeriode,
            @DateTimeFormat(iso = DATE_TIME) @Parameter(name = "dataTotal", description = "Data mínima de la que demana informació per totals", required = false) @RequestParam(required = false) OffsetDateTime dataTotal) throws java.io.IOException {

        long startTime = System.currentTimeMillis();
        InformacioSistema infoSistema = null;
        try {
            es.caib.comanda.model.server.monitoring.InformacioSistema infoServer = MonitorHelper.getInfoSistema();
            if (infoServer != null) {
                infoSistema = InformacioSistema.builder()
                        .processadors(infoServer.getProcessadors())
                        .carregaSistema(infoServer.getCarregaSistema())
                        .cpuSistema(infoServer.getCpuSistema())
                        .memoriaTotal(infoServer.getMemoriaTotal())
                        .memoriaDisponible(infoServer.getMemoriaDisponible())
                        .espaiDiscTotal(infoServer.getEspaiDiscTotal())
                        .espaiDiscLliure(infoServer.getEspaiDiscLliure())
                        .sistemaOperatiu(infoServer.getSistemaOperatiu())
                        .dataArrencada(infoServer.getDataArrencada())
                        .tempsFuncionant(infoServer.getTempsFuncionant())
                        .build();
            }
        } catch (Exception e) {
            // Ignorar errors en obtenir info sistema
        }
        SalutHelper.BuildInfo buildInfo = SalutHelper.getBuildInfo();
//        Integer latenciaDb = measureDbLatencyMs();
        Integer latencia = (int) (System.currentTimeMillis() - startTime);

        return SalutInfo.builder()
                .codi("COM")
                .data(buildInfo.getBuildDate())
                .versio(buildInfo.getVersion())
                .estatGlobal(EstatSalut.builder().estat(EstatSalutEnum.UP).latencia(latencia).build())
                .estatBaseDeDades(EstatSalut.builder().estat(EstatSalutEnum.UP)/*.latencia(latenciaDb)*/.build())
                .informacioSistema(infoSistema)
                .integracions(Collections.emptyList())
                .missatges(Collections.emptyList())
                .subsistemes(Collections.emptyList())
                .build();

        // TODO: Afegir informació de subsistemes, i alarmes (com a missatges)
    }

//    public Integer measureDbLatencyMs() {
//
//        try {
//            Session session = em.unwrap(Session.class);
//
//            final String[] sql = new String[1];
//
//            // 1) Detectar producte de BBDD amb JDBC
//            session.doWork(conn -> {
//                String product = conn.getMetaData().getDatabaseProductName().toLowerCase();
//                sql[0] = product.contains("oracle") ? "SELECT 1 FROM DUAL" : "SELECT 1";
//            });
//
//            // 2) Mesurar query
//            long start = System.currentTimeMillis();
//            em.createNativeQuery(sql[0])
//                    .setHint("org.hibernate.timeout", 2) // segons
//                    .getSingleResult();
//
//            return (int) (System.currentTimeMillis() - start);
//        } catch (Exception e) {
//            return null;
//        }
//    }

}
