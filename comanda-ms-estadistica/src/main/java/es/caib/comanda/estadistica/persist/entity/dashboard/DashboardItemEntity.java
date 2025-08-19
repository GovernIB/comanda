package es.caib.comanda.estadistica.persist.entity.dashboard;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Classe que representa un element del Dashboard (quadre de comandament) en forma d'entitat de base de dades.
 *
 * Aquesta entitat conté informació sobre la configuració d'un element visual del Dashboard, incloent la seva posició (X, Y),
 * dimensions (amplada i alçada) i les referències al Dashboard al qual pertany i l'element visual (widget) que representa.
 *
 * La taula associada a aquesta entitat està definida amb un prefix configurat a BaseConfig. Compta amb clau forana tant cap al Dashboard
 * com cap a l'EstadisticaWidgetEntity, definint la relació ambdós elements.
 *
 * Aquesta classe hereta de BaseAuditableEntity, proporcionant funcionalitats estàndard per al control d'auditoria i identificador únic.
 *
 * Autor: Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_dashboard_item")
@Getter
@Setter
@NoArgsConstructor
public class DashboardItemEntity extends BaseAuditableEntity<DashboardItem> {

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(
            name = "dashboard_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dboard_item_dboard_fk"),
            nullable = false)
    private DashboardEntity dashboard;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(
            name = "widget_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dboard_item_widget_fk"),
            nullable = false)
    private EstadisticaWidgetEntity widget;

    @Column(name = "entorn_id", nullable = false)
    private Long entornId;

    @Column(name = "pos_x", nullable = false)
    private int posX;
    @Column(name = "pos_y", nullable = false)
    private int posY;
    @Column(name = "width", nullable = false)
    private int width;
    @Column(name = "height", nullable = false)
    private int height;

    // Estils del widget sobreescrits al dashboard (opcionals)
    @Column(name = "atributs_visuals", length = 4000)
    protected String atributsVisualsJson;

}
