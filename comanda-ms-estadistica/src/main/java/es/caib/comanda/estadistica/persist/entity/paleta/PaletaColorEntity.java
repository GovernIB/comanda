package es.caib.comanda.estadistica.persist.entity.paleta;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletaColor;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
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
        name = BaseConfig.DB_PREFIX + "est_color_palette_color",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "est_cpalcol_pos_uk", columnNames = { "palette_id", "posicio" })
        })
public class PaletaColorEntity extends BaseAuditableEntity<PaletaColor> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "palette_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "est_cpalcol_pal_fk"),
            nullable = false)
    private PaletaEntity paleta;

    @Column(name = "posicio", nullable = false)
    private Integer posicio;

    @Column(name = "valor", nullable = false, length = 64)
    private String valor;
}
