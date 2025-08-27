package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.salut.persist.entity.SalutEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Informació per a l'informe de latència del recurs salut.
 *
 * @author Límit Tecnologies
 */
@Getter
@AllArgsConstructor
public class SalutInformeLatenciaItem implements Serializable {

	private LocalDateTime data;
	private Integer latenciaMitja;

	public SalutInformeLatenciaItem(
			Date dataAgrupacio,
			Integer latenciaMitja) {
		this.data = dataAgrupacio.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		this.latenciaMitja = latenciaMitja;
	}

    public SalutInformeLatenciaItem(SalutEntity salutEntity, Integer minuteOffset) {
        switch (salutEntity.getTipusRegistre()) {
            case HORA:
                this.data = salutEntity.getData().withMinute(0).withSecond(0);
                break;
            case DIA:
                this.data = salutEntity.getData().withHour(0).withMinute(0).withSecond(0);
                break;
            case MINUTS:
                this.data = salutEntity.getData().plusMinutes(minuteOffset).withSecond(0);
                break;
            default:
                this.data = salutEntity.getData().withSecond(0);
        }
        this.latenciaMitja = salutEntity.getAppLatenciaMitjana();
    }

}
