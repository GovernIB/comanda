package es.caib.comanda.estadistica.persist.entity.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;
import org.hibernate.annotations.Formula;

/**
 * Representa l'entitat de persistència per a les dimensions en el sistema estadístic.
 *
 * Les dimensions són utilitzades per a categoritzar i organitzar dades dins d'un context específic d'una aplicació.
 * Aquesta classe inclou les propietats de la dimensió, com ara el nom, el codi, una descripció opcional, l'identificador
 * de l'entorn d'aplicació al qual pertany, i una llista de valors associats.
 *
 * Es defineix com una entitat JPA amb mapeig a una taula de base de dades anomenada "est_dimensio". Té una
 * restricció d'unicitat basada en les columnes `nom` i `entorn_app_id`.
 *
 * Relacions:
 * - Té una relació OneToMany amb l'entitat DimensioValorEntity per gestionar els valors associats a la dimensió.
 *
 * Columnes:
 * - `codi`: Un codi únic i obligatori per identificar la dimensió (màx. 16 caràcters).
 * - `nom`: Un nom únic dins del mateix entorn d'aplicació (màx. 64 caràcters, obligatori).
 * - `descripcio`: Una descripció opcional de la dimensió (màx. 1024 caràcters).
 * - `entorn_app_id`: Identificador obligatori de l'entorn d'aplicació.
 *
 * Validacions:
 * - `codi` i `nom` són obligatoris i tenen restriccions de longitud.
 * - La relació amb DimensioValorEntity s'administra amb cascada per assegurar que els valors associats es gestionen
 *   automàticament amb la dimensió.
 *
 * Aquesta classe extén BaseEntity per heretar propietats i funcionalitats comunes, com la gestió de l'identificador únic.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "est_dimensio",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "dim_nom_uk", columnNames = { "nom", "entorn_app_id" })
        }
)
@Getter
@Setter
@NoArgsConstructor
public class DimensioEntity extends BaseEntity<Dimensio> {

    @Column(name = "codi", length = 16, nullable = false)
    private String codi;
    @Column(name = "nom", length = 64, nullable = false)
    private String nom;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;
    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;
    @OneToMany(mappedBy = "dimensio", cascade = CascadeType.ALL)
    private List<DimensioValorEntity> valors;

    @Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "est_dimensio_valor dv where dv.dimensio_id = id and dv.agrupable = true)")
    private Integer agrupableCount;

}
