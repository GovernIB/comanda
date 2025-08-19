package es.caib.comanda.permisos.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import es.caib.comanda.permisos.logic.intf.model.Permis;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "permis")
@Getter
@Setter
@NoArgsConstructor
public class PermisEntity extends BaseAuditableEntity<Permis> {

    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;
    @Column(name = "usuari", length = 64)
    private String usuari;
    @Column(name = "grup", length = 64)
    private String grup;
    @ManyToMany
    @JoinTable(
            name = BaseConfig.DB_PREFIX + "permis_acces",
            joinColumns = @JoinColumn(name = "permis_id"),
            inverseJoinColumns = @JoinColumn(name = "acces_id",
            nullable = false)
    )
    private List<AccesEntity> permisos;
    @ManyToOne
    @JoinColumn(
            name = "objecte_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "permis_objecte_fk"),
            nullable = false)
    private ObjecteEntity objecte;
    @ManyToMany
    @JoinTable(
            name = BaseConfig.DB_PREFIX + "permis_hereus",
            joinColumns = @JoinColumn(name = "permis_id"),
            inverseJoinColumns = @JoinColumn(name = "hereu_id")
    )
    private List<ObjecteEntity> objectesHereus;

}
