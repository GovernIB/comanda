package es.caib.comanda.estadistica.persist.entity.dashboard;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_dashboard_titol")
@Getter
@Setter
@NoArgsConstructor
public class DashboardTitolEntity extends BaseAuditableEntity<DashboardTitol> {

    @ManyToOne
    @JoinColumn(
            name = "dashboard_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dboard_titol_dboard_fk"),
            nullable = false)
    private DashboardEntity dashboard;

    @Column(name = "titol", length = 255, nullable = false)
    private String titol;
    @Column(name = "subtitol", length = 255)
    private String subtitol;

    @Column(name = "pos_x", nullable = false)
    private int posX;
    @Column(name = "pos_y", nullable = false)
    private int posY;
    @Column(name = "width", nullable = false)
    private int width;
    @Column(name = "height", nullable = false)
    private int height;

    @Column(name = "color_titol", length = 8)
    private String colorTitol;
    @Column(name = "mida_font_titol")
    private Integer midaFontTitol;
    @Column(name = "color_subtitol", length = 8)
    private String colorSubtitol;
    @Column(name = "mida_font_subtitol")
    private Integer midaFontSubtitol;
    @Column(name = "color_fons", length = 8)
    private String colorFons;
    @Column(name = "mostrar_vora")
    private Boolean mostrarVora;
    @Column(name = "color_vora", length = 8)
    private String colorVora;
    @Column(name = "ample_vora")
    private Integer ampleVora;

}
