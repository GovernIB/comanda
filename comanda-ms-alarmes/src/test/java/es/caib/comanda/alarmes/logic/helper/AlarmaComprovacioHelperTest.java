package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.logic.service.sse.ComandaSseEventPublisher;
import es.caib.comanda.alarmes.logic.event.AlarmaMailEventPublisher;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigCondicio;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigTipus;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.model.Salut;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigPeriodeUnitat;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

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
        lenient().when(parametresHelper.getParametreEnter("es.caib.comanda.alarma.salut.freshness.seconds", 120)).thenReturn(120);
        lenient().when(parametresHelper.getParametreEnter("es.caib.comanda.alarma.recovery.stability.seconds", 180)).thenReturn(180);
    }

    @Test
    @DisplayName("Comprova APP_CAIGUDA quan l'app està caiguda (DOWN)")
    void comprovar_quanAppCaigudaDown_retornaTrueIProcessaAfirmativa() {
        // Arrange
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
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
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
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
        config.setTipus(AlarmaConfigTipus.APP_LATENCIA);
        config.setCondicio(AlarmaConfigCondicio.MAJOR);
        config.setValor(new BigDecimal(500));

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
        config.setTipus(AlarmaConfigTipus.APP_LATENCIA);
        config.setCondicio(AlarmaConfigCondicio.MAJOR);
        config.setValor(new BigDecimal(500));

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
        config.setTipus(AlarmaConfigTipus.APP_LATENCIA);
        config.setCondicio(AlarmaConfigCondicio.MENOR);
        config.setValor(new BigDecimal(100));

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
        config.setTipus(AlarmaConfigTipus.APP_LATENCIA);
        config.setCondicio(AlarmaConfigCondicio.MENOR_IGUAL);
        config.setValor(new BigDecimal(100));

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
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        
        es.caib.comanda.alarmes.persist.entity.AlarmaEntity alarmaActiva = new es.caib.comanda.alarmes.persist.entity.AlarmaEntity();
        alarmaActiva.setId(100L);
        alarmaActiva.setAlarmaConfig(config);
        alarmaActiva.setDataActivacio(java.time.LocalDateTime.now().minusHours(1));
        alarmaActiva.setEstat(AlarmaEstat.ACTIVA);

        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.of(alarmaActiva));

        // Act
        boolean result = alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(result).isFalse();
        assertThat(alarmaActiva.getDataFinalitzacio()).isNull();
    }

    @Test
    @DisplayName("Finalitza una alarma quan la recuperació ja és estable")
    void comprovar_quanCondicioJaNoEsCompleixIRecuperacioEstable_finalitzaAlarma() {
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
        when(parametresHelper.getParametreEnter("es.caib.comanda.alarma.recovery.stability.seconds", 180)).thenReturn(1);
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);

        AlarmaEntity alarmaActiva = new AlarmaEntity();
        alarmaActiva.setId(100L);
        alarmaActiva.setAlarmaConfig(config);
        alarmaActiva.setDataActivacio(LocalDateTime.now().minusHours(1));
        alarmaActiva.setEstat(AlarmaEstat.ACTIVA);

        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.of(alarmaActiva));

        alarmaComprovacioHelper.comprovar(config);
        sleepSilently(1100);

        boolean result = alarmaComprovacioHelper.comprovar(config);

        assertThat(result).isFalse();
        assertThat(alarmaActiva.getDataFinalitzacio()).isNotNull();
    }

    @Test
    @DisplayName("Esborra una alarma en estat ESBORRANY quan la condició ja no es compleix")
    void comprovar_quanCondicioJaNoEsCompleixIEstatEsborrany_esborraAlarma() {
        // Arrange
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.UP.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        
        es.caib.comanda.alarmes.persist.entity.AlarmaEntity alarmaEsborrany = new es.caib.comanda.alarmes.persist.entity.AlarmaEntity();
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
        config.setTipus(AlarmaConfigTipus.APP_LATENCIA);
        config.setCondicio(AlarmaConfigCondicio.MAJOR_IGUAL);
        config.setValor(new BigDecimal(100));

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
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
        PagedModel<EntityModel<Salut>> pagedModel = pagedModelFor(freshSalut().appEstat(EstatSalutEnum.DOWN.name()).build());

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        when(salutServiceClient.find(any(), anyString(), any(), any(), anyString(), anyInt(), any(), anyString()))
                .thenReturn(pagedModel);
        
        es.caib.comanda.alarmes.persist.entity.AlarmaEntity alarmaOberta = new es.caib.comanda.alarmes.persist.entity.AlarmaEntity();
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
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
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
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
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
        alarmaEsborrany.setCreatedDate(java.time.LocalDateTime.now().minusMinutes(2));

        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
                .thenReturn(Optional.of(alarmaEsborrany));

        // Act
        alarmaComprovacioHelper.comprovar(config);

        // Assert
        assertThat(alarmaEsborrany.getEstat()).isEqualTo(AlarmaEstat.ACTIVA);
        assertThat(alarmaEsborrany.getDataActivacio()).isNotNull();
        verify(alarmaMailEventPublisher).publish(alarmaEsborrany);
    }

    @Test
    @DisplayName("Processar condició afirmativa sense període quan hi ha un ESBORRANY (l'activa immediatament)")
    void processarCondicioAfirmativa_sensePeriodeIAlarmaEsborrany_activaImmediatament() {
        // Arrange
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
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
        verify(alarmaMailEventPublisher, never()).publish(any()); // No s'assigna a alarmaActivada a la línia 157, només es canvia l'estat
    }

    @Test
    @DisplayName("Processar condició afirmativa sense període quan no hi ha alarma anterior (crea ACTIVA i envia mail)")
    void processarCondicioAfirmativa_sensePeriodeISenseAlarmaAnterior_creaActivaIEnviaMail() {
        // Arrange
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
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
        verify(alarmaMailEventPublisher).publish(novaAlarma);
    }

    @Test
    @DisplayName("Quan la salut és antiga no activa ni finalitza alarmes")
    void comprovar_quanSalutNoEsRecent_noModificaAlarmes() {
        config.setTipus(AlarmaConfigTipus.APP_CAIGUDA);
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

//        when(alarmaRepository.findTopByAlarmaConfigAndDataFinalitzacioIsNullOrderByIdDesc(config))
//                .thenReturn(Optional.of(alarmaActiva));

        boolean result = alarmaComprovacioHelper.comprovar(config);

        assertThat(result).isFalse();
        assertThat(alarmaActiva.getDataFinalitzacio()).isNull();
        verify(alarmaRepository, never()).save(any());
        verify(alarmaRepository, never()).delete(any());
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
}
