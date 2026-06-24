package es.caib.comanda.ms.configuracio.util;

import es.caib.comanda.configuracio.logic.intf.util.AuthHeaderUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthHeaderUtilTest {

    @Mock
    private Environment environment;

    @Test
    @DisplayName("buildValorAuth: quan valorEntornApp és null, retorna valorStatic")
    void buildValorAuth_quanValorEntornAppNull_retornaValorStatic() {
        String result = AuthHeaderUtil.buildValorAuth("staticUser", null, false, environment);
        assertThat(result).isEqualTo("staticUser");
        verifyNoInteractions(environment);
    }

    @Test
    @DisplayName("buildValorAuth: quan parametreAuth és false, retorna valorEntornApp literal")
    void buildValorAuth_quanParametreAuthFalse_retornaLiteral() {
        String result = AuthHeaderUtil.buildValorAuth("staticUser", "literalUser", false, environment);
        assertThat(result).isEqualTo("literalUser");
        verifyNoInteractions(environment);
    }

    @Test
    @DisplayName("buildValorAuth: quan parametreAuth és true, busca a Environment")
    void buildValorAuth_quanParametreAuthTrue_buscaAEnvironment() {
        when(environment.getProperty("app.user.key")).thenReturn("envUser");
        String result = AuthHeaderUtil.buildValorAuth("staticUser", "app.user.key", true, environment);
        assertThat(result).isEqualTo("envUser");
        verify(environment).getProperty("app.user.key");
    }

    @Test
    @DisplayName("buildValorAuth: quan parametreAuth és true però propietat no existeix, retorna null")
    void buildValorAuth_quanParametreAuthTrueIPropietatNoExisteix_retornaNull() {
        when(environment.getProperty("nonexistent.key")).thenReturn(null);
        String result = AuthHeaderUtil.buildValorAuth("staticUser", "nonexistent.key", true, environment);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("buildValorAuth: quan valorEntornApp és cadena buida, retorna cadena buida")
    void buildValorAuth_quanValorEntornAppBuit_retornaBuit() {
        String result = AuthHeaderUtil.buildValorAuth("staticUser", "", false, environment);
        assertThat(result).isEmpty();
        verifyNoInteractions(environment);
    }

    @Test
    @DisplayName("buildAuthHttpEntity: quan no hi ha usuari, retorna HttpEntity sense capçalera Authorization")
    void buildAuthHttpEntity_quanNoHiHaUsuari_retornaHttpEntitySenseAuthorization() {
        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                null, "staticPass", null, null, false, environment);

        assertThat(result).isNotNull();
        assertThat(result.getHeaders()).isNotNull();
        assertThat(result.getHeaders().getFirst("Authorization")).isNull();
    }


    @Test
    @DisplayName("buildAuthHttpEntity: quan usuari és buit, retorna HttpEntity sense capçalera Authorization")
    void buildAuthHttpEntity_quanUsuariBuit_retornaHttpEntitySenseAuthorization() {
        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                "staticUser", "staticPass", "", "secret", false, environment);

        assertThat(result).isNotNull();
        assertThat(result.getHeaders()).isNotNull();
        assertThat(result.getHeaders().getFirst("Authorization")).isNull();
    }

    @Test
    @DisplayName("buildAuthHttpEntity: amb credencials literals, genera Basic Auth correcte")
    void buildAuthHttpEntity_ambCredencialsLiterals_generaBasicAuthCorrecte() {
        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                "staticUser", "staticPass", "admin", "secret", false, environment);

        assertThat(result).isNotNull();
        String authHeader = result.getHeaders().getFirst("Authorization");
        assertThat(authHeader).isNotNull();
        assertThat(authHeader).startsWith("Basic ");

        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        assertThat(decoded).isEqualTo("admin:secret");
    }

    @Test
    @DisplayName("buildAuthHttpEntity: amb parametreAuth=true, busca credencials a Environment")
    void buildAuthHttpEntity_ambParametreAuthTrue_buscaCredencialsAEnvironment() {
        when(environment.getProperty("app.user.key")).thenReturn("envUser");
        when(environment.getProperty("app.pass.key")).thenReturn("envPass");

        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                "staticUser", "staticPass", "app.user.key", "app.pass.key", true, environment);

        assertThat(result).isNotNull();
        String authHeader = result.getHeaders().getFirst("Authorization");
        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        assertThat(decoded).isEqualTo("envUser:envPass");

        verify(environment).getProperty("app.user.key");
        verify(environment).getProperty("app.pass.key");
    }

    @Test
    @DisplayName("buildAuthHttpEntity: quan contrasenya és null, usa contrasenya estàtica")
    void buildAuthHttpEntity_quanContrasenyaNull_usaContrasenyaEstatica() {
        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                "staticUser", "staticPass", "admin", null, false, environment);

        assertThat(result).isNotNull();
        String authHeader = result.getHeaders().getFirst("Authorization");
        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        assertThat(decoded).isEqualTo("admin:staticPass");
    }

    @Test
    @DisplayName("buildAuthHttpEntity: quan contrasenya és buida, permet password buida explícita")
    void buildAuthHttpEntity_quanContrasenyaBuida_permitePasswordBuida() {
        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                "staticUser", "staticPass", "admin", "", false, environment);

        assertThat(result).isNotNull();
        String authHeader = result.getHeaders().getFirst("Authorization");
        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        assertThat(decoded).isEqualTo("admin:");
    }

    @Test
    @DisplayName("buildAuthHttpEntity: fallback a valors estàtics quan no hi ha valors d'entornApp")
    void buildAuthHttpEntity_fallbackAValorsEstatics() {
        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                "staticUser", "staticPass", null, null, false, environment);

        assertThat(result).isNotNull();
        String authHeader = result.getHeaders().getFirst("Authorization");
        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        assertThat(decoded).isEqualTo("staticUser:staticPass");
    }

    @Test
    @DisplayName("buildAuthHttpEntity: combina usuari d'Environment amb contrasenya d'Environment")
    void buildAuthHttpEntity_combinaAmbdosDeEnvironment() {
        when(environment.getProperty("app.user.key")).thenReturn("envUser");
        when(environment.getProperty("app.pass.key")).thenReturn("envPass");

        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                "staticUser", "staticPass", "app.user.key", "app.pass.key", true, environment);

        assertThat(result).isNotNull();
        String authHeader = result.getHeaders().getFirst("Authorization");
        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        assertThat(decoded).isEqualTo("envUser:envPass");

        verify(environment).getProperty("app.user.key");
        verify(environment).getProperty("app.pass.key");
    }

    @Test
    @DisplayName("buildAuthHttpEntity: combina usuari literal amb contrasenya literal")
    void buildAuthHttpEntity_combinaAmbdosLiterals() {
        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                "staticUser", "staticPass", "literalUser", "literalPass", false, environment);

        assertThat(result).isNotNull();
        String authHeader = result.getHeaders().getFirst("Authorization");
        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        assertThat(decoded).isEqualTo("literalUser:literalPass");

        verifyNoInteractions(environment);
    }

    @Test
    @DisplayName("buildAuthHttpEntity: amb caràcters especials, codifica correctament en Base64")
    void buildAuthHttpEntity_ambCaractersEspecials_codificaCorrectament() {
        HttpEntity<Void> result = AuthHeaderUtil.buildAuthHttpEntity(
                "staticUser", "staticPass", "user@domain.com", "password", false, environment);

        assertThat(result).isNotNull();
        String authHeader = result.getHeaders().getFirst("Authorization");
        String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
        assertThat(decoded).isEqualTo("user@domain.com:password");
    }
}