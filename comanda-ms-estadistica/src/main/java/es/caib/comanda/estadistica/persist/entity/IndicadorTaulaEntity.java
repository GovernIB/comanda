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
