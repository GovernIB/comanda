package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.AppManual;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Entitat de base de dades que representa un subsistema.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "app_ctx_manual",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "app_manual_appnom_uk", columnNames = { "app_ctx_id", "nom" })
		})
@Getter
@Setter
@NoArgsConstructor
public class AppManualEntity extends BaseEntity<AppManual> {

	@Column(name = "nom", length = 128, nullable = false)
	private String nom;
	@Column(name = "path", length = 255, nullable = false)
	private String path;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "app_ctx_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "manual_app_ctx_fk"))
	private AppContextEntity appContext;

}
