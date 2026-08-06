package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPluginDir3;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirResponse;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.model.v1.estadistica.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

    @Mock
    private DimensioRepository dimensioRepository;
    @Mock
    private DimensioValorRepository dimensioValorRepository;
    @Mock
    private IndicadorRepository indicadorRepository;
    @Mock
    private TempsRepository tempsRepository;
    @Mock
    private FetRepository fetRepository;

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Environment environment;
    @Mock
    private UnitatsOrganitzativesPluginDir3 unitatsOrganitzativesPluginDir3;

    @InjectMocks
    private EstadisticaHelper estadisticaHelper;
    private EntornApp entornApp;

    private static final String INFO_URL = "http://test.com/estadistica/info";
    private static final String DADES_URL = "http://test.com/estadistica/dades";

    @BeforeEach
    void setUp() {
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
        when(dimensioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
        when(dimensioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
        when(dimensioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
        when(dimensioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
        when(dimensioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
        when(dimensioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
        when(dimensioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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

    // ========================================================================
    // TESTOS ADDICIONALS PER A COBERTURA COMPLETA (>90%)
    // ========================================================================

    @Test
    @DisplayName("crearIndicadors: ignora indicadors amb nom en blanc")
    void crearIndicadors_quanNomEnBlanc_llavorsIgnoraIndicador() {
        // Arrange
        List<IndicadorDesc> indicadors = List.of(buildIndicadorDesc("CODI1", "  "));

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearIndicadors", indicadors, 10L);

        // Assert
        verify(indicadorRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearIndicadors: actualitza indicador existent amb descripcio i format")
    void crearIndicadors_quanIndicadorExisteix_llavorsActualitzaAmbDescripcioIFormat() {
        // Arrange
        IndicadorDesc desc = new IndicadorDesc();
        desc.setCodi("CODI1");
        desc.setNom("Nom 1");
        desc.setDescripcio("Desc 1");
        desc.setFormat(Format.LONG);

        IndicadorEntity existing = new IndicadorEntity();
        when(indicadorRepository.findByCodiAndEntornAppId("CODI1", 10L)).thenReturn(Optional.of(existing));

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearIndicadors", List.of(desc), 10L);

        // Assert
        assertThat(existing.getDescripcio()).isEqualTo("Desc 1");
        assertThat(existing.getFormat()).isEqualTo(Format.LONG);
        verify(indicadorRepository).save(existing);
    }

    @Test
    @DisplayName("crearDimensions: ignora dimensions amb nom en blanc")
    void crearDimensions_quanNomEnBlanc_llavorsIgnoraDimensio() {
        // Arrange
        List<DimensioDesc> dimensions = List.of(buildDimensioDesc("CODI1", "  ", List.of()));

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearDimensions", dimensions, 10L);

        // Assert
        verify(dimensioRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearDimensions: actualitza dimensio existent amb descripcio")
    void crearDimensions_quanDimensioExisteix_llavorsActualitzaAmbDescripcio() {
        // Arrange
        DimensioDesc desc = new DimensioDesc();
        desc.setCodi("DIM1");
        desc.setNom("Dimensio 1");
        desc.setDescripcio("Desc 1");

        DimensioEntity existing = new DimensioEntity();
        when(dimensioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(dimensioRepository.findByCodiAndEntornAppId("DIM1", 10L)).thenReturn(Optional.of(existing));

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearDimensions", List.of(desc), 10L);

        // Assert
        assertThat(existing.getDescripcio()).isEqualTo("Desc 1");
        verify(dimensioRepository).save(existing);
    }

    @Test
    @DisplayName("crearDimensions: crea dimensio CONS quan el tipus es ORGAN_GESTOR i no existeix CONS")
    void crearDimensions_quanTipusOrganGestorISenseCONS_llavorsCreaDimensioCONS() {
        // Arrange
        DimensioDesc desc = new DimensioDesc();
        desc.setCodi("ORG");
        desc.setNom("Organitzacio");
        desc.setValors(List.of("VAL1"));

        DimensioEntity savedOrg = new DimensioEntity();
        savedOrg.setTipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum.ORGAN_GESTOR);
        DimensioValorEntity valEntity = new DimensioValorEntity();
        valEntity.setValor("VAL1");
        savedOrg.setValors(List.of(valEntity));

        when(dimensioRepository.findByCodiAndEntornAppId("ORG", 10L)).thenReturn(Optional.of(new DimensioEntity()));
        when(dimensioRepository.save(any(DimensioEntity.class))).thenReturn(savedOrg);
        when(dimensioRepository.findByCodiAndEntornAppId("CONS", 10L)).thenReturn(Optional.empty());
        when(unitatsOrganitzativesPluginDir3.getConselleria("VAL1")).thenReturn("CONS1");

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearDimensions", List.of(desc), 10L);

        // Assert
        verify(dimensioRepository, times(3)).save(any(DimensioEntity.class)); // Una per ORG, una per CONS
    }

    @Test
    @DisplayName("crearDimensions: processa valors en batches i crea els que falten")
    void crearDimensions_quanHiHaValorsNous_llavorsCreaValorsFaltantsEnBatches() {
        // Arrange
        DimensioDesc desc = new DimensioDesc();
        desc.setCodi("DIM1");
        desc.setNom("Dimensio 1");
        desc.setValors(List.of("V1", "V2", "V3"));

        DimensioEntity savedDim = new DimensioEntity();
        savedDim.setCodi("DIM1");

        when(dimensioRepository.findByCodiAndEntornAppId("DIM1", 10L)).thenReturn(Optional.of(new DimensioEntity()));
        when(dimensioRepository.save(any(DimensioEntity.class))).thenReturn(savedDim);

        DimensioValorEntity existingVal = new DimensioValorEntity();
        existingVal.setValor("V1");
        when(dimensioValorRepository.findByDimensioAndValorIn(any(), anyList())).thenReturn(List.of(existingVal));

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearDimensions", List.of(desc), 10L);

        // Assert
        ArgumentCaptor<List<DimensioValorEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(dimensioValorRepository).saveAll(captor.capture());
        List<DimensioValorEntity> savedValues = captor.getValue();
        assertThat(savedValues).hasSize(2);
        assertThat(savedValues.stream().map(DimensioValorEntity::getValor)).containsExactlyInAnyOrder("V2", "V3");
    }

    @Test
    @DisplayName("splitIntoBatches: divideix una llista en batches de la mida especificada")
    void splitIntoBatches_quanLlistaEsGran_llavorsDivideixCorrectament() {
        // Arrange
        List<Integer> list = List.of(1, 2, 3, 4, 5);

        // Act
        @SuppressWarnings("unchecked")
        List<List<Integer>> batches = (List<List<Integer>>) ReflectionTestUtils.invokeMethod(estadisticaHelper, "splitIntoBatches", list, 2);

        // Assert
        assertThat(batches).hasSize(3);
        assertThat(batches.get(0)).containsExactly(1, 2);
        assertThat(batches.get(1)).containsExactly(3, 4);
        assertThat(batches.get(2)).containsExactly(5);
    }

    @Test
    @DisplayName("crearEstadistiques: retorna missatge d'error quan es produeix una excepcio")
    void crearEstadistiques_quanEsProdueixExcepcio_llavorsRetornaMissatgeError() {
        // Arrange
        RegistresEstadistics registres = buildRegistresEstadistics();
        when(tempsRepository.save(any(TempsEntity.class))).thenThrow(new RuntimeException("Error específic"));

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearEstadistiques", registres, 10L);

        // Assert
        assertThat(result).isEqualTo("Error específic");
    }

    @Test
    @DisplayName("crearEstadistiques: retorna cadena buida quan l'excepcio no te missatge")
    void crearEstadistiques_quanExcepcioSenseMissatge_llavorsRetornaBuit() {
        // Arrange
        RegistresEstadistics registres = buildRegistresEstadistics();
        when(tempsRepository.save(any(TempsEntity.class))).thenThrow(new RuntimeException());

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearEstadistiques", registres, 10L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("crearFets: afegeix CONS al mapa de dimensions quan el tipus es ORGAN_GESTOR")
    void crearFets_quanTipusOrganGestor_llavorsAfegeixCONSAlMapa() {
        // Arrange
        TempsEntity temps = new TempsEntity(LocalDate.now());
        RegistreEstadistic registre = new RegistreEstadistic();

        Dimensio dimensio = mock(Dimensio.class);
        lenient().when(dimensio.getCodi()).thenReturn("ORG");
        lenient().when(dimensio.getValor()).thenReturn("VAL1");
        registre.setDimensions(List.of(dimensio));

        es.caib.comanda.model.v1.estadistica.Fet fet = mock(es.caib.comanda.model.v1.estadistica.Fet.class);
        lenient().when(fet.getCodi()).thenReturn("IND1");
        lenient().when(fet.getValor()).thenReturn(100.0);
        registre.setFets(List.of(fet));

        DimensioEntity dimEntity = new DimensioEntity();
        dimEntity.setTipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum.ORGAN_GESTOR);
        when(dimensioRepository.findByCodiAndEntornAppId("ORG", 10L)).thenReturn(Optional.of(dimEntity));
        when(unitatsOrganitzativesPluginDir3.getConselleria("VAL1")).thenReturn("CONS1");

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearFets", List.of(registre), temps, 10L);

        // Assert
        ArgumentCaptor<List<es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(fetRepository).saveAll(captor.capture());
        es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity savedFet = captor.getValue().get(0);
        assertThat(savedFet.getDimensionsJson()).containsEntry("CONS", "CONS1");
    }

    @Test
    @DisplayName("crearFets: guarda en batches quan la llista arriba a la mida maxima")
    void crearFets_quanArribaBatchSize_llavorsGuardaEnBatches() {
        // Arrange
        TempsEntity temps = new TempsEntity(LocalDate.now());
        List<RegistreEstadistic> registres = new ArrayList<>();
        for (int i = 0; i < 501; i++) {
            RegistreEstadistic registre = new RegistreEstadistic();
            Dimensio dimensio = mock(Dimensio.class);
            lenient().when(dimensio.getCodi()).thenReturn("DIM" + i);
            lenient().when(dimensio.getValor()).thenReturn("VAL" + i);
            registre.setDimensions(List.of(dimensio));

            es.caib.comanda.model.v1.estadistica.Fet fet = mock(es.caib.comanda.model.v1.estadistica.Fet.class);
            lenient().when(fet.getCodi()).thenReturn("IND" + i);
            lenient().when(fet.getValor()).thenReturn(100.0);
            registre.setFets(List.of(fet));
            registres.add(registre);

            when(dimensioRepository.findByCodiAndEntornAppId(eq("DIM" + i), eq(10L))).thenReturn(Optional.empty());
        }

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearFets", registres, temps, 10L);

        // Assert
        verify(fetRepository, times(2)).saveAll(anyList()); // 500 + 1
    }

    @Test
    @DisplayName("crearFets: no fa res quan la llista de registres es null o buida")
    void crearFets_quanRegistresNullOBuits_llavorsNoFaRes() {
        // Arrange
        TempsEntity temps = new TempsEntity(LocalDate.now());

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearFets", (List<RegistreEstadistic>) null, temps, 10L);
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "crearFets", Collections.emptyList(), temps, 10L);

        // Assert
        verify(fetRepository, never()).deleteAllByTempsAndEntornAppId(any(), anyLong());
    }

    @Test
    @DisplayName("handleEstadisticaException: finalitza info action quan no ha finalitzat")
    void handleEstadisticaException_quanInfoNoFinalitzat_llavorsFinalitzaInfoAction() {
        // Arrange
        MonitorEstadistica monitor = mock(MonitorEstadistica.class);
        when(monitor.isFinishedInfoAction()).thenReturn(false);
        RestClientException ex = new RestClientException("Error");

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "handleEstadisticaException", entornApp, monitor, ex);

        // Assert
        verify(monitor).endInfoAction(ex);
        verify(monitor, never()).endDadesAction(any());
    }

    @Test
    @DisplayName("handleEstadisticaException: finalitza dades action quan info ha finalitzat pero dades no")
    void handleEstadisticaException_quanInfoFinalitzatIDadesNo_llavorsFinalitzaDadesAction() {
        // Arrange
        MonitorEstadistica monitor = mock(MonitorEstadistica.class);
        when(monitor.isFinishedInfoAction()).thenReturn(true);
        when(monitor.isFinishedDadesAction()).thenReturn(false);
        RestClientException ex = new RestClientException("Error");

        // Act
        ReflectionTestUtils.invokeMethod(estadisticaHelper, "handleEstadisticaException", entornApp, monitor, ex);

        // Assert
        verify(monitor).endDadesAction(ex);
        verify(monitor, never()).endInfoAction(any());
    }

    @Test
    @DisplayName("getRegistreEstadisticMessage: retorna false quan fets es null")
    void getRegistreEstadisticMessage_quanFetsNull_llavorsRetornaFalse() {
        // Arrange
        RegistresEstadistics registres = new RegistresEstadistics();
        registres.setFets(null);

        // Act
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(estadisticaHelper, "getRegistreEstadisticMessage", registres);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getRegistreEstadisticMessage: retorna false quan fets es buida")
    void getRegistreEstadisticMessage_quanFetsBuida_llavorsRetornaFalse() {
        // Arrange
        RegistresEstadistics registres = new RegistresEstadistics();
        registres.setFets(Collections.emptyList());

        // Act
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(estadisticaHelper, "getRegistreEstadisticMessage", registres);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getRegistreEstadisticMessage: retorna true quan fets te elements")
    void getRegistreEstadisticMessage_quanFetsTeElements_llavorsRetornaTrue() {
        // Arrange
        RegistresEstadistics registres = new RegistresEstadistics();
        registres.setFets(List.of(mock(RegistreEstadistic.class)));

        // Act
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(estadisticaHelper, "getRegistreEstadisticMessage", registres);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("toFet: converteix correctament FetEntity a Fet")
    void toFet_quanEsCrida_llavorsConverteixCorrectament() {
        // Arrange
        es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity entity = new es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity();
        entity.setEntornAppId(10L);
        TempsEntity temps = new TempsEntity(LocalDate.now());
        entity.setTemps(temps);
        entity.setDimensionsJson(Map.of("DIM1", "VAL1"));
        entity.setIndicadorsJson(Map.of("IND1", 100.0));

        // Act
        es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet result = estadisticaHelper.toFet(entity);

        // Assert
        assertThat(result.getEntornAppId()).isEqualTo(10L);
        assertThat(result.getTemps().getData()).isEqualTo(LocalDate.now());
        assertThat(result.getDimensionsJson()).containsEntry("DIM1", "VAL1");
        assertThat(result.getIndicadorsJson()).containsEntry("IND1", 100.0);
    }

    @Test
    @DisplayName("toFets: converteix correctament una llista de FetEntity a llista de Fet")
    void toFets_quanEsCrida_llavorsConverteixLlistaCorrectament() {
        // Arrange
        es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity entity = new es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity();
        entity.setEntornAppId(10L);
        entity.setTemps(new TempsEntity(LocalDate.now()));

        // Act
        List<es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet> result = estadisticaHelper.toFets(List.of(entity));

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntornAppId()).isEqualTo(10L);
    }
}
