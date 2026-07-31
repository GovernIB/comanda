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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Prova aïllada (sense mocks) de la resolució real de colors a partir d'una plantilla, per
 * reproduir el bug reportat: la previsualització (resolta pel frontend) mostra el tema destacat
 * correctament, però el dashboard real (resolt per aquesta classe) no. Aquesta prova construeix
 * una plantilla realista amb dos grups de paleta (LIGHT i LIGHT_HIGHLIGHTED, amb colors diferents)
 * per comprovar si `applyTemplateDefaults` realment aplica el color del grup destacat.
 */
class DashboardStyleResolverHelperTest {

    private final DashboardStyleResolverHelper helper = new DashboardStyleResolverHelper();

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

    @Test
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
    void testApplyTemplateDefaults_ambPropietatDeScopeEspecificDelWidget_TambeSAplica() {
        // Comprova que una propietat definida amb scope SIMPLE (no només COMMON) també es resol.
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
    void testApplyTemplateDefaults_quanElGrupDestacatNoExisteix_noResolCapColor() {
        // Si la plantilla només té el grup LIGHT (sense LIGHT_HIGHLIGHTED configurat), demanar-lo
        // no ha de fer petar res, però tampoc ha de resoldre cap color (queda null).
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
}
