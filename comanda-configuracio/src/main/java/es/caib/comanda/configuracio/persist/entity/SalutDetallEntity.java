package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.SalutDetall;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entitat de base de dades que emmagatzema els detalls relacionats
 * amb les informacions de salut.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "salut_detall")
@Getter
@Setter
@NoArgsConstructor
public class SalutDetallEntity extends BaseEntity<SalutDetall> {

	@Column(name = "codi", length = 10, nullable = false)
	private String codi;
	@Column(name = "nom", length = 100, nullable = false)
	private String nom;
	@Column(name = "valor", length = 1024, nullable = false)
	private String valor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "salut_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "salutdet_salut_fk"))
	private SalutEntity salut;

}
