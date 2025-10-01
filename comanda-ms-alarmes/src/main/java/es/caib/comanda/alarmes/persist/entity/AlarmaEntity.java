package es.caib.comanda.alarmes.persist.entity;

import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import es.caib.comanda.ms.persist.entity.BaseEntity;
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
	@Column(name = "data_activacio")
	private LocalDateTime dataActivacio;
	@Column(name = "data_enviament")
	private LocalDateTime dataEnviament;
	@Column(name = "data_esborrat")
	private LocalDateTime dataEsborrat;

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
		this.alarmaConfig = alarmaConfig;
	}

}
