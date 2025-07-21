package es.caib.comanda.permisos.persist.entity;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.permisos.logic.intf.model.Permis;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "acces")
@Getter
@Setter
@NoArgsConstructor
public class AccesEntity extends BaseEntity<Permis> {

    @Column(name = "acces", length = 64)
    private String acces;

}
