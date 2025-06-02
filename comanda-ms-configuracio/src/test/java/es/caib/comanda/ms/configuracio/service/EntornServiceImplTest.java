package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.configuracio.logic.service.EntornServiceImpl;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class EntornServiceImplTest {

    @Mock
    private AppRepository appRepository;

    @Mock
    private IntegracioRepository integracioRepository;

    @Mock
    private SubsistemaRepository subsistemaRepository;

    @InjectMocks
    private EntornServiceImpl entornService;

    private EntornEntity entornEntity;
    private Entorn entornResource;

    @BeforeEach
    void setUp() {
        // Setup test data
        entornEntity = new EntornEntity();
        entornEntity.setId(1L);
        entornEntity.setNom("Test Entorn");
        
        entornResource = new Entorn();
        entornResource.setId(1L);
        entornResource.setNom("Test Entorn");
    }

    @Test
    void testEntornServiceExists() {
        // This is a simple test to verify that the service can be instantiated
        // Since EntornServiceImpl doesn't override any methods from BaseMutableResourceService,
        // we don't need to test any specific functionality
        assertNotNull(entornService);
    }
}