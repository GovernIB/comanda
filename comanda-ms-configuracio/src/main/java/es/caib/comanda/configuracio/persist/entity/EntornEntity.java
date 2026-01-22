package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.ms.persist.entity.ReorderableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "entorn",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "entorn_codi_uk", columnNames = { "codi" })
        })
@Getter
@Setter
@NoArgsConstructor
public class EntornEntity extends BaseEntity<Entorn> implements ReorderableEntity<Long> {

    @Column(name = "codi", length = 16, nullable = false)
    private String codi;
    @Column(name = "nom", length = 255)
    private String nom;
    @Column(name = "ordre")
    private Long ordre;

    @OneToMany(mappedBy= "entorn", cascade = CascadeType.REMOVE)
    private Set<EntornAppEntity> entornAppEntities;

    @Override
    public Long getOrder() {
        return ordre;
    }
    @Override
    public void setOrder(Long order) {
        this.ordre = order;
    }
}
