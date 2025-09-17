package es.caib.comanda.ms.salut.service;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.JasperReportsHelper;
import es.caib.comanda.ms.logic.helper.ObjectMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import es.caib.comanda.salut.logic.helper.MetricsHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.intf.model.Salut;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
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

import static org.junit.jupiter.api.Assertions.*;

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

}
