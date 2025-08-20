package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * Entitat de base de dades que representa una aplicació a monitoritzar.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "app",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "app_codi_uk", columnNames = { "codi" })
		})
@Getter
@Setter
@NoArgsConstructor
public class AppEntity extends BaseAuditableEntity<App> {

	@Column(name = "codi", length = 16, nullable = false)
	private String codi;
	@Column(name = "nom", length = 100, nullable = false)
	private String nom;
	@Column(name = "descripcio", length = 1000, nullable = false)
	private String descripcio;
	@Column(name = "activa", nullable = false)
	private boolean activa;

	@Lob
	@Column(name = "logo", nullable = true)
	private byte[] logo;

	@OneToMany(mappedBy= "app", cascade = CascadeType.ALL)
	private List<EntornAppEntity> entornApps;

}
