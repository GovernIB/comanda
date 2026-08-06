package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteRole;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleValueType;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaColorEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaGrupPaletesEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.WidgetStylePropertyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardStyleResolverHelper")
class DashboardStyleResolverHelperTest {

    @InjectMocks
    private DashboardStyleResolverHelper helper = new DashboardStyleResolverHelper();

    // =========================================================================
    // MÈTODES AUXILIARS DE CONSTRUCCIÓ (Mantinguts de la base original)
    // =========================================================================

    private PaletaEntity paleta(String... colors) {
        PaletaEntity paleta = new PaletaEntity();
        paleta.setId(1L);
        paleta.setNom("Paleta");
        List<PaletaColorEntity> paletaColors = new java.util.ArrayList<>();
        for (int i = 0; i < colors.length; i++) {
            PaletaColorEntity color = new PaletaColorEntity();
            color.setPaleta(paleta);
            color.setPosicio(i);
            color.setValor(colors[i]);
            paletaColors.add(color);
        }
        paleta.setColors(paletaColors);
        return paleta;
    }

    private PlantillaGrupPaletesEntity grup(PlantillaEntity plantilla, PaletteGroupType groupType, PaletaEntity widgetPaleta, PaletaEntity chartPaleta) {
        PlantillaGrupPaletesEntity grup = new PlantillaGrupPaletesEntity();
        grup.setPlantilla(plantilla);
        grup.setGroupType(groupType);
        grup.setWidgetPalette(widgetPaleta);
        grup.setChartPalette(chartPaleta);
        return grup;
    }

    private WidgetStylePropertyEntity propietat(PlantillaEntity plantilla, WidgetStyleScope scope, String propertyName, PaletteRole role, int paletteIndex) {
        WidgetStylePropertyEntity propietat = new WidgetStylePropertyEntity();
        propietat.setPlantilla(plantilla);
        propietat.setScope(scope);
        propietat.setPropertyName(propertyName);
        propietat.setValueType(WidgetStyleValueType.COLOR);
        propietat.setPaletteRole(role);
        propietat.setPaletteIndex(paletteIndex);
        return propietat;
    }

    // =========================================================================
    // TESTOS ORIGINALS (Mantinguts i validats)
    // =========================================================================

    @Test
    @DisplayName("applyTemplateDefaults: amb grup destacat i normal resol cada color diferent")
    void testApplyTemplateDefaults_ambGrupDestacatIGrupNormal_resolCadaColorDiferent() {
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(1L);
        plantilla.setNom("Blau");

        PaletaEntity paletaLight = paleta("#FFFFFF");
        PaletaEntity paletaHighlighted = paleta("#1565C0");

        plantilla.setPaletteGroups(List.of(
            grup(plantilla, PaletteGroupType.LIGHT, paletaLight, paletaLight),
            grup(plantilla, PaletteGroupType.LIGHT_HIGHLIGHTED, paletaHighlighted, paletaHighlighted)
        ));
        plantilla.setStyleProperties(List.of(
            propietat(plantilla, WidgetStyleScope.COMMON, "colorFons", PaletteRole.WIDGET, 0)
        ));

        AtributsVisuals resolvedLight = new AtributsVisualsSimple();
        helper.applyTemplateDefaults(resolvedLight, plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.SIMPLE);

        AtributsVisuals resolvedHighlighted = new AtributsVisualsSimple();
        helper.applyTemplateDefaults(resolvedHighlighted, plantilla, PaletteGroupType.LIGHT_HIGHLIGHTED, WidgetStyleScope.SIMPLE);

        assertEquals("#FFFFFF", ((AtributsVisualsSimple) resolvedLight).getColorFons());
        assertEquals("#1565C0", ((AtributsVisualsSimple) resolvedHighlighted).getColorFons());
        assertNotEquals(resolvedLight, resolvedHighlighted);
    }

    @Test
    @DisplayName("applyTemplateDefaults: amb propietat de scope específic del widget també s'aplica")
    void testApplyTemplateDefaults_ambPropietatDeScopeEspecificDelWidget_TambeSAplica() {
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(1L);
        PaletaEntity paleta = paleta("#AAAAAA", "#BBBBBB");
        plantilla.setPaletteGroups(List.of(
            grup(plantilla, PaletteGroupType.LIGHT_HIGHLIGHTED, paleta, paleta)
        ));
        plantilla.setStyleProperties(List.of(
            propietat(plantilla, WidgetStyleScope.SIMPLE, "colorFons", PaletteRole.WIDGET, 1)
        ));

        AtributsVisuals resolved = new AtributsVisualsSimple();
        helper.applyTemplateDefaults(resolved, plantilla, PaletteGroupType.LIGHT_HIGHLIGHTED, WidgetStyleScope.SIMPLE);

        assertEquals("#BBBBBB", ((AtributsVisualsSimple) resolved).getColorFons());
    }

    @Test
    @DisplayName("applyTemplateDefaults: quan el grup destacat no existeix no resol cap color")
    void testApplyTemplateDefaults_quanElGrupDestacatNoExisteix_noResolCapColor() {
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(1L);
        PaletaEntity paleta = paleta("#FFFFFF");
        plantilla.setPaletteGroups(List.of(
            grup(plantilla, PaletteGroupType.LIGHT, paleta, paleta)
        ));
        plantilla.setStyleProperties(List.of(
            propietat(plantilla, WidgetStyleScope.COMMON, "colorFons", PaletteRole.WIDGET, 0)
        ));

        AtributsVisuals resolved = new AtributsVisualsSimple();
        helper.applyTemplateDefaults(resolved, plantilla, PaletteGroupType.LIGHT_HIGHLIGHTED, WidgetStyleScope.SIMPLE);

        assertNull(((AtributsVisualsSimple) resolved).getColorFons());
    }

    @Test
    @DisplayName("applyTemplateDefaults: per gràfic resol la paleta de colors del grup destacat")
    void testApplyTemplateDefaults_perGrafic_resolLaPaletaDeColorsDelGrupDestacat() {
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(1L);
        PaletaEntity paletaLight = paleta("#111111", "#222222");
        PaletaEntity paletaHighlighted = paleta("#333333", "#444444");
        plantilla.setPaletteGroups(List.of(
            grup(plantilla, PaletteGroupType.LIGHT, paletaLight, paletaLight),
            grup(plantilla, PaletteGroupType.LIGHT_HIGHLIGHTED, paletaHighlighted, paletaHighlighted)
        ));
        plantilla.setStyleProperties(List.of());

        AtributsVisuals resolved = new AtributsVisualsGrafic();
        helper.applyTemplateDefaults(resolved, plantilla, PaletteGroupType.LIGHT_HIGHLIGHTED, WidgetStyleScope.GRAFIC);

        assertEquals("#333333,#444444", ((AtributsVisualsGrafic) resolved).getColorsPaleta());
    }

    // =========================================================================
    // NOUS TESTOS PER A COBERTURA COMPLETA (>90%)
    // =========================================================================

    @Test
    @DisplayName("applyTemplateDefaults: retorna null quan el target és null")
    void applyTemplateDefaults_quanTargetEsNull_llavorsRetornaNull() {
        AtributsVisuals result = helper.applyTemplateDefaults(null, new PlantillaEntity(), PaletteGroupType.LIGHT, WidgetStyleScope.COMMON);
        assertNull(result);
    }

    @Test
    @DisplayName("applyTemplateDefaults: retorna el target quan la plantilla és null")
    void applyTemplateDefaults_quanTemplateEsNull_llavorsRetornaTarget() {
        AtributsVisualsSimple target = new AtributsVisualsSimple();
        AtributsVisuals result = helper.applyTemplateDefaults(target, null, PaletteGroupType.LIGHT, WidgetStyleScope.COMMON);
        assertEquals(target, result);
    }

    @Test
    @DisplayName("applyTemplateDefaults: retorna el target quan styleProperties és null")
    void applyTemplateDefaults_quanStylePropertiesEsNull_llavorsRetornaTarget() {
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setStyleProperties(null);
        AtributsVisualsSimple target = new AtributsVisualsSimple();

        AtributsVisuals result = helper.applyTemplateDefaults(target, plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.COMMON);
        assertEquals(target, result);
    }

    @Test
    @DisplayName("applyTemplateDefaults: no sobre-escriu si el camp ja té un valor")
    void applyTemplateDefaults_quanCurrentValueNoEsNull_llavorsNoSobreEscriu() {
        PlantillaEntity plantilla = new PlantillaEntity();
        PaletaEntity paleta = paleta("#FFFFFF");
        plantilla.setPaletteGroups(List.of(grup(plantilla, PaletteGroupType.LIGHT, paleta, paleta)));
        plantilla.setStyleProperties(List.of(propietat(plantilla, WidgetStyleScope.COMMON, "colorFons", PaletteRole.WIDGET, 0)));

        AtributsVisualsSimple target = new AtributsVisualsSimple();
        target.setColorFons("#EXISTENT"); // Ja té valor

        helper.applyTemplateDefaults(target, plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.COMMON);

        assertEquals("#EXISTENT", target.getColorFons());
    }

    @Test
    @DisplayName("resolveColor: retorna null quan la propietat no existeix")
    void resolveColor_quanPropietatNoExisteix_llavorsRetornaNull() {
        PlantillaEntity plantilla = new PlantillaEntity();
        String color = helper.resolveColor(plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.COMMON, "inexistent");
        assertNull(color);
    }

    @Test
    @DisplayName("resolveColor: retorna null quan la propietat no és de tipus COLOR")
    void resolveColor_quanPropietatNoEsColor_llavorsRetornaNull() {
        PlantillaEntity plantilla = new PlantillaEntity();
        WidgetStylePropertyEntity prop = new WidgetStylePropertyEntity();
        prop.setScope(WidgetStyleScope.COMMON);
        prop.setPropertyName("ampleVora");
        prop.setValueType(WidgetStyleValueType.NUMBER);
        plantilla.setStyleProperties(List.of(prop));

        String color = helper.resolveColor(plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.COMMON, "ampleVora");
        assertNull(color);
    }

    @Test
    @DisplayName("resolveColor: retorna null quan PaletteRole és null")
    void resolveColor_quanPaletteRoleEsNull_llavorsRetornaNull() {
        PlantillaEntity plantilla = new PlantillaEntity();
        WidgetStylePropertyEntity prop = new WidgetStylePropertyEntity();
        prop.setScope(WidgetStyleScope.COMMON);
        prop.setPropertyName("colorFons");
        prop.setValueType(WidgetStyleValueType.COLOR);
        prop.setPaletteRole(null);
        prop.setPaletteIndex(0);
        plantilla.setStyleProperties(List.of(prop));

        String color = helper.resolveColor(plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.COMMON, "colorFons");
        assertNull(color);
    }

    @Test
    @DisplayName("resolveColor: retorna null quan PaletteIndex és null")
    void resolveColor_quanPaletteIndexEsNull_llavorsRetornaNull() {
        PlantillaEntity plantilla = new PlantillaEntity();
        WidgetStylePropertyEntity prop = new WidgetStylePropertyEntity();
        prop.setScope(WidgetStyleScope.COMMON);
        prop.setPropertyName("colorFons");
        prop.setValueType(WidgetStyleValueType.COLOR);
        prop.setPaletteRole(PaletteRole.WIDGET);
        prop.setPaletteIndex(null);
        plantilla.setStyleProperties(List.of(prop));

        String color = helper.resolveColor(plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.COMMON, "colorFons");
        assertNull(color);
    }

    @Test
    @DisplayName("resolveColor: retorna null quan la paleta és null")
    void resolveColor_quanPaletaEsNull_llavorsRetornaNull() {
        PlantillaEntity plantilla = new PlantillaEntity();
        PlantillaGrupPaletesEntity grup = new PlantillaGrupPaletesEntity();
        grup.setGroupType(PaletteGroupType.LIGHT);
        grup.setWidgetPalette(null);
        plantilla.setPaletteGroups(List.of(grup));

        plantilla.setStyleProperties(List.of(propietat(plantilla, WidgetStyleScope.COMMON, "colorFons", PaletteRole.WIDGET, 0)));

        String color = helper.resolveColor(plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.COMMON, "colorFons");
        assertNull(color);
    }

    @Test
    @DisplayName("resolveColor: retorna null quan l'index està fora de rang")
    void resolveColor_quanIndexForaDeRang_llavorsRetornaNull() {
        PlantillaEntity plantilla = new PlantillaEntity();
        PaletaEntity paleta = paleta("#FFFFFF"); // Només 1 color (índex 0)
        plantilla.setPaletteGroups(List.of(grup(plantilla, PaletteGroupType.LIGHT, paleta, paleta)));

        // Demanem l'índex 5, que no existeix
        plantilla.setStyleProperties(List.of(propietat(plantilla, WidgetStyleScope.COMMON, "colorFons", PaletteRole.WIDGET, 5)));

        String color = helper.resolveColor(plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.COMMON, "colorFons");
        assertNull(color);
    }

    @Test
    @DisplayName("applyChartPalette: no fa res quan colorsPaleta ja existeix")
    void applyChartPalette_quanColorsPaletaJaExisteix_llavorsNoSobreEscriu() {
        PlantillaEntity plantilla = new PlantillaEntity();
        PaletaEntity paleta = paleta("#111111", "#222222");
        plantilla.setPaletteGroups(List.of(grup(plantilla, PaletteGroupType.LIGHT, paleta, paleta)));

        AtributsVisualsGrafic target = new AtributsVisualsGrafic();
        target.setColorsPaleta("#CUSTOM"); // Ja té valor

        helper.applyTemplateDefaults(target, plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.GRAFIC);

        assertEquals("#CUSTOM", target.getColorsPaleta());
    }

    @Test
    @DisplayName("applyChartPalette: no fa res quan la chartPalette és null")
    void applyChartPalette_quanChartPaletteEsNull_llavorsNoFaRes() {
        PlantillaEntity plantilla = new PlantillaEntity();
        PlantillaGrupPaletesEntity grup = new PlantillaGrupPaletesEntity();
        grup.setGroupType(PaletteGroupType.LIGHT);
        grup.setChartPalette(null);
        plantilla.setPaletteGroups(List.of(grup));

        AtributsVisualsGrafic target = new AtributsVisualsGrafic();
        helper.applyTemplateDefaults(target, plantilla, PaletteGroupType.LIGHT, WidgetStyleScope.GRAFIC);

        assertNull(target.getColorsPaleta());
    }

    @Test
    @DisplayName("findGroup: utilitza LIGHT com a valor per defecte quan groupType és null")
    void findGroup_quanGroupTypeEsNull_llavorsUsaLightPerDefecte() {
        PlantillaEntity plantilla = new PlantillaEntity();
        PaletaEntity paleta = paleta("#FFFFFF");
        PlantillaGrupPaletesEntity grup = new PlantillaGrupPaletesEntity();
        grup.setGroupType(PaletteGroupType.LIGHT);
        grup.setWidgetPalette(paleta);
        plantilla.setPaletteGroups(List.of(grup));

        PlantillaGrupPaletesEntity result = (PlantillaGrupPaletesEntity) ReflectionTestUtils.invokeMethod(
            helper, "findGroup", plantilla, (PaletteGroupType) null);

        assertEquals(PaletteGroupType.LIGHT, result.getGroupType());
    }

    // =========================================================================
    // TESTOS PER A convertScalar (Cobertura de branques de tipus)
    // =========================================================================

    @Test
    @DisplayName("convertScalar: retorna null quan el valor d'entrada és null")
    void convertScalar_quanValorEsNull_llavorsRetornaNull() {
        Object result = ReflectionTestUtils.invokeMethod(helper, "convertScalar", null, String.class);
        assertNull(result);
    }

    @Test
    @DisplayName("convertScalar: retorna el mateix valor quan el tipus és String")
    void convertScalar_quanTipusEsString_llavorsRetornaMateixValor() {
        Object result = ReflectionTestUtils.invokeMethod(helper, "convertScalar", "text", String.class);
        assertEquals("text", result);
    }

    @Test
    @DisplayName("convertScalar: converteix correctament a Boolean (Object)")
    void convertScalar_quanTipusEsBooleanObject_llavorsConverteix() {
        Object result = ReflectionTestUtils.invokeMethod(helper, "convertScalar", "true", Boolean.class);
        assertEquals(true, result);
    }

    @Test
    @DisplayName("convertScalar: converteix correctament a boolean (primitiu)")
    void convertScalar_quanTipusEsBooleanPrimitiu_llavorsConverteix() {
        Object result = ReflectionTestUtils.invokeMethod(helper, "convertScalar", "false", boolean.class);
        assertEquals(false, result);
    }

    @Test
    @DisplayName("convertScalar: converteix correctament a Integer")
    void convertScalar_quanTipusEsInteger_llavorsConverteix() {
        Object result = ReflectionTestUtils.invokeMethod(helper, "convertScalar", "42", Integer.class);
        assertEquals(42, result);
    }

    @Test
    @DisplayName("convertScalar: converteix correctament a Double")
    void convertScalar_quanTipusEsDouble_llavorsConverteix() {
        Object result = ReflectionTestUtils.invokeMethod(helper, "convertScalar", "3.14", Double.class);
        assertEquals(3.14, result);
    }

    @Test
    @DisplayName("convertScalar: retorna null quan el tipus no està suportat")
    void convertScalar_quanTipusNoSuportat_llavorsRetornaNull() {
        Object result = ReflectionTestUtils.invokeMethod(helper, "convertScalar", "123", Long.class);
        assertNull(result);
    }
}
