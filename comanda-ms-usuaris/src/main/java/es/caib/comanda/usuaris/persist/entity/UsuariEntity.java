package es.caib.comanda.usuaris.persist.entity;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "usuari",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "usuari_codi_uk", columnNames = { "codi" })
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UsuariEntity extends BaseAuditableEntity<Usuari> {

    @NaturalId
    @Column(name = "codi", length = 64, nullable = false)
    private String codi;
    @Column(name = "nom", length = 255, nullable = false)
    private String nom;
    @Column(name = "nif", length = 10)
    private String nif;
    @Column(name = "email", length = 255)
    private String email;

}
