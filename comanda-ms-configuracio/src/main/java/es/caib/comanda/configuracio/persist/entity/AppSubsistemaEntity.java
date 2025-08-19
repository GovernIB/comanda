package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.AppSubsistema;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
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
@Table(name = BaseConfig.DB_PREFIX + "app_subsistema",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "app_subsistema_appcodi_uk", columnNames = { "entorn_app_id", "codi" })
		})
@Getter
@Setter
@NoArgsConstructor
public class AppSubsistemaEntity extends BaseAuditableEntity<AppSubsistema> {

	@Column(name = "codi", length = 10, nullable = false)
	private String codi;
	@Column(name = "nom", length = 100, nullable = false)
	private String nom;
	@Column(name = "actiu", nullable = false)
	private boolean actiu;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "entorn_app_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "app_subs_entorn_app_fk"))
	private EntornAppEntity entornApp;

}
