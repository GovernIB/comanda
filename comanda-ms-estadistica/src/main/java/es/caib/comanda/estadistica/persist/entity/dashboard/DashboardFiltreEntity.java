package es.caib.comanda.estadistica.persist.entity.dashboard;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltre;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltreTipus;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Configuració persistida d'un filtre de capçalera de dashboard. Vegeu {@link DashboardFiltre}.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_dashboard_filtre")
@Getter
@Setter
@NoArgsConstructor
public class DashboardFiltreEntity extends BaseAuditableEntity<DashboardFiltre> {

    @ManyToOne
    @JoinColumn(
            name = "dashboard_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dboard_filtre_dboard_fk"),
            nullable = false)
    private DashboardEntity dashboard;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipus", length = 16, nullable = false)
    private DashboardFiltreTipus tipus;

    @Column(name = "dimensio_codi", length = 32)
    private String dimensioCodi;

    @Column(name = "titol", length = 64)
    private String titol;

    @Column(name = "ordre", nullable = false)
    private int ordre;

    @Column(name = "multiple", nullable = false)
    private boolean multiple = true;

}
