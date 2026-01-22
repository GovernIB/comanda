package es.caib.comanda.alarmes.persist.entity;

import es.caib.comanda.alarmes.logic.intf.model.*;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entitat de base de dades que emmagatzema els usuaris destinataris d'una alarma.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "alarma_usuari")
@Getter
@Setter
@NoArgsConstructor
public class AlarmaUsuariEntity extends BaseEntity<AlarmaUsuari> {

	@Column(name = "usuari", length = 100, nullable = false)
	private String usuari;
	@Column(name = "llegida", nullable = false)
	private boolean llegida;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "alarma_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "alarmausu_alarma_fk"))
	private AlarmaEntity alarma;

	@Builder
	public AlarmaUsuariEntity(
			AlarmaUsuari alarmaUsuari,
			AlarmaEntity alarma) {
		this.usuari = alarmaUsuari.getUsuari();
		this.llegida = alarmaUsuari.isLlegida();
		this.alarma = alarma;
	}

}
