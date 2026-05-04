package es.caib.comanda.estadistica.persist.entity.paleta;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.DashboardTemplatePaletteGroup;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
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
import javax.persistence.UniqueConstraint;

@Entity
@Getter
@Setter
@Table(
        name = BaseConfig.DB_PREFIX + "est_dashboard_template_palette_group",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "est_tpalgrp_type_uk", columnNames = { "template_id", "group_type" })
        })
public class PlantillaGrupPaletesEntity extends BaseAuditableEntity<DashboardTemplatePaletteGroup> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "template_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "est_tpalgrp_tpl_fk"),
            nullable = false)
    private PlantillaEntity plantilla;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false, length = 32)
    private PaletteGroupType groupType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "widget_palette_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "est_tpalgrp_wpal_fk"),
            nullable = false)
    private PaletaEntity widgetPalette;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "chart_palette_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "est_tpalgrp_cpal_fk"),
            nullable = false)
    private PaletaEntity chartPalette;

    @Column(name = "ordre")
    private Integer ordre;
}
