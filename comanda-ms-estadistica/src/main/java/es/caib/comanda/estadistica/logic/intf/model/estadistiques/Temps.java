package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Classe que representa una dimensió temporal.
 *
 * Aquesta classe encapsula les dades corresponents a una instància temporal específica, detallant aspectes
 * com la data, l'anualitat, el trimestre, el mes, la setmana, el dia de la setmana i el dia. Permet una
 * classificació i organització temporal per a diferents aplicacions estadístiques o d'anàlisi.
 *
 * Propietats:
 * - data: La data representada com a objecte LocalDate. És obligatori.
 * - anualitat: L'any associat a la data. És obligatori.
 * - trimestre: El trimestre (1-4) associat a la data. És obligatori.
 * - mes: El mes (1-12) associat a la data. És obligatori.
 * - setmana: La setmana de l'any associada a la data. És obligatori.
 * - diaSetmana: Enumeració que indica el dia de la setmana (Dilluns-Diumenge). És obligatori.
 * - dia: El dia dins del mes associat a la data (1-31). És obligatori.
 *
 * Aquesta classe hereta de BaseResource, proporcionant un identificador únic del tipus Long per a cada instància.
 *
 * Validacions:
 * - Totes les propietats marcades amb l'anotació `@NotNull` són obligatòries i no poden ser nules.
 *
 * Ús principal:
 * - Aquesta classe és útil per estructurar, filtrar i categoritzar informació basada en dimensions temporals,
 * normalment usada en un context d'anàlisi estadística o organització per temps.
 *
 * Notes sobre anotacions:
 * - Es fan servir anotacions de Lombok com ara @Getter, @Setter, @Builder, @NoArgsConstructor, i @AllArgsConstructor
 * per simplificar la generació de codi com getters, setters, constructors i patrons constructius.
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
                        grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.DELETE }
                )
        }
)
public class Temps extends BaseResource<Long> {

    @NotNull
    private LocalDate data;
    @NotNull
    private int anualitat;
    @NotNull
    private int trimestre;
    @NotNull
    private int mes;
    @NotNull
    private int setmana;
    @NotNull
    private DiaSetmanaEnum diaSetmana;
    @NotNull
    private int dia;

}
