package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.Indicador;
import es.caib.comanda.ms.estadistica.model.Format;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "est_indicador",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "ind_nom_uk", columnNames = { "nom", "entorn_app_id" })
        }
)
@Getter
@Setter
@NoArgsConstructor
public class IndicadorEntity extends BaseEntity<Indicador> {

    @Column(name = "nom", length = 64, nullable = false)
    private String nom;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;
    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;
    @Column(name = "format", length = 64)
    @Enumerated(EnumType.STRING)
    private Format format;
}
