package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.SalutEstat;
import es.caib.comanda.configuracio.logic.intf.model.SalutIntegracio;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entitat de base de dades que emmagatzema les informacions de salut
 * relacionades amb una integració.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "salut_integracio")
@Getter
@Setter
@NoArgsConstructor
public class SalutIntegracioEntity extends BaseEntity<SalutIntegracio> {

	@Column(name = "codi", length = 16, nullable = false)
	private String codi;
	@Enumerated(EnumType.STRING)
	@Column(name = "estat", nullable = false)
	private SalutEstat estat;
	@Column(name = "latencia")
	private Integer latencia;
	@Column(name = "total_ok", nullable = false)
	private Long totalOk;
	@Column(name = "total_error", nullable = false)
	private Long totalError;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "salut_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "salutint_salut_fk"))
	private SalutEntity salut;

}
