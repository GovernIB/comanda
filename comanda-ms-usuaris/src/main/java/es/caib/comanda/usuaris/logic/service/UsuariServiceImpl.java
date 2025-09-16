package es.caib.comanda.usuaris.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.ms.back.config.WebSecurityConfig;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.usuaris.logic.intf.model.LanguageEnum;
import es.caib.comanda.usuaris.logic.intf.model.NumOfElementsPerPageENum;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;
import es.caib.comanda.usuaris.logic.intf.service.UsuariService;
import es.caib.comanda.usuaris.persist.entity.UsuariEntity;
import es.caib.comanda.usuaris.persist.repository.UsuariRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UsuariServiceImpl extends BaseMutableResourceService<Usuari, Long, UsuariEntity> implements UsuariService {

    @Override
    public Usuari getOneByCodi(String codi) {
        log.debug("Obtenint usuari {codi}", codi);
        Optional<UsuariEntity> usuariEntity = ((UsuariRepository) entityRepository).findByCodi(codi);
        if (usuariEntity.isPresent()) {
            return entityToResource(usuariEntity.get());
        }
        return null;
    }

	@Override
	protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries) {
		List<Filter> filters = new ArrayList<>();
		if (currentSpringFilter != null && !currentSpringFilter.isEmpty()) {
			filters.add(Filter.parse(currentSpringFilter));
		}
		filters.add(FilterBuilder.equal("codi", getUsuariFromAuth().getCodi()));
		return FilterBuilder.and(filters).generate();
	}

	@Override
    public void refreshCurrentUser() {
		Usuari usuariInfo = getUsuariFromAuth();

		Optional<UsuariEntity> usuariEntityOptional = ((UsuariRepository) entityRepository).findByCodi(usuariInfo.getCodi());
		UsuariEntity usuariEntity = null;
		if (usuariEntityOptional.isPresent()) {
			usuariEntity = usuariEntityOptional.get();
		}
		else {
			usuariEntity = new UsuariEntity();
			usuariEntity.setCodi(usuariInfo.getCodi());
			usuariEntity.setIdioma(LanguageEnum.CA);
			usuariEntity.setNumElementsPagina(NumOfElementsPerPageENum.AUTOMATIC);
		}
		usuariEntity.setNom(usuariInfo.getNom());
		usuariEntity.setNif(usuariInfo.getNif());
		usuariEntity.setEmail(usuariInfo.getEmail());
		entityRepository.save(usuariEntity);
	}

	private static Usuari getUsuariFromAuth() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		var usuariInfo = new Usuari();

		// SpringBoot
		if (authentication.getPrincipal() instanceof Jwt) {
			Jwt principal = (Jwt) authentication.getPrincipal();
			usuariInfo.setCodi(authentication.getName());
			usuariInfo.setNom(principal.getClaimAsString("name"));
			usuariInfo.setNif(principal.getClaimAsString("nif"));
			usuariInfo.setEmail(principal.getClaimAsString("email"));
		}
		// JBoss
		else if (authentication.getPrincipal() instanceof User) {
			var authDetails = (WebSecurityConfig.PreauthWebAuthenticationDetails) authentication.getDetails();
			usuariInfo.setCodi(authDetails.getPreferredUsername());
			usuariInfo.setNom(authDetails.getName());
			usuariInfo.setNif(authDetails.getNif());
			usuariInfo.setEmail(authDetails.getEmail());
		}
		else {
//			TODO Millorar tipo de excepció
			throw new RuntimeException("Authentication principal not supported: " + authentication.getPrincipal().getClass().getName());
		}
		return usuariInfo;
	}

}
