package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.*;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirResponse;
import es.caib.comanda.estadistica.persist.entity.estadistiques.*;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.model.v1.estadistica.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstadisticaHelperTest {

    @Mock private DimensioRepository dimensioRepository;
    @Mock private DimensioValorRepository dimensioValorRepository;
    @Mock private IndicadorRepository indicadorRepository;
    @Mock private TempsRepository tempsRepository;
    @Mock private FetRepository fetRepository;
    @Mock private EstadisticaClientHelper estadisticaClientHelper;
    @Mock private RestTemplate restTemplate;
    @Mock private Environment environment;

    private EstadisticaHelper estadisticaHelper;
    private EntornApp entornApp;

    private static final String INFO_URL = "http://test.com/estadistica/info";
    private static final String DADES_URL = "http://test.com/estadistica/dades";

    @BeforeEach
    void setUp() {
        estadisticaHelper = new EstadisticaHelper(
                dimensioRepository, dimensioValorRepository, indicadorRepository,
                tempsRepository, fetRepository, estadisticaClientHelper,
                restTemplate, environment
        );

        ReflectionTestUtils.setField(estadisticaHelper, "statsAuthUser", "staticUser");
        ReflectionTestUtils.setField(estadisticaHelper, "statsAuthPassword", "staticPass");
        ReflectionTestUtils.setField(estadisticaHelper, "self", estadisticaHelper);

        AppRef app = new AppRef(1L, "TestApp");
        EntornRef entorn = new EntornRef(1L, "TestEntorn");

        entornApp = new EntornApp();
        entornApp.setId(10L);
        entornApp.setApp(app);
        entornApp.setEntorn(entorn);
        entornApp.setEstadisticaUrl(DADES_URL);
        entornApp.setEstadisticaInfoUrl(INFO_URL);
    }

    @Test
    @DisplayName("getEstadisticaInfoDades: quan isEstadisticaAuth és false, no envia capçalera d'autenticació")
    void getEstadisticaInfoDades_quanEstadisticaAuthFalse_noEnviaAuthHeader() {
        // Given
        entornApp.setEstadisticaAuth(false);
        entornApp.setNomUsuariAuth("user");
        entornApp.setContrasenyaAuth("pass");
        mockEstadistiquesInfoResponse();
        mockRegistresEstadisticsResponse(false);
        // When
        estadisticaHelper.getEstadisticaInfoDades(entornApp);
        // Then
        verify(restTemplate).getForObject(eq(INFO_URL), eq(EstadistiquesInfo.class));
        verify(restTemplate).getForObject(eq(DADES_URL), eq(RegistresEstadistics.class));
    }

    @Test
    @DisplayName("getEstadisticaInfoDades: quan isEstadisticaAuth és true amb credencials literals, envia Basic Auth")
    void getEstadisticaInfoDades_quanEstadisticaAuthTrueAmbCredencialsLiterals_enviaBasicAuth() {
        // Given
        entornApp.setEstadisticaAuth(true);
        entornApp.setParametreAuth(false);
        entornApp.setNomUsuariAuth("admin");
        entornApp.setContrasenyaAuth("secret");
        mockEstadistiquesInfoResponse();
        mockRegistresEstadisticsResponse(false);
        // When
        estadisticaHelper.getEstadisticaInfoDades(entornApp);
        // Then
        verify(restTemplate).exchange(eq(INFO_URL), eq(HttpMethod.GET), argThat(entity -> {
            if (entity == null || entity.getHeaders() == null) return false;
            String authHeader = entity.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) return false;
            String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
            return "admin:secret".equals(decoded);
        }), eq(EstadistiquesInfo.class));
    }

    @Test
    @DisplayName("getEstadisticaInfoDades: quan parametreAuth és true, busca credencials a l'Environment")
    void getEstadisticaInfoDades_quanParametreAuthTrue_buscaAEnvironment() {
        // Given
        entornApp.setEstadisticaAuth(true);
        entornApp.setParametreAuth(true);
        entornApp.setNomUsuariAuth("app.user.key");
        entornApp.setContrasenyaAuth("app.pass.key");
        lenient().when(environment.getProperty("app.user.key")).thenReturn("envUser");
        lenient().when(environment.getProperty("app.pass.key")).thenReturn("envPass");
        mockEstadistiquesInfoResponse();
        mockRegistresEstadisticsResponse(false);
        // When
        estadisticaHelper.getEstadisticaInfoDades(entornApp);
        // Then
        verify(environment, atLeast(1)).getProperty("app.user.key");
        verify(environment, atLeast(1)).getProperty("app.pass.key");
        verify(restTemplate).exchange(eq(INFO_URL), eq(HttpMethod.GET), argThat(entity -> {
            String authHeader = entity.getHeaders().getFirst("Authorization");
            String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
            return "envUser:envPass".equals(decoded);
        }), eq(EstadistiquesInfo.class));
    }

    @Test
    @DisplayName("getEstadisticaInfoDades: quan no hi ha credencials literals ni parametreAuth, usa valors estàtics")
    void getEstadisticaInfoDades_quanNoHiHaCredencials_usaValorsEstatics() {
        // Given
        entornApp.setEstadisticaAuth(true);
        entornApp.setParametreAuth(false);
        entornApp.setNomUsuariAuth(null);
        entornApp.setContrasenyaAuth(null);
        mockEstadistiquesInfoResponse();
        mockRegistresEstadisticsResponse(false);
        // When
        estadisticaHelper.getEstadisticaInfoDades(entornApp);
        // Then
        verify(restTemplate).exchange(eq(INFO_URL), eq(HttpMethod.GET), argThat(entity -> {
            String authHeader = entity.getHeaders().getFirst("Authorization");
            String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
            return "staticUser:staticPass".equals(decoded);
        }), eq(EstadistiquesInfo.class));
    }

    @Test
    @DisplayName("getEstadisticaInfoDades: quan parametreAuth és true però la propietat no existeix, no envia autenticació")
    void getEstadisticaInfoDades_quanParametreAuthTrueIPropietatNoExisteix_noEnviaAuth() {
        // Given
        entornApp.setEstadisticaAuth(true);
        entornApp.setParametreAuth(true);
        entornApp.setNomUsuariAuth("nonexistent.key");
        entornApp.setContrasenyaAuth("nonexistent.key");
        lenient().when(environment.getProperty("nonexistent.key")).thenReturn(null);
        mockEstadistiquesInfoResponse();
        mockRegistresEstadisticsResponse(false);
        // When
        estadisticaHelper.getEstadisticaInfoDades(entornApp);
        // Then
        verify(environment, atLeast(1)).getProperty("nonexistent.key");
        verify(restTemplate).getForObject(eq(INFO_URL), eq(EstadistiquesInfo.class));
        verify(restTemplate).getForObject(eq(DADES_URL), eq(RegistresEstadistics.class));
    }

    @Test
    @DisplayName("getEstadisticaInfoDades: permet usuari sense contrasenya (cadena buida)")
    void getEstadisticaInfoDades_quanContrasenyaBuida_enviaBasicAuthAmbContrasenyaBuida() {
        // Given
        entornApp.setEstadisticaAuth(true);
        entornApp.setParametreAuth(false);
        entornApp.setNomUsuariAuth("admin");
        entornApp.setContrasenyaAuth("");
        mockEstadistiquesInfoResponse();
        mockRegistresEstadisticsResponse(false);
        // When
        estadisticaHelper.getEstadisticaInfoDades(entornApp);
        // Then
        verify(restTemplate).exchange(eq(INFO_URL), eq(HttpMethod.GET), argThat(entity -> {
            String authHeader = entity.getHeaders().getFirst("Authorization");
            String decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)));
            return "admin:".equals(decoded);
        }), eq(EstadistiquesInfo.class));
    }

    @Test
    @DisplayName("getEstadisticaInfoDadesAmbUrl: retorna error quan hi ha RestClientException")
    void getEstadisticaInfoDadesAmbUrl_retornaErrorQuanHiHaExcepcio() {
        // Given
        entornApp.setEstadisticaAuth(false);
        when(restTemplate.getForObject(eq(INFO_URL), eq(EstadistiquesInfo.class)))
                .thenThrow(new RestClientException("Connection refused"));
        // When
        FetObtenirResponse result = estadisticaHelper.getEstadisticaInfoDadesAmbUrl(entornApp, DADES_URL, false);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Connection refused");
    }

    @Test
    @DisplayName("getEstadisticaInfoDades: amb múltiples dies, concatena el paràmetre a la URL")
    void getEstadisticaInfoDades_ambMultiplesDies_concatenaUrl() {
        // Given
        entornApp.setEstadisticaAuth(false);
        mockEstadistiquesInfoResponse();
        RegistresEstadistics registres = buildRegistresEstadistics();
        lenient().when(restTemplate.exchange(
                eq(DADES_URL + "/7"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(List.of(registres), HttpStatus.OK));
        lenient().when(tempsRepository.findByData(any(LocalDate.class))).thenReturn(null);
        lenient().when(tempsRepository.save(any(TempsEntity.class))).thenAnswer(invocation -> {
            TempsEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });
        // When
        estadisticaHelper.getEstadisticaInfoDades(entornApp, 7);
        // Then
        verify(restTemplate).exchange(
                eq(DADES_URL + "/7"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    @DisplayName("createOrGetTempsEntity: crea nova entitat si no existeix")
    void createOrGetTempsEntity_creaNovaSiNoExisteix() {
        // Given
        LocalDate data = LocalDate.of(2024, 1, 15);
        when(tempsRepository.findByData(data)).thenReturn(null);
        when(tempsRepository.save(any(TempsEntity.class))).thenAnswer(invocation -> {
            TempsEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });
        // When
        TempsEntity result = estadisticaHelper.createOrGetTempsEntity(data);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isEqualTo(data);
        verify(tempsRepository).save(any(TempsEntity.class));
    }

    @Test
    @DisplayName("createOrGetTempsEntity: retorna entitat existent si ja existeix")
    void createOrGetTempsEntity_retornaExistentSiJaExisteix() {
        // Given
        LocalDate data = LocalDate.of(2024, 1, 15);
        TempsEntity existing = new TempsEntity(data);
        existing.setId(1L);
        when(tempsRepository.findByData(data)).thenReturn(existing);
        // When
        TempsEntity result = estadisticaHelper.createOrGetTempsEntity(data);
        // Then
        assertThat(result).isSameAs(existing);
        verify(tempsRepository, never()).save(any());
    }

    private void mockEstadistiquesInfoResponse() {
        EstadistiquesInfo info = new EstadistiquesInfo();
        info.setIndicadors(List.of(buildIndicadorDesc("IND1", "Indicador 1")));
        info.setDimensions(List.of(buildDimensioDesc("DIM1", "Dimensio 1", List.of("valor1", "valor2"))));
        lenient().when(restTemplate.getForObject(eq(INFO_URL), eq(EstadistiquesInfo.class)))
                .thenReturn(info);
        lenient().when(restTemplate.exchange(eq(INFO_URL), eq(HttpMethod.GET), any(), eq(EstadistiquesInfo.class)))
                .thenReturn(new ResponseEntity<>(info, HttpStatus.OK));
    }

    private void mockRegistresEstadisticsResponse(boolean multiplesDies) {
        RegistresEstadistics registres = buildRegistresEstadistics();
        if (multiplesDies) {
            lenient().when(restTemplate.exchange(
                    eq(DADES_URL),
                    eq(HttpMethod.GET),
                    any(),
                    any(ParameterizedTypeReference.class)
            )).thenReturn(new ResponseEntity<>(List.of(registres), HttpStatus.OK));
        } else {
            lenient().when(restTemplate.getForObject(eq(DADES_URL), eq(RegistresEstadistics.class)))
                    .thenReturn(registres);
            lenient().when(restTemplate.exchange(
                    eq(DADES_URL),
                    eq(HttpMethod.GET),
                    any(),
                    eq(RegistresEstadistics.class)
            )).thenReturn(new ResponseEntity<>(registres, HttpStatus.OK));
        }
        lenient().when(tempsRepository.findByData(any(LocalDate.class))).thenReturn(null);
        lenient().when(tempsRepository.save(any(TempsEntity.class))).thenAnswer(invocation -> {
            TempsEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });
    }

    private RegistresEstadistics buildRegistresEstadistics() {
        Dimensio dimensio = mock(Dimensio.class);
        lenient().when(dimensio.getCodi()).thenReturn("DIM1");
        lenient().when(dimensio.getValor()).thenReturn("valor1");

        es.caib.comanda.model.v1.estadistica.Fet fet = mock(es.caib.comanda.model.v1.estadistica.Fet.class);
        lenient().when(fet.getCodi()).thenReturn("IND1");
        lenient().when(fet.getValor()).thenReturn(100.0);

        RegistreEstadistic registre = new RegistreEstadistic();
        registre.setDimensions(List.of(dimensio));
        registre.setFets(List.of(fet));

        RegistresEstadistics registres = new RegistresEstadistics();
        registres.setTemps(OffsetDateTime.now());
        registres.setFets(List.of(registre));

        return registres;
    }

    private IndicadorDesc buildIndicadorDesc(String codi, String nom) {
        IndicadorDesc indicador = new IndicadorDesc();
        indicador.setCodi(codi);
        indicador.setNom(nom);
        return indicador;
    }

    private DimensioDesc buildDimensioDesc(String codi, String nom, List<String> valors) {
        DimensioDesc dimensio = new DimensioDesc();
        dimensio.setCodi(codi);
        dimensio.setNom(nom);
        dimensio.setValors(valors);
        return dimensio;
    }
}