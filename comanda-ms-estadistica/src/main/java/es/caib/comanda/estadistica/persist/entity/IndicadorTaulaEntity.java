package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.TableColumnsEnum;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entitat JPA per a representar la relació entre un indicador específic i un widget estadístic dins una taula, amb un tipus
 * específic de columna.
 *
 * Aquesta classe proporciona el model per a persistir dades relacionades amb taules estadístiques, incloent:
 * - La referència a l'indicador associat.
 * - El widget vinculat.
 * - El tipus de columna especificat.
 * - Un títol opcional per identificar l'element a nivell visual o descriptiu.
 *
 * Propòsits principals:
 * - Gestionar la vinculació entre indicadors i widgets estadístics.
 * - Permetre la configuració i personalització de l'aspecte i contingut dels elements en una taula estadística.
 *
 * Anotacions:
 * - `@Entity`: Defineix que aquesta classe és una entitat JPA.
 * - `@Table`: Especifica el nom de la taula a la base de dades, amb prefix definit a BaseConfig.
 * - Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`) per generar automàticament els accessors, mutadors i un constructor
 *   sense arguments.
 *
 * Herència:
 * Aquesta classe hereta de BaseAuditableEntity amb parametrització genèrica IndicadorTaula.
 * Això permet l'auditoria automàtica (creador, timestamps de creació/modificació, etc.) i operacions genèriques sobre
 * recursos IndicadorTaula.
 *
 * Relacions:
 * - `indicador`: Relació ManyToOne amb IndicadorEntity. Representa l'indicador associat a aquesta taula.
 * - `widget`: Relació ManyToOne amb EstadisticaTaulaWidgetEntity. Representa el widget vinculat.
 *
 * Validació:
 * - Tant `indicador` com `widget` són camps obligatoris (nullable = false).
 * - El camp `tipus` utilitza l'enumeració TableColumnsEnum per mantenir valors restringits i coherents.
 * - Camp de text `titol` amb un màxim de 64 caràcters.
 *
 * Aquesta entitat està orientada a facilitar la interacció entre dades estadístiques i interfícies visuals amb
 * configuració flexible de components tabulars.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "indicador_table")
@Getter
@Setter
@NoArgsConstructor
public class IndicadorTaulaEntity extends BaseAuditableEntity<IndicadorTaula> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "indicador_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "indtab_indicador_fk"),
            nullable = false)
    private IndicadorEntity indicador;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "widget_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "indtab_widget_fk"),
            nullable = false)
    private EstadisticaTaulaWidgetEntity widget;
    // first_seen, last_seen, avg_interval, average, etc.
    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", length = 16)
    private TableColumnsEnum tipus;
    @Column(name = "titol", length = 64)
    private String titol;
}
