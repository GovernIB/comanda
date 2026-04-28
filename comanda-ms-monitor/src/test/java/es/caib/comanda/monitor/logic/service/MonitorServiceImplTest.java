package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.monitor.logic.helper.MonitorClientHelper;
import es.caib.comanda.monitor.logic.intf.model.Monitor;
import es.caib.comanda.monitor.persist.entity.MonitorEntity;
import es.caib.comanda.monitor.persist.repository.MonitorRepository;
import es.caib.comanda.ms.logic.helper.ObjectMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceReferenceToEntityHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock
    private MonitorClientHelper monitorClientHelper;

    @InjectMocks
    private MonitorServiceImpl monitorService;

    private MonitorEntity monitorEntity;
    private Monitor monitorResource;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(monitorService, "resourceEntityMappingHelper", resourceEntityMappingHelper);
        ReflectionTestUtils.setField(monitorService, "resourceReferenceToEntityHelper", resourceReferenceToEntityHelper);
        ReflectionTestUtils.setField(monitorService, "entityRepository", entityRepository);
        ReflectionTestUtils.setField(monitorService, "monitorClientHelper", monitorClientHelper);

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

    @ParameterizedTest
    @MethodSource("methodSourceNamedFilter")
    @DisplayName("namedFilterToSpringFilter: matriu d'escenaris")
    void testNamedFilterToSpringFilter(String descripcion,
                                      String namedFilter,
                                      List<Long> idsRetornats,
                                      String springFilterEsperat) {
        // Arrange
        if (namedFilter != null && namedFilter.startsWith(Monitor.FILTER_BY_APP_NAMEDFILTER)) {
            long appId = Long.parseLong(namedFilter.split(":")[1]);
            when(monitorClientHelper.findEntornAppIdsByAppId(appId)).thenReturn(idsRetornats);
        } else if (namedFilter != null && namedFilter.startsWith(Monitor.FILTER_BY_ENTORN_NAMEDFILTER)) {
            long entornId = Long.parseLong(namedFilter.split(":")[1]);
            when(monitorClientHelper.findEntornAppIdsByEntornId(entornId)).thenReturn(idsRetornats);
        }

        // Act
        String result = monitorService.namedFilterToSpringFilter(namedFilter);

        // Assert
        assertThat(result).as(descripcion).isEqualTo(springFilterEsperat);
    }

    private static Stream<Arguments> methodSourceNamedFilter() {
        return Stream.of(
                Arguments.of(
                    "FILTER_BY_APP amb IDs → retorna cláusula IN",
                    Monitor.FILTER_BY_APP_NAMEDFILTER + "1",
                    Arrays.asList(10L, 11L, 12L),
                    Monitor.Fields.entornAppId + " in(10,11,12)"
                ),
                Arguments.of(
                    "FILTER_BY_APP amb un sol ID → retorna IN amb un element",
                    Monitor.FILTER_BY_APP_NAMEDFILTER + "2",
                    Arrays.asList(99L),
                    Monitor.Fields.entornAppId + " in(99)"
                ),
                Arguments.of(
                    "FILTER_BY_APP amb llista buida → retorna filtre que no coincideix",
                    Monitor.FILTER_BY_APP_NAMEDFILTER + "3",
                    Arrays.asList(),
                    Monitor.Fields.entornAppId + " : '0'"
                ),
                Arguments.of(
                    "FILTER_BY_ENTORN amb IDs → retorna cláusula IN",
                    Monitor.FILTER_BY_ENTORN_NAMEDFILTER + "4",
                    Arrays.asList(20L, 21L, 22L),
                    Monitor.Fields.entornAppId + " in(20,21,22)"
                ),
                Arguments.of(
                    "FILTER_BY_ENTORN amb llista buida → retorna filtre que no coincideix",
                    Monitor.FILTER_BY_ENTORN_NAMEDFILTER + "5",
                    Arrays.asList(),
                    Monitor.Fields.entornAppId + " : '0'"
                ),
                Arguments.of(
                    "Nom de filtre null → retorna null",
                    null,
                    null,
                    null
                ),
                Arguments.of(
                    "Nom de filtre desconegut → retorna null",
                    "FILTER_DESCONEGUT",
                    null,
                    null
                )
        );
    }

    @Test
    @DisplayName("namedFilterToSpringFilter: sense appId llança ArrayIndexOutOfBoundsException")
    void testNamedFilterToSpringFilter_ArrayIndexOutOfBoundsException() {
        // Arrange
        String namedFilter = Monitor.FILTER_BY_APP_NAMEDFILTER;

        // Act & Assert
        assertThatThrownBy(() -> monitorService.namedFilterToSpringFilter(namedFilter))
            .isInstanceOf(ArrayIndexOutOfBoundsException.class)
            .as("Ha de llançar excepció quan el filtre no conté appId després de ':'");
    }

    @Test
    @DisplayName("namedFilterToSpringFilter: format incorrecte de l'appId llança NumberFormatException")
    void testNamedFilterToSpringFilter_NumberFormatException() {
        // Arrange
        String namedFilter = Monitor.FILTER_BY_APP_NAMEDFILTER + ":test";

        // Act & Assert
        assertThatThrownBy(() -> monitorService.namedFilterToSpringFilter(namedFilter))
            .isInstanceOf(NumberFormatException.class)
            .as("Ha de llançar excepció quan el filtre conté un valor no numeric després de ':'");
    }

}