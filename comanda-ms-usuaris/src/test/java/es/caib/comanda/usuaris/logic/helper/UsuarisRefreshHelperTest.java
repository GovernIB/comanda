package es.caib.comanda.usuaris.logic.helper;

import es.caib.comanda.ms.back.config.WebSecurityConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;
import es.caib.comanda.usuaris.persist.entity.UsuariEntity;
import es.caib.comanda.usuaris.persist.repository.UsuariRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarisRefreshHelperTest {

    @Mock
    private UsuariRepository usuariRepository;
    @Mock
    private AuthenticationHelper authenticationHelper;

    @InjectMocks
    private UsuarisRefreshHelper helper;

    private static final String USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        lenient().when(authenticationHelper.getCurrentUserName()).thenReturn(USERNAME);
        ReflectionTestUtils.setField(helper, "mappableRoles", "ROLE_USER,ROLE_ADMIN");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void refreshCurrentUser_quanUsuariExisteix_actualitzaActivitat() {
        UsuariEntity usuariEntity = new UsuariEntity();
        usuariEntity.setCodi(USERNAME);
        when(usuariRepository.findByCodi(USERNAME)).thenReturn(Optional.of(usuariEntity));

        // Mock getUsuariFromAuth
        UsuarisRefreshHelper spyHelper = spy(helper);
        doReturn(new Usuari()).when(spyHelper).getUsuariFromAuth();

        spyHelper.refreshCurrentUser();

        verify(usuariRepository).save(any(UsuariEntity.class));
        assertThat(usuariEntity.getDarreraActivitat()).isNotNull();
    }

    @Test
    void refreshCurrentUser_quanSessioTancada_actualitzaPeriode() {
        UsuariEntity usuariEntity = new UsuariEntity();
        usuariEntity.setCodi(USERNAME);
        usuariEntity.setDarreraActivitat(LocalDateTime.now().minusMinutes(31));
        usuariEntity.setIniciPeriodeActual(LocalDateTime.now().minusMinutes(60));
        when(usuariRepository.findByCodi(USERNAME)).thenReturn(Optional.of(usuariEntity));

        // Mock getUsuariFromAuth
        UsuarisRefreshHelper spyHelper = spy(helper);
        doReturn(new Usuari()).when(spyHelper).getUsuariFromAuth();

        spyHelper.refreshCurrentUser();

        verify(usuariRepository).save(any(UsuariEntity.class));
        assertThat(usuariEntity.getIniciDarrerPeriode()).isNotNull();
        assertThat(usuariEntity.getFiDarrerPeriode()).isNotNull();
        assertThat(usuariEntity.getIniciPeriodeActual()).isNotNull();
    }

    @Test
    void refreshCurrentUser_quanUsuariNou_creaUsuari() {
        when(usuariRepository.findByCodi(USERNAME)).thenReturn(Optional.empty());

        Usuari usuariAuth = new Usuari();
        usuariAuth.setCodi(USERNAME);
        usuariAuth.setNom("Test Name");
        usuariAuth.setNif("12345678Z");
        usuariAuth.setEmail("test@test.es");

        // Mock getUsuariFromAuth
        UsuarisRefreshHelper spyHelper = spy(helper);
        doReturn(usuariAuth).when(spyHelper).getUsuariFromAuth();

        spyHelper.refreshCurrentUser();

        verify(usuariRepository).save(any(UsuariEntity.class));
    }

    @Test
    void refreshCurrentUser_quanThrottling_noCridaSave() {
        UsuariEntity usuariEntity = new UsuariEntity();
        usuariEntity.setCodi(USERNAME);
        when(usuariRepository.findByCodi(USERNAME)).thenReturn(Optional.of(usuariEntity));

        // Mock getUsuariFromAuth
        UsuarisRefreshHelper spyHelper = spy(helper);
        doReturn(new Usuari()).when(spyHelper).getUsuariFromAuth();

        // First call
        spyHelper.refreshCurrentUser();
        verify(usuariRepository, times(1)).save(any(UsuariEntity.class));

        // Second call (immediate)
        spyHelper.refreshCurrentUser();
        verify(usuariRepository, times(1)).save(any(UsuariEntity.class)); // Still 1
    }

    @Test
    void getUsuariFromAuth_quanJwt_retornaUsuari() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("name")).thenReturn("Name");
        when(jwt.getClaimAsString("nif")).thenReturn("12345678Z");
        when(jwt.getClaimAsString("email")).thenReturn("test@test.es");
        when(jwt.getClaim("azp")).thenReturn("clientId");
        when(jwt.getClaim("realm_access")).thenReturn(Collections.emptyMap());
        when(jwt.getClaims()).thenReturn(Map.of("resource_access", Collections.emptyMap()));

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);
        when(auth.getName()).thenReturn("username");

        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        Usuari usuari = helper.getUsuariFromAuth();

        assertThat(usuari.getCodi()).isEqualTo("username");
        assertThat(usuari.getNom()).isEqualTo("Name");
    }

    @Test
    void getUsuariFromAuth_quanUser_retornaUsuari() {
        WebSecurityConfig.PreauthWebAuthenticationDetails authDetails = mock(WebSecurityConfig.PreauthWebAuthenticationDetails.class);
        when(authDetails.getPreferredUsername()).thenReturn("username");
        when(authDetails.getName()).thenReturn("Name");
        when(authDetails.getNif()).thenReturn("12345678Z");
        when(authDetails.getEmail()).thenReturn("test@test.es");
        when(authDetails.getOriginalRoles()).thenReturn(new String[]{"ROLE_USER"});

        User user = mock(User.class);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(auth.getDetails()).thenReturn(authDetails);

        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);

        Usuari usuari = helper.getUsuariFromAuth();

        assertThat(usuari.getCodi()).isEqualTo("username");
        assertThat(usuari.getNom()).isEqualTo("Name");
        assertThat(usuari.getRols()).contains("ROLE_USER");
    }
}
