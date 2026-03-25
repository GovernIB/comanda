package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.ms.back.config.WebSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationHelperTest {

    private AuthenticationHelper authenticationHelper;

    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        authenticationHelper = new AuthenticationHelper();
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Retorna el nom de l'usuari actual")
    void getCurrentUserName_retornaNomUsuari() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        // Act
        String result = authenticationHelper.getCurrentUserName();

        // Assert
        assertThat(result).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Retorna els rols de l'usuari actual")
    void getCurrentUserRoles_retornaRols() {
        // Arrange
        List<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(authorities).when(authentication).getAuthorities();

        // Act
        String[] result = authenticationHelper.getCurrentUserRoles();

        // Assert
        assertThat(result).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Verifica si l'usuari té un rol")
    void isCurrentUserInRole_quanTeRol_retornaTrue() {
        // Arrange
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(authorities).when(authentication).getAuthorities();

        // Act
        boolean result = authenticationHelper.isCurrentUserInRole("ROLE_ADMIN");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Verifica si l'usuari no té un rol")
    void isCurrentUserInRole_quanNoTeRol_retornaFalse() {
        // Arrange
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(authorities).when(authentication).getAuthorities();

        // Act
        boolean result = authenticationHelper.isCurrentUserInRole("ROLE_ADMIN");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Retorna els rols del realm quan l'autenticació és amb Spring Boot")
    void getCurrentUserRealmRoles_quanAuthEsJwt_retornaRealmRoles() {
        // Arrange
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("consulta", "admin"));
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "testuser")
                .claim("realm_access", realmAccess)
                .build();
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt);
        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

        // Act
        String[] result = authenticationHelper.getCurrentUserRealmRoles();

        // Assert
        assertThat(result).containsExactly("consulta", "admin");
    }

    @Test
    @DisplayName("Retorna els rols originals quan l'autenticació és preautenticada de JBoss")
    void getCurrentUserRealmRoles_quanAuthEsJboss_retornaOriginalRoles() {
        // Arrange
        WebSecurityConfig.PreauthWebAuthenticationDetails details =
                new WebSecurityConfig.PreauthWebAuthenticationDetails(
                        new MockHttpServletRequest(),
                        List.of(new SimpleGrantedAuthority("ROLE_USER")),
                        "jwt-token",
                        "testuser",
                        "Test User",
                        "test@example.com",
                        "12345678A",
                        new String[]{"consulta", "admin"});
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(details);

        // Act
        String[] result = authenticationHelper.getCurrentUserRealmRoles();

        // Assert
        assertThat(result).containsExactly("consulta", "admin");
    }

    // Helper per Mockito amb genèrics (getAuthorities retorna Collection<? extends GrantedAuthority>)
    @SuppressWarnings("unchecked")
    private org.mockito.stubbing.Stubber doReturn(Object value) {
        return org.mockito.Mockito.doReturn(value);
    }
}
