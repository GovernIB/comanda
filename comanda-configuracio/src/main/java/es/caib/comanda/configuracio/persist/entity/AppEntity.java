package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.App;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDateTime;

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
	@Column(name = "info_url", length = 200, nullable = false)
	private String infoUrl;
	@Column(name = "info_interval", nullable = false)
	private Integer infoInterval;
	@Column(name = "info_data")
	private LocalDateTime infoData;
	@Column(name = "salut_url", length = 200, nullable = false)
	private String salutUrl;
	@Column(name = "salut_interval", nullable = false)
	private Integer salutInterval;
	@Column(name = "versio", length = 10)
	private String versio;
	@Column(name = "activa", nullable = false)
	private boolean activa;

	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "integracio int where int.app_id = id)")
	private Integer integracioCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "subsistema sub where sub.app_id = id)")
	private Integer subsistemaCount;

}
