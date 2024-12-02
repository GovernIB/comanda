package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entitat de base de dades que representa un subsistema.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "subsistema",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "subsistema_appcodi_uk", columnNames = { "app_id", "codi" })
		})
@Getter
@Setter
@NoArgsConstructor
public class SubsistemaEntity extends BaseAuditableEntity<Long> {

	@Column(name = "codi", length = 10, unique = true, nullable = false)
	private String codi;
	@Column(name = "nom", length = 100, nullable = false)
	private String nom;
	@Column(name = "actiu", nullable = false)
	private boolean actiu;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "app_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "subsistema_app_fk"))
	private AppEntity app;

	/*@Builder
	public SubsistemaEntity(AppEntity app) {
		this.app = app;
	}*/

}
