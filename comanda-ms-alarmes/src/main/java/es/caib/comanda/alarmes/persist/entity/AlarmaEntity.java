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
	@Column(name = "notificada", nullable = false)
	private boolean notificada;

	@Builder
	public AlarmaEntity(Alarma alarma) {
		this.entornAppId = alarma.getEntornAppId();
		this.missatge = alarma.getMissatge();
		this.estat = alarma.getEstat();
		this.notificada = alarma.isNotificada();
	}

}
