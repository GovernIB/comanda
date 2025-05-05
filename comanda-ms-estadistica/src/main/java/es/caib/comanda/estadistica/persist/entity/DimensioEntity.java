package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.Dimensio;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "est_dimensio",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "dim_nom_uk", columnNames = { "nom", "entorn_app_id" })
        }
)
@Getter
@Setter
@NoArgsConstructor
public class DimensioEntity extends BaseEntity<Dimensio> {

    @Column(name = "nom", length = 64, nullable = false)
    private String nom;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;
    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;
    @OneToMany(mappedBy = "dimensio", cascade = CascadeType.ALL)
    private List<DimensioValorEntity> valors;

}
