package es.caib.comanda.ms.back.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.IDToken;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Tests per a JbossKeycloakLogoutSuccessHandler")
class JbossKeycloakLogoutSuccessHandlerTest {

    private static final String ISSUER_TOKEN = "https://sso.caib.es/auth/realms/GOIB";
    private static final String END_SESSION_DESCOBERT = "https://sso.caib.es/auth/realms/GOIB/protocol/openid-connect/logout";

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Map<String, String> endpointsDescoberts;
    private Function<String, String> endSessionResolver;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest("GET", "/comandaback/logout");
        request.setScheme("https");
        request.setServerName("comanda.caib.es");
        request.setServerPort(443);
        request.setContextPath("/comandaback");
        response = new MockHttpServletResponse();
        endpointsDescoberts = new HashMap<>();
        endSessionResolver = endpointsDescoberts::get;
    }

    private void ambSessioKeycloak(String issuer, String azp, String idTokenString) {
        IDToken idToken = new IDToken();
        idToken.issuer(issuer);
        idToken.issuedFor(azp);
        KeycloakSecurityContext ksc = mock(KeycloakSecurityContext.class);
        when(ksc.getIdToken()).thenReturn(idToken);
        when(ksc.getIdTokenString()).thenReturn(idTokenString);
        request.setAttribute(KeycloakSecurityContext.class.getName(), ksc);
    }

    private static Map<String, String> queryParams(String url) {
        Map<String, String> params = new HashMap<>();
        String query = URI.create(url).getRawQuery();
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            params.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return params;
    }

    @Test
    @DisplayName("onLogoutSuccess: redirigeix a l'end_session_endpoint descobert amb id_token_hint, client_id (azp) i post_logout_redirect_uri")
    void onLogoutSuccess_quanHiHaSessioKeycloak_llavorsRedirigeixAlEndSessionAmbTotsElsParametres() throws Exception {
        ambSessioKeycloak(ISSUER_TOKEN, "goib-default", "id.token.jwt");
        endpointsDescoberts.put(ISSUER_TOKEN, END_SESSION_DESCOBERT);
        JbossKeycloakLogoutSuccessHandler handler = new JbossKeycloakLogoutSuccessHandler(null, null, endSessionResolver);

        handler.onLogoutSuccess(request, response, null);

        String redirect = response.getRedirectedUrl();
        assertThat(redirect).startsWith(END_SESSION_DESCOBERT + "?");
        Map<String, String> params = queryParams(redirect);
        assertThat(params).containsEntry("id_token_hint", "id.token.jwt");
        assertThat(params).containsEntry("client_id", "goib-default");
        assertThat(params).containsEntry("post_logout_redirect_uri", "https://comanda.caib.es/comandaback/");
    }

    @Test
    @DisplayName("onLogoutSuccess: l'issuer surt del claim iss del token, no de la configuració")
    void onLogoutSuccess_quanConfiguracioApuntaAUnAltreRealm_llavorsUsaIssDelToken() throws Exception {
        ambSessioKeycloak(ISSUER_TOKEN, "goib-default", "id.token.jwt");
        JbossKeycloakLogoutSuccessHandler handler = new JbossKeycloakLogoutSuccessHandler(
                "https://sso.caib.es/auth", "ALTRE_REALM", endSessionResolver);

        handler.onLogoutSuccess(request, response, null);

        assertThat(response.getRedirectedUrl()).startsWith(ISSUER_TOKEN + "/protocol/openid-connect/logout?");
        assertThat(queryParams(response.getRedirectedUrl())).containsEntry("client_id", "goib-default");
    }

    @Test
    @DisplayName("onLogoutSuccess: si la descoberta OIDC falla, usa el path de Keycloak")
    void onLogoutSuccess_quanDescobertaNoDisponible_llavorsUsaPathKeycloak() throws Exception {
        ambSessioKeycloak(ISSUER_TOKEN, "goib-default", "id.token.jwt");
        JbossKeycloakLogoutSuccessHandler handler = new JbossKeycloakLogoutSuccessHandler(null, null, endSessionResolver);

        handler.onLogoutSuccess(request, response, null);

        assertThat(response.getRedirectedUrl()).startsWith(ISSUER_TOKEN + "/protocol/openid-connect/logout?");
    }

    @Test
    @DisplayName("onLogoutSuccess: sense sessió Keycloak usa les propietats es.caib.comanda.auth.url/realm i no envia id_token_hint ni client_id")
    void onLogoutSuccess_quanNoHiHaSessioKeycloak_llavorsUsaPropietatsDeConfiguracio() throws Exception {
        JbossKeycloakLogoutSuccessHandler handler = new JbossKeycloakLogoutSuccessHandler(
                "https://sso.caib.es/auth/", "GOIB", endSessionResolver);

        handler.onLogoutSuccess(request, response, null);

        String redirect = response.getRedirectedUrl();
        assertThat(redirect).startsWith(ISSUER_TOKEN + "/protocol/openid-connect/logout?");
        Map<String, String> params = queryParams(redirect);
        assertThat(params).doesNotContainKey("id_token_hint");
        assertThat(params).doesNotContainKey("client_id");
    }

    @Test
    @DisplayName("onLogoutSuccess: amb sessió Keycloak funciona encara que no s'hagin configurat les propietats")
    void onLogoutSuccess_quanNoHiHaPropietatsPeroSiToken_llavorsTancaLaSessioSso() throws Exception {
        ambSessioKeycloak(ISSUER_TOKEN, "goib-default", "id.token.jwt");
        JbossKeycloakLogoutSuccessHandler handler = new JbossKeycloakLogoutSuccessHandler(null, null, endSessionResolver);

        handler.onLogoutSuccess(request, response, null);

        assertThat(response.getRedirectedUrl()).startsWith(ISSUER_TOKEN + "/protocol/openid-connect/logout?");
        assertThat(queryParams(response.getRedirectedUrl())).containsEntry("client_id", "goib-default");
    }

    @Test
    @DisplayName("onLogoutSuccess: sense sessió Keycloak ni configuració torna a l'arrel de l'aplicació")
    void onLogoutSuccess_quanNoHiHaIssuer_llavorsRedirigeixALArrel() throws Exception {
        JbossKeycloakLogoutSuccessHandler handler = new JbossKeycloakLogoutSuccessHandler(null, null, endSessionResolver);

        handler.onLogoutSuccess(request, response, null);

        assertThat(response.getRedirectedUrl()).isEqualTo("/comandaback/");
    }

    @Test
    @DisplayName("onLogoutSuccess: invalida la sessió HTTP local")
    void onLogoutSuccess_quanHiHaSessioHttp_llavorsLaInvalida() throws Exception {
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        ambSessioKeycloak(ISSUER_TOKEN, "goib-default", "id.token.jwt");
        JbossKeycloakLogoutSuccessHandler handler = new JbossKeycloakLogoutSuccessHandler(null, null, endSessionResolver);

        handler.onLogoutSuccess(request, response, null);

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("onLogoutSuccess: amb port no estàndard l'inclou a post_logout_redirect_uri")
    void onLogoutSuccess_quanPortNoEstandard_llavorsLIncloua() throws Exception {
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        ambSessioKeycloak(ISSUER_TOKEN, "goib-default", "id.token.jwt");
        JbossKeycloakLogoutSuccessHandler handler = new JbossKeycloakLogoutSuccessHandler(null, null, endSessionResolver);

        handler.onLogoutSuccess(request, response, null);

        assertThat(queryParams(response.getRedirectedUrl()))
                .containsEntry("post_logout_redirect_uri", "http://localhost:8080/comandaback/");
    }
}
