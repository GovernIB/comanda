package es.caib.comanda.alarmes.persist.entity;

import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entitat de base de dades que emmagatzema la informació d'una alarma.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "alarma")
@Getter
@Setter
@NoArgsConstructor
public class AlarmaEntity extends BaseAuditableEntity<Alarma> {

	@Column(name = "entorn_app_id", nullable = false)
	private Long entornAppId;

	@Column(name = "missatge", length = 1024, nullable = false)
	private String missatge;
	@Enumerated(EnumType.STRING)
	@Column(name = "estat", length = 10, nullable = false)
	private AlarmaEstat estat;

	/** Data d'inici de l'esdeveniment que ha generat l'alarma */
	@Column(name = "data_activacio")
	private LocalDateTime dataActivacio;
	// TODO actualment, aquest camp no té cap ús, possiblement s'hauria d'esborrar
	@Deprecated
	@Column(name = "data_enviament")
	private LocalDateTime dataEnviament;
	/** Data en què es va marcar com a llegida l'alarma */
	@Column(name = "data_esborrat")
	private LocalDateTime dataEsborrat;
	/** Data en què l'esdeveniment de l'alarma ha deixat de complir les condicions establertes */
	@Column(name = "data_finalitzacio")
	private LocalDateTime dataFinalitzacio;
	/**
	 * Data en què s'ha iniciat la recuperació de l'alarma.
	 * Les alarmes han de superar el temps d'estabilitat definit a PROP_ALARMA_RECOVERY_STABILITY_SECONDS abans de finalitzar-se.
	 */
	@Column(name = "data_inici_recuperacio")
	private LocalDateTime dataIniciRecuperacio;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "alarma_config_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "alarma_alarmaconf_fk"))
	private AlarmaConfigEntity alarmaConfig;

	@Builder
	public AlarmaEntity(
			Alarma alarma,
			AlarmaConfigEntity alarmaConfig) {
		this.entornAppId = alarma.getEntornAppId();
		this.missatge = alarma.getMissatge();
		this.estat = alarma.getEstat();
        this.dataActivacio = alarma.getDataActivacio();
        this.dataEnviament = alarma.getDataEnviament();
        this.dataEsborrat = alarma.getDataEsborrat();
        this.dataFinalitzacio = alarma.getDataFinalitzacio();
        this.dataIniciRecuperacio = alarma.getDataIniciRecuperacio();
		this.alarmaConfig = alarmaConfig;
	}

}
