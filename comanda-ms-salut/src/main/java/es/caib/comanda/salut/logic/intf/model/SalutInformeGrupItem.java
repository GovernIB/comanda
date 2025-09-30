package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.salut.persist.entity.SalutEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Informació d'una agrupació temporal de salut
 *
 * @author Límit Tecnologies
 */
@Getter
@AllArgsConstructor
public class SalutInformeGrupItem implements Serializable {
	private LocalDateTime data;
}
