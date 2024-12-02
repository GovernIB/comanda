package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.SalutNivell;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entitat de base de dades que emmagatzema els missatges relacionats
 * amb les informacions de salut.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "salut_missatge")
@Getter
@Setter
@NoArgsConstructor
public class SalutMissatgeEntity extends BaseEntity<Long> {

	@Column(name = "data", nullable = false)
	private LocalDateTime data;
	@Enumerated(EnumType.STRING)
	@Column(name = "nivell", nullable = false)
	private SalutNivell nivell;
	@Column(name = "missatge", length = 2048, nullable = false)
	private String missatge;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "salut_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "salutmsg_salut_fk"))
	private SalutEntity salut;

	/*@Builder
	public SalutMissatgeEntity(
			SalutEntity salut,
			IntegracioEntity integracio) {
		this.salut = salut;
		this.integracio = integracio;
	}*/

}
