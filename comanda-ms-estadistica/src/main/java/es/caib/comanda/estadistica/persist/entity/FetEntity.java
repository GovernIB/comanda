package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.config.DoubleJsonMapConverter;
import es.caib.comanda.estadistica.logic.config.StringJsonMapConverter;
import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Map;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_fet")
//@TypeDef(name = "json", typeClass = JsonType.class)
@Getter
@Setter
@NoArgsConstructor
public class FetEntity extends BaseEntity<Fet> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "temps_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "fet_temps_fk"),
            nullable = false)
    private TempsEntity temps;

    @Convert(converter = StringJsonMapConverter.class)
    @Column(name = "dimensions_json", length = 4000)
    private Map<String, String> dimensionsJson;

    @Convert(converter = DoubleJsonMapConverter.class)
    @Column(name = "indicadors_json", length = 4000)
    private Map<String, Double> indicadorsJson;

    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;
}
