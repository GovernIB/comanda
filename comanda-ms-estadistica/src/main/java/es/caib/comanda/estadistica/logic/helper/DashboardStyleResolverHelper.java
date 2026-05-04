package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteRole;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleValueType;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaGrupPaletesEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.WidgetStylePropertyEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DashboardStyleResolverHelper {

    public String resolveColor(
            PlantillaEntity template,
            PaletteGroupType groupType,
            WidgetStyleScope scope,
            String propertyName) {

        WidgetStylePropertyEntity property = findProperty(template, scope, propertyName);
        if (property == null || !WidgetStyleValueType.COLOR.equals(property.getValueType())) {
            return null;
        }
        return resolveColor(template, groupType, property);
    }

    public AtributsVisuals applyTemplateDefaults(
            AtributsVisuals target,
            PlantillaEntity template,
            PaletteGroupType groupType,
            WidgetStyleScope scope) {

        if (target == null || template == null || template.getStyleProperties() == null) {
            return target;
        }

        applyScope(target, template, groupType, WidgetStyleScope.COMMON);
        if (!WidgetStyleScope.COMMON.equals(scope)) {
            applyScope(target, template, groupType, scope);
        }
        if (target instanceof AtributsVisualsGrafic) {
            applyChartPalette((AtributsVisualsGrafic) target, template, groupType);
        }
        return target;
    }

    private void applyScope(AtributsVisuals target, PlantillaEntity template, PaletteGroupType groupType, WidgetStyleScope scope) {
        template.getStyleProperties().stream()
                .filter(property -> scope.equals(property.getScope()))
                .forEach(property -> applyProperty(target, template, groupType, property));
    }

    private void applyProperty(
            AtributsVisuals target,
            PlantillaEntity template,
            PaletteGroupType groupType,
            WidgetStylePropertyEntity property) {

        Field field = ReflectionUtils.findField(target.getClass(), property.getPropertyName());
        if (field == null) {
            return;
        }
        ReflectionUtils.makeAccessible(field);
        Object currentValue = ReflectionUtils.getField(field, target);
        if (currentValue != null) {
            return;
        }

        Object resolvedValue = WidgetStyleValueType.COLOR.equals(property.getValueType())
                ? resolveColor(template, groupType, property)
                : convertScalar(property.getScalarValue(), field.getType());
        if (resolvedValue != null) {
            ReflectionUtils.setField(field, target, resolvedValue);
        }
    }

    private String resolveColor(PlantillaEntity template, PaletteGroupType groupType, WidgetStylePropertyEntity property) {
        PlantillaGrupPaletesEntity group = findGroup(template, groupType);
        if (group == null || property.getPaletteRole() == null || property.getPaletteIndex() == null) {
            return null;
        }
        PaletaEntity palette = PaletteRole.CHART.equals(property.getPaletteRole())
                ? group.getChartPalette()
                : group.getWidgetPalette();
        if (palette == null || palette.getColors() == null || palette.getColors().size() <= property.getPaletteIndex()) {
            return null;
        }
        return palette.getColors().stream()
                .sorted(Comparator.comparing(color -> color.getPosicio() == null ? Integer.MAX_VALUE : color.getPosicio()))
                .skip(property.getPaletteIndex())
                .findFirst()
                .map(color -> color.getValor())
                .orElse(null);
    }

    private void applyChartPalette(AtributsVisualsGrafic target, PlantillaEntity template, PaletteGroupType groupType) {
        if (target.getColorsPaleta() != null) {
            return;
        }
        PlantillaGrupPaletesEntity group = findGroup(template, groupType);
        if (group == null || group.getChartPalette() == null || group.getChartPalette().getColors() == null) {
            return;
        }
        String colors = group.getChartPalette().getColors().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(color -> color.getPosicio() == null ? Integer.MAX_VALUE : color.getPosicio()))
                .map(color -> color.getValor())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
        if (!colors.isEmpty()) {
            target.setColorsPaleta(colors);
        }
    }

    private PlantillaGrupPaletesEntity findGroup(PlantillaEntity template, PaletteGroupType groupType) {
        if (template == null || template.getPaletteGroups() == null) {
            return null;
        }
        PaletteGroupType effectiveGroup = groupType != null ? groupType : PaletteGroupType.LIGHT;
        return template.getPaletteGroups().stream()
                .filter(group -> effectiveGroup.equals(group.getGroupType()))
                .findFirst()
                .orElse(null);
    }

    private WidgetStylePropertyEntity findProperty(PlantillaEntity template, WidgetStyleScope scope, String propertyName) {
        if (template == null || template.getStyleProperties() == null) {
            return null;
        }
        return template.getStyleProperties().stream()
                .filter(property -> scope.equals(property.getScope()))
                .filter(property -> propertyName.equals(property.getPropertyName()))
                .findFirst()
                .orElse(null);
    }

    private Object convertScalar(String value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (String.class.equals(targetType)) {
            return value;
        }
        if (Boolean.class.equals(targetType) || boolean.class.equals(targetType)) {
            return Boolean.valueOf(value);
        }
        if (Integer.class.equals(targetType) || int.class.equals(targetType)) {
            return Integer.valueOf(value);
        }
        if (Double.class.equals(targetType) || double.class.equals(targetType)) {
            return Double.valueOf(value);
        }
        return null;
    }
}
