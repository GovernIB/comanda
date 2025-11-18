package es.caib.comanda.acl.persist.entity;

import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.ms.persist.entity.ResourceEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.Id;

/**
 * Mapping lleuger per mantenir un identificador estable d'API per a AclEntry.
 * Les dades reals d'autorització es desen en les taules Spring ACL (com_acl_*).
 */
@Getter
@Setter
@NoArgsConstructor
public class AclEntryEntity implements ResourceEntity<AclEntry, String> {

	@Id
	private @Nullable String id;
	private AclEntry resource;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return null == getId();
	}

	@Builder
	public AclEntryEntity(
			String id,
			AclEntry resource) {
		this.id = id;
		this.resource = resource;
	}

}
