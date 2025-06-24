package es.caib.comanda.estadistica.logic.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula;
import es.caib.comanda.estadistica.logic.intf.model.consulta.DadesComunsWidgetConsulta;
import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetTaulaItem;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeMode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.periode.PresetPeriode;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsultaEstadisticaHelperTest {

    @Mock
    private FetRepository fetRepository;

    @Mock
    private AtributsVisualsHelper atributsVisualsHelper;

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @Mock
    private DashboardItemRepository dashboardItemRepository;

    @InjectMocks
    private ConsultaEstadisticaHelper consultaEstadisticaHelper;

    private ObjectMapper objectMapper;
    private List<FetEntity> fetEntities;
    private List<TempsEntity> tempsEntities;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Carregar dades de test
        fetEntities = objectMapper.readValue(
                new ClassPathResource("es/caib/comanda/estadistica/logic/helper/fets.json").getInputStream(),
                new TypeReference<List<FetEntity>>() {});

        tempsEntities = objectMapper.readValue(
                new ClassPathResource("es/caib/comanda/estadistica/logic/helper/temps.json").getInputStream(),
                new TypeReference<List<TempsEntity>>() {});

        // Associar temps a fets
        for (FetEntity fet : fetEntities) {
            Long tempsId = fet.getTemps().getId();
            TempsEntity temps = tempsEntities.stream()
                    .filter(t -> t.getId().equals(tempsId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Temps not found with id: " + tempsId));
            fet.setTemps(temps);
        }
    }

    @Test
    void testGetEstadistiquesPeriode() {
        // Arrange
        Long entornAppId = 1L;
        LocalDate dataInici = LocalDate.of(2023, 1, 1);
        LocalDate dataFi = LocalDate.of(2023, 3, 31);

        List<FetEntity> filteredFets = fetEntities.stream()
                .filter(fet -> fet.getEntornAppId().equals(entornAppId) &&
                        fet.getTemps().getData().isAfter(dataInici.minusDays(1)) &&
                        fet.getTemps().getData().isBefore(dataFi.plusDays(1)))
                .collect(Collectors.toList());

        when(fetRepository.findByEntornAppIdAndTempsDataBetween(entornAppId, dataInici, dataFi))
                .thenReturn(filteredFets);

        // Act
        List<Fet> result = consultaEstadisticaHelper.getEstadistiquesPeriode(entornAppId, dataInici, dataFi);

        // Assert
        assertNotNull(result);
        assertEquals(filteredFets.size(), result.size());
    }

    @Test
    void testGetEstadistiquesPeriodeAmbDimensions() {
        // Arrange
        Long entornAppId = 1L;
        LocalDate dataInici = LocalDate.of(2023, 1, 1);
        LocalDate dataFi = LocalDate.of(2023, 3, 31);
        Map<String, List<String>> dimensionsFiltre = new HashMap<>();
        dimensionsFiltre.put("departament", List.of("RRHH"));

        List<FetEntity> filteredFets = fetEntities.stream()
                .filter(fet -> fet.getEntornAppId().equals(entornAppId) &&
                        fet.getTemps().getData().isAfter(dataInici.minusDays(1)) &&
                        fet.getTemps().getData().isBefore(dataFi.plusDays(1)) &&
                        fet.getDimensionsJson().get("departament").equals("RRHH"))
                .collect(Collectors.toList());

        when(fetRepository.findByEntornAppIdAndTempsDataBetweenAndDimensions(
                eq(entornAppId), eq(dataInici), eq(dataFi), any()))
                .thenReturn(filteredFets);

        // Act
        List<Fet> result = consultaEstadisticaHelper.getEstadistiquesPeriodeAmbDimensions(
                entornAppId, dataInici, dataFi, dimensionsFiltre);

        // Assert
        assertNotNull(result);
        assertEquals(filteredFets.size(), result.size());
        result.forEach(fet -> assertEquals("RRHH", fet.getDimensionsJson().get("departament")));
    }

    @Test
    void testGetDadesWidget_Simple() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        // Create widget hierarchy
        es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity widget = 
                new es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity();

        // Create indicator info
        es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity indicadorInfo = 
                new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity();

        // Create indicator
        es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity indicador = 
                new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity();
        indicador.setCodi("visites");

        // Set up the hierarchy
        indicadorInfo.setIndicador(indicador);
        indicadorInfo.setAgregacio(TableColumnsEnum.SUM);

        widget.setPeriodeMode(PeriodeMode.PRESET);
        widget.setPresetPeriode(PresetPeriode.DARRERS_30_DIES);
        widget.setIndicadorInfo(indicadorInfo);

        dashboardItem.setWidget(widget);

        // Mock the repository call
        when(dashboardItemRepository.findById(dashboardItem.getId())).thenReturn(java.util.Optional.of(dashboardItem));

        // Mock the estadisticaClientHelper
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(any(), any()))
                .thenReturn(EntornApp.builder().id(1L).entorn(EntornRef.builder().id(2L).build()).build());
        when(estadisticaClientHelper.entornById(any()))
                .thenReturn(Entorn.builder().codi("DEV").build());

        // Mock the fetRepository
        when(fetRepository.getValorSimpleAgregat(
                any(), any(), any(), any(), any()))
                .thenReturn("42.0");

        // Act
        InformeWidgetItem result = consultaEstadisticaHelper.getDadesWidget(dashboardItem);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetSimpleItem);
        es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetSimpleItem simpleItem = 
                (es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetSimpleItem) result;
        assertEquals("42.0", simpleItem.getValor());
    }

    @Test
    void testCalculateValorSimple() {
        // Arrange
        Long entornAppId = 1L;
        PeriodeResolverHelper.PeriodeDates periodeConsulta = new PeriodeResolverHelper.PeriodeDates(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 3, 31)
        );

        // Create widget hierarchy
        es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity widget = 
                new es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity();

        // Create indicator info
        es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity indicadorInfo = 
                new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity();

        // Create indicator
        es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity indicador = 
                new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity();
        indicador.setCodi("visites");

        // Set up the hierarchy
        indicadorInfo.setIndicador(indicador);
        indicadorInfo.setAgregacio(TableColumnsEnum.SUM);

        // Use reflection to set the indicadorInfo field in widget
        try {
            java.lang.reflect.Field field = es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity.class
                    .getDeclaredField("indicadorInfo");
            field.setAccessible(true);
            field.set(widget, indicadorInfo);
        } catch (Exception e) {
            fail("Failed to set indicadorInfo field: " + e.getMessage());
        }

        // Mock the repository call
        when(fetRepository.getValorSimpleAgregat(
                eq(entornAppId), 
                eq(periodeConsulta.start), 
                eq(periodeConsulta.end), 
                any(), 
                any(IndicadorAgregacio.class)))
                .thenReturn("33.0");

        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "calculateValorSimple", 
                    es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity.class,
                    PeriodeResolverHelper.PeriodeDates.class,
                    Long.class);
            method.setAccessible(true);

            // Act
            String result = (String) method.invoke(consultaEstadisticaHelper, widget, periodeConsulta, entornAppId);

            // Assert
            assertNotNull(result);
            assertEquals("33.0", result);

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetPercentatgeComparacio() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "getPercentatgeComparacio", 
                    double.class,
                    double.class);
            method.setAccessible(true);

            // Act & Assert
            assertEquals(50.0, (double) method.invoke(consultaEstadisticaHelper, 100.0, 150.0));
            assertEquals(-25.0, (double) method.invoke(consultaEstadisticaHelper, 100.0, 75.0));
            assertEquals(0.0, (double) method.invoke(consultaEstadisticaHelper, 100.0, 100.0));
            assertEquals(100.0, (double) method.invoke(consultaEstadisticaHelper, 0.0, 10.0));
            assertEquals(-100.0, (double) method.invoke(consultaEstadisticaHelper, 0.0, -10.0));
            assertEquals(0.0, (double) method.invoke(consultaEstadisticaHelper, 0.0, 0.0));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testIsZero() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("isZero", double.class);
            method.setAccessible(true);

            // Act & Assert
            assertTrue((boolean) method.invoke(consultaEstadisticaHelper, 0.0));
            assertTrue((boolean) method.invoke(consultaEstadisticaHelper, 0.0000000001));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, 0.1));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, 1.0));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testIsNumeric() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("isNumeric", String.class);
            method.setAccessible(true);

            // Act & Assert
            assertTrue((boolean) method.invoke(consultaEstadisticaHelper, "123"));
            assertTrue((boolean) method.invoke(consultaEstadisticaHelper, "123.45"));
            assertTrue((boolean) method.invoke(consultaEstadisticaHelper, "-123.45"));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, "abc"));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, "123abc"));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, (Object) null));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, ""));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testIsDate() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("isDate", String.class);
            method.setAccessible(true);

            // Act & Assert
            assertTrue((boolean) method.invoke(consultaEstadisticaHelper, "01/01/2023"));
            assertTrue((boolean) method.invoke(consultaEstadisticaHelper, "1/1/2023"));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, "01-01-2023"));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, "2023-13-01"));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, "abc"));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, (Object) null));
            assertFalse((boolean) method.invoke(consultaEstadisticaHelper, ""));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testToDouble() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("toDouble", String.class);
            method.setAccessible(true);

            // Act & Assert
            assertEquals(123.0, (Double) method.invoke(consultaEstadisticaHelper, "123"));
            assertEquals(123.45, (Double) method.invoke(consultaEstadisticaHelper, "123.45"));
            assertEquals(-123.45, (Double) method.invoke(consultaEstadisticaHelper, "-123.45"));
            assertEquals(null, (Double) method.invoke(consultaEstadisticaHelper, "abc"));
            assertEquals(null, (Double) method.invoke(consultaEstadisticaHelper, (Object )null));
            assertEquals(null, (Double) method.invoke(consultaEstadisticaHelper, ""));
            assertEquals(null, (Double) method.invoke(consultaEstadisticaHelper, " "));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testToFet() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("toFet", FetEntity.class);
            method.setAccessible(true);

            // Arrange
            FetEntity fetEntity = fetEntities.get(0);

            // Act
            Fet result = (Fet) method.invoke(consultaEstadisticaHelper, fetEntity);

            // Assert
            assertNotNull(result);
            assertEquals(fetEntity.getEntornAppId(), result.getEntornAppId());
            assertEquals(fetEntity.getTemps().getData(), result.getTemps().getData());
            assertEquals(fetEntity.getDimensionsJson(), result.getDimensionsJson());
            assertEquals(fetEntity.getIndicadorsJson(), result.getIndicadorsJson());

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testToFets() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("toFets", List.class);
            method.setAccessible(true);

            // Act
            @SuppressWarnings("unchecked")
            List<Fet> result = (List<Fet>) method.invoke(consultaEstadisticaHelper, fetEntities);

            // Assert
            assertNotNull(result);
            assertEquals(fetEntities.size(), result.size());
            for (int i = 0; i < fetEntities.size(); i++) {
                assertEquals(fetEntities.get(i).getEntornAppId(), result.get(i).getEntornAppId());
                assertEquals(fetEntities.get(i).getTemps().getData(), result.get(i).getTemps().getData());
                assertEquals(fetEntities.get(i).getDimensionsJson(), result.get(i).getDimensionsJson());
                assertEquals(fetEntities.get(i).getIndicadorsJson(), result.get(i).getIndicadorsJson());
            }

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testExtractKeyExcluding() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("extractKeyExcluding", Map.class, String.class);
            method.setAccessible(true);

            // Arrange
            Map<String, String> map = new HashMap<>();
            map.put("key1", "value1");
            map.put("key2", "value2");
            map.put("key3", "value3");

            // Act & Assert
            assertEquals("key1", (String) method.invoke(consultaEstadisticaHelper, map, "key2"));
            assertEquals("key2", (String) method.invoke(consultaEstadisticaHelper, map, "key1"));
            assertEquals("key1", (String) method.invoke(consultaEstadisticaHelper, map, "key3"));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testConvertToPieChartSeriesSimple() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "convertToPieChartSeriesSimple", List.class, String.class, String.class);
            method.setAccessible(true);

            // Arrange
            List<Map<String, String>> files = new ArrayList<>();
            Map<String, String> file1 = new HashMap<>();
            file1.put("departament", "RRHH");
            file1.put("valor", "100");
            Map<String, String> file2 = new HashMap<>();
            file2.put("departament", "IT");
            file2.put("valor", "200");
            files.add(file1);
            files.add(file2);

            // Act
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(
                    consultaEstadisticaHelper, files, "departament", "valor");

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            // Find RRHH and IT entries
            Map<String, Object> rrhhEntry = null;
            Map<String, Object> itEntry = null;
            for (Map<String, Object> entry : result) {
                if ("RRHH".equals(entry.get("label"))) {
                    rrhhEntry = entry;
                } else if ("IT".equals(entry.get("label"))) {
                    itEntry = entry;
                }
            }

            assertNotNull(rrhhEntry);
            assertNotNull(itEntry);
            assertEquals(100.0, rrhhEntry.get("value"));
            assertEquals(200.0, itEntry.get("value"));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testConvertToChartSeriesSimple() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "convertToChartSeriesSimple", List.class, String.class, String.class);
            method.setAccessible(true);

            // Arrange
            List<Map<String, String>> files = new ArrayList<>();
            Map<String, String> file1 = new HashMap<>();
            file1.put("mes", "Gener");
            file1.put("valor", "100");
            Map<String, String> file2 = new HashMap<>();
            file2.put("mes", "Febrer");
            file2.put("valor", "200");
            files.add(file1);
            files.add(file2);

            // Act
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(
                    consultaEstadisticaHelper, files, "mes", "valor");

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            // Find Gener and Febrer entries
            Map<String, Object> generEntry = null;
            Map<String, Object> febrerEntry = null;
            for (Map<String, Object> entry : result) {
                if ("Gener".equals(entry.get("mes"))) {
                    generEntry = entry;
                } else if ("Febrer".equals(entry.get("mes"))) {
                    febrerEntry = entry;
                }
            }

            assertNotNull(generEntry);
            assertNotNull(febrerEntry);
            assertEquals(100.0, generEntry.get("valor"));
            assertEquals(200.0, febrerEntry.get("valor"));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testGroupByAndAggregate() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("groupByAndAggregate", List.class, String.class, String.class);
            method.setAccessible(true);

            // Arrange
            List<Map<String, String>> files = new ArrayList<>();
            Map<String, String> fila1 = new HashMap<>();
            fila1.put("departament", "RRHH");
            fila1.put("valor", "100");
            Map<String, String> fila2 = new HashMap<>();
            fila2.put("departament", "RRHH");
            fila2.put("valor", "200");
            Map<String, String> fila3 = new HashMap<>();
            fila3.put("departament", "IT");
            fila3.put("valor", "300");
            files.add(fila1);
            files.add(fila2);
            files.add(fila3);

            // Act
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(
                    consultaEstadisticaHelper, files, "departament", "valor");

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            // Find RRHH and IT entries
            Map<String, Object> rrhhEntry = null;
            Map<String, Object> itEntry = null;
            for (Map<String, Object> entry : result) {
                if ("RRHH".equals(entry.get("label"))) {
                    rrhhEntry = entry;
                } else if ("IT".equals(entry.get("label"))) {
                    itEntry = entry;
                }
            }

            assertNotNull(rrhhEntry);
            assertNotNull(itEntry);
            assertEquals(300.0, rrhhEntry.get("value"));
            assertEquals(300.0, itEntry.get("value"));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetLabelAgrupacioTemporal() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "getLabelAgrupacioTemporal", PeriodeUnitat.class);
            method.setAccessible(true);

            // Act & Assert
            assertEquals("Dia", (String) method.invoke(consultaEstadisticaHelper, PeriodeUnitat.DIA));
            assertEquals("Setmana", (String) method.invoke(consultaEstadisticaHelper, PeriodeUnitat.SETMANA));
            assertEquals("Mes", (String) method.invoke(consultaEstadisticaHelper, PeriodeUnitat.MES));
            assertEquals("Any", (String) method.invoke(consultaEstadisticaHelper, PeriodeUnitat.ANY));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testDetermineWidgetType() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "determineWidgetType", DashboardItemEntity.class);
            method.setAccessible(true);

            // Arrange
            DashboardItemEntity dashboardItem = new DashboardItemEntity();

            // Test with SimpleWidget
            es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity simpleWidget = 
                    new es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity();
            dashboardItem.setWidget(simpleWidget);

            // Act & Assert
            assertEquals(
                es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus.SIMPLE, 
                method.invoke(consultaEstadisticaHelper, dashboardItem)
            );

            // Test with GraficWidget
            es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity graficWidget = 
                    new es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity();
            dashboardItem.setWidget(graficWidget);

            // Act & Assert
            assertEquals(
                es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus.GRAFIC, 
                method.invoke(consultaEstadisticaHelper, dashboardItem)
            );

            // Test with TaulaWidget
            es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity taulaWidget = 
                    new es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity();
            dashboardItem.setWidget(taulaWidget);

            // Act & Assert
            assertEquals(
                es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus.TAULA, 
                method.invoke(consultaEstadisticaHelper, dashboardItem)
            );

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetDadesComunsConsulta() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "getDadesComunsConsulta", DashboardItemEntity.class);
            method.setAccessible(true);

            // Arrange
            DashboardItemEntity dashboardItem = new DashboardItemEntity();
            dashboardItem.setId(1L);
            dashboardItem.setEntornId(2L);

            es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity widget = 
                    new es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity();
            widget.setPeriodeMode(PeriodeMode.PRESET);
            widget.setPresetPeriode(PresetPeriode.DARRERS_30_DIES);
            dashboardItem.setWidget(widget);

            when(estadisticaClientHelper.entornAppFindByAppAndEntorn(any(), any()))
                    .thenReturn(EntornApp.builder().id(3L).entorn(EntornRef.builder().id(2L).build()).build());
            when(estadisticaClientHelper.entornById(any()))
                    .thenReturn(Entorn.builder().codi("DEV").build());

            // Act
            Object result = method.invoke(consultaEstadisticaHelper, dashboardItem);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof es.caib.comanda.estadistica.logic.intf.model.consulta.DadesComunsWidgetConsulta);
            es.caib.comanda.estadistica.logic.intf.model.consulta.DadesComunsWidgetConsulta dades = 
                    (es.caib.comanda.estadistica.logic.intf.model.consulta.DadesComunsWidgetConsulta) result;
            assertEquals(3L, dades.getEntornAppId());
            assertEquals("DEV", dades.getEntornCodi());
            assertNotNull(dades.getPeriodeDates());

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testResolveAtributsVisuals() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("resolveAtributsVisuals", DashboardItemEntity.class);
            method.setAccessible(true);

            // Arrange
            DashboardItemEntity dashboardItem = new DashboardItemEntity();
            dashboardItem.setId(1L);

            // Test with SimpleWidget
            EstadisticaSimpleWidgetEntity simpleWidget = new EstadisticaSimpleWidgetEntity();
            dashboardItem.setWidget(simpleWidget);

            AtributsVisualsSimple atributsSimple = new AtributsVisualsSimple();
            when(atributsVisualsHelper.getAtributsVisuals(any(EstadisticaWidgetEntity.class))).thenReturn(atributsSimple);

            // Act
            Object result = method.invoke(consultaEstadisticaHelper, dashboardItem);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof AtributsVisualsSimple);

            // Test with GraficWidget
            EstadisticaGraficWidgetEntity graficWidget = new EstadisticaGraficWidgetEntity();
            dashboardItem.setWidget(graficWidget);

            AtributsVisualsGrafic atributsGrafic = new AtributsVisualsGrafic();
            when(atributsVisualsHelper.getAtributsVisuals(any(EstadisticaWidgetEntity.class))).thenReturn(atributsGrafic);

            // Act
            result = method.invoke(consultaEstadisticaHelper, dashboardItem);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof AtributsVisualsGrafic);

            // Test with TaulaWidget
            es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity taulaWidget = 
                    new es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity();
            dashboardItem.setWidget(taulaWidget);

            AtributsVisualsTaula atributsTaula =  new AtributsVisualsTaula();
            when(atributsVisualsHelper.getAtributsVisuals(any(EstadisticaWidgetEntity.class))).thenReturn(atributsTaula);

            // Act
            result = method.invoke(consultaEstadisticaHelper, dashboardItem);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof AtributsVisualsTaula);

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testResolveAtributsVisualsMerging() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            ObjectMapper mapper = new ObjectMapper();
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("resolveAtributsVisuals", DashboardItemEntity.class);
            method.setAccessible(true);

            // Arrange
            DashboardItemEntity dashboardItem = new DashboardItemEntity();
            dashboardItem.setId(1L);

            // Create widget with visual attributes
            EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
            AtributsVisualsSimple widgetAtributs = new AtributsVisualsSimple();
            widgetAtributs.setColorText("#AAAAAA");
            widgetAtributs.setColorFons("#FCFCFC");
            widget.setAtributsVisualsJson(mapper.writeValueAsString(widgetAtributs));
            dashboardItem.setWidget(widget);

            // Create dashboard item visual attributes
            AtributsVisualsSimple dashboardAtributs = new AtributsVisualsSimple();
            dashboardAtributs.setColorText("#CCCCCC");
            dashboardAtributs.setColorVora("#DDDDDD");
            dashboardItem.setAtributsVisualsJson(mapper.writeValueAsString(widgetAtributs));

            when(atributsVisualsHelper.getAtributsVisuals(any(EstadisticaWidgetEntity.class))).thenReturn(widgetAtributs);
            when(atributsVisualsHelper.getAtributsVisuals(any(DashboardItemEntity.class))).thenReturn(dashboardAtributs);

            // Act
            Object result = method.invoke(consultaEstadisticaHelper, dashboardItem);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof AtributsVisualsSimple);
            AtributsVisualsSimple mergedAtributs = (AtributsVisualsSimple) result;
            assertEquals("#CCCCCC", mergedAtributs.getColorText());
            assertEquals("#FCFCFC", mergedAtributs.getColorFons());
            assertEquals("#DDDDDD", mergedAtributs.getColorVora());

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testCreateDimensionsFiltre() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "createDimensionsFiltre", List.class);
            method.setAccessible(true);

            // Arrange
            List<es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity> dimensioValors = new ArrayList<>();

            es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity dimensioValor1 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity();
            es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity dimensio1 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity();
            dimensio1.setCodi("departament");
            dimensioValor1.setDimensio(dimensio1);
            dimensioValor1.setValor("RRHH");
            dimensioValors.add(dimensioValor1);

            es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity dimensioValor2 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity();
            es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity dimensio2 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity();
            dimensio2.setCodi("departament");
            dimensioValor2.setDimensio(dimensio2);
            dimensioValor2.setValor("IT");
            dimensioValors.add(dimensioValor2);

            es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity dimensioValor3 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity();
            es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity dimensio3 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity();
            dimensio3.setCodi("ubicacio");
            dimensioValor3.setDimensio(dimensio3);
            dimensioValor3.setValor("Palma");
            dimensioValors.add(dimensioValor3);

            // Act
            @SuppressWarnings("unchecked")
            Map<String, List<String>> result = (Map<String, List<String>>) method.invoke(
                    consultaEstadisticaHelper, dimensioValors);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.containsKey("departament"));
            assertTrue(result.containsKey("ubicacio"));
            assertEquals(2, result.get("departament").size());
            assertEquals(1, result.get("ubicacio").size());
            assertTrue(result.get("departament").contains("RRHH"));
            assertTrue(result.get("departament").contains("IT"));
            assertTrue(result.get("ubicacio").contains("Palma"));

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testCalculateCanviPercentual() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "calculateCanviPercentual", 
                    es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity.class,
                    String.class,
                    PeriodeResolverHelper.PeriodeDates.class,
                    Long.class);
            method.setAccessible(true);

            // Arrange
            es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity widget = 
                    new es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity();

            es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity indicadorInfo = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity();

            es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity indicador = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity();
            indicador.setCodi("visites");

            indicadorInfo.setIndicador(indicador);
            indicadorInfo.setAgregacio(TableColumnsEnum.SUM);

            // Use reflection to set the indicadorInfo field in widget
            java.lang.reflect.Field field = es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity.class
                    .getDeclaredField("indicadorInfo");
            field.setAccessible(true);
            field.set(widget, indicadorInfo);

            // Set compararPeriodeAnterior to true
            java.lang.reflect.Field compararField = es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity.class
                    .getDeclaredField("compararPeriodeAnterior");
            compararField.setAccessible(true);
            compararField.set(widget, true);

            String valorConsulta = "100.0";
            PeriodeResolverHelper.PeriodeDates periodePrevi = new PeriodeResolverHelper.PeriodeDates(
                    LocalDate.of(2023, 1, 1),
                    LocalDate.of(2023, 1, 31)
            );
            Long entornAppId = 1L;

            when(fetRepository.getValorSimpleAgregat(
                    eq(entornAppId), 
                    eq(periodePrevi.start), 
                    eq(periodePrevi.end), 
                    any(), 
                    any(IndicadorAgregacio.class)))
                    .thenReturn("50.0");

            // Act
            String result = (String) method.invoke(
                    consultaEstadisticaHelper, widget, valorConsulta, periodePrevi, entornAppId);

            // Assert
            assertNotNull(result);
            assertEquals("100,00", result);

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetDadesWidgetSimple() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "getDadesWidgetSimple", 
                    DashboardItemEntity.class,
                    es.caib.comanda.estadistica.logic.intf.model.consulta.DadesComunsWidgetConsulta.class);
            method.setAccessible(true);

            // Arrange
            DashboardItemEntity dashboardItem = new DashboardItemEntity();
            dashboardItem.setId(1L);
            dashboardItem.setEntornId(1L);

            es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity widget = 
                    new es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity();

            es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity indicadorInfo = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity();

            es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity indicador = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity();
            indicador.setCodi("visites");

            indicadorInfo.setIndicador(indicador);
            indicadorInfo.setAgregacio(TableColumnsEnum.SUM);

            // Use reflection to set the indicadorInfo field in widget
            java.lang.reflect.Field field = es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity.class
                    .getDeclaredField("indicadorInfo");
            field.setAccessible(true);
            field.set(widget, indicadorInfo);

            dashboardItem.setWidget(widget);

            es.caib.comanda.estadistica.logic.intf.model.consulta.DadesComunsWidgetConsulta dadesComunsConsulta = 
                    es.caib.comanda.estadistica.logic.intf.model.consulta.DadesComunsWidgetConsulta.builder()
                    .entornAppId(1L)
                    .entornCodi("DEV")
                    .periodeDates(new PeriodeResolverHelper.PeriodeDates(
                            LocalDate.of(2023, 1, 1),
                            LocalDate.of(2023, 1, 31)
                    ))
                    .build();

            when(fetRepository.getValorSimpleAgregat(
                    any(), any(), any(), any(), any(IndicadorAgregacio.class)))
                    .thenReturn("42.0");

            // Act
            Object result = method.invoke(consultaEstadisticaHelper, dashboardItem, dadesComunsConsulta);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetSimpleItem);
            es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetSimpleItem simpleItem = 
                    (es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetSimpleItem) result;
            assertEquals("42.0", simpleItem.getValor());

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetDadesWidgetGrafic() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "getDadesWidgetGrafic", 
                    DashboardItemEntity.class,
                    es.caib.comanda.estadistica.logic.intf.model.consulta.DadesComunsWidgetConsulta.class);
            method.setAccessible(true);

            // Arrange
            DashboardItemEntity dashboardItem = new DashboardItemEntity();
            dashboardItem.setId(1L);
            dashboardItem.setEntornId(1L);

            EstadisticaGraficWidgetEntity widget = new EstadisticaGraficWidgetEntity();

            // Set widget properties
            widget.setTipusGrafic(TipusGraficEnum.LINE_CHART);
            widget.setTipusDades(TipusGraficDataEnum.UN_INDICADOR);
            widget.setTempsAgrupacio(PeriodeUnitat.DIA);

            // Create indicator info
            IndicadorTaulaEntity indicadorInfo = new IndicadorTaulaEntity();

            IndicadorEntity indicador = new IndicadorEntity();
            indicador.setCodi("visites");

            indicadorInfo.setIndicador(indicador);
            indicadorInfo.setAgregacio(TableColumnsEnum.SUM);
            indicadorInfo.setTitol("Visites");

            widget.setIndicadorsInfo(List.of(indicadorInfo));

            dashboardItem.setWidget(widget);

            DadesComunsWidgetConsulta dadesComunsConsulta =DadesComunsWidgetConsulta.builder()
                    .entornAppId(1L)
                    .entornCodi("DEV")
                    .periodeDates(new PeriodeResolverHelper.PeriodeDates(
                            LocalDate.of(2023, 1, 1),
                            LocalDate.of(2023, 1, 31)
                    ))
                    .build();

            // Mock repository response
            List<Map<String, String>> mockData = new ArrayList<>();
            Map<String, String> dataPoint1 = new HashMap<>();
            dataPoint1.put("agrupacio", "2023-01-15");
            dataPoint1.put("visites", "100");
            mockData.add(dataPoint1);

            Map<String, String> dataPoint2 = new HashMap<>();
            dataPoint2.put("agrupacio", "2023-01-16");
            dataPoint2.put("visites", "150");
            mockData.add(dataPoint2);

            when(fetRepository.getValorsGraficUnIndicador(any(), any(), any(), any(), any(), any())).thenReturn(mockData);

            // Act
            Object result = method.invoke(consultaEstadisticaHelper, dashboardItem, dadesComunsConsulta);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetGraficItem);
            es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetGraficItem graficItem = 
                    (es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetGraficItem) result;
            assertNotNull(graficItem.getDades());
            assertFalse(graficItem.getDades().isEmpty());

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetDadesWidgetTaula() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod("getDadesWidgetTaula", DashboardItemEntity.class, DadesComunsWidgetConsulta.class);
            method.setAccessible(true);

            // Arrange
            DashboardItemEntity dashboardItem = new DashboardItemEntity();
            dashboardItem.setId(1L);
            dashboardItem.setEntornId(1L);

            EstadisticaTaulaWidgetEntity widget =new EstadisticaTaulaWidgetEntity();

            // Create indicator info list
            List<IndicadorTaulaEntity> indicadorsList = new ArrayList<>();

            IndicadorTaulaEntity indicadorInfo = new IndicadorTaulaEntity();
            IndicadorEntity indicador = new IndicadorEntity();
            indicador.setCodi("visites");


            indicadorInfo.setIndicador(indicador);
            indicadorInfo.setAgregacio(TableColumnsEnum.SUM);
            indicadorInfo.setTitol("Visites");
            indicadorsList.add(indicadorInfo);

            widget.setColumnes(indicadorsList);

            DimensioEntity dimensio = new DimensioEntity();
            dimensio.setCodi("departament");
            dimensio.setDescripcio("departament");
            widget.setDimensioAgrupacio(dimensio);
            widget.setTitolAgrupament("Agrupament");

            dashboardItem.setWidget(widget);

            DadesComunsWidgetConsulta dadesComunsConsulta = DadesComunsWidgetConsulta.builder()
                    .entornAppId(1L)
                    .entornCodi("DEV")
                    .periodeDates(new PeriodeResolverHelper.PeriodeDates(
                            LocalDate.of(2023, 1, 1),
                            LocalDate.of(2023, 1, 31)
                    ))
                    .build();

            // Mock repository response
            List<Map<String, String>> mockData = new ArrayList<>();
            Map<String, String> dataPoint1 = new HashMap<>();
            dataPoint1.put("agrupacio", "2023-01-15");
            dataPoint1.put("visites", "100");
            mockData.add(dataPoint1);

            Map<String, String> dataPoint2 = new HashMap<>();
            dataPoint2.put("agrupacio", "2023-01-16");
            dataPoint2.put("visites", "150");
            mockData.add(dataPoint2);

            when(fetRepository.getValorsTaulaAgregat(any(), any(), any(), any(), any(), any())).thenReturn(mockData);

            // Act
            Object result = method.invoke(consultaEstadisticaHelper, dashboardItem, dadesComunsConsulta);

            // Assert
            assertNotNull(result);
            assertTrue(result instanceof InformeWidgetTaulaItem);
            InformeWidgetTaulaItem taulaItem = (InformeWidgetTaulaItem) result;
            assertNotNull(taulaItem.getColumnes());
            assertNotNull(taulaItem.getFiles());
            assertFalse(taulaItem.getFiles().isEmpty());

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testFilesToSeries() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "filesToSeries", 
                    List.class,
                    es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum.class,
                    es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum.class);
            method.setAccessible(true);

            // Arrange
            List<Map<String, String>> files = new ArrayList<>();
            Map<String, String> file1 = new HashMap<>();
            file1.put("agrupacio", "2023-01-15");
            file1.put("visites", "100");
            files.add(file1);

            Map<String, String> file2 = new HashMap<>();
            file2.put("agrupacio", "2023-01-16");
            file2.put("visites", "150");
            files.add(file2);

            // Act - Test with LINE_CHART and UN_INDICADOR
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) method.invoke(
                    consultaEstadisticaHelper, 
                    files, 
                    es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum.LINE_CHART,
                    es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum.UN_INDICADOR);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());

            // Act - Test with PIE_CHART and UN_INDICADOR
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resultPie = (List<Map<String, Object>>) method.invoke(
                    consultaEstadisticaHelper, 
                    files, 
                    es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum.PIE_CHART,
                    es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum.UN_INDICADOR);

            // Assert
            assertNotNull(resultPie);
            assertFalse(resultPie.isEmpty());

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testGetColumnNames() {
        // Use reflection to access the private method
        java.lang.reflect.Method method;
        try {
            method = ConsultaEstadisticaHelper.class.getDeclaredMethod(
                    "getColumnNames", 
                    List.class);
            method.setAccessible(true);

            // Arrange
            List<es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity> indicadorsList = 
                    new ArrayList<>();

            es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity indicadorInfo1 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity();

            es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity indicador1 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity();
            indicador1.setCodi("visites");
            indicador1.setNom("Visites");

            indicadorInfo1.setIndicador(indicador1);
            indicadorInfo1.setAgregacio(TableColumnsEnum.SUM);
            indicadorsList.add(indicadorInfo1);

            es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity indicadorInfo2 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity();

            es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity indicador2 = 
                    new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity();
            indicador2.setCodi("temps");
            indicador2.setNom("Temps");

            indicadorInfo2.setIndicador(indicador2);
            indicadorInfo2.setAgregacio(TableColumnsEnum.AVERAGE);
            indicadorsList.add(indicadorInfo2);

            // Act
            String[] result = (String[]) method.invoke(consultaEstadisticaHelper, indicadorsList);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.length);
            assertEquals("agrupacio", result[0]);
            assertEquals("col1", result[1]);
            assertEquals("col2", result[2]);

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }
}
