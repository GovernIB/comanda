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
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "dim_nom_uk", columnNames = { "nom", "aplicacio_codi" })
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
    @Column(name = "aplicacio_codi", length = 16, nullable = false)
    private String aplicacioCodi;
    @OneToMany(mappedBy = "dimensio", cascade = CascadeType.ALL)
    private List<DimensioValorEntity> valors;

}
