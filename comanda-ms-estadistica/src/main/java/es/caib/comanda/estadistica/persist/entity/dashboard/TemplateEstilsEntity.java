package es.caib.comanda.estadistica.persist.entity.dashboard;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.TemplateEstils;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Entitat que representa una plantilla d'estils a la base de dades.
 */
@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "est_template_estils",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "template_nom_uk", columnNames = { "nom" })
        })
@Getter
@Setter
@NoArgsConstructor
public class TemplateEstilsEntity extends BaseAuditableEntity<TemplateEstils> {

    @Column(name = "nom", length = 64, nullable = false)
    private String nom;

    @Column(name = "colors_clar", length = 1000)
    private String colorsClar;

    @Column(name = "colors_fosc", length = 1000)
    private String colorsFosc;

    @Column(name = "destacats_clar", length = 1000)
    private String destacatsClar;

    @Column(name = "destacats_fosc", length = 1000)
    private String destacatsFosc;

    @Column(name = "estils_default", length = 4000)
    private String estilsDefaultJson;
}
