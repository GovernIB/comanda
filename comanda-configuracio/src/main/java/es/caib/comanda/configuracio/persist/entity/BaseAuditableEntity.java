package es.caib.comanda.configuracio.persist.entity;

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
 * @param <PK> classe de la clau primària de l'entitat.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditableEntity<PK extends Serializable> implements AuditableEntity, Persistable<PK> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private @Nullable PK id;

	@CreatedBy
	@Column(name = "created_by", length = 64, nullable = false)
	private String createdBy;
	@CreatedDate
	@Column(name = "created_date", nullable = false)
	private LocalDateTime createdDate;
	@LastModifiedBy
	@Column(name = "lastmod_by", length = 64)
	private String lastModifiedBy;
	@LastModifiedDate
	@Column(name = "lastmod_date")
	private LocalDateTime lastModifiedDate;
	@Version
	@Getter(AccessLevel.NONE)
	@Column(name = "v")
	private long v;

	@Override
	public PK getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return null == getId();
	}

	@Override
	public void updateCreated(
			String createdBy,
			LocalDateTime createdDate) {
		this.createdBy = createdBy;
		this.createdDate = (createdDate != null) ? createdDate : LocalDateTime.now();
	}

	@Override
	public void updateLastModified(
			String lastModifiedBy,
			LocalDateTime lastModifiedDate) {
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDate = (lastModifiedDate != null) ? lastModifiedDate : LocalDateTime.now();
	}

}