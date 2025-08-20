package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.Integracio;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Entitat de base de dades que representa una integració.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "integracio",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "integracio_codi_uk", columnNames = { "codi" })
		})
@Getter
@Setter
@NoArgsConstructor
public class IntegracioEntity extends BaseAuditableEntity<Integracio> {

	@Column(name = "codi", length = 16, unique = true, nullable = false)
	private String codi;
	@Column(name = "nom", length = 100, nullable = false)
	private String nom;

	@Lob
	@Column(name = "logo", nullable = true)
	private byte[] logo;

}
