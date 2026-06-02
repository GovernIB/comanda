package es.caib.comanda.alarmes.persist.entity;

import es.caib.comanda.alarmes.logic.intf.model.*;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import es.caib.comanda.ms.persist.entity.ReorderableEntity;
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
public class AlarmaConfigEntity extends BaseAuditableEntity<AlarmaConfig> implements ReorderableEntity<Long> {

	@Column(name = "entorn_app_id", nullable = false)
	private Long entornAppId;
	// Camp intern usat per fer soft-delete de AlarmaConfig
	@Column(name = "esborrat", nullable = false)
	private boolean esborrat;

	@Column(name = "nom", length = 200)
	private String nom;
	@Column(name = "missatge", length = 1024, nullable = false)
	private String missatge;
	@Column(name = "rule_version")
	private Integer ruleVersion;
	@Lob
	@Column(name = "rule_json")
	private String ruleJson;
	@Column(name = "admin", nullable = false)
	private boolean admin;
	@Column(name = "correu_generic", nullable = false)
	private boolean correuGeneric;
	@Column(name = "periode_unitat", length = 10)
    @Enumerated(EnumType.STRING)
	private AlarmaConfigPeriodeUnitat periodeUnitat;
	@Column(name = "periode_valor")
	private BigDecimal periodeValor;
    @Column(name = "notificacio_finalitzada")
    private boolean notificacioFinalitzada = true;
	@Column(name = "ordre")
	private Long ordre;
	@Column(name = "aturar_avaluacio_posteriors")
	private boolean aturarAvaluacioPosteriors = false;

	@Builder
	public AlarmaConfigEntity(AlarmaConfig alarmaConfig) {
		this.entornAppId = alarmaConfig.getEntornAppId();
		this.nom = alarmaConfig.getNom();
		this.missatge = alarmaConfig.getMissatge();
		this.periodeUnitat = alarmaConfig.getPeriodeUnitat();
		this.periodeValor = alarmaConfig.getPeriodeValor();
		this.admin = alarmaConfig.isAdmin();
		this.correuGeneric = alarmaConfig.isCorreuGeneric();
		this.notificacioFinalitzada = alarmaConfig.isNotificacioFinalitzada();
		this.aturarAvaluacioPosteriors = alarmaConfig.isAturarAvaluacioPosteriors();
	}

	// El parentId fa referència al mateix AlarmaConfigEntity per a poder
	// recuperar la row actual al mètode reorderFindLinesWithParent
	@Override
	public Long getOrderParentId() {
		// S'assumeix que no s'usarà la reordenació per canviar de entornApp, ja que baseboot fa una
		// comprovació de parentIdChanged que no quedarà reflectida aquí si es canvia l'entornApp.
		return getId();
	}

	@Override
	public Long getOrder() {
		return ordre;
	}

	@Override
	public void setOrder(Long order) {
		this.ordre = order;
	}
}
