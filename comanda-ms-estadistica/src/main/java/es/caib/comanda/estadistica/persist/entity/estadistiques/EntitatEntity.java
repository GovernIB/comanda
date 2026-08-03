package es.caib.comanda.estadistica.persist.entity.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Entitat;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "entitat")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class EntitatEntity extends BaseEntity<Entitat> {

    @Column(name = "codi", unique = true, nullable = false)
    private String codi;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "codi_dir3", nullable = false)
    private String codiDir3;

}
