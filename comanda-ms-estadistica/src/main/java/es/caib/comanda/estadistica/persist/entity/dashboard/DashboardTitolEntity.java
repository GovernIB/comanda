package es.caib.comanda.estadistica.persist.entity.dashboard;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitolTipus;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.PosicioSubtitol;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

    public static final int TITOL_MAX_LENGTH = 255;
    public static final int SUBTITOL_MAX_LENGTH = 255;
    public static final int COLOR_MAX_LENGTH = 8;

    @ManyToOne
    @JoinColumn(
            name = "dashboard_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dboard_titol_dboard_fk"),
            nullable = false)
    private DashboardEntity dashboard;

    @Column(name = "titol", length = TITOL_MAX_LENGTH, nullable = false)
    private String titol;
    @Column(name = "subtitol", length = SUBTITOL_MAX_LENGTH)
    private String subtitol;

    @Column(name = "pos_x", nullable = false)
    private int posX;
    @Column(name = "pos_y", nullable = false)
    private int posY;
    @Column(name = "width", nullable = false)
    private int width;
    @Column(name = "height", nullable = false)
    private int height;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus_titol", length = 32)
    private DashboardTitolTipus tipusTitol;
    @Column(name = "color_titol", length = COLOR_MAX_LENGTH)
    private String colorTitol;
    @Column(name = "mida_font_titol")
    private Integer midaFontTitol;
    @Column(name = "color_subtitol", length = COLOR_MAX_LENGTH)
    private String colorSubtitol;
    @Column(name = "mida_font_subtitol")
    private Integer midaFontSubtitol;
    @Column(name = "color_fons", length = 8)
    private String colorFons;
    @Enumerated(EnumType.STRING)
    @Column(name = "posicio_subtitol", length = 32)
    private PosicioSubtitol posicioSubtitol;
    @Column(name = "separacio_subtitol")
    private Integer separacioSubtitol;
    @Column(name = "mostrar_vora_top")
    private Boolean mostrarVoraTop;
    @Column(name = "color_vora_top", length = 8)
    private String colorVoraTop;
    @Column(name = "ample_vora_top")
    private Integer ampleVoraTop;
    @Column(name = "mostrar_vora_right")
    private Boolean mostrarVoraRight;
    @Column(name = "color_vora_right", length = 8)
    private String colorVoraRight;
    @Column(name = "ample_vora_right")
    private Integer ampleVoraRight;
    @Column(name = "mostrar_vora_bottom")
    private Boolean mostrarVoraBottom;
    @Column(name = "color_vora_bottom", length = 8)
    private String colorVoraBottom;
    @Column(name = "ample_vora_bottom")
    private Integer ampleVoraBottom;
    @Column(name = "mostrar_vora_left")
    private Boolean mostrarVoraLeft;
    @Column(name = "color_vora_left", length = 8)
    private String colorVoraLeft;
    @Column(name = "ample_vora_left")
    private Integer ampleVoraLeft;
    @Column(name = "destacat")
    private Boolean destacat;
    @Column(name = "personalitzat")
    private Boolean personalitzat;
    @ManyToOne
    @JoinColumn(
            name = "plantilla_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dboard_titol_tpl_fk"))
    private PlantillaEntity plantilla;

}
