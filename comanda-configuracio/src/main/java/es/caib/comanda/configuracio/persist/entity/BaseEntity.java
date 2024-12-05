package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.model.Resource;
import liquibase.pro.packaged.E;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base per a definir les demés entitats de l'aplicació.
 *
 * @param <E> classe del recurs associat.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity<E> implements ResourceEntity<E, Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private @Nullable Long id;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return null == getId();
	}

}