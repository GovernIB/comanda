package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_fet")
@Getter
@Setter
@NoArgsConstructor
public class FetEntity extends BaseEntity<Fet> {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "indicador_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "fet_indicador_fk"),
            nullable = false)
    private IndicadorEntity indicator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "temps_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "fet_temps_fk"),
            nullable = false)
    private TempsEntity temps;

    @ManyToMany
    @JoinTable(
            name = BaseConfig.DB_PREFIX + "est_fet_dimensio_valor",
            joinColumns = @JoinColumn(name = "fet_id"),
            inverseJoinColumns = @JoinColumn(name = "dimensio_valor_id")
    )
    private Set<DimensioValorEntity> dimensioValors;
    private Double valor;

}
