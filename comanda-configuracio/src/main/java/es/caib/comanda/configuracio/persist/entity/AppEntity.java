package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import lombok.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;

/**
 * Entitat de base de dades que representa una aplicació.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "app")
@Getter
@Setter
@NoArgsConstructor
public class AppEntity extends BaseEntity<Long> {

	@Column(name = "codi", length = 10, unique = true, nullable = false)
	private String codi;
	@Column(name = "nom", length = 100, nullable = false)
	private String nom;
	@Column(name = "descripcio", length = 1000, nullable = false)
	private String descripcio;
	@Column(name = "info_url", length = 200, nullable = false)
	private String infoUrl;
	@Column(name = "salut_url", length = 200, nullable = false)
	private String salutUrl;

}
