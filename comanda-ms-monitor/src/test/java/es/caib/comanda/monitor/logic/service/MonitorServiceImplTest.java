package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.monitor.logic.intf.model.Monitor;
import es.caib.comanda.monitor.persist.entity.MonitorEntity;
import es.caib.comanda.monitor.persist.repository.MonitorRepository;
import es.caib.comanda.ms.logic.helper.ObjectMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceReferenceToEntityHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MonitorServiceImplTest {

    @Spy
    private ResourceEntityMappingHelper resourceEntityMappingHelper = new ResourceEntityMappingHelper(new ObjectMappingHelper());
    @Spy
    private ResourceReferenceToEntityHelper resourceReferenceToEntityHelper = new ResourceReferenceToEntityHelper();
    @Mock
    private MonitorRepository entityRepository;

    @InjectMocks
    private MonitorServiceImpl monitorService;

    private MonitorEntity monitorEntity;
    private Monitor monitorResource;

    @BeforeEach
    void setUp() {
        // Setup test data
        monitorEntity = new MonitorEntity();
        monitorEntity.setId(1L);
        monitorEntity.setEntornAppId(1L);
        monitorEntity.setModul(ModulEnum.SALUT);
        monitorEntity.setTipus(AccioTipusEnum.ENTRADA);
        monitorEntity.setData(LocalDateTime.now());
        monitorEntity.setUrl("http://test.com/api");
        monitorEntity.setOperacio("Test Operation");
        monitorEntity.setTempsResposta(100L);
        monitorEntity.setEstat(EstatEnum.OK);
        monitorEntity.setCodiUsuari("testuser");

        monitorResource = new Monitor();
        monitorResource.setId(1L);
        monitorResource.setEntornAppId(1L);
        monitorResource.setModul(ModulEnum.SALUT);
        monitorResource.setTipus(AccioTipusEnum.ENTRADA);
        monitorResource.setData(LocalDateTime.now());
        monitorResource.setUrl("http://test.com/api");
        monitorResource.setOperacio("Test Operation");
        monitorResource.setTempsResposta(100L);
        monitorResource.setEstat(EstatEnum.OK);
        monitorResource.setCodiUsuari("testuser");
    }

    @Test
    void testGetOne() {
        // Arrange
        when(entityRepository.findOne(any(Specification.class))).thenReturn(Optional.of(monitorEntity));

        // Act
        Monitor result = monitorService.getOne(1L, null);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals(ModulEnum.SALUT, result.getModul());
        assertEquals(AccioTipusEnum.ENTRADA, result.getTipus());
        assertEquals("Test Operation", result.getOperacio());
        assertEquals(EstatEnum.OK, result.getEstat());

        // Verify repository was called
        verify(entityRepository).findOne(any(Specification.class));
    }

    @Test
    void testCreate() {
        // Arrange
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        when(entityRepository.saveAndFlush(any(MonitorEntity.class))).thenReturn(monitorEntity);

        // Act
        Monitor result = monitorService.create(monitorResource, answers);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals(ModulEnum.SALUT, result.getModul());
        assertEquals(AccioTipusEnum.ENTRADA, result.getTipus());
        assertEquals("Test Operation", result.getOperacio());
        assertEquals(EstatEnum.OK, result.getEstat());

        // Verify repository was called
        verify(entityRepository).saveAndFlush(any(MonitorEntity.class));
    }

    @Test
    void testUpdate() {
        // Arrange
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        when(entityRepository.findOne(any(Specification.class))).thenReturn(Optional.of(monitorEntity));
        when(entityRepository.saveAndFlush(any(MonitorEntity.class))).thenReturn(monitorEntity);

        // Act
        Monitor result = monitorService.update(1L, monitorResource, answers);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals(ModulEnum.SALUT, result.getModul());
        assertEquals(AccioTipusEnum.ENTRADA, result.getTipus());
        assertEquals("Test Operation", result.getOperacio());
        assertEquals(EstatEnum.OK, result.getEstat());

        // Verify repository was called
        verify(entityRepository).findOne(any(Specification.class));
        verify(entityRepository).saveAndFlush(any(MonitorEntity.class));
    }

    @Test
    void testDelete() {
        // Arrange
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        when(entityRepository.findOne(any(Specification.class))).thenReturn(Optional.of(monitorEntity));

        // Act
        monitorService.delete(1L, answers);

        // Verify repository was called
        verify(entityRepository).findOne(any(Specification.class));
        verify(entityRepository).delete(monitorEntity);
    }

    @Test
    void testFindPage() {
        // Arrange
        List<MonitorEntity> entityPage = Arrays.asList(monitorEntity);
        when(entityRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(entityPage);

        // Act
        Page<Monitor> result = monitorService.findPage(null, null, null, null, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(ModulEnum.SALUT, result.getContent().get(0).getModul());
        assertEquals(AccioTipusEnum.ENTRADA, result.getContent().get(0).getTipus());
        assertEquals("Test Operation", result.getContent().get(0).getOperacio());
        assertEquals(EstatEnum.OK, result.getContent().get(0).getEstat());

        // Verify repository was called
        verify(entityRepository).findAll(any(Specification.class), any(Sort.class));
    }
}