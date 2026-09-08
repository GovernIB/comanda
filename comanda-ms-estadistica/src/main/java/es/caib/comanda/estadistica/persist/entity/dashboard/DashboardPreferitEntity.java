package es.caib.comanda.estadistica.persist.entity.dashboard;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardPreferit;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Classe que representa una entitat de la base de dades per a un Dashboard Preferit.
 *
 * Aquesta entitat gestiona la relació de preferència entre un usuari (identificat pel seu codi immutable)
 * i un Dashboard. Permet emmagatzemar quins dashboards ha marcat com a preferits cada usuari.
 *
 * Autor: Límit Tecnologies
 */
@Entity
@Table(
    name = BaseConfig.DB_PREFIX + "est_dashboard_preferit",
    uniqueConstraints = {
        @UniqueConstraint(
            name = BaseConfig.DB_PREFIX + "est_dashboard_preferit_uk",
            columnNames = { "usuari_codi", "est_dashboard_id" }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class DashboardPreferitEntity extends BaseEntity<DashboardPreferit> {

    public static final int USUARI_CODI_MAX_LENGTH = 64;

    @Column(name = "usuari_codi", length = USUARI_CODI_MAX_LENGTH, nullable = false)
    private String usuariCodi;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "est_dashboard_id", nullable = false)
    private DashboardEntity dashboard;

}
