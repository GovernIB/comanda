package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.ms.salut.model.DetallSalut;
import es.caib.comanda.ms.salut.model.EstatSalut;
import es.caib.comanda.ms.salut.model.EstatSalutEnum;
import es.caib.comanda.ms.salut.model.IntegracioPeticions;
import es.caib.comanda.ms.salut.model.IntegracioSalut;
import es.caib.comanda.ms.salut.model.MissatgeSalut;
import es.caib.comanda.ms.salut.model.SalutInfo;
import es.caib.comanda.ms.salut.model.SubsistemaSalut;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.persist.entity.SalutDetallEntity;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutIntegracioEntity;
import es.caib.comanda.salut.persist.entity.SalutMissatgeEntity;
import es.caib.comanda.salut.persist.entity.SalutSubsistemaEntity;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalutInfoHelperTest {

    @Mock
    private SalutRepository salutRepository;

    @Mock
    private SalutIntegracioRepository salutIntegracioRepository;

    @Mock
    private SalutSubsistemaRepository salutSubsistemaRepository;

    @Mock
    private SalutMissatgeRepository salutMissatgeRepository;

    @Mock
    private SalutDetallRepository salutDetallRepository;

    @Mock
    private SalutClientHelper salutClientHelper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SalutInfoHelper salutInfoHelper;

    @Captor
    private ArgumentCaptor<SalutEntity> salutEntityCaptor;

    @Captor
    private ArgumentCaptor<SalutIntegracioEntity> salutIntegracioEntityCaptor;

    @Captor
    private ArgumentCaptor<SalutSubsistemaEntity> salutSubsistemaEntityCaptor;

    @Captor
    private ArgumentCaptor<SalutMissatgeEntity> salutMissatgeEntityCaptor;

    @Captor
    private ArgumentCaptor<SalutDetallEntity> salutDetallEntityCaptor;

    private EntornApp entornApp;
    private SalutInfo salutInfo;
    private SalutEntity salutEntity;

    @BeforeEach
    void setUp() {
        // Setup EntornApp
        entornApp = EntornApp.builder()
                .id(1L)
                .app(new AppRef(1L, "Test App"))
                .entorn(new EntornRef(1L, "Test Entorn"))
                .salutUrl("http://test.com/health")
                .activa(true)
                .build();

        // Setup SalutInfo
        EstatSalut appEstat = EstatSalut.builder()
                .estat(EstatSalutEnum.UP)
                .latencia(100)
                .build();

        EstatSalut bdEstat = EstatSalut.builder()
                .estat(EstatSalutEnum.UP)
                .latencia(50)
                .build();

        IntegracioPeticions peticions = IntegracioPeticions.builder()
                .totalOk(10L)
                .totalError(2L)
                .build();

        IntegracioSalut integracioSalut = IntegracioSalut.builder()
                .codi("TEST_INTEGRACIO")
                .estat(EstatSalutEnum.UP)
                .latencia(75)
                .peticions(peticions)
                .build();

        SubsistemaSalut subsistemaSalut = SubsistemaSalut.builder()
                .codi("TEST_SUBSISTEMA")
                .estat(EstatSalutEnum.UP)
                .latencia(60)
                .totalOk(20L)
                .totalError(5L)
                .build();

        MissatgeSalut missatgeSalut = MissatgeSalut.builder()
                .data(new Date())
                .nivell("info")
                .missatge("Test message")
                .build();

        DetallSalut detallSalut = DetallSalut.builder()
                .codi("TEST_DETALL")
                .nom("Test Detall")
                .valor("Test Value")
                .build();

        salutInfo = SalutInfo.builder()
                .codi("TEST")
                .data(new Date())
                .estat(appEstat)
                .bd(bdEstat)
                .integracions(Arrays.asList(integracioSalut))
                .subsistemes(Arrays.asList(subsistemaSalut))
                .missatges(Arrays.asList(missatgeSalut))
                .altres(Arrays.asList(detallSalut))
                .versio("1.0.0")
                .build();

        // Setup SalutEntity
        salutEntity = new SalutEntity();
        salutEntity.setId(1L);
        salutEntity.setEntornAppId(1L);
        salutEntity.setData(LocalDateTime.now());
        salutEntity.setAppEstat(SalutEstat.UP);
        salutEntity.setAppLatencia(100);
        salutEntity.setBdEstat(SalutEstat.UP);
        salutEntity.setBdLatencia(50);

    }

    @Test
    void testGetSalutInfo_Success() {
        // Arrange
        when(salutRepository.save(any(SalutEntity.class))).thenReturn(salutEntity);
        // Mock RestTemplate
        when(restTemplate.getForObject(anyString(), eq(SalutInfo.class))).thenReturn(salutInfo);

        // Act
        salutInfoHelper.getSalutInfo(entornApp);

        // Assert
        verify(salutRepository).save(salutEntityCaptor.capture());
        SalutEntity capturedSalutEntity = salutEntityCaptor.getValue();

        assertEquals(1L, capturedSalutEntity.getEntornAppId().longValue());
        assertEquals(SalutEstat.UP, capturedSalutEntity.getAppEstat());
        assertEquals(Integer.valueOf(100), capturedSalutEntity.getAppLatencia());
        assertEquals(SalutEstat.UP, capturedSalutEntity.getBdEstat());
        assertEquals(Integer.valueOf(50), capturedSalutEntity.getBdLatencia());

        verify(salutIntegracioRepository).save(any(SalutIntegracioEntity.class));
        verify(salutSubsistemaRepository).save(any(SalutSubsistemaEntity.class));
        verify(salutMissatgeRepository).save(any(SalutMissatgeEntity.class));
        verify(salutDetallRepository).save(any(SalutDetallEntity.class));
    }

    @Test
    void testGetSalutInfo_Exception() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(SalutInfo.class)))
                .thenThrow(new RestClientException("Test exception"));
        when(salutRepository.save(any(SalutEntity.class))).thenReturn(salutEntity);

        // Act
        salutInfoHelper.getSalutInfo(entornApp);

        // Assert
        verify(salutRepository).save(salutEntityCaptor.capture());
        SalutEntity capturedSalutEntity = salutEntityCaptor.getValue();

        assertEquals(1L, capturedSalutEntity.getEntornAppId().longValue());
        assertEquals(SalutEstat.UNKNOWN, capturedSalutEntity.getAppEstat());

        verify(salutIntegracioRepository, never()).save(any(SalutIntegracioEntity.class));
        verify(salutSubsistemaRepository, never()).save(any(SalutSubsistemaEntity.class));
        verify(salutMissatgeRepository, never()).save(any(SalutMissatgeEntity.class));
        verify(salutDetallRepository, never()).save(any(SalutDetallEntity.class));
    }


}
