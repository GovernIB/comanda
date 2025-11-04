package es.caib.comanda.acl.persist.repository;

import es.caib.comanda.acl.persist.entity.AclEntryMapEntity;
import es.caib.comanda.acl.persist.enums.AclAction;
import es.caib.comanda.acl.persist.enums.ResourceType;
import es.caib.comanda.acl.persist.enums.SubjectType;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AclEntryMapRepository extends BaseRepository<AclEntryMapEntity, Long> {

    @Query("select e from AclEntryMapEntity e " +
            "where e.resourceType = :resourceType and e.resourceId = :resourceId " +
            "and e.action in (:actions) " +
            "and ((e.subjectType = 'USER' and e.subjectValue = :user) " +
            "     or (e.subjectType = 'ROLE' and e.subjectValue in (:roles)))")
    List<AclEntryMapEntity> findForUserAndRoles(
            @Param("user") String user,
            @Param("roles") Collection<String> roles,
            @Param("resourceType") ResourceType resourceType,
            @Param("resourceId") Long resourceId,
            @Param("actions") Collection<AclAction> actions);

    @Query("select e from AclEntryMapEntity e " +
            "where e.resourceType = :resourceType and e.resourceId = :resourceId " +
            "and e.action in (:actions) " +
            "and e.subjectType = :subjectType and e.subjectValue = :subjectValue")
    List<AclEntryMapEntity> findForSubject(
            @Param("subjectType") SubjectType subjectType,
            @Param("subjectValue") String subjectValue,
            @Param("resourceType") ResourceType resourceType,
            @Param("resourceId") Long resourceId,
            @Param("actions") Collection<AclAction> actions);

    @Query("select e from AclEntryMapEntity e " +
            "where e.resourceType = :resourceType and e.resourceId = :resourceId order by e.effect asc")
    List<AclEntryMapEntity> findAllByResource(
            @Param("resourceType") ResourceType resourceType,
            @Param("resourceId") Long resourceId);
}
