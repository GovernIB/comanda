package es.caib.comanda.usuaris.logic.helper;

import es.caib.comanda.ms.back.config.WebSecurityConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.usuaris.logic.intf.model.*;
import es.caib.comanda.usuaris.persist.entity.UsuariEntity;
import es.caib.comanda.usuaris.persist.repository.UsuariRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarisAuthHelper {

	@Value("${" + WebSecurityConfig.MAPPABLE_ROLES_SOURCE + "}")
	private String mappableRoles;

	private final UsuariRepository usuariRepository;
	private final AuthenticationHelper authenticationHelper;

	public void refreshCurrentUser() {
		Optional<UsuariEntity> usuariEntityOptional = usuariRepository.findByCodi(
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
			usuariEntity.setTemaAplicacio(TemaAplicacioEnum.SISTEMA);
			usuariEntity.setEstilMenu(MenuEstilEnum.TEMA);
			usuariEntity.setAlarmaMail(true);
		}
		if (usuariEntity.getTemaAplicacio() == null) {
			usuariEntity.setTemaAplicacio(TemaAplicacioEnum.SISTEMA);
		}
		if (usuariEntity.getEstilMenu() == null) {
			usuariEntity.setEstilMenu(MenuEstilEnum.TEMA);
		}
		usuariEntity.setNom(usuariAuth.getNom());
		usuariEntity.setNif(usuariAuth.getNif());
		usuariEntity.setEmail(usuariAuth.getEmail());
		usuariRepository.save(usuariEntity);
	}

	public Usuari getUsuariFromAuth() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Usuari usuari = new Usuari();
		if (authentication.getPrincipal() instanceof Jwt) {
			// Authenticació provinent de Spring Boot
			Jwt jwt = (Jwt)authentication.getPrincipal();
			usuari.setCodi(authentication.getName());
			usuari.setNom(jwt.getClaimAsString("name"));
			usuari.setNif(jwt.getClaimAsString("nif"));
			usuari.setEmail(jwt.getClaimAsString("email"));
			usuari.setRols(extractRolesFromJwt(jwt));
		} else if (authentication.getPrincipal() instanceof User) {
			// Authenticació provinent de JBoss
			WebSecurityConfig.PreauthWebAuthenticationDetails authDetails = (WebSecurityConfig.PreauthWebAuthenticationDetails)authentication.getDetails();
			usuari.setCodi(authDetails.getPreferredUsername());
			usuari.setNom(authDetails.getName());
			usuari.setNif(authDetails.getNif());
			usuari.setEmail(authDetails.getEmail());
			usuari.setRols(filterAllowedRoles(Arrays.stream(authDetails.getOriginalRoles()).collect(Collectors.toSet())));
		} else {
			// TODO Millorar excepció
			throw new RuntimeException("Authentication principal not supported: " + authentication.getPrincipal().getClass().getName());
		}
		return usuari;
	}

	private String[] extractRolesFromJwt(Jwt jwt) {
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
		return filterAllowedRoles(roles);
	}

	private String[] filterAllowedRoles(Set<String> roles) {
		if (roles == null) {return new String[0];}
		Set<String> allowedRoles = Set.of(mappableRoles.split(","));
		roles.removeIf(r -> !allowedRoles.contains(r));
		return roles.toArray(new String[0]);
	}
}
