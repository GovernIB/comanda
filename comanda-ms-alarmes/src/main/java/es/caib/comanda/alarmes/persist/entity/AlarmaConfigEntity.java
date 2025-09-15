package es.caib.comanda.alarmes.persist.entity;

import es.caib.comanda.alarmes.logic.intf.model.*;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entitat de base de dades que emmagatzema la configuració d'una alarma.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "alarma_config")
@Getter
@Setter
@NoArgsConstructor
public class AlarmaConfigEntity extends BaseAuditableEntity<AlarmaConfig> {

	@Column(name = "entorn_app_id", nullable = false)
	private Long entornAppId;

	@Column(name = "nom", length = 200, nullable = false)
	private String nom;
	@Column(name = "missatge", length = 1024, nullable = false)
	private String missatge;
	@Enumerated(EnumType.STRING)
	@Column(name = "tipus", length = 15, nullable = false)
	private AlarmaConfigTipus tipus;
	@Enumerated(EnumType.STRING)
	@Column(name = "condicio", length = 15, nullable = false)
	private AlarmaConfigCondicio condicio;
	@Column(name = "valor", nullable = false)
	private BigDecimal valor;
	@Column(name = "admin", nullable = false)
	private boolean admin;
	@Column(name = "periode_unitat", length = 10)
	private AlarmaConfigPeriodeUnitat periodeUnitat;
	@Column(name = "periode_valor")
	private BigDecimal periodeValor;

	@Builder
	public AlarmaConfigEntity(AlarmaConfig alarmaConfig) {
		this.entornAppId = alarmaConfig.getEntornAppId();
		this.nom = alarmaConfig.getNom();
		this.missatge = alarmaConfig.getMissatge();
		this.tipus = alarmaConfig.getTipus();
		this.condicio = alarmaConfig.getCondicio();
		this.valor = alarmaConfig.getValor();
		this.periodeUnitat = alarmaConfig.getPeriodeUnitat();
		this.periodeValor = alarmaConfig.getPeriodeValor();
		this.admin = alarmaConfig.getAdmin();
	}

}
