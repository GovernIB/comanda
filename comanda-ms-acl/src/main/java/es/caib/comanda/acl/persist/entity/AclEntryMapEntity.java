package es.caib.comanda.acl.persist.entity;

import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.client.model.acl.AclAction;
import es.caib.comanda.client.model.acl.AclEffect;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.client.model.acl.SubjectType;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Mapping lleuger per mantenir un identificador estable d'API per a AclEntry.
 * Les dades reals d'autorització es desen en les taules Spring ACL (com_acl_*).
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "acl_entry_map",
        indexes = {
                @Index(name = BaseConfig.DB_PREFIX + "acl_entry_map_idx_subject", columnList = "subject_type, subject_value"),
                @Index(name = BaseConfig.DB_PREFIX + "acl_entry_map_idx_resource", columnList = "resource_type, resource_id"),
        })
@Getter
@Setter
@NoArgsConstructor
public class AclEntryMapEntity extends BaseAuditableEntity<AclEntry> {

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", length = 16, nullable = false)
    private SubjectType subjectType;

    @Column(name = "subject_value", length = 128, nullable = false)
    private String subjectValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 32, nullable = false)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

//    @Column(name = "entorn_app_id")
//    private Long entornAppId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 16, nullable = false)
    private AclAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect", length = 8, nullable = false)
    private AclEffect effect;
}
