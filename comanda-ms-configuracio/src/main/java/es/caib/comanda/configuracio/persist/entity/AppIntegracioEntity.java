package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.model.AppIntegracio;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
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
@Table(name = BaseConfig.DB_PREFIX + "app_integracio",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "app_integracio_appcodi_uk", columnNames = { "entorn_app_id", "codi" })
		})
@Getter
@Setter
@NoArgsConstructor
public class AppIntegracioEntity extends BaseAuditableEntity<AppIntegracio> {

	@Column(name = "codi", length = 16, unique = true, nullable = false)
	private String codi;
	@Column(name = "nom", length = 100, nullable = false)
	private String nom;
	@Column(name = "activa", nullable = false)
	private boolean activa;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "entorn_app_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "app_integ_entorn_app_fk"))
	private EntornAppEntity entornApp;

}
