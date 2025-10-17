package es.caib.comanda.estadistica.persist.entity.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.CompactacioEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador;
import es.caib.comanda.ms.estadistica.model.Format;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Entitat JPA que representa un Indicador dins de l'entorn de l'aplicació.
 *
 * Aquesta entitat correspon a la taula "com_est_indicador" i permet emmagatzemar informació relacionada amb indicadors
 * estadístics o de mesura en el sistema. La taula inclou una restricció d'unicitat sobre el nom i l'identificador
 * de l'entorn (entorn_app_id) per evitar duplicats.
 *
 * Camps disponibles:
 * - codi: Identificador únic i obligatori de l'indicador, emmagatzemat amb una longitud màxima de 16 caràcters.
 * - nom: Nom descriptiu i obligatori de l'indicador, limitat a 64 caràcters.
 * - descripcio: Descripció opcional de l'indicador amb una longitud màxima de 1024 caràcters.
 * - entornAppId: Identificador obligatori que associa l'indicador al seu entorn d'aplicació.
 * - format: Indica el format del valor de l'indicador, definit pel tipus enum `Format` (ex. LONG, DECIMAL, etc.).
 *
 * Objectiu principal:
 * - Gestió estructurada i persistent dels indicadors del sistema, proporcionant atributs clau per identificar-los,
 *   descriure'ls i associar-los amb l'entorn d'aplicació.
 *
 * Característiques tècniques:
 * - Mapatge JPA mitjançant anotacions `@Entity` i `@Table` amb una restricció d'unicitat definida per `@UniqueConstraint`
 *   sobre els camps "nom" i "entorn_app_id".
 * - Inclou validacions com la longitud màxima i camp obligatori a través de l'anotació `@Column`.
 * - Herència de la classe base `BaseEntity<Indicador>` per aprofitar funcionalitats bàsiques comunes.
 *
 * Aquesta entitat s'utilitza en combinació amb `IndicadorRepository` per a operacions sobre la base de dades,
 * com ara consultes, insercions i actualitzacions d'indicadors.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "est_indicador",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "ind_nom_uk", columnNames = { "nom", "entorn_app_id" })
        }
)
@Getter
@Setter
@NoArgsConstructor
public class IndicadorEntity extends BaseEntity<Indicador> {

    @Column(name = "codi", length = 32, nullable = false)
    private String codi;
    @Column(name = "nom", length = 64, nullable = false)
    private String nom;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;
    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;
    @Column(name = "format", length = 64)
    @Enumerated(EnumType.STRING)
    private Format format;

    @Column(name = "compactable", nullable = false)
    private Boolean compactable = true;
    @Column(name = "tipus_compactacio", length = 64)
    @Enumerated(EnumType.STRING)
    private CompactacioEnum tipusCompactacio;
    @ManyToOne
    @JoinColumn(
            name = "compactacio_indicador_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "ind_compactacio_fk"))
    private IndicadorEntity indicadorComptadorPerMitjana;

    public String getCodiNomDescription() {
        return this.codi + " - " + this.nom;
    }

}
