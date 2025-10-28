package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

/**
 * Classe que representa un "Fet", una entitat que encapsula dades estadístiques dins d'un context específic.
 *
 * Un "Fet" és una combinació de diverses dimensions i indicadors que permeten descriure certs aspectes d'un conjunt
 * de dades relacionades amb un temps específic i un entorn d'aplicació. Aquesta classe s'utilitza dins del sistema
 * per analitzar i processar informació estadística estructurada.
 *
 * Propietats:
 * - temps: Objecte que representa la informació temporal associada al fet. És obligatori.
 * - dimensionsJson: Map que conté les dimensions del fet com a clau (identificador únic) i valor (descripció). És obligatori.
 * - indicadorsJson: Map que conté els indicadors del fet amb els seus valors numèrics. És obligatori.
 * - entornAppId: Identificador de l'entorn d'aplicació associat al fet. És obligatori.
 *
 * Aquesta classe hereta de BaseResource, proporcionant un identificador únic del tipus Long per a cada instància.
 *
 * Validacions:
 * - `temps`, `dimensionsJson`, `indicadorsJson` i `entornAppId` són camps obligatoris i no poden ser nuls.
 *
 * Aquesta classe empra anotacions del paquet Lombok com ara @Getter, @Setter, @Builder, @NoArgsConstructor, i @AllArgsConstructor,
 * per a simplificar la gestió de getters, setters i constructors.
 *
 * Objectiu:
 * - Facilitar la gestió dels registres estadístics per al seu ús en càlculs, visualitzacions o anàlisis.
 * - Proveir una base comuna per a l'emmagatzematge i manipulació de dades estructurades.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ResourceConfig(
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN },
                        grantedPermissions = { PermissionEnum.READ }
                )
        },
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.REPORT, code = Fet.FET_REPORT_DATES_DISPONIBLES, formClass = Long.class),
                @ResourceArtifact(type = ResourceArtifactType.REPORT, code = Fet.FET_REPORT_DADES_DIA, formClass = Fet.FetObtenirParamAction.class),
                @ResourceArtifact(type = ResourceArtifactType.ACTION, code = Fet.FET_ACTION_OBTENIR_PER_DATA, formClass = Fet.FetObtenirParamAction.class, accessConstraints = {
                        @ResourceAccessConstraint(
                                type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                                roles = { BaseConfig.ROLE_ADMIN }
                        )
                }),
                @ResourceArtifact(type = ResourceArtifactType.ACTION, code = Fet.FET_ACTION_OBTENIR_PER_INTERVAL, formClass = Fet.FetObtenirParamAction.class, accessConstraints = {
                        @ResourceAccessConstraint(
                                type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                                roles = { BaseConfig.ROLE_ADMIN }
                        )
                }),
        }
)
public class Fet extends BaseResource<Long> {

    public final static String FET_REPORT_DATES_DISPONIBLES = "dates_disponibles";
    public final static String FET_REPORT_DADES_DIA = "dades_dia";
    public final static String FET_ACTION_OBTENIR_PER_DATA = "obtenir_per_data";
    public final static String FET_ACTION_OBTENIR_PER_INTERVAL = "obtenir_per_interval";

    @NotNull
    private Temps temps;
    @NotNull
    private Map<String, String> dimensionsJson;
    @NotNull
    private Map<String, Double> indicadorsJson;
    @NotNull
    private Long entornAppId;

    @NotNull
    private FetTipusEnum tipus;
    private int numDies;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FetObtenirParamAction implements Serializable {
        private Long entornAppId;
        private LocalDate dataInici;
        private LocalDate dataFi;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class FetObtenirResponse implements Serializable {
        private Boolean success;
        private String message;
        private Map<String, Boolean> diesAmbDades;
    }
}
