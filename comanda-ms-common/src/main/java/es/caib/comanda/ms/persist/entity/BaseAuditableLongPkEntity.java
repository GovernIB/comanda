package es.caib.comanda.ms.persist.entity;

import es.caib.comanda.ms.logic.intf.model.Resource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;

/**
 * Base per a definir les altres entitats de l'aplicació amb PK de tipus Long
 *
 * @param <R> classe del recurs associat.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseAuditableLongPkEntity<R extends Resource<?>> extends BaseAuditableEntity<R, Long> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private @Nullable Long id;

	@Override
	public boolean isNew() {
		return null == getId();
	}
}