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

/**
 * Classe que representa un entity de la base de dades per a un Dashboard.
 *
 * Aquesta entitat conté els atributs que defineixen les característiques d'un Dashboard, com el títol, la descripció i la
 * llista d'elements associats. És utilitzada en la persistència de dades per emmagatzemar i recuperar informació dels Dashboards.
 *
 * La taula està definida amb el prefix configurat a BaseConfig i inclou una restricció d'unicitat pel títol del Dashboard.
 *
 * Aquesta classe hereta de BaseAuditableEntity per afegir funcionalitats de control de versions i informació d'auditoria.
 *
 * Autor: Límit Tecnologies
 */
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
