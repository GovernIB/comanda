package es.caib.comanda.ms.salut.service;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.JasperReportsHelper;
import es.caib.comanda.ms.logic.helper.ObjectMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import es.caib.comanda.salut.logic.helper.MetricsHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.intf.model.*;
import es.caib.comanda.salut.logic.service.SalutServiceImpl;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static es.caib.comanda.salut.logic.helper.SalutInfoHelper.MINUTS_PER_AGRUPACIO;
import static es.caib.comanda.salut.logic.intf.model.Salut.SALUT_REPORT_ESTATS;
import static es.caib.comanda.salut.logic.intf.model.Salut.SALUT_REPORT_GRUPS_DATES;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SalutServiceImplTest {

    // Test subclass to expose protected methods if needed
    static class TestableSalutServiceImpl extends SalutServiceImpl {
        
        public TestableSalutServiceImpl(SalutRepository entityRepository,
                                      ResourceEntityMappingHelper resourceEntityMappingHelper,
                                      ObjectMappingHelper objectMappingHelper,
                                      JasperReportsHelper jasperReportsHelper,
                                      SalutIntegracioRepository salutIntegracioRepository,
                                      SalutSubsistemaRepository salutSubsistemaRepository,
                                      SalutMissatgeRepository salutMissatgeRepository,
                                      SalutDetallRepository salutDetallRepository,
                                      SalutClientHelper salutClientHelper,
                                      MetricsHelper metricsHelper) {
            super(
					salutIntegracioRepository,
		            salutSubsistemaRepository,
		            salutMissatgeRepository,
		            salutDetallRepository,
		            salutClientHelper,
		            metricsHelper);
            
            // Set the parent class fields using reflection
            try {
                java.lang.reflect.Field entityRepositoryField = BaseReadonlyResourceService.class.getDeclaredField("entityRepository");
                entityRepositoryField.setAccessible(true);
                entityRepositoryField.set(this, entityRepository);
                
                java.lang.reflect.Field resourceEntityMappingHelperField = BaseReadonlyResourceService.class.getDeclaredField("resourceEntityMappingHelper");
                resourceEntityMappingHelperField.setAccessible(true);
                resourceEntityMappingHelperField.set(this, resourceEntityMappingHelper);
                
                java.lang.reflect.Field objectMappingHelperField = BaseReadonlyResourceService.class.getDeclaredField("objectMappingHelper");
                objectMappingHelperField.setAccessible(true);
                objectMappingHelperField.set(this, objectMappingHelper);
                
                java.lang.reflect.Field jasperReportsHelperField = BaseReadonlyResourceService.class.getDeclaredField("jasperReportsHelper");
                jasperReportsHelperField.setAccessible(true);
                jasperReportsHelperField.set(this, jasperReportsHelper);
            } catch (Exception e) {
                throw new RuntimeException("Error setting parent class fields", e);
            }
        }
        
        public SalutRepository getRepository() {
            return (SalutRepository) this.entityRepository;
        }

        public Salut convertToResource(SalutEntity entity) {
            return super.entityToResource(entity);
        }

    }

    @Mock
    private SalutRepository entityRepository;
    
    @Spy
    private ResourceEntityMappingHelper resourceEntityMappingHelper = new ResourceEntityMappingHelper(new ObjectMappingHelper());
    
    @Mock
    private ObjectMappingHelper objectMappingHelper;
    
    @Mock
    private JasperReportsHelper jasperReportsHelper;

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
	private MetricsHelper metricsHelper;

    @Mock
    private EntornAppServiceClient entornAppServiceClient;

    private TestableSalutServiceImpl salutService;

    private SalutEntity salutEntity;
    private Salut salutResource;
    private EntornApp entornApp;

    @BeforeEach
    void setUp() {
        // Initialize the service with mocked dependencies
        salutService = new TestableSalutServiceImpl(
            entityRepository,
            resourceEntityMappingHelper,
            objectMappingHelper,
            jasperReportsHelper,
            salutIntegracioRepository,
            salutSubsistemaRepository,
            salutMissatgeRepository,
            salutDetallRepository,
            salutClientHelper,
            metricsHelper
        );
        
        // Setup test data
        salutEntity = new SalutEntity();
        salutEntity.setId(1L);
        salutEntity.setEntornAppId(1L);
        salutEntity.setData(LocalDateTime.now());
        salutEntity.setAppEstat(SalutEstat.UP);
        salutEntity.setAppLatencia(100);
        salutEntity.setBdEstat(SalutEstat.UP);
        salutEntity.setBdLatencia(50);

        salutResource = new Salut();
        salutResource.setId(1L);
        salutResource.setEntornAppId(1L);
        salutResource.setVersio("1.0.0"); // Added versio field which is required
        salutResource.setAppEstat(SalutEstat.UP);
        salutResource.setAppLatencia(100);
        salutResource.setBdEstat(SalutEstat.UP);
        salutResource.setBdLatencia(50);

        // Use builder pattern for EntornApp
        entornApp = EntornApp.builder()
                .id(1L)
                .build();
    }

    @Test
    void testGetRepository() {
        // Act
        SalutRepository repository = salutService.getRepository();

        // Assert
        assertSame(entityRepository, repository);
    }

    @Test
    void testConvertToResource() {
        // Act
        Salut result = salutService.convertToResource(salutEntity);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals(SalutEstat.UP, result.getAppEstat());
        assertEquals(Integer.valueOf(100), result.getAppLatencia());
        assertEquals(SalutEstat.UP, result.getBdEstat());
        assertEquals(Integer.valueOf(50), result.getBdLatencia());
    }

    private void assertIncrement(List<SalutInformeGrupItem> result, java.time.temporal.TemporalAmount step) {
        assertFalse(result.isEmpty(), "Generated list should not be empty");
        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime current = result.get(i).getData();
            LocalDateTime next = result.get(i + 1).getData();
            assertEquals(current.plus(step), next, "Unexpected increment at index " + i);
        }
    }

    @Test
    void testGrupsDates_Minut() {
        SalutInformeGrupsParams params = new SalutInformeGrupsParams();
        params.setAgrupacio(SalutInformeAgrupacio.MINUT);
        params.setDataReferencia(LocalDateTime.of(2023, 1, 1, 10, 17));

        List<SalutInformeGrupItem> result = (salutService.new InformeGrupsDates()).generateData(SALUT_REPORT_GRUPS_DATES, salutEntity, params);

        assertEquals(
                params.getDataReferencia().minusMinutes(15),
                result.get(0).getData(),
                "First item should be 15 minutes before the reference date"
        );
        assertIncrement(result, java.time.Duration.ofMinutes(1));
    }

    @Test
    void testGrupsDates_MinutsHora() {
        SalutInformeGrupsParams params = new SalutInformeGrupsParams();
        params.setAgrupacio(SalutInformeAgrupacio.MINUTS_HORA);
        params.setDataReferencia(LocalDateTime.of(2023, 1, 1, 10, MINUTS_PER_AGRUPACIO*2-1));

        List<SalutInformeGrupItem> result = (salutService.new InformeGrupsDates()).generateData(SALUT_REPORT_GRUPS_DATES, salutEntity, params);

        assertEquals(
                LocalDateTime.of(2023, 1, 1, 10, MINUTS_PER_AGRUPACIO),
                result.get(result.size() - 1).getData(),
                String.format("Minute value not divisible by %d", MINUTS_PER_AGRUPACIO));
        assertEquals(
                LocalDateTime.of(2023, 1, 1, 10, MINUTS_PER_AGRUPACIO).minusHours(1),
                result.get(0).getData(),
                "First item should be 1 hour before the reference date"
        );
        assertIncrement(result, java.time.Duration.ofMinutes(SalutInfoHelper.MINUTS_PER_AGRUPACIO));
    }

    @Test
    void testGrupsDates_Hora() {
        SalutInformeGrupsParams params = new SalutInformeGrupsParams();
        params.setAgrupacio(SalutInformeAgrupacio.HORA);
        params.setDataReferencia(LocalDateTime.of(2023, 1, 1, 10, 0));

        List<SalutInformeGrupItem> result = (salutService.new InformeGrupsDates()).generateData(SALUT_REPORT_GRUPS_DATES, salutEntity, params);

        assertEquals(
                params.getDataReferencia().minusDays(1),
                result.get(0).getData(),
                "First item should be 1 day before the reference date"
        );
        assertIncrement(result, java.time.Duration.ofHours(1));
    }

    @Test
    void testGrupsDates_DiaSetmana() {
        SalutInformeGrupsParams params = new SalutInformeGrupsParams();
        params.setAgrupacio(SalutInformeAgrupacio.DIA_SETMANA);
        params.setDataReferencia(LocalDateTime.of(2023, 1, 1, 10, 0));

        List<SalutInformeGrupItem> result = (salutService.new InformeGrupsDates()).generateData(SALUT_REPORT_GRUPS_DATES, salutEntity, params);

        assertEquals(
                params.getDataReferencia().withHour(0).minusDays(7),
                result.get(0).getData(),
                "First item should be 7 days before the reference date"
        );
        assertIncrement(result, java.time.Period.ofDays(1));
    }

    @Test
    void testGrupsDates_DiaMes() {
        SalutInformeGrupsParams params = new SalutInformeGrupsParams();
        params.setAgrupacio(SalutInformeAgrupacio.DIA_MES);
        params.setDataReferencia(LocalDateTime.of(2023, 1, 17, 10, 0));

        List<SalutInformeGrupItem> result = (salutService.new InformeGrupsDates()).generateData(SALUT_REPORT_GRUPS_DATES, salutEntity, params);

        assertEquals(
                params.getDataReferencia().withHour(0).minusDays(30),
                result.get(0).getData(),
                "First item should be 30 days before the reference date"
        );
        assertIncrement(result, java.time.Period.ofDays(1));
    }

    @Test
    void testEstatRequest() {
        when(entityRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(any(), any(LocalDateTime.class), any()))
                .thenReturn(List.of(salutEntity));

        long entornAppId = 1L;

        SalutInformeParams params =  new SalutInformeParams(
                LocalDateTime.of(2023, 1, 17, 10, 0),
                LocalDateTime.of(2023, 1, 17, 11, 0),
                entornAppId,
                SalutInformeAgrupacio.MINUTS_HORA
        );
        salutService.new InformeEstat().generateData(SALUT_REPORT_ESTATS, salutEntity, params);

        verify(entityRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                eq(entornAppId),
                eq(params.getDataInici()),
                eq(TipusRegistreSalut.MINUTS)
        );

    }

    @Test
    void testEstatsRequest() {
        when(entityRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(any(), any(LocalDateTime.class), any()))
                .thenReturn(List.of(salutEntity));

        List<Long> entornAppList = List.of(1L, 2L, 3L);
        SalutInformeLlistatParams params = new SalutInformeLlistatParams(
                entornAppList,
                LocalDateTime.of(2023, 1, 17, 10, 0),
                LocalDateTime.of(2023, 1, 17, 11, 0),
                SalutInformeAgrupacio.MINUTS_HORA
        );
        salutService.new InformeEstats().generateData(SALUT_REPORT_ESTATS, salutEntity, params);

        for (Long entornAppId : entornAppList) {
            verify(entityRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                    eq(entornAppId),
                    eq(params.getDataInici()),
                    eq(TipusRegistreSalut.MINUTS)
            );
        }
    }

    @Test
    void testLatenciaRequest() {
        when(entityRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(any(), any(LocalDateTime.class), any()))
                .thenReturn(List.of(salutEntity));

        long entornAppId = 1L;

        SalutInformeParams params =  new SalutInformeParams(
                LocalDateTime.of(2023, 1, 17, 10, 0),
                LocalDateTime.of(2023, 1, 17, 11, 0),
                entornAppId,
                SalutInformeAgrupacio.MINUTS_HORA
        );
        salutService.new InformeLatencia().generateData(SALUT_REPORT_ESTATS, salutEntity, params);

        verify(entityRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                eq(entornAppId),
                eq(params.getDataInici()),
                eq(TipusRegistreSalut.MINUTS)
        );
    }
}
