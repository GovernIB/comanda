package es.caib.comanda.alarmes.logic.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.alarmes.logic.event.AlarmaMailEventPublisher;
import es.caib.comanda.alarmes.logic.event.AlarmaMailEventType;
import es.caib.comanda.alarmes.logic.intf.model.*;
import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.model.*;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlarmaComprovacioHelperTest {

    @Mock
    private AlarmaRepository alarmaRepository;
    @Mock
    private SalutServiceClient salutServiceClient;
    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock
    private AlarmaMailEventPublisher alarmaMailEventPublisher;
    @Mock
    private ComandaSseEventPublisher comandaSseEventPublisher;
    @Mock
    private ParametresHelper parametresHelper;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AlarmaComprovacioHelper alarmaComprovacioHelper;

    private AlarmaConfigEntity config;
    private static final String AUTH_HEADER = "Bearer token";

    @BeforeEach
    void setUp() {
        config = new AlarmaConfigEntity();
        config.setId(1L);
        config.setEntornAppId(10L);
        config.setNom("Test Config");
        config.setMissatge("Missatge d'alarma");
        lenient().when(parametresHelper.getParametreEnter(eq("es.caib.comanda.alarma.salut.freshness.seconds"), any())).thenReturn(120);
        lenient().when(parametresHelper.getParametreEnter(eq("es.caib.comanda.alarma.recovery.stability.seconds"), any())).thenReturn(180);
        lenient().when(alarmaRepository.save(any(AlarmaEntity.class)))
            .thenAnswer(invocation -> {
                AlarmaEntity entity = invocation.getArgument(0);
                if (entity.getId() == null) {
                    entity.setId(123L);
                }
                return entity;
            });

    }

    private void mockRule(AlarmaConfigRegla regla) {
        config.setRuleJson("{}");
        try {
            lenient().when(objectMapper.readValue(anyString(), eq(AlarmaConfigRegla.class))).thenReturn(regla);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Comprova APP_CAIGUDA quan l'app està caiguda (DOWN)")
    void comprovar_quanAppCaigudaDown_retornaTrueIProcessaAfirmativa() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.DOWN.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), eq("entornAppId:10"), any(), any(), eq("0"), eq(1), any(), eq(AUTH_HEADER)))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
        verify(alarmaRepository).save(any());
    }

    @Test
    @DisplayName("Comprova APP_CAIGUDA quan l'app està amunt (UP)")
    void comprovar_quanAppUp_retornaFalseIProcessaNegativa() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isFalse();
        verify(alarmaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Comprova APP_LATENCIA quan supera el llindar (MAJOR)")
    void comprovar_quanLatenciaMajor_retornaTrue() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.LATENCIA)
                .comparador(AlarmaConfigReglaComparador.MAJOR)
                .valorNumeric(new BigDecimal(500))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).appLatencia(600).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
        verify(alarmaRepository).save(any());
    }

    @Test
    @DisplayName("Comprova APP_LATENCIA quan no supera el llindar (MAJOR)")
    void comprovar_quanLatenciaMenor_retornaFalse() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.LATENCIA)
                .comparador(AlarmaConfigReglaComparador.MAJOR)
                .valorNumeric(new BigDecimal(500))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).appLatencia(400).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isFalse();
        verify(alarmaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Comprova APP_LATENCIA quan és MENOR que el llindar")
    void comprovar_quanLatenciaMenor_retornaTrue() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.LATENCIA)
                .comparador(AlarmaConfigReglaComparador.MENOR)
                .valorNumeric(new BigDecimal(100))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).appLatencia(50).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
        verify(alarmaRepository).save(any());
    }

    @Test
    @DisplayName("Comprova APP_LATENCIA quan és MENOR_IGUAL que el llindar")
    void comprovar_quanLatenciaMenorIgual_retornaTrue() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.LATENCIA)
                .comparador(AlarmaConfigReglaComparador.MENOR_IGUAL)
                .valorNumeric(new BigDecimal(100))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).appLatencia(100).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("No finalitza una alarma fins que la recuperació és estable")
    void comprovar_quanCondicioJaNoEsCompleixPeroRecuperacioNoEstable_noFinalitzaAlarma() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        AlarmaEntity alarmaActiva = new AlarmaEntity();
        alarmaActiva.setId(100L);
        alarmaActiva.setAlarmaConfig(config);
        alarmaActiva.setDataActivacio(LocalDateTime.now().minusHours(1));
        alarmaActiva.setEstat(AlarmaEstat.ACTIVA);
        alarmaActiva.setDataIniciRecuperacio(LocalDateTime.now().minusSeconds(10));

        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.of(alarmaActiva));

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
        assertThat(alarmaActiva.getDataFinalitzacio()).isNull();
        assertThat(alarmaActiva.getDataIniciRecuperacio()).isNotNull();
    }

    @Test
    @DisplayName("Finalitza una alarma quan la recuperació ja és estable")
    void comprovar_quanCondicioJaNoEsCompleixIRecuperacioEstable_finalitzaAlarma() {
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());
        when(parametresHelper.getParametreEnter(eq("es.caib.comanda.alarma.recovery.stability.seconds"), any())).thenReturn(1);
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        AlarmaEntity alarmaActiva = new AlarmaEntity();
        alarmaActiva.setId(100L);
        alarmaActiva.setAlarmaConfig(config);
        alarmaActiva.setDataActivacio(LocalDateTime.now().minusHours(1));
        alarmaActiva.setEstat(AlarmaEstat.ACTIVA);
        alarmaActiva.setDataIniciRecuperacio(LocalDateTime.now().minusSeconds(2));

        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.of(alarmaActiva));

        boolean result = alarmaComprovacioHelper.comprovar(config);

        assertThat(result).isFalse();
        assertThat(alarmaActiva.getDataFinalitzacio()).isNotNull();
        assertThat(alarmaActiva.getDataIniciRecuperacio()).isNull();
    }

    @Test
    @DisplayName("Esborra una alarma en estat ESBORRANY quan la condició ja no es compleix")
    void comprovar_quanCondicioJaNoEsCompleixIEstatEsborrany_esborraAlarma() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        AlarmaEntity alarmaEsborrany = new AlarmaEntity();
        alarmaEsborrany.setEstat(AlarmaEstat.ESBORRANY);

        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.of(alarmaEsborrany));

        // Act
        alarmaComprovacioHelper.comprovar(config);

        // Assert
        verify(alarmaRepository).delete(alarmaEsborrany);
    }

    @Test
    @DisplayName("Comprova APP_LATENCIA quan és MAJOR_IGUAL que el llindar")
    void comprovar_quanLatenciaMajorIgual_retornaTrue() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.LATENCIA)
                .comparador(AlarmaConfigReglaComparador.MAJOR_IGUAL)
                .valorNumeric(new BigDecimal(100))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).appLatencia(100).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Processa condició afirmativa quan ja existeix una alarma oberta (no crea nova)")
    void processarCondicioAfirmativa_quanJaOberta_noCreaNova() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.DOWN.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        AlarmaEntity alarmaOberta = new AlarmaEntity();
        alarmaOberta.setEstat(AlarmaEstat.ACTIVA);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.of(alarmaOberta));

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
        verify(alarmaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Processar condició afirmativa amb període quan no hi ha alarma anterior (crea ESBORRANY)")
    void processarCondicioAfirmativa_ambPeriodeIColorSenseAlarmaAnterior_creaEsborrany() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());
        config.setPeriodeValor(new BigDecimal(60));
        config.setPeriodeUnitat(AlarmaConfigPeriodeUnitat.SEGONS);

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.DOWN.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        alarmaComprovacioHelper.comprovar(config);

        // Assert
        verify(alarmaRepository).save(argThat(entity -> entity.getEstat() == AlarmaEstat.ESBORRANY));
    }

    @Test
    @DisplayName("Processar condició afirmativa amb període quan hi ha ESBORRANY i ha passat el temps (activa alarma)")
    void processarCondicioAfirmativa_ambPeriodeIAlarmaEsborranyExpirada_activaAlarma() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());
        config.setPeriodeValor(new BigDecimal(1));
        config.setPeriodeUnitat(AlarmaConfigPeriodeUnitat.MINUTS);

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.DOWN.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        AlarmaEntity alarmaEsborrany = new AlarmaEntity();
        alarmaEsborrany.setEstat(AlarmaEstat.ESBORRANY);
        alarmaEsborrany.setAlarmaConfig(config);
        // Creada fa 2 minuts
        alarmaEsborrany.setCreatedDate(LocalDateTime.now().minusMinutes(2));

        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.of(alarmaEsborrany));

        // Act
        alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(alarmaEsborrany.getEstat()).isEqualTo(AlarmaEstat.ACTIVA);
        assertThat(alarmaEsborrany.getDataActivacio()).isNotNull();
        verify(alarmaMailEventPublisher).publish(alarmaEsborrany, AlarmaMailEventType.ACTIVACIO);
    }

    @Test
    @DisplayName("Processar condició afirmativa sense període quan hi ha un ESBORRANY (l'activa immediatament)")
    void processarCondicioAfirmativa_sensePeriodeIAlarmaEsborrany_activaImmediatament() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());
        config.setPeriodeValor(null); // Sense període

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.DOWN.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        AlarmaEntity alarmaEsborrany = new AlarmaEntity();
        alarmaEsborrany.setEstat(AlarmaEstat.ESBORRANY);

        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.of(alarmaEsborrany));

        // Act
        alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(alarmaEsborrany.getEstat()).isEqualTo(AlarmaEstat.ACTIVA);
        verify(alarmaMailEventPublisher, times(1)).publish(any(), any());
    }

    @Test
    @DisplayName("Processar condició afirmativa sense període quan no hi ha alarma anterior (crea ACTIVA i envia mail)")
    void processarCondicioAfirmativa_sensePeriodeISenseAlarmaAnterior_creaActivaIEnviaMail() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());
        config.setPeriodeValor(null);

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.DOWN.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        AlarmaEntity novaAlarma = new AlarmaEntity();
        novaAlarma.setEstat(AlarmaEstat.ACTIVA);
        novaAlarma.setAlarmaConfig(config);
        when(alarmaRepository.save(any())).thenReturn(novaAlarma);

        // Act
        alarmaComprovacioHelper.comprovar(config);

        // Assert
        verify(alarmaRepository).save(argThat(entity -> entity.getEstat() == AlarmaEstat.ACTIVA));
        verify(alarmaMailEventPublisher).publish(novaAlarma, AlarmaMailEventType.ACTIVACIO);
    }

    @Test
    @DisplayName("Quan la salut és antiga no activa ni finalitza alarmes")
    void comprovar_quanSalutNoEsRecent_noModificaAlarmes() {
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build());
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(Salut.builder()
                .appEstat(EstatSalutEnum.UP.name())
                .data(LocalDateTime.now().minusMinutes(10))
                .build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        AlarmaEntity alarmaActiva = new AlarmaEntity();
        alarmaActiva.setId(100L);
        alarmaActiva.setAlarmaConfig(config);
        alarmaActiva.setEstat(AlarmaEstat.ACTIVA);

        boolean result = alarmaComprovacioHelper.comprovar(config);

        assertThat(result).isFalse();
        assertThat(alarmaActiva.getDataFinalitzacio()).isNull();
        verify(alarmaRepository, never()).save(any());
        verify(alarmaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Comprova SUBSISTEMA estat EN DOWN")
    void comprovar_quanSubsistemaEstatDown_retornaTrue() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.SUBSISTEMA)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .codiObjecte("SUBSIS1")
                .valorsText(Collections.singletonList("DOWN"))
                .build());

        Salut salut = freshSalut()
                .appEstat(EstatSalutEnum.UP.name())
                .subsistemes(Collections.singletonList(
                        SalutSubsistema.builder().codi("SUBSIS1").estat(SalutEstat.DOWN).build()
                ))
                .build();
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(salut);

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
        verify(alarmaRepository).save(any());
    }

    @Test
    @DisplayName("Comprova INTEGRACIO estat EN DOWN")
    void comprovar_quanIntegracioEstatDown_retornaTrue() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.INTEGRACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .codiObjecte("INT1")
                .valorsText(Collections.singletonList("DOWN"))
                .build());

        Salut salut = freshSalut()
                .appEstat(EstatSalutEnum.UP.name())
                .integracions(Collections.singletonList(
                        SalutIntegracio.builder().codi("INT1").estat(SalutEstat.DOWN).build()
                ))
                .build();
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(salut);

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Comprova GRUP AND amb dues condicions")
    void comprovar_quanGrupAndDuesCondicions_retornaTrueSiAmbduesEsCompleixen() {
        // Arrange
        AlarmaConfigRegla condicio1 = AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build();
        AlarmaConfigRegla condicio2 = AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.LATENCIA)
                .comparador(AlarmaConfigReglaComparador.MAJOR)
                .valorNumeric(new BigDecimal(1000))
                .build();

        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.GRUP)
                .operador(AlarmaConfigReglaOperador.AND)
                .fills(java.util.Arrays.asList(condicio1, condicio2))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut()
                .appEstat(EstatSalutEnum.DOWN.name())
                .appLatencia(1500)
                .build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Comprova GRUP OR amb dues condicions")
    void comprovar_quanGrupOrDuesCondicions_retornaTrueSiUnaEsCompleix() {
        // Arrange
        AlarmaConfigRegla condicio1 = AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.EN)
                .valorsText(Collections.singletonList("DOWN"))
                .build();
        AlarmaConfigRegla condicio2 = AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.LATENCIA)
                .comparador(AlarmaConfigReglaComparador.MAJOR)
                .valorNumeric(new BigDecimal(1000))
                .build();

        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.GRUP)
                .operador(AlarmaConfigReglaOperador.OR)
                .fills(java.util.Arrays.asList(condicio1, condicio2))
                .build());

        // Condició 1 false (UP), Condició 2 true (1500 > 1000)
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut()
                .appEstat(EstatSalutEnum.UP.name())
                .appLatencia(1500)
                .build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Comprova CARREGA_MITJANA_SISTEMA (LAVG) MAJOR que el llindar")
    void comprovar_quanCarregaSistemaMajor_retornaTrue() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.SISTEMA)
                .metrica(AlarmaConfigReglaMetrica.CARREGA_MITJANA_SISTEMA)
                .comparador(AlarmaConfigReglaComparador.MAJOR)
                .valorNumeric(new BigDecimal(1))
                .build());

        Salut salut = freshSalut()
                .detalls(Collections.singletonList(
                        SalutDetall.builder().codi("LAVG").valor("1.5").build()
                ))
                .build();
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(salut);

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Comprova MEMORIA_DISPONIBLE (MED) MENOR que el llindar amb unitats GB")
    void comprovar_quanMemoriaDisponibleMenor_retornaTrue() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.SISTEMA)
                .metrica(AlarmaConfigReglaMetrica.MEMORIA_DISPONIBLE)
                .comparador(AlarmaConfigReglaComparador.MENOR)
                .valorNumeric(new BigDecimal(2048)) // 2GB en MB
                .build());

        Salut salut = freshSalut()
                .detalls(Collections.singletonList(
                        SalutDetall.builder().codi("MED").valor("1 GB").build()
                ))
                .build();
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(salut);

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Comprova ESPAI_DISC_LLIURE (EDL) MENOR que el llindar amb unitats TB")
    void comprovar_quanEspaiDiscLliureMenor_retornaTrue() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.SISTEMA)
                .metrica(AlarmaConfigReglaMetrica.ESPAI_DISC_LLIURE)
                .comparador(AlarmaConfigReglaComparador.MENOR)
                .valorNumeric(new BigDecimal(2097152)) // 2TB en MB
                .build());

        Salut salut = freshSalut()
                .detalls(Collections.singletonList(
                        SalutDetall.builder().codi("EDL").valor("1.5 TB").build()
                ))
                .build();
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(salut);

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Comprova text comparador DIFERENT")
    void comprovar_quanComparadorDiferent_retornaTrueSiNoCoincideix() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.DIFERENT)
                .valorsText(Collections.singletonList("UP"))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.DOWN.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Comprova text comparador IGUAL")
    void comprovar_quanComparadorIgual_retornaTrueSiCoincideix() {
        // Arrange
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.APLICACIO)
                .metrica(AlarmaConfigReglaMetrica.ESTAT)
                .comparador(AlarmaConfigReglaComparador.IGUAL)
                .valorsText(Collections.singletonList("DOWN"))
                .build());

        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.DOWN.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Comprova MEMORIA_DISPONIBLE amb unitats KB i B")
    void comprovar_quanMemoriaDisponibleKBIB_retornaCorrectament() {
        // Test KB
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.SISTEMA)
                .metrica(AlarmaConfigReglaMetrica.MEMORIA_DISPONIBLE)
                .comparador(AlarmaConfigReglaComparador.MENOR)
                .valorNumeric(new BigDecimal(1)) // 1 MB
                .build());

        Salut salutKB = freshSalut().detalls(Collections.singletonList(
                SalutDetall.builder().codi("MED").valor("0,5 KB").build()
        )).build();

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModelFor(salutKB));
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        boolean resultKB = alarmaComprovacioHelper.comprovar(config);
        assertThat(resultKB).as("Check KB").isTrue();

        // Test B
        Salut salutB = freshSalut().detalls(Collections.singletonList(
                SalutDetall.builder().codi("MED").valor("1048576 B").build()
        )).build();
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModelFor(salutB));

        // 1048576 B = 1 MB. Condició: MENOR que 1 MB. Result: false.
        boolean resultB = alarmaComprovacioHelper.comprovar(config);
        assertThat(resultB).as("Check B").isFalse();
    }

    @Test
    @DisplayName("Comprova normalització de nombres amb coma decimal")
    void comprovar_quanNombreAmbComa_parsejaCorrectament() {
        mockRule(AlarmaConfigRegla.builder()
                .tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
                .ambit(AlarmaConfigReglaAmbit.SISTEMA)
                .metrica(AlarmaConfigReglaMetrica.CARREGA_MITJANA_SISTEMA)
                .comparador(AlarmaConfigReglaComparador.MAJOR)
                .valorNumeric(new BigDecimal("80.5"))
                .build());

        Salut salut = freshSalut().detalls(Collections.singletonList(
                SalutDetall.builder().codi("LAVG").valor("80,6").build()
        )).build();

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModelFor(salut));
        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.empty());

        assertThat(alarmaComprovacioHelper.comprovar(config)).isTrue();
    }

	@Test
	@DisplayName("L'activació d'una nova alarma agafa la data de la Salut")
	void comprovar_quanNovaAlarma_dataActivacioEsLaDeLaSalut() {
		// Arrange
		mockRule(AlarmaConfigRegla.builder()
			.tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
			.ambit(AlarmaConfigReglaAmbit.APLICACIO)
			.metrica(AlarmaConfigReglaMetrica.ESTAT)
			.comparador(AlarmaConfigReglaComparador.EN)
			.valorsText(Collections.singletonList("DOWN"))
			.build());
		LocalDateTime dataSalut = LocalDateTime.now().minusMinutes(1);
		PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(Salut.builder()
			.appEstat(EstatSalutEnum.DOWN.name())
			.data(dataSalut)
			.build());

		when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
		when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
			.thenReturn(pagedModel);
		when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
			.thenReturn(Optional.empty());

		// Act
		alarmaComprovacioHelper.comprovar(config);

		// Assert
		verify(alarmaRepository).save(argThat(entity ->
			entity.getEstat() == AlarmaEstat.ACTIVA &&
				dataSalut.equals(entity.getDataActivacio())
		));
	}

	@Test
	@DisplayName("L'activació d'un esborrany agafa la data de la Salut")
	void comprovar_quanActivaEsborrany_dataActivacioEsLaDeLaSalut() {
		// Arrange
		mockRule(AlarmaConfigRegla.builder()
			.tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
			.ambit(AlarmaConfigReglaAmbit.APLICACIO)
			.metrica(AlarmaConfigReglaMetrica.ESTAT)
			.comparador(AlarmaConfigReglaComparador.EN)
			.valorsText(Collections.singletonList("DOWN"))
			.build());
		config.setPeriodeValor(new BigDecimal(1));
		config.setPeriodeUnitat(AlarmaConfigPeriodeUnitat.MINUTS);

		LocalDateTime dataSalut = LocalDateTime.now().minusMinutes(1);
		PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(Salut.builder()
			.appEstat(EstatSalutEnum.DOWN.name())
			.data(dataSalut)
			.build());

		when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
		when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
			.thenReturn(pagedModel);

		AlarmaEntity alarmaEsborrany = new AlarmaEntity();
		alarmaEsborrany.setEstat(AlarmaEstat.ESBORRANY);
		alarmaEsborrany.setAlarmaConfig(config);
		alarmaEsborrany.setCreatedDate(LocalDateTime.now().minusMinutes(2));

		when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
			.thenReturn(Optional.of(alarmaEsborrany));

		// Act
		alarmaComprovacioHelper.comprovar(config);

		// Assert
		assertThat(alarmaEsborrany.getEstat()).isEqualTo(AlarmaEstat.ACTIVA);
		assertThat(alarmaEsborrany.getDataActivacio()).isEqualTo(dataSalut);
	}

	@Test
	@DisplayName("L'activació d'un esborrany quan es canvia a sense període agafa la data de la Salut")
	void comprovar_quanCanviaASensePeriodeIActivaEsborrany_dataActivacioEsLaDeLaSalut() {
		// Arrange
		mockRule(AlarmaConfigRegla.builder()
			.tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
			.ambit(AlarmaConfigReglaAmbit.APLICACIO)
			.metrica(AlarmaConfigReglaMetrica.ESTAT)
			.comparador(AlarmaConfigReglaComparador.EN)
			.valorsText(Collections.singletonList("DOWN"))
			.build());
		config.setPeriodeValor(null); // Sense període

		LocalDateTime dataSalut = LocalDateTime.now().minusMinutes(1);
		PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(Salut.builder()
			.appEstat(EstatSalutEnum.DOWN.name())
			.data(dataSalut)
			.build());

		when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
		when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
			.thenReturn(pagedModel);

		AlarmaEntity alarmaEsborrany = new AlarmaEntity();
		alarmaEsborrany.setEstat(AlarmaEstat.ESBORRANY);
		alarmaEsborrany.setAlarmaConfig(config);

		when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
			.thenReturn(Optional.of(alarmaEsborrany));

		// Act
		alarmaComprovacioHelper.comprovar(config);

		// Assert
		assertThat(alarmaEsborrany.getEstat()).isEqualTo(AlarmaEstat.ACTIVA);
		assertThat(alarmaEsborrany.getDataActivacio()).isEqualTo(dataSalut);
		verify(alarmaMailEventPublisher).publish(alarmaEsborrany, AlarmaMailEventType.ACTIVACIO);
	}

	@Test
	@DisplayName("La finalització d'una alarma agafa la data d'inici de la recuperació")
	void comprovar_quanFinalitzaAlarma_dataFinalitzacioEsLaDeIniciRecuperacio() {
		// Arrange
		mockRule(AlarmaConfigRegla.builder()
			.tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
			.ambit(AlarmaConfigReglaAmbit.APLICACIO)
			.metrica(AlarmaConfigReglaMetrica.ESTAT)
			.comparador(AlarmaConfigReglaComparador.EN)
			.valorsText(Collections.singletonList("DOWN"))
			.build());
		// Forcem que la recuperació sigui estable immediatament
		when(parametresHelper.getParametreEnter(eq("es.caib.comanda.alarma.recovery.stability.seconds"), any())).thenReturn(0);

		LocalDateTime dataSalut = LocalDateTime.now().minusSeconds(10);
		PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(Salut.builder()
			.appEstat(EstatSalutEnum.UP.name())
			.data(dataSalut)
			.build());

		when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
		when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
			.thenReturn(pagedModel);

		AlarmaEntity alarmaActiva = new AlarmaEntity();
		alarmaActiva.setEstat(AlarmaEstat.ACTIVA);
		alarmaActiva.setDataActivacio(LocalDateTime.now().minusHours(1));
		LocalDateTime dataIniciRecuperacio = LocalDateTime.now().minusSeconds(5);
		alarmaActiva.setDataIniciRecuperacio(dataIniciRecuperacio);

		when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
			.thenReturn(Optional.of(alarmaActiva));

		// Act
		alarmaComprovacioHelper.comprovar(config);

		// Assert
		assertThat(alarmaActiva.getDataFinalitzacio()).isEqualTo(dataIniciRecuperacio);
	}

	@ParameterizedTest(name = "valor=\"{0}\", comparador={1}, llindar={2}, resultat={3}")
	@MethodSource("memoriaDisponibleCases")
	@DisplayName("Comprova MEMORIA_DISPONIBLE amb diferents unitats i comparadors")
	void comprovar_quanMemoriaDisponible_retornaCorrectament(
		String valorSalut,
		AlarmaConfigReglaComparador comparador,
		BigDecimal valorNumeric,
		boolean expectedResult
	) {
		lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader())
			.thenReturn(AUTH_HEADER);

		lenient().when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(any()))
			.thenReturn(Optional.empty());

		mockRule(AlarmaConfigRegla.builder()
			.tipusNode(AlarmaConfigReglaTipusNode.CONDICIO)
			.ambit(AlarmaConfigReglaAmbit.SISTEMA)
			.metrica(AlarmaConfigReglaMetrica.MEMORIA_DISPONIBLE)
			.comparador(comparador)
			.valorNumeric(valorNumeric)
			.build());

		Salut salut = freshSalut().detalls(Collections.singletonList(
			SalutDetall.builder()
				.codi("MED")
				.valor(valorSalut)
				.build()
		)).build();

		lenient().when(salutServiceClient.find(
				any(),
				anyString(),
				any(),
				any(),
				anyString(),
				anyInt(),
				any(),
				anyString()))
			.thenReturn(pagedModelFor(salut));

		assertThat(alarmaComprovacioHelper.comprovar(config))
			.isEqualTo(expectedResult);
	}

	private static Stream<Arguments> memoriaDisponibleCases() {
		List<Object[]> baseCases = new ArrayList<>();
		List<Arguments> cases = new ArrayList<>();

		// Comparacions
		baseCases.add(new Object[]{"4096 K", AlarmaConfigReglaComparador.MENOR, BigDecimal.TEN, true});
		baseCases.add(new Object[]{"4096 K", AlarmaConfigReglaComparador.MENOR, BigDecimal.ONE, false});
		baseCases.add(new Object[]{"2 M", AlarmaConfigReglaComparador.MAJOR, BigDecimal.ONE, true});
		baseCases.add(new Object[]{"2 T", AlarmaConfigReglaComparador.MAJOR, BigDecimal.ONE, true});
		baseCases.add(new Object[]{"1 G", AlarmaConfigReglaComparador.MAJOR, new BigDecimal(2048), false});
		baseCases.add(new Object[]{"1024 M", AlarmaConfigReglaComparador.IGUAL, new BigDecimal(1024), true});
		baseCases.add(new Object[]{"1 G", AlarmaConfigReglaComparador.IGUAL, new BigDecimal(1024), true});
		baseCases.add(new Object[]{"1 T", AlarmaConfigReglaComparador.IGUAL, new BigDecimal(1024), false});
		// Conversió d'unitats
		baseCases.add(new Object[]{"1 K", AlarmaConfigReglaComparador.IGUAL, new BigDecimal(1), false}); // KB s'arrodoneix a la unitat nativa (MB)
		baseCases.add(new Object[]{"600 K", AlarmaConfigReglaComparador.IGUAL, new BigDecimal(1), true}); // KB s'arrodoneix a la unitat nativa (MB)
		baseCases.add(new Object[]{"1 M", AlarmaConfigReglaComparador.IGUAL, new BigDecimal(1), true});
		baseCases.add(new Object[]{"1 G", AlarmaConfigReglaComparador.IGUAL, new BigDecimal(1024), true});
		baseCases.add(new Object[]{"1 T", AlarmaConfigReglaComparador.IGUAL, new BigDecimal(1024*1024), true});

		// Add base cases as decimal scale
		cases.addAll(baseCases.stream()
			.map(baseCase -> Arguments.of(baseCase[0] + "B", baseCase[1], baseCase[2], baseCase[3]))
			.collect(Collectors.toList()));
		// Add base cases as binary scale
		cases.addAll(baseCases.stream()
			.map(baseCase -> Arguments.of(baseCase[0] + "iB", baseCase[1], baseCase[2], baseCase[3]))
			.collect(Collectors.toList()));
        return cases.stream();
	}

    private Salut.SalutBuilder freshSalut() {
        return Salut.builder().data(LocalDateTime.now());
    }

    private PagedModel<EntityModel<Salut>> pagedModelFor(Salut salut) {
        EntityModel<Salut> entityModel = EntityModel.of(salut);
        return PagedModel.of(Collections.singletonList(entityModel), new PagedModel.PageMetadata(1, 0, 1));
    }

    private void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupció inesperada durant el test", ex);
        }
    }

    @ParameterizedTest(name = "desde={0}, fins={1}, horaActual={2} → esperat={3}")
    @MethodSource("periodeInactiuCases")
    @DisplayName("isAlarmaPeriodeInactiu: diferents combinacions de franges horàries")
    void isAlarmaPeriodeInactiu_casosParametritzats(
            LocalTime inactiuDesde,
            LocalTime inactiuFins,
            LocalTime horaActual,
            boolean expectedResult
    ) {
        // Arrange && Act
        boolean result = alarmaComprovacioHelper.estaDinsFranjaHoraria(inactiuDesde, inactiuFins, horaActual);
        // Assert
        assertThat(result).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> periodeInactiuCases() {
        return Stream.of(
                Arguments.of(LocalTime.of(9, 0), LocalTime.of(18, 0), LocalTime.of(12, 0), true),
                Arguments.of(LocalTime.of(9, 0), LocalTime.of(18, 0), LocalTime.of(8, 0), false),
                Arguments.of(LocalTime.of(9, 0), LocalTime.of(18, 0), LocalTime.of(19, 0), false),
                Arguments.of(LocalTime.of(9, 0), LocalTime.of(18, 0), LocalTime.of(9, 0), true),
                Arguments.of(LocalTime.of(22, 0), LocalTime.of(3, 0), LocalTime.of(23, 0), true),
                Arguments.of(LocalTime.of(22, 0), LocalTime.of(3, 0), LocalTime.of(1, 0), true),
                Arguments.of(LocalTime.of(22, 0), LocalTime.of(3, 0), LocalTime.of(10, 0), false),
                Arguments.of(LocalTime.of(22, 0), LocalTime.of(3, 0), LocalTime.of(22, 0), true),
                Arguments.of(null, LocalTime.of(3, 0), LocalTime.of(0, 0), true),
                Arguments.of(LocalTime.of(22, 0), null, LocalTime.of(23, 0), true),
                Arguments.of(LocalTime.of(10, 0), LocalTime.of(10, 0), LocalTime.of(10, 0), true),
                Arguments.of(LocalTime.of(10, 0), LocalTime.of(10, 0), LocalTime.of(10, 1), false)
        );
    }

}
