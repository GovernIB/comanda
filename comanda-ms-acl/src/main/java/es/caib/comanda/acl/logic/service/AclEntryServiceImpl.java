package es.caib.comanda.acl.logic.service;

import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.logic.intf.service.AclEntryService;
import es.caib.comanda.acl.persist.entity.AclEntryMapEntity;
import es.caib.comanda.acl.persist.repository.AclEntryMapRepository;
import es.caib.comanda.client.model.acl.AclAction;
import es.caib.comanda.client.model.acl.AclEffect;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.client.model.acl.SubjectType;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException.AnswerValue;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AclEntryServiceImpl extends BaseMutableResourceService<AclEntry, Long, AclEntryMapEntity> implements AclEntryService {

    private final AclEntryMapRepository aclEntryMapRepository;
    private final MutableAclService mutableAclService;

//    @Override
//    protected Class<AclEntry> getResourceClass() {
//        return AclEntry.class;
//    }
//
//    @Override
//    protected Class<AclEntryMapEntity> getEntityClass() {
//        return AclEntryMapEntity.class;
//    }

    private List<Permission> toSpringPermissions(AclAction action) {
        switch (action) {
            case READ:
                return Collections.singletonList(BasePermission.READ);
            case WRITE:
                // WRITE should not imply ADMINISTRATION
                return Collections.singletonList(BasePermission.WRITE);
            case ADMIN:
            default:
                return Collections.singletonList(BasePermission.ADMINISTRATION);
        }
    }

    public String cacheKey(String user, Collection<String> roles, ResourceType resourceType, Long resourceId, AclAction action) {
        String rolesStr = roles == null ? "" : roles.stream().sorted().collect(Collectors.joining(","));
        return String.join("|",
                Optional.ofNullable(user).orElse(""),
                rolesStr,
                resourceType.name(),
                String.valueOf(resourceId),
                action.name());
    }

    private ObjectIdentity buildObjectIdentity(ResourceType resourceType, Long resourceId) {
        return new ObjectIdentityImpl(resourceType.name(), resourceId);
    }

    private List<Sid> buildSids(String user, List<String> roles) {
        List<Sid> sids = new ArrayList<>();
        if (user != null && !user.isBlank()) {
            sids.add(new PrincipalSid(user));
        }
        if (!CollectionUtils.isEmpty(roles)) {
            for (String r : roles) {
                if (r != null && !r.isBlank()) sids.add(new GrantedAuthoritySid(r));
            }
        }
        return sids;
    }

    @Override
    @Cacheable(value = "aclCheckCache", key = "#root.target.cacheKey(#user, #roles, #resourceType, #resourceId, #action)")
    public boolean checkPermission(String user, List<String> roles, ResourceType resourceType, Long resourceId, AclAction action) {
        try {
            ObjectIdentity oi = buildObjectIdentity(resourceType, resourceId);
            List<Sid> sids = buildSids(user, roles);
            if (sids.isEmpty()) return false;
            List<Permission> permissions = toSpringPermissions(action);
            Acl acl = mutableAclService.readAclById(oi, sids);
            return acl.isGranted(permissions, sids, false);
        } catch (org.springframework.security.acls.model.NotFoundException nf) {
            log.debug("No ACL found in Spring ACL for {}:{}", resourceType, resourceId);
            return false;
        } catch (Exception e) {
            log.warn("Spring ACL check error (type={}, id={})", resourceType, resourceId, e);
            return false;
        }
    }

    private void syncSpringAclForResource(ResourceType resourceType, Long resourceId) {
        // Ensure an Authentication exists for Spring ACL createAcl() which requires a non-null principal
        org.springframework.security.core.Authentication prevAuth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean tempAuthSet = false;
        if (prevAuth == null) {
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken tempAuth =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            "system", "N/A",
                            java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(tempAuth);
            tempAuthSet = true;
        }
        try {
            ObjectIdentity oi = buildObjectIdentity(resourceType, resourceId);
            MutableAcl acl;
            try {
                acl = (MutableAcl) mutableAclService.readAclById(oi);
                for (int i = acl.getEntries().size() - 1; i >= 0; i--) {
                    acl.deleteAce(i);
                }
            } catch (org.springframework.security.acls.model.NotFoundException nf) {
                acl = (MutableAcl) mutableAclService.createAcl(oi);
            }
            // Reconstrueix ACEs a partir de mapping table
            List<AclEntryMapEntity> all = aclEntryMapRepository.findAllByResource(resourceType, resourceId);
            int index = 0;
            for (AclEntryMapEntity e : all) {
                boolean granting = e.getEffect() == AclEffect.ALLOW;
                Sid sid = (e.getSubjectType() == SubjectType.USER) ? new PrincipalSid(e.getSubjectValue()) : new GrantedAuthoritySid(e.getSubjectValue());
                List<Permission> perms = toSpringPermissions(e.getAction());
                for (Permission p : perms) {
                    acl.insertAce(index++, p, sid, granting);
                }
            }
            mutableAclService.updateAcl(acl);
        } finally {
            if (tempAuthSet) {
                // Clear temporary authentication
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
            }
        }
    }

    // Invalidate cache en canvis i sincronitza Spring ACL
    @Override
    @Transactional
    @CacheEvict(value = "aclCheckCache", allEntries = true)
    public AclEntry create(AclEntry resource, Map<String, AnswerValue> answers) {
        AclEntry created = super.create(resource, answers);
        syncSpringAclForResource(resource.getResourceType(), resource.getResourceId());
        return created;
    }

    @Override
    @Transactional
    @CacheEvict(value = "aclCheckCache", allEntries = true)
    public AclEntry update(Long id, AclEntry resource, Map<String, AnswerValue> answers) throws ResourceNotFoundException {
        AclEntry updated = super.update(id, resource, answers);
        syncSpringAclForResource(resource.getResourceType(), resource.getResourceId());
        return updated;
    }

    @Override
    @Transactional
    @CacheEvict(value = "aclCheckCache", allEntries = true)
    public void delete(Long id, Map<String, AnswerValue> answers) throws ResourceNotFoundException {
        AclEntryMapEntity entity = getEntity(id, null);
        ResourceType type = entity.getResourceType();
        Long rid = entity.getResourceId();
        super.delete(id, answers);
        syncSpringAclForResource(type, rid);
    }

    @Override
    public boolean checkPermissionsAny(String user, List<String> roles, ResourceType resourceType, Long resourceId, List<AclAction> actions) {
        if (actions == null || actions.isEmpty()) return false;
        for (AclAction a : actions) {
            if (checkPermission(user, roles, resourceType, resourceId, a)) return true;
        }
        return false;
    }

    @Override
    public boolean checkPermissionsAll(String user, List<String> roles, ResourceType resourceType, Long resourceId, List<AclAction> actions) {
        if (actions == null || actions.isEmpty()) return false;
        for (AclAction a : actions) {
            if (!checkPermission(user, roles, resourceType, resourceId, a)) return false;
        }
        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "aclCheckCache", allEntries = true)
    public List<AclEntry> createAll(List<AclEntry> entries) {
        if (entries == null || entries.isEmpty()) return Collections.emptyList();
        List<AclEntry> created = new ArrayList<>();
        for (AclEntry e : entries) {
            AclEntry c = super.create(e, Collections.emptyMap());
            created.add(c);
            syncSpringAclForResource(c.getResourceType(), c.getResourceId());
        }
        return created;
    }

    @Override
    @Transactional
    @CacheEvict(value = "aclCheckCache", allEntries = true)
    public List<AclEntry> updateAll(List<AclEntry> entries) {
        if (entries == null || entries.isEmpty()) return Collections.emptyList();
        List<AclEntry> updated = new ArrayList<>();
        for (AclEntry e : entries) {
            if (e.getId() == null) continue;
            try {
                AclEntry u = super.update(e.getId(), e, Collections.emptyMap());
                updated.add(u);
                syncSpringAclForResource(u.getResourceType(), u.getResourceId());
            } catch (ResourceNotFoundException ex) {
                log.debug("ACL entry not found for bulk update: {}", e.getId());
            }
        }
        return updated;
    }

    @Override
    @Transactional
    @CacheEvict(value = "aclCheckCache", allEntries = true)
    public void deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        for (Long id : ids) {
            try {
                AclEntryMapEntity entity = getEntity(id, null);
                ResourceType type = entity.getResourceType();
                Long rid = entity.getResourceId();
                super.delete(id, Collections.emptyMap());
                syncSpringAclForResource(type, rid);
            } catch (ResourceNotFoundException ex) {
                log.debug("ACL entry not found for bulk delete: {}", id);
            }
        }
    }
}
