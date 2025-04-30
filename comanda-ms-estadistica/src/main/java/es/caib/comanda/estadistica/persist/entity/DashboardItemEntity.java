package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.DashboardItem;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public class DashboardItemEntity extends BaseEntity<DashboardItem> {

    @Column(name = "pos_x", nullable = false)
    private int posX;
    @Column(name = "pos_y", nullable = false)
    private int posY;
    @Column(name = "width", nullable = false)
    private int width;
    @Column(name = "height", nullable = false)
    private int height;

    @ManyToOne
    @JoinColumn(
            name = "dashboard_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dboard_item_dboard_fk"),
            nullable = false)
    private DashboardEntity dashboard;

    @ManyToOne
    @JoinColumn(
            name = "view_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dboard_item_view_fk"),
            nullable = false)
    private EstadisticaWidgetEntity view;

}
