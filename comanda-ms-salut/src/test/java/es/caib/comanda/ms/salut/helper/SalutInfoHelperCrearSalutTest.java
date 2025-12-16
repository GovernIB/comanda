package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.model.v1.salut.EstatSalut;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.model.v1.salut.IntegracioPeticions;
import es.caib.comanda.model.v1.salut.IntegracioSalut;
import es.caib.comanda.model.v1.salut.MissatgeSalut;
import es.caib.comanda.model.v1.salut.InformacioSistema;
import es.caib.comanda.model.v1.salut.SalutInfo;
import es.caib.comanda.model.v1.salut.SalutNivell;
import es.caib.comanda.model.v1.salut.SubsistemaSalut;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalutInfoHelperCrearSalutTest {

    @Mock private SalutRepository salutRepository;
    @Mock private SalutIntegracioRepository salutIntegracioRepository;
    @Mock private SalutSubsistemaRepository salutSubsistemaRepository;
    @Mock private SalutMissatgeRepository salutMissatgeRepository;
    @Mock private SalutDetallRepository salutDetallRepository;
    @Mock private es.caib.comanda.salut.logic.helper.SalutClientHelper salutClientHelper;
    @Mock private RestTemplate restTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private SalutInfoHelper helper;

    @Captor private ArgumentCaptor<SalutEntity> salutEntityCaptor;
    @Captor private ArgumentCaptor<SalutIntegracioEntity> integracioCaptor;
    @Captor private ArgumentCaptor<SalutSubsistemaEntity> subsistemaCaptor;
    @Captor private ArgumentCaptor<SalutMissatgeEntity> missatgeCaptor;
    @Captor private ArgumentCaptor<SalutDetallEntity> detallCaptor;

    private Method crearSalutMethod;

    @BeforeEach
    void setup() throws Exception {
        crearSalutMethod = SalutInfoHelper.class.getDeclaredMethod("crearSalut", SalutInfo.class, Long.class, LocalDateTime.class);
        crearSalutMethod.setAccessible(true);
    }

    @Test
    void crearSalut_returns_null_when_info_null() throws Exception {
        Object res = crearSalutMethod.invoke(helper, new Object[]{null, 10L, LocalDateTime.now()});
        assertNull(res);
        verifyNoInteractions(salutIntegracioRepository, salutSubsistemaRepository, salutMissatgeRepository, salutDetallRepository);
    }

    @Test
    void crearSalut_maps_fields_and_children_with_defaults() throws Exception {
        Long entornAppId = 7L;
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        // App estat amb null -> UNKNOWN per codi
        EstatSalut appEstat = EstatSalut.builder().estat(null).latencia(123).build();
        EstatSalut bdEstat = EstatSalut.builder().estat(EstatSalutEnum.WARN).latencia(45).build();

        IntegracioSalut integ = IntegracioSalut.builder()
                .codi("INT1").estat(EstatSalutEnum.DOWN).latencia(10)
                .peticions(IntegracioPeticions.builder()
                        .totalOk(10L).totalError(2L).totalTempsMig(125)
                        .peticionsOkUltimPeriode(2L).peticionsErrorUltimPeriode(0L).tempsMigUltimPeriode(130).build()) // totals per defecte 0
                .build();
        SubsistemaSalut subs = SubsistemaSalut.builder()
                .codi("SUB1").estat(EstatSalutEnum.UP).latencia(5)
                .totalOk(20L).totalError(1L).totalTempsMig(236)
                .peticionsOkUltimPeriode(4L).peticionsErrorUltimPeriode(0L).tempsMigUltimPeriode(228)
                .build();
        MissatgeSalut msg = MissatgeSalut.builder()
                .data(new Date()).nivell(SalutNivell.WARN).missatge("m1").build();
        // Nou format: objecte InformacioSistema (mantindrem persistència derivant DetallSalut)
        InformacioSistema sys = InformacioSistema.builder()
                .memoriaTotal("V")
                .build();

        SalutInfo info = SalutInfo.builder()
                .codi("C")
                .data(new Date())
                .estatGlobal(appEstat)
                .estatBaseDeDades(bdEstat)
                .integracions(Collections.singletonList(integ))
                .subsistemes(Collections.singletonList(subs))
                .missatges(Collections.singletonList(msg))
                .informacioSistema(sys)
                .build();

        when(salutRepository.save(any(SalutEntity.class))).thenAnswer(inv -> {
            SalutEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(1L);
            return e;
        });
        when(salutIntegracioRepository.save(any(SalutIntegracioEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(salutSubsistemaRepository.save(any(SalutSubsistemaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(salutMissatgeRepository.save(any(SalutMissatgeEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(salutDetallRepository.save(any(SalutDetallEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Long id = (Long) crearSalutMethod.invoke(helper, info, entornAppId, now);
        assertNotNull(id);

        verify(salutRepository, atLeastOnce()).save(salutEntityCaptor.capture());
        SalutEntity saved = salutEntityCaptor.getValue();
        assertEquals(entornAppId, saved.getEntornAppId());
        assertEquals(now, saved.getData());
        assertEquals(SalutEstat.UNKNOWN, saved.getAppEstat());
        assertEquals(Integer.valueOf(123), saved.getAppLatencia());
        assertEquals(SalutEstat.WARN, saved.getBdEstat());
        assertEquals(Integer.valueOf(45), saved.getBdLatencia());

        verify(salutIntegracioRepository).save(integracioCaptor.capture());
        SalutIntegracioEntity iSaved = integracioCaptor.getValue();
        assertEquals("INT1", iSaved.getCodi());
        assertEquals(SalutEstat.DOWN, iSaved.getEstat());
        assertEquals(Integer.valueOf(10), iSaved.getLatencia());
        assertEquals(Long.valueOf(10), iSaved.getTotalOk());
        assertEquals(Long.valueOf(2), iSaved.getTotalError());

        verify(salutSubsistemaRepository).save(subsistemaCaptor.capture());
        SalutSubsistemaEntity sSaved = subsistemaCaptor.getValue();
        assertEquals("SUB1", sSaved.getCodi());
        assertEquals(SalutEstat.UP, sSaved.getEstat());
        assertEquals(Integer.valueOf(5), sSaved.getLatencia());
        assertEquals(Long.valueOf(20), sSaved.getTotalOk());
        assertEquals(Long.valueOf(1), sSaved.getTotalError());

        verify(salutMissatgeRepository).save(missatgeCaptor.capture());
        SalutMissatgeEntity mSaved = missatgeCaptor.getValue();
        assertEquals("m1", mSaved.getMissatge());
        assertEquals(SalutNivell.WARN, mSaved.getNivell());
        assertNotNull(mSaved.getData());

        verify(salutDetallRepository).save(detallCaptor.capture());
        SalutDetallEntity dSaved = detallCaptor.getValue();
        // S'espera que el codi derivi del camp establert a InformacioSistema (memoriaTotal -> MET)
        assertEquals("MET", dSaved.getCodi());
        assertEquals("V", dSaved.getValor());
    }

    @Test
    void creaAgregatSalut_and_afegirSalut_handle_null_input() {
        assertNull(helper.creaAgregatSalut(1L, es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut.DIA, null));
        SalutEntity agg = new SalutEntity();
        assertNull(helper.afegirSalut(agg, null));
    }
}
