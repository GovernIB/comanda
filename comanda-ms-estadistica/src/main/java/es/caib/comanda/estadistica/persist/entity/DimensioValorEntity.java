package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.DimensioValor;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_dimensio_valor")
@Getter
@Setter
@NoArgsConstructor
public class DimensioValorEntity extends BaseEntity<DimensioValor> {

    @Column(name = "valor", length = 255)
    private String valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dimensio_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dim_valor_dim_fk"))
    private DimensioEntity dimensio;

}
