package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "entorn",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "entorn_codi_uk", columnNames = { "codi" })
        })
@Getter
@Setter
@NoArgsConstructor
public class EntornEntity extends BaseEntity<Entorn> {

    @Column(name = "codi", length = 16, nullable = false)
    private String codi;
    @Column(name = "nom", length = 255)
    private String nom;

}
