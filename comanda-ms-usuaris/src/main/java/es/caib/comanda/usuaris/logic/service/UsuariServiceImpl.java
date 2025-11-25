package es.caib.comanda.usuaris.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.config.WebSecurityConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.usuaris.logic.intf.model.LanguageEnum;
import es.caib.comanda.usuaris.logic.intf.model.NumOfElementsPerPageENum;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;
import es.caib.comanda.usuaris.logic.intf.service.UsuariService;
import es.caib.comanda.usuaris.persist.entity.UsuariEntity;
import es.caib.comanda.usuaris.persist.repository.UsuariRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UsuariServiceImpl extends BaseMutableResourceService<Usuari, Long, UsuariEntity> implements UsuariService {

	@Value("${" + BaseConfig.PROP_SECURITY_MAPPABLE_ROLES + ":" +
			BaseConfig.ROLE_ADMIN + "," +
			BaseConfig.ROLE_CONSULTA + "}")
	private String mappableRoles;

	private final AuthenticationHelper authenticationHelper;

	@Override
	protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries) {
		List<Filter> filters = new ArrayList<>();
		if (currentSpringFilter != null && !currentSpringFilter.isEmpty()) {
			filters.add(Filter.parse(currentSpringFilter));
		}
		filters.add(FilterBuilder.equal("codi", authenticationHelper.getCurrentUserName()));
		return FilterBuilder.and(filters).generate();
	}

	@Override
	protected void afterConversion(UsuariEntity entity, Usuari resource) {
		Usuari usuariAuth = getUsuariFromAuth();
		resource.setRols(usuariAuth.getRols());
	}

	@Override
	public void refreshCurrentUser() {
		Optional<UsuariEntity> usuariEntityOptional = ((UsuariRepository)entityRepository).findByCodi(
				authenticationHelper.getCurrentUserName());
		UsuariEntity usuariEntity;
		Usuari usuariAuth = getUsuariFromAuth();
		if (usuariEntityOptional.isPresent()) {
			usuariEntity = usuariEntityOptional.get();
		} else {
			usuariEntity = new UsuariEntity();
			usuariEntity.setCodi(usuariAuth.getCodi());
			usuariEntity.setIdioma(LanguageEnum.CA);
			usuariEntity.setNumElementsPagina(NumOfElementsPerPageENum.AUTOMATIC);
		}
		usuariEntity.setNom(usuariAuth.getNom());
		usuariEntity.setNif(usuariAuth.getNif());
		usuariEntity.setEmail(usuariAuth.getEmail());
		entityRepository.save(usuariEntity);
	}

	private Usuari getUsuariFromAuth() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Usuari usuari = new Usuari();
		if (authentication.getPrincipal() instanceof Jwt) {
			// Authenticació provinent de Spring Boot
			Jwt jwt = (Jwt)authentication.getPrincipal();
			usuari.setCodi(authentication.getName());
			usuari.setNom(jwt.getClaimAsString("name"));
			usuari.setNif(jwt.getClaimAsString("nif"));
			usuari.setEmail(jwt.getClaimAsString("email"));
			Set<String> roles = extractRolesFromJwt(jwt);
			usuari.setRols(roles.toArray(new String[0]));
		} else if (authentication.getPrincipal() instanceof User) {
			// Authenticació provinent de JBoss
			WebSecurityConfig.PreauthWebAuthenticationDetails authDetails = (WebSecurityConfig.PreauthWebAuthenticationDetails)authentication.getDetails();
			usuari.setCodi(authDetails.getPreferredUsername());
			usuari.setNom(authDetails.getName());
			usuari.setNif(authDetails.getNif());
			usuari.setEmail(authDetails.getEmail());
			usuari.setRols(authDetails.getOriginalRoles());
		} else {
			// TODO Millorar excepció
			throw new RuntimeException("Authentication principal not supported: " + authentication.getPrincipal().getClass().getName());
		}
		return usuari;
	}

	protected Set<String> extractRolesFromJwt(Jwt jwt) {
		Set<String> roles = new HashSet<>();
		// Recuperam els rols a nivell de REALM
		Map<String, Object> realmAccess = jwt.getClaim("realm_access");
		if (realmAccess != null && !realmAccess.isEmpty()) {
			List<String> realmRoles = ((List<String>)realmAccess.get("roles"));
			if (realmRoles != null && !realmRoles.isEmpty()) {
				roles.addAll(realmRoles);
			}
		}
		// Obtenim el clientId (al claim "azp")
		String clientId = jwt.getClaim("azp");
		// Recuperam els rols del client
		if (clientId != null && !clientId.isEmpty()) {
			Map<String, Object> resourceAccess = (Map<String, Object>)jwt.getClaims().get("resource_access");
			if (resourceAccess != null && !resourceAccess.isEmpty()) {
				Map<String, Object> clientAccess = (Map<String, Object>)resourceAccess.get(clientId);
				if (clientAccess != null && !clientAccess.isEmpty()) {
					List<String> clientRoles = ((List<String>)clientAccess.get("roles"));
					if (clientRoles != null && !clientRoles.isEmpty()) {
						roles.addAll(clientRoles);
					}
				}
			}
		}
		Set<String> allowedRoles = Set.of(mappableRoles.split(","));
		roles.removeIf(r -> !allowedRoles.contains(r));
		return roles;
	}

}
