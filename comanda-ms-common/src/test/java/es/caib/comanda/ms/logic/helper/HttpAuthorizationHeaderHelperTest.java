package es.caib.comanda.ms.logic.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpAuthorizationHeaderHelperTest {

    @Mock
    private RestTemplate restTemplate;

    private HttpAuthorizationHeaderHelper helper;

    @BeforeEach
    void setUp() {
        helper = new HttpAuthorizationHeaderHelper(restTemplate);
        ReflectionTestUtils.setField(helper, "authUsername", "user");
        ReflectionTestUtils.setField(helper, "authPassword", "pass");
    }

    @Test
    @DisplayName("Retorna Basic Auth quan no hi ha Keycloak configurat")
    void getAuthorizationHeader_quanNoConfigurat_retornaBasic() {
        // Act
        String result = helper.getAuthorizationHeader();

        // Assert
        String expected = "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Retorna Bearer Token quan Keycloak està configurat")
    void getAuthorizationHeader_quanConfigurat_retornaBearer() {
        // Arrange
        ReflectionTestUtils.setField(helper, "providerBaseUrl", "http://auth");
        ReflectionTestUtils.setField(helper, "providerRealm", "realm");
        ReflectionTestUtils.setField(helper, "providerClientId", "client");

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("access_token", "token123");
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseBody);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        String result = helper.getAuthorizationHeader();

        // Assert
        assertThat(result).isEqualTo("Bearer token123");
    }
}
