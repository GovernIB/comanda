package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
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
        name = BaseConfig.DB_PREFIX + "est_dashboard",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "dashboard_titol_uk", columnNames = { "titol" })
        })
@Getter
@Setter
@NoArgsConstructor
public class DashboardEntity extends BaseAuditableEntity<Long> {

    @Column(name = "titol", length = 64, nullable = false)
    private String titol;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;

    @OneToMany(mappedBy = "dashboard", cascade = CascadeType.ALL)
    private List<DashboardItemEntity> items;

}
