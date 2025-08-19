package es.caib.comanda.permisos.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.permisos.logic.intf.model.Objecte;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "objecte")
@Getter
@Setter
@NoArgsConstructor
public class ObjecteEntity extends BaseEntity<Objecte> {

    @Column(name = "tipus", length = 64, nullable = false)
    private String tipus;
    @Column(name = "nom", length = 255, nullable = false)
    private String nom;
    @Column(name = "identificador", length = 64, nullable = false)
    private String identificador;

    @ManyToMany(mappedBy = "objectesHereus")
    private List<PermisEntity> permisosCausants;

}
