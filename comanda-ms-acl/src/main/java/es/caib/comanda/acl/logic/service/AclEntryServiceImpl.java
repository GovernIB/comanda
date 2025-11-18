package es.caib.comanda.acl.logic.service;

import es.caib.comanda.acl.logic.helper.AclHelper;
import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.logic.intf.service.AclEntryService;
import es.caib.comanda.acl.persist.entity.AclEntryEntity;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.client.model.acl.SubjectType;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AclEntryServiceImpl extends BaseMutableResourceService<AclEntry, String, AclEntryEntity> implements AclEntryService {

	private final AclHelper aclHelper;

	protected boolean isEntityRepositoryOptional() {
		return true;
	}

	@Override
	protected Optional<AclEntryEntity> entityRepositoryFindOne(String id) {
		AclEntry.AclEntryPk pk = AclEntry.AclEntryPk.deserializeFromString(id);
		Class<?> resourceClass = getClassFromResourceType(pk.getResourceType());
		if (resourceClass != null) {
			Acl acl = aclHelper.get(
					resourceClass,
					pk.getResourceId(),
					null);
			if (acl != null) {
				Optional<AccessControlEntry> entry = acl.getEntries().stream().
						filter(e -> e.getId().equals(pk.getEntryId())).
						findFirst();
				if (entry.isPresent()) {
					return Optional.of(
							formAccessControlEntryToAclEntryEntity(entry.get(), pk));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	protected Page<AclEntryEntity> entityRepositoryFindEntities(
			String quickFilter,
			String filter,
			String[] namedQueries,
			Pageable pageable) {
		boolean filterContainsOr = filter != null && filter.contains(" or ");
		List<String[]> filterTriplets = extractFilterTriplets(filter);
		String filterResourceType = filterTriplets.stream().
				filter(t -> t[0].equals("resourceType") && t[1].equals(":")).
				findFirst().
				map(t-> t[2]).
				orElse(null);
		String filterResourceId = filterTriplets.stream().
				filter(t -> t[0].equals("resourceId") && t[1].equals(":")).
				findFirst().
				map(t-> t[2]).
				orElse(null);
		boolean filterOk = filter != null && !filterContainsOr && filterResourceType != null && filterResourceId != null;
		if (filterOk) {
			Class<?> resourceClass = getClassFromResourceType(ResourceType.valueOf(filterResourceType));
			Long resourceId = Long.parseLong(filterResourceId);
			Acl acl = aclHelper.get(
					resourceClass,
					filterResourceId,
					null);
			if (acl != null) {
				List<AclEntryEntity> resultList = acl.getEntries().stream().
						map(e -> {
							AclEntry.AclEntryPk pk = new AclEntry.AclEntryPk(
									getResourceTypeFromClass(resourceClass),
									resourceId,
									(Long) e.getId());
							return formAccessControlEntryToAclEntryEntity(e, pk);
						}).
						collect(Collectors.toList());
				return new PageImpl<>(resultList, pageable, resultList.size());
			} else {
				return Page.empty();
			}
		} else {
			throw new RuntimeException("Filtre no suportat");
		}
	}

	@Override
	protected AclEntryEntity entitySaveFlushAndRefresh(AclEntryEntity entity) {
		AclEntry resource = entity.getResource();
		List<PermissionEnum> permissionsGranted = new ArrayList<>();
		if (resource.isReadAllowed()) permissionsGranted.add(PermissionEnum.READ);
		if (resource.isWriteAllowed()) permissionsGranted.add(PermissionEnum.WRITE);
		if (resource.isCreateAllowed()) permissionsGranted.add(PermissionEnum.CREATE);
		if (resource.isDeleteAllowed()) permissionsGranted.add(PermissionEnum.DELETE);
		if (resource.isAdminAllowed()) permissionsGranted.add(PermissionEnum.ADMINISTRATION);
		if (resource.isPerm0Allowed()) permissionsGranted.add(PermissionEnum.PERM0);
		if (resource.isPerm1Allowed()) permissionsGranted.add(PermissionEnum.PERM1);
		if (resource.isPerm2Allowed()) permissionsGranted.add(PermissionEnum.PERM2);
		if (resource.isPerm3Allowed()) permissionsGranted.add(PermissionEnum.PERM3);
		if (resource.isPerm4Allowed()) permissionsGranted.add(PermissionEnum.PERM4);
		if (resource.isPerm5Allowed()) permissionsGranted.add(PermissionEnum.PERM5);
		if (resource.isPerm6Allowed()) permissionsGranted.add(PermissionEnum.PERM6);
		if (resource.isPerm7Allowed()) permissionsGranted.add(PermissionEnum.PERM7);
		if (resource.isPerm8Allowed()) permissionsGranted.add(PermissionEnum.PERM8);
		if (resource.isPerm9Allowed()) permissionsGranted.add(PermissionEnum.PERM9);
		aclHelper.set(
				getClassFromResourceType(resource.getResourceType()),
				resource.getResourceId(),
				resource.getSubjectName(),
				resource.getSubjectType().equals(SubjectType.ROLE),
				permissionsGranted);
		return entity;
	}

	@Override
	protected AclEntry entityDetachConvertAndMerge(
			AclEntryEntity entity,
			Map<String, AnswerRequiredException.AnswerValue> answers,
			boolean create) {
		AclEntry response = entityToResource(entity);
		entityAfterMergeLogic(response, entity, answers, create);
		return response;
	}

	@Override
	protected void entityRepositoryDelete(AclEntryEntity entity) {
		AclEntry resource = entity.getResource();
		aclHelper.delete(
				getClassFromResourceType(resource.getResourceType()),
				resource.getResourceId(),
				resource.getSubjectName(),
				resource.getSubjectType().equals(SubjectType.ROLE));
	}

	@Override
	protected void entityRepositoryFlush() {
	}

	@Override
	protected AclEntry entityToResource(AclEntryEntity entity) {
		return entity.getResource();
	}

	protected AclEntryEntity resourceToEntity(
			AclEntry resource,
			String pk,
			Map<String, Persistable<?>> referencedEntities) {
		return AclEntryEntity.builder().
				id(pk).
				resource(resource).
				build();
	}

	protected void updateEntityWithResource(
			AclEntryEntity entity,
			AclEntry resource,
			Map<String, Persistable<?>> referencedEntities) {
		entity.setResource(resource);
	}

	private AclEntryEntity formAccessControlEntryToAclEntryEntity(
			AccessControlEntry ace,
			AclEntry.AclEntryPk pk) {
		AclEntry aclEntry = new AclEntry();
		Sid sid = ace.getSid();
		if (sid instanceof PrincipalSid) {
			aclEntry.setSubjectType(SubjectType.USER);
			aclEntry.setSubjectValue(((PrincipalSid)sid).getPrincipal());
		} else if (sid instanceof GrantedAuthoritySid) {
			aclEntry.setSubjectType(SubjectType.ROLE);
			aclEntry.setSubjectValue(((GrantedAuthoritySid)sid).getGrantedAuthority());
		}
		aclEntry.setResourceType(pk.getResourceType());
		aclEntry.setResourceId(pk.getResourceId());
		return resourceToEntity(
				aclEntry,
				pk.serializeToString(),
				null);
	}

	private static final Pattern TRIPLET_PATTERN = Pattern.compile(
			"([a-zA-Z_][a-zA-Z0-9_\\.]*)" +
					"\\s*" +
					"(:|=|!=|>=|<=|>|<|~|!~|\\bin\\b)" +
					"\\s*" +
					"(\\[[^\\]]*\\]|'[^']*'|\"[^\"]*\"|\\d+\\.?\\d*|[^\\s\\)]+)",
			Pattern.CASE_INSENSITIVE);

	private List<String[]> extractFilterTriplets(String filter) {
		Matcher matcher = TRIPLET_PATTERN.matcher(filter);
		List<String[]> triplets = new ArrayList<>();
		while (matcher.find()) {
			String field = matcher.group(1);
			String op = matcher.group(2);
			String value = matcher.group(3);
			if (value.startsWith("'") && value.endsWith("'")) {
				value = value.substring(1, value.length() - 1);
			}
			triplets.add(new String[]{field, op, value});
		}
		return triplets;
	}

	private Class<?> getClassFromResourceType(ResourceType resourceType) {
		if (resourceType != null) {
			try {
				switch (resourceType) {
					case ENTORN_APP:
						return Class.forName("es.caib.comanda.client.model.EntornApp");
					case DASHBOARD:
						return Class.forName("es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard");
				}
			} catch (Exception ex) {
				log.error("Couldn't find class for resource type " + resourceType);
			}
		}
		return null;
	}

	private ResourceType getResourceTypeFromClass(Class<?> resourceClass) {
		if (resourceClass != null) {
			if (resourceClass.getName().equals("es.caib.comanda.client.model.EntornApp")) {
				return ResourceType.ENTORN_APP;
			} else if (resourceClass.getName().equals("es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard")) {
				return ResourceType.DASHBOARD;
			} else {
				log.error("Couldn't find ResourceType for class " + resourceClass);
			}
		}
		return null;
	}

	/*
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
        AclEntryMapEntity entity = getEntity(id);
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
                AclEntryMapEntity entity = getEntity(id);
                ResourceType type = entity.getResourceType();
                Long rid = entity.getResourceId();
                super.delete(id, Collections.emptyMap());
                syncSpringAclForResource(type, rid);
            } catch (ResourceNotFoundException ex) {
                log.debug("ACL entry not found for bulk delete: {}", id);
            }
        }
    }
	 */

}
