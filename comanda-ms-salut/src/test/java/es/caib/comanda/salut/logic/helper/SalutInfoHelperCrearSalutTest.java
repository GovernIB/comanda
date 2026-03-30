package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.model.v1.salut.EstatSalut;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.model.v1.salut.InformacioSistema;
import es.caib.comanda.model.v1.salut.IntegracioPeticions;
import es.caib.comanda.model.v1.salut.IntegracioSalut;
import es.caib.comanda.model.v1.salut.MissatgeSalut;
import es.caib.comanda.model.v1.salut.SalutInfo;
import es.caib.comanda.model.v1.salut.SalutNivell;
import es.caib.comanda.model.v1.salut.SubsistemaSalut;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
    void setUp() throws Exception {
        crearSalutMethod = SalutInfoHelper.class.getDeclaredMethod("crearSalut", SalutInfo.class, Long.class, LocalDateTime.class);
        crearSalutMethod.setAccessible(true);
    }

    @Test
    void crearSalut_quanSalutInfoEsNull_retornaNullISensePersistirFills() throws Exception {
        Object res = crearSalutMethod.invoke(helper, new Object[]{null, 10L, LocalDateTime.now()});
        assertThat(res).isNull();
        verifyNoInteractions(salutIntegracioRepository, salutSubsistemaRepository, salutMissatgeRepository, salutDetallRepository);
    }

    @Test
    void crearSalut_quanHiHaDadesValides_mapejaElsCampsIFillsAmbDefaults() throws Exception {
        Long entornAppId = 7L;
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        EstatSalut appEstat = EstatSalut.builder().estat(null).latencia(123).build();
        EstatSalut bdEstat = EstatSalut.builder().estat(EstatSalutEnum.WARN).latencia(45).build();

        IntegracioSalut integ = IntegracioSalut.builder()
                .codi("INT1").estat(EstatSalutEnum.DOWN).latencia(10)
                .peticions(IntegracioPeticions.builder()
                        .totalOk(10L).totalError(2L).totalTempsMig(125)
                        .peticionsOkUltimPeriode(2L).peticionsErrorUltimPeriode(0L).tempsMigUltimPeriode(130).build())
                .build();
        SubsistemaSalut subs = SubsistemaSalut.builder()
                .codi("SUB1").estat(EstatSalutEnum.UP).latencia(5)
                .totalOk(20L).totalError(1L).totalTempsMig(236)
                .peticionsOkUltimPeriode(4L).peticionsErrorUltimPeriode(0L).tempsMigUltimPeriode(228)
                .build();
        MissatgeSalut msg = MissatgeSalut.builder()
                .data(OffsetDateTime.now()).nivell(SalutNivell.WARN).missatge("m1").build();
        InformacioSistema sys = InformacioSistema.builder()
                .memoriaTotal("V")
                .build();

        SalutInfo info = SalutInfo.builder()
                .codi("C")
                .data(OffsetDateTime.now())
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
        assertThat(id).isNotNull();

        verify(salutRepository, atLeastOnce()).save(salutEntityCaptor.capture());
        SalutEntity saved = salutEntityCaptor.getValue();
        assertThat(saved.getEntornAppId()).isEqualTo(entornAppId);
        assertThat(saved.getData()).isEqualTo(now);
        assertThat(saved.getAppEstat()).isEqualTo(SalutEstat.UNKNOWN);
        assertThat(saved.getAppLatencia()).isEqualTo(123);
        assertThat(saved.getBdEstat()).isEqualTo(SalutEstat.WARN);
        assertThat(saved.getBdLatencia()).isEqualTo(45);

        verify(salutIntegracioRepository).save(integracioCaptor.capture());
        SalutIntegracioEntity iSaved = integracioCaptor.getValue();
        assertThat(iSaved.getCodi()).isEqualTo("INT1");
        assertThat(iSaved.getEstat()).isEqualTo(SalutEstat.DOWN);
        assertThat(iSaved.getLatencia()).isEqualTo(10);
        assertThat(iSaved.getTotalOk()).isEqualTo(10L);
        assertThat(iSaved.getTotalError()).isEqualTo(2L);

        verify(salutSubsistemaRepository).save(subsistemaCaptor.capture());
        SalutSubsistemaEntity sSaved = subsistemaCaptor.getValue();
        assertThat(sSaved.getCodi()).isEqualTo("SUB1");
        assertThat(sSaved.getEstat()).isEqualTo(SalutEstat.UP);
        assertThat(sSaved.getLatencia()).isEqualTo(5);
        assertThat(sSaved.getTotalOk()).isEqualTo(20L);
        assertThat(sSaved.getTotalError()).isEqualTo(1L);

        verify(salutMissatgeRepository).save(missatgeCaptor.capture());
        SalutMissatgeEntity mSaved = missatgeCaptor.getValue();
        assertThat(mSaved.getMissatge()).isEqualTo("m1");
        assertThat(mSaved.getNivell()).isEqualTo(SalutNivell.WARN);
        assertThat(mSaved.getData()).isNotNull();

        verify(salutDetallRepository).save(detallCaptor.capture());
        SalutDetallEntity dSaved = detallCaptor.getValue();
        assertThat(dSaved.getCodi()).isEqualTo("MET");
        assertThat(dSaved.getValor()).isEqualTo("V");
    }

    @Test
    void creaAgregatSalutIAfegirSalut_quanRebenValorsNull_retornenNull() {
        assertThat(helper.creaAgregatSalut(1L, es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut.DIA, null)).isNull();
        SalutEntity agg = new SalutEntity();
        assertThat(helper.afegirSalut(agg, null)).isNull();
    }
}
