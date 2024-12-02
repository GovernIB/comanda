package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entitat de base de dades que representa una integració.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "integracio",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "integracio_appcodi_uk", columnNames = { "app_id", "codi" })
		})
@Getter
@Setter
@NoArgsConstructor
public class IntegracioEntity extends BaseAuditableEntity<Long> {

	@Column(name = "codi", length = 16, unique = true, nullable = false)
	private String codi;
	@Column(name = "nom", length = 100, nullable = false)
	private String nom;
	@Column(name = "activa", nullable = false)
	private boolean activa;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "app_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "integracio_app_fk"))
	private AppEntity app;

	/*@Builder
	public IntegracioEntity(AppEntity app) {
		this.app = app;
	}*/

}
