package es.caib.comanda.estadistica.persist.entity.paleta;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteRole;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleProperty;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleValueType;
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
        name = BaseConfig.DB_PREFIX + "est_widget_style_property",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "est_wstprop_name_uk", columnNames = { "template_id", "scope", "property_name" })
        })
public class WidgetStylePropertyEntity extends BaseAuditableEntity<WidgetStyleProperty> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "template_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "est_wstprop_tpl_fk"),
            nullable = false)
    private PlantillaEntity plantilla;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 32)
    private WidgetStyleScope scope;

    @Column(name = "property_name", nullable = false, length = 64)
    private String propertyName;

    @Column(name = "label", length = 128)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 32)
    private WidgetStyleValueType valueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "palette_role", length = 16)
    private PaletteRole paletteRole;

    @Column(name = "palette_index")
    private Integer paletteIndex;

    @Column(name = "scalar_value", length = 1000)
    private String scalarValue;

    @Column(name = "default_property")
    private Boolean defaultProperty;

    @Column(name = "ordre")
    private Integer ordre;
}
