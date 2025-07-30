package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.model.AppContext;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

/**
 * Entitat de base de dades que representa un subsistema.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "app_context",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "app_ctx_appcodi_uk", columnNames = { "entorn_app_id", "codi" })
		})
@Getter
@Setter
@NoArgsConstructor
public class AppContextEntity extends BaseAuditableEntity<AppContext> {

	@Column(name = "codi", length = 64, unique = true, nullable = false)
	private String codi;
	@Column(name = "nom", length = 100, nullable = false)
	private String nom;
	@Column(name = "path", length = 255, nullable = false)
	private String path;
	@Column(name = "api", length = 255)
	private String api;
	@Column(name = "actiu", nullable = false)
	private boolean actiu;

	@OneToMany(mappedBy = "appContext", cascade = CascadeType.ALL)
	private List<AppManualEntity> manuals;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "entorn_app_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "app_ctx_entorn_app_fk"))
	private EntornAppEntity entornApp;

}
