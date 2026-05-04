package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.paleta.DashboardTemplatePaletteGroup;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletaColor;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteRole;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Plantilla;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleProperty;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleValueType;
import es.caib.comanda.estadistica.logic.intf.service.PlantillaService;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaGrupPaletesEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaColorEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.WidgetStylePropertyEntity;
import es.caib.comanda.estadistica.persist.repository.PaletaRepository;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlantillaServiceImpl extends BaseMutableResourceService<Plantilla, Long, PlantillaEntity> implements PlantillaService {

    private final PaletaRepository paletaRepository;

    @Override
    protected void beforeCreateSave(PlantillaEntity entity, Plantilla resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        try {
            syncTemplate(entity, resource);
        } catch (RuntimeException ex) {
            throw new ResourceNotCreatedException(resource.getClass(), ex.getMessage(), ex);
        }
    }

    @Override
    protected void beforeUpdateSave(PlantillaEntity entity, Plantilla resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        try {
            syncTemplate(entity, resource);
        } catch (RuntimeException ex) {
            throw new ResourceNotUpdatedException(resource.getClass(), String.valueOf(entity.getId()), ex.getMessage(), ex);
        }
    }

    @Override
    protected void afterConversion(PlantillaEntity entity, Plantilla resource) {
        List<Paleta> palettes = toPaletteResources(paletaRepository.findAllByOrderByNomAscIdAsc());
        resource.setPaletteGroups(toGroupResources(entity));
        resource.setStyleProperties(toStylePropertyResources(entity));

        if (palettes.isEmpty() && (resource.getPaletteGroups() == null || resource.getPaletteGroups().isEmpty())) {
            palettes = defaultPaletteResources(resource.getColors());
        }
        resource.setPaletes(palettes);
        if (resource.getPaletteGroups() == null || resource.getPaletteGroups().isEmpty()) {
            resource.setPaletteGroups(defaultGroups(palettes));
        }
        if (resource.getStyleProperties() == null || resource.getStyleProperties().isEmpty()) {
            resource.setStyleProperties(defaultStyleProperties());
        } else {
            resource.setStyleProperties(mergeDefaultStyleProperties(resource.getStyleProperties()));
        }
    }

    private void syncTemplate(PlantillaEntity entity, Plantilla resource) {
        List<Paleta> paletteResources = paletteResources(resource);
        Map<String, PaletaEntity> palettesByClientId = savePaletteResources(paletteResources);
        Map<Long, PaletaEntity> palettesById = palettesByClientId.values().stream()
                .filter(palette -> palette.getId() != null)
                .collect(Collectors.toMap(PaletaEntity::getId, palette -> palette, (left, right) -> left));

        List<DashboardTemplatePaletteGroup> groups = resource.getPaletteGroups();
        if (groups == null || groups.isEmpty()) {
            groups = defaultGroups(paletteResources);
        }
        syncGroups(entity, groups, palettesByClientId, palettesById);
        syncStyleProperties(entity, mergeDefaultStyleProperties(resource.getStyleProperties()));
        validateTemplate(entity);
    }

    private List<Paleta> paletteResources(Plantilla resource) {
        if (resource.getPaletes() != null && !resource.getPaletes().isEmpty()) {
            return resource.getPaletes();
        }
        return defaultPaletteResources(resource.getColors());
    }

    private Map<String, PaletaEntity> savePaletteResources(List<Paleta> paletteResources) {
        Map<String, PaletaEntity> result = new HashMap<>();
        for (Paleta palette : paletteResources) {
            PaletaEntity entity = resolvePaletteEntityForSave(palette);
            entity.setNom(palette.getNom());
            entity.setDescripcio(palette.getDescripcio());
            syncColors(entity, palette);
            PaletaEntity saved = paletaRepository.saveAndFlush(entity);
            String clientId = paletteClientId(palette);
            result.put(clientId, saved);
            result.put(String.valueOf(saved.getId()), saved);
        }
        return result;
    }

    private PaletaEntity resolvePaletteEntityForSave(Paleta palette) {
        if (palette.getId() != null) {
            return paletaRepository.findById(palette.getId()).orElseGet(PaletaEntity::new);
        }

        Long clientId = parseLong(palette.getClientId());
        if (clientId != null) {
            Optional<PaletaEntity> entity = paletaRepository.findById(clientId);
            if (entity.isPresent()) {
                return entity.get();
            }
        }

        if (palette.getNom() != null) {
            Optional<PaletaEntity> entity = paletaRepository.findByNom(palette.getNom());
            if (entity.isPresent()) {
                return entity.get();
            }
        }

        return new PaletaEntity();
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void syncColors(PaletaEntity entity, Paleta resource) {
        List<PaletaColorEntity> colors = entity.getColors();
        if (colors == null) {
            colors = new ArrayList<>();
            entity.setColors(colors);
        } else {
            colors.clear();
        }
        if (resource.getColors() == null) {
            return;
        }
        List<PaletaColorEntity> targetColors = colors;
        resource.getColors().stream()
                .filter(color -> color != null && color.getValor() != null)
                .sorted(Comparator.comparing(color -> color.getPosicio() == null ? Integer.MAX_VALUE : color.getPosicio()))
                .forEachOrdered(color -> {
                    PaletaColorEntity colorEntity = new PaletaColorEntity();
                    colorEntity.setPaleta(entity);
                    colorEntity.setPosicio(targetColors.size());
                    colorEntity.setValor(color.getValor());
                    targetColors.add(colorEntity);
                });
    }

    private void syncGroups(
            PlantillaEntity entity,
            List<DashboardTemplatePaletteGroup> groups,
            Map<String, PaletaEntity> palettesByClientId,
            Map<Long, PaletaEntity> palettesById) {

        List<PlantillaGrupPaletesEntity> groupEntities = entity.getPaletteGroups();
        if (groupEntities == null) {
            groupEntities = new ArrayList<>();
            entity.setPaletteGroups(groupEntities);
        } else {
            groupEntities.clear();
        }

        Map<PaletteGroupType, DashboardTemplatePaletteGroup> uniqueGroups = new EnumMap<>(PaletteGroupType.class);
        for (DashboardTemplatePaletteGroup group : groups) {
            if (group.getGroupType() == null) {
                throw new IllegalArgumentException("El grup de paletes ha d'indicar el tipus de grup");
            }
            if (uniqueGroups.put(group.getGroupType(), group) != null) {
                throw new IllegalArgumentException("La plantilla no pot tenir grups de paletes duplicats: " + group.getGroupType());
            }
        }

        for (PaletteGroupType groupType : PaletteGroupType.values()) {
            DashboardTemplatePaletteGroup group = uniqueGroups.get(groupType);
            if (group == null) {
                throw new IllegalArgumentException("Falta el grup de paletes " + groupType);
            }
            PlantillaGrupPaletesEntity groupEntity = new PlantillaGrupPaletesEntity();
            groupEntity.setPlantilla(entity);
            groupEntity.setGroupType(groupType);
            groupEntity.setWidgetPalette(resolvePalette(group.getWidgetPalette(), group.getWidgetPaletteClientId(), palettesByClientId, palettesById));
            groupEntity.setChartPalette(resolvePalette(group.getChartPalette(), group.getChartPaletteClientId(), palettesByClientId, palettesById));
            groupEntity.setOrdre(group.getOrdre() != null ? group.getOrdre() : groupEntities.size());
            groupEntities.add(groupEntity);
        }
    }

    private PaletaEntity resolvePalette(
            ResourceReference<Paleta, Long> reference,
            String clientId,
            Map<String, PaletaEntity> palettesByClientId,
            Map<Long, PaletaEntity> palettesById) {
        if (reference != null && reference.getId() != null) {
            PaletaEntity entity = palettesById.get(reference.getId());
            if (entity == null) {
                entity = paletaRepository.findById(reference.getId()).orElse(null);
            }
            if (entity != null) {
                return entity;
            }
        }
        if (clientId != null && palettesByClientId.containsKey(clientId)) {
            return palettesByClientId.get(clientId);
        }
        throw new IllegalArgumentException("Cada grup ha de tenir paleta de widget i paleta de grafic");
    }

    private void syncStyleProperties(PlantillaEntity entity, List<WidgetStyleProperty> properties) {
        List<WidgetStylePropertyEntity> propertyEntities = entity.getStyleProperties();
        if (propertyEntities == null) {
            propertyEntities = new ArrayList<>();
            entity.setStyleProperties(propertyEntities);
        } else {
            propertyEntities.clear();
        }

        for (int index = 0; index < properties.size(); index++) {
            WidgetStyleProperty property = properties.get(index);
            WidgetStylePropertyEntity propertyEntity = new WidgetStylePropertyEntity();
            propertyEntity.setPlantilla(entity);
            propertyEntity.setScope(property.getScope());
            propertyEntity.setPropertyName(property.getPropertyName());
            propertyEntity.setLabel(property.getLabel());
            propertyEntity.setValueType(property.getValueType());
            propertyEntity.setPaletteRole(property.getPaletteRole());
            propertyEntity.setPaletteIndex(property.getPaletteIndex());
            propertyEntity.setScalarValue(property.getScalarValue());
            propertyEntity.setDefaultProperty(Boolean.TRUE.equals(property.getDefaultProperty()));
            propertyEntity.setOrdre(property.getOrdre() != null ? property.getOrdre() : index);
            propertyEntities.add(propertyEntity);
        }
    }

    private void validateTemplate(PlantillaEntity entity) {
        if (entity.getPaletteGroups() == null || entity.getPaletteGroups().size() != PaletteGroupType.values().length) {
            throw new IllegalArgumentException("Cada plantilla ha de tenir exactament quatre grups de paletes");
        }
        for (PlantillaGrupPaletesEntity group : entity.getPaletteGroups()) {
            if (group.getWidgetPalette() == null || group.getChartPalette() == null) {
                throw new IllegalArgumentException("Cada grup de paletes ha de tenir paleta de widget i paleta de grafic");
            }
            if (group.getWidgetPalette().getColors() == null || group.getWidgetPalette().getColors().isEmpty()
                    || group.getChartPalette().getColors() == null || group.getChartPalette().getColors().isEmpty()) {
                throw new IllegalArgumentException("Les paletes assignades no poden estar buides");
            }
        }
        if (entity.getStyleProperties() == null) {
            return;
        }
        for (WidgetStylePropertyEntity property : entity.getStyleProperties()) {
            if (!WidgetStyleValueType.COLOR.equals(property.getValueType())) {
                continue;
            }
            if (property.getPaletteRole() == null || property.getPaletteIndex() == null || property.getPaletteIndex() < 0) {
                throw new IllegalArgumentException("La propietat de color " + property.getPropertyName() + " ha d'apuntar a una posicio de paleta");
            }
            for (PlantillaGrupPaletesEntity group : entity.getPaletteGroups()) {
                PaletaEntity palette = PaletteRole.CHART.equals(property.getPaletteRole()) ? group.getChartPalette() : group.getWidgetPalette();
                if (palette.getColors() == null || property.getPaletteIndex() >= palette.getColors().size()) {
                    throw new IllegalArgumentException("La propietat " + property.getPropertyName()
                            + " apunta a una posicio inexistent de la paleta " + palette.getNom());
                }
            }
        }
    }

    private List<Paleta> defaultPaletteResources(Map<String, String> legacyColors) {
        List<Paleta> palettes = new ArrayList<>();
        palettes.add(defaultPalette("light-widget", "Tema clar - widget", legacyWidgetColors(legacyColors, "clar", "#ffffff", "#1f2937")));
        palettes.add(defaultPalette("light-chart", "Tema clar - grafic", legacyChartColors(legacyColors, "clar")));
        palettes.add(defaultPalette("light-highlighted-widget", "Tema clar destacat - widget", legacyWidgetColors(legacyColors, "dest", "#ef6c00", "#ffffff")));
        palettes.add(defaultPalette("light-highlighted-chart", "Tema clar destacat - grafic", legacyChartColors(legacyColors, "dest")));
        palettes.add(defaultPalette("dark-widget", "Tema fosc - widget", legacyWidgetColors(legacyColors, "obs", "#2f343d", "#ffffff")));
        palettes.add(defaultPalette("dark-chart", "Tema fosc - grafic", legacyChartColors(legacyColors, "obs")));
        palettes.add(defaultPalette("dark-highlighted-widget", "Tema fosc destacat - widget", legacyWidgetColors(legacyColors, "obs_dest", "#111827", "#ffffff")));
        palettes.add(defaultPalette("dark-highlighted-chart", "Tema fosc destacat - grafic", legacyChartColors(legacyColors, "obs_dest")));
        for (int i = 0; i < palettes.size(); i++) {
            palettes.get(i).setOrdre(i);
        }
        return palettes;
    }

    private Paleta defaultPalette(String clientId, String name, List<String> colors) {
        Paleta palette = new Paleta();
        palette.setClientId(clientId);
        palette.setNom(name);
        palette.setColors(new ArrayList<>());
        for (int i = 0; i < colors.size(); i++) {
            PaletaColor color = new PaletaColor();
            color.setPosicio(i);
            color.setValor(colors.get(i));
            palette.getColors().add(color);
        }
        return palette;
    }

    private List<String> legacyWidgetColors(Map<String, String> colors, String suffix, String defaultBackground, String defaultText) {
        List<String> defaults = Arrays.asList(defaultBackground, defaultText, "#d1d5db", "#2563eb", "#16a34a", "#f3f4f6", "#e5e7eb", "#9ca3af");
        if (colors == null || colors.isEmpty()) {
            return defaults;
        }
        List<String> ordered = new ArrayList<>();
        addLegacyColor(ordered, colors.get("colorFons_" + suffix));
        addLegacyColor(ordered, colors.get("colorText_" + suffix));
        addLegacyColor(ordered, colors.get("colorVora_" + suffix));
        addLegacyColor(ordered, colors.get("colorIcona_" + suffix));
        addLegacyColor(ordered, colors.get("colorTextDestacat_" + suffix));
        addLegacyColor(ordered, colors.get("colorFonsIcona_" + suffix));
        addLegacyColor(ordered, colors.get("colorFonsTaula_" + suffix));
        addLegacyColor(ordered, colors.get("colorTextTaula_" + suffix));
        defaults.forEach(color -> addLegacyColor(ordered, color));
        return ordered;
    }

    private List<String> legacyChartColors(Map<String, String> colors, String suffix) {
        List<String> defaults = Arrays.asList("#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b");
        if (colors != null) {
            String palette = colors.get("colorsPaleta_" + suffix);
            if (palette != null && !palette.trim().isEmpty()) {
                List<String> result = Arrays.stream(palette.split(","))
                        .map(String::trim)
                        .filter(value -> !value.isEmpty())
                        .collect(Collectors.toList());
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return defaults;
    }

    private void addLegacyColor(List<String> colors, String color) {
        if (color != null && !color.trim().isEmpty() && !colors.contains(color)) {
            colors.add(color);
        }
    }

    private List<DashboardTemplatePaletteGroup> defaultGroups(List<Paleta> palettes) {
        Map<String, Paleta> byClientId = palettes.stream().collect(Collectors.toMap(this::paletteClientId, palette -> palette, (left, right) -> left));
        List<DashboardTemplatePaletteGroup> groups = new ArrayList<>();
        groups.add(defaultGroup(PaletteGroupType.LIGHT, paletteByKeyOrIndex(byClientId, palettes, "light-widget", 0), paletteByKeyOrIndex(byClientId, palettes, "light-chart", 1), 0));
        groups.add(defaultGroup(PaletteGroupType.LIGHT_HIGHLIGHTED, paletteByKeyOrIndex(byClientId, palettes, "light-highlighted-widget", 2), paletteByKeyOrIndex(byClientId, palettes, "light-highlighted-chart", 3), 1));
        groups.add(defaultGroup(PaletteGroupType.DARK, paletteByKeyOrIndex(byClientId, palettes, "dark-widget", 4), paletteByKeyOrIndex(byClientId, palettes, "dark-chart", 5), 2));
        groups.add(defaultGroup(PaletteGroupType.DARK_HIGHLIGHTED, paletteByKeyOrIndex(byClientId, palettes, "dark-highlighted-widget", 6), paletteByKeyOrIndex(byClientId, palettes, "dark-highlighted-chart", 7), 3));
        return groups;
    }

    private Paleta paletteByKeyOrIndex(Map<String, Paleta> byClientId, List<Paleta> palettes, String key, int index) {
        Paleta palette = byClientId.get(key);
        if (palette != null) {
            return palette;
        }
        if (palettes != null && palettes.size() > index) {
            return palettes.get(index);
        }
        return palettes != null && !palettes.isEmpty() ? palettes.get(0) : null;
    }

    private DashboardTemplatePaletteGroup defaultGroup(PaletteGroupType type, Paleta widgetPalette, Paleta chartPalette, int order) {
        DashboardTemplatePaletteGroup group = new DashboardTemplatePaletteGroup();
        group.setGroupType(type);
        group.setWidgetPaletteClientId(paletteClientId(widgetPalette));
        group.setChartPaletteClientId(paletteClientId(chartPalette));
        group.setOrdre(order);
        return group;
    }

    private List<WidgetStyleProperty> mergeDefaultStyleProperties(List<WidgetStyleProperty> provided) {
        Map<String, WidgetStyleProperty> result = new LinkedHashMap<>();
        for (WidgetStyleProperty property : defaultStyleProperties()) {
            result.put(propertyKey(property), property);
        }
        if (provided != null) {
            for (WidgetStyleProperty property : provided) {
                if (property != null && property.getScope() != null && property.getPropertyName() != null) {
                    if (!isConfigurableStyleProperty(property)) {
                        continue;
                    }
                    WidgetStyleProperty base = result.get(propertyKey(property));
                    result.put(propertyKey(property), mergeProperty(base, property));
                }
            }
        }
        return new ArrayList<>(result.values());
    }

    private boolean isConfigurableStyleProperty(WidgetStyleProperty property) {
        return !(WidgetStyleScope.SIMPLE.equals(property.getScope()) && "icona".equals(property.getPropertyName()));
    }

    private WidgetStyleProperty mergeProperty(WidgetStyleProperty base, WidgetStyleProperty override) {
        if (base == null) {
            return override;
        }
        WidgetStyleProperty property = new WidgetStyleProperty();
        property.setId(override.getId());
        property.setScope(override.getScope());
        property.setPropertyName(override.getPropertyName());
        property.setLabel(override.getLabel() != null ? override.getLabel() : base.getLabel());
        property.setValueType(override.getValueType() != null ? override.getValueType() : base.getValueType());
        property.setPaletteRole(override.getPaletteRole() != null ? override.getPaletteRole() : base.getPaletteRole());
        property.setPaletteIndex(override.getPaletteIndex() != null ? override.getPaletteIndex() : base.getPaletteIndex());
        property.setScalarValue(override.getScalarValue() != null ? override.getScalarValue() : base.getScalarValue());
        property.setDefaultProperty(Boolean.TRUE.equals(base.getDefaultProperty()) && override.getId() == null);
        property.setOrdre(override.getOrdre() != null ? override.getOrdre() : base.getOrdre());
        return property;
    }

    private String propertyKey(WidgetStyleProperty property) {
        return property.getScope() + ":" + property.getPropertyName();
    }

    private List<WidgetStyleProperty> defaultStyleProperties() {
        List<WidgetStyleProperty> properties = new ArrayList<>();
        addColor(properties, WidgetStyleScope.COMMON, "colorFons", "Fons", PaletteRole.WIDGET, 0);
        addColor(properties, WidgetStyleScope.COMMON, "colorText", "Text", PaletteRole.WIDGET, 1);
        addColor(properties, WidgetStyleScope.COMMON, "colorVora", "Vora", PaletteRole.WIDGET, 2);
        addScalar(properties, WidgetStyleScope.COMMON, "mostrarVora", "Mostrar vora", WidgetStyleValueType.BOOLEAN, "false");
        addScalar(properties, WidgetStyleScope.COMMON, "ampleVora", "Amplada vora", WidgetStyleValueType.NUMBER, "1");
        addScalar(properties, WidgetStyleScope.COMMON, "midaFontTitol", "Mida titol", WidgetStyleValueType.NUMBER, "22");
        addScalar(properties, WidgetStyleScope.COMMON, "midaFontDescripcio", "Mida descripcio", WidgetStyleValueType.NUMBER, "14");

        addColor(properties, WidgetStyleScope.SIMPLE, "colorIcona", "Color icona", PaletteRole.WIDGET, 3);
        addColor(properties, WidgetStyleScope.SIMPLE, "colorFonsIcona", "Fons icona", PaletteRole.WIDGET, 5);
        addColor(properties, WidgetStyleScope.SIMPLE, "colorTextDestacat", "Text destacat", PaletteRole.WIDGET, 4);
        addScalar(properties, WidgetStyleScope.SIMPLE, "midaFontValor", "Mida valor", WidgetStyleValueType.NUMBER, "48");
        addScalar(properties, WidgetStyleScope.SIMPLE, "midaFontUnitats", "Mida unitats", WidgetStyleValueType.NUMBER, "16");
        addScalar(properties, WidgetStyleScope.SIMPLE, "midaFontCanviPercentual", "Mida canvi", WidgetStyleValueType.NUMBER, "18");

        for (int i = 0; i < 6; i++) {
            addColor(properties, WidgetStyleScope.GRAFIC, "chartSerieColor" + (i + 1), "Serie " + (i + 1), PaletteRole.CHART, i);
        }
        addScalar(properties, WidgetStyleScope.GRAFIC, "mostrarReticula", "Mostrar reticula", WidgetStyleValueType.BOOLEAN, "false");
        addScalar(properties, WidgetStyleScope.GRAFIC, "barStacked", "Barres apilades", WidgetStyleValueType.BOOLEAN, "false");
        addScalar(properties, WidgetStyleScope.GRAFIC, "barHorizontal", "Barres horitzontals", WidgetStyleValueType.BOOLEAN, "false");
        addScalar(properties, WidgetStyleScope.GRAFIC, "lineShowPoints", "Mostrar punts", WidgetStyleValueType.BOOLEAN, "true");
        addScalar(properties, WidgetStyleScope.GRAFIC, "area", "Area", WidgetStyleValueType.BOOLEAN, "false");
        addScalar(properties, WidgetStyleScope.GRAFIC, "lineSmooth", "Linia suau", WidgetStyleValueType.BOOLEAN, "false");
        addScalar(properties, WidgetStyleScope.GRAFIC, "lineWidth", "Amplada linia", WidgetStyleValueType.NUMBER, "2");
        addScalar(properties, WidgetStyleScope.GRAFIC, "outerRadius", "Radi exterior", WidgetStyleValueType.NUMBER, "100");
        addScalar(properties, WidgetStyleScope.GRAFIC, "pieDonut", "Donut", WidgetStyleValueType.BOOLEAN, "false");
        addScalar(properties, WidgetStyleScope.GRAFIC, "innerRadius", "Radi interior", WidgetStyleValueType.NUMBER, "40");
        addScalar(properties, WidgetStyleScope.GRAFIC, "pieShowLabels", "Mostrar etiquetes", WidgetStyleValueType.BOOLEAN, "true");
        addScalar(properties, WidgetStyleScope.GRAFIC, "labelSize", "Mida etiqueta", WidgetStyleValueType.NUMBER, "12");
        addScalar(properties, WidgetStyleScope.GRAFIC, "gaugeMin", "Gauge minim", WidgetStyleValueType.NUMBER, "0");
        addScalar(properties, WidgetStyleScope.GRAFIC, "gaugeMax", "Gauge maxim", WidgetStyleValueType.NUMBER, "100");
        addScalar(properties, WidgetStyleScope.GRAFIC, "gaugeRangs", "Rangs gauge", WidgetStyleValueType.TEXT, "50,75,100");
        addScalar(properties, WidgetStyleScope.GRAFIC, "heatmapMinValue", "Heatmap minim", WidgetStyleValueType.NUMBER, "0");
        addScalar(properties, WidgetStyleScope.GRAFIC, "heatmapMaxValue", "Heatmap maxim", WidgetStyleValueType.NUMBER, "100");

        addColor(properties, WidgetStyleScope.TAULA, "colorTextTaula", "Text taula", PaletteRole.WIDGET, 1);
        addColor(properties, WidgetStyleScope.TAULA, "colorFonsTaula", "Fons taula", PaletteRole.WIDGET, 0);
        addScalar(properties, WidgetStyleScope.TAULA, "mostrarCapcalera", "Mostrar capcalera", WidgetStyleValueType.BOOLEAN, "true");
        addColor(properties, WidgetStyleScope.TAULA, "colorCapcalera", "Text capcalera", PaletteRole.WIDGET, 1);
        addColor(properties, WidgetStyleScope.TAULA, "colorFonsCapcalera", "Fons capcalera", PaletteRole.WIDGET, 5);
        addScalar(properties, WidgetStyleScope.TAULA, "mostrarAlternancia", "Alternancia", WidgetStyleValueType.BOOLEAN, "true");
        addColor(properties, WidgetStyleScope.TAULA, "colorAlternancia", "Color altern", PaletteRole.WIDGET, 6);
        addScalar(properties, WidgetStyleScope.TAULA, "mostrarVoraTaula", "Mostrar vora taula", WidgetStyleValueType.BOOLEAN, "false");
        addColor(properties, WidgetStyleScope.TAULA, "colorVoraTaula", "Vora taula", PaletteRole.WIDGET, 2);
        addScalar(properties, WidgetStyleScope.TAULA, "ampleVoraTaula", "Amplada vora taula", WidgetStyleValueType.NUMBER, "1");
        addScalar(properties, WidgetStyleScope.TAULA, "mostrarSeparadorHoritzontal", "Separador horitzontal", WidgetStyleValueType.BOOLEAN, "true");
        addColor(properties, WidgetStyleScope.TAULA, "colorSeparadorHoritzontal", "Color separador horitzontal", PaletteRole.WIDGET, 2);
        addScalar(properties, WidgetStyleScope.TAULA, "ampleSeparadorHoritzontal", "Amplada separador horitzontal", WidgetStyleValueType.NUMBER, "1");
        addScalar(properties, WidgetStyleScope.TAULA, "mostrarSeparadorVertical", "Separador vertical", WidgetStyleValueType.BOOLEAN, "false");
        addColor(properties, WidgetStyleScope.TAULA, "colorSeparadorVertical", "Color separador vertical", PaletteRole.WIDGET, 2);
        addScalar(properties, WidgetStyleScope.TAULA, "ampleSeparadorVertical", "Amplada separador vertical", WidgetStyleValueType.NUMBER, "1");

        addTitleStyleProperties(properties, WidgetStyleScope.TITOL_1, "Titol 1", 28, 3);
        addTitleStyleProperties(properties, WidgetStyleScope.TITOL_2, "Titol 2", 22, 2);
        addTitleStyleProperties(properties, WidgetStyleScope.TITOL_3, "Titol 3", 18, 1);
        return properties;
    }

    private void addTitleStyleProperties(List<WidgetStyleProperty> properties, WidgetStyleScope scope, String prefix, int fontSize, int underlineWidth) {
        addColor(properties, scope, "colorFons", prefix + " fons", PaletteRole.WIDGET, 0);
        addColor(properties, scope, "colorTitol", prefix + " text", PaletteRole.WIDGET, 1);
        addScalar(properties, scope, "midaFontTitol", prefix + " mida font", WidgetStyleValueType.NUMBER, String.valueOf(fontSize));
        addScalar(properties, scope, "mostrarVora", prefix + " subratllat", WidgetStyleValueType.BOOLEAN, "true");
        addColor(properties, scope, "colorVora", prefix + " color subratllat", PaletteRole.WIDGET, 2);
        addScalar(properties, scope, "ampleVora", prefix + " gruix subratllat", WidgetStyleValueType.NUMBER, String.valueOf(underlineWidth));
    }

    private void addColor(List<WidgetStyleProperty> properties, WidgetStyleScope scope, String name, String label, PaletteRole role, int index) {
        WidgetStyleProperty property = baseProperty(properties, scope, name, label, WidgetStyleValueType.COLOR);
        property.setPaletteRole(role);
        property.setPaletteIndex(index);
    }

    private void addScalar(List<WidgetStyleProperty> properties, WidgetStyleScope scope, String name, String label, WidgetStyleValueType type, String value) {
        WidgetStyleProperty property = baseProperty(properties, scope, name, label, type);
        property.setScalarValue(value);
    }

    private WidgetStyleProperty baseProperty(List<WidgetStyleProperty> properties, WidgetStyleScope scope, String name, String label, WidgetStyleValueType type) {
        WidgetStyleProperty property = new WidgetStyleProperty();
        property.setScope(scope);
        property.setPropertyName(name);
        property.setLabel(label);
        property.setValueType(type);
        property.setDefaultProperty(true);
        property.setOrdre(properties.size());
        properties.add(property);
        return property;
    }

    private List<Paleta> toPaletteResources(List<PaletaEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toPaletteResource).collect(Collectors.toList());
    }

    private Paleta toPaletteResource(PaletaEntity entity) {
        Paleta resource = new Paleta();
        resource.setId(entity.getId());
        resource.setClientId(String.valueOf(entity.getId()));
        resource.setNom(entity.getNom());
        resource.setDescripcio(entity.getDescripcio());
        List<PaletaColor> colors = new ArrayList<>();
        if (entity.getColors() != null) {
            entity.getColors().stream()
                    .sorted(Comparator.comparing(color -> color.getPosicio() == null ? Integer.MAX_VALUE : color.getPosicio()))
                    .forEach(color -> {
                        PaletaColor colorResource = new PaletaColor();
                        colorResource.setId(color.getId());
                        colorResource.setPaleta(ResourceReference.toResourceReference(entity.getId(), entity.getNom()));
                        colorResource.setPosicio(color.getPosicio());
                        colorResource.setValor(color.getValor());
                        colors.add(colorResource);
                    });
        }
        resource.setColors(colors);
        return resource;
    }

    private List<DashboardTemplatePaletteGroup> toGroupResources(PlantillaEntity entity) {
        if (entity.getPaletteGroups() == null) {
            return Collections.emptyList();
        }
        return entity.getPaletteGroups().stream()
                .sorted(Comparator.comparing(group -> group.getOrdre() == null ? Integer.MAX_VALUE : group.getOrdre()))
                .map(group -> {
                    DashboardTemplatePaletteGroup resource = new DashboardTemplatePaletteGroup();
                    resource.setId(group.getId());
                    resource.setGroupType(group.getGroupType());
                    resource.setWidgetPalette(ResourceReference.toResourceReference(group.getWidgetPalette().getId(), group.getWidgetPalette().getNom()));
                    resource.setChartPalette(ResourceReference.toResourceReference(group.getChartPalette().getId(), group.getChartPalette().getNom()));
                    resource.setWidgetPaletteClientId(String.valueOf(group.getWidgetPalette().getId()));
                    resource.setChartPaletteClientId(String.valueOf(group.getChartPalette().getId()));
                    resource.setOrdre(group.getOrdre());
                    return resource;
                }).collect(Collectors.toList());
    }

    private List<WidgetStyleProperty> toStylePropertyResources(PlantillaEntity entity) {
        if (entity.getStyleProperties() == null) {
            return Collections.emptyList();
        }
        return entity.getStyleProperties().stream()
                .sorted(Comparator.comparing(property -> property.getOrdre() == null ? Integer.MAX_VALUE : property.getOrdre()))
                .map(property -> {
                    WidgetStyleProperty resource = new WidgetStyleProperty();
                    resource.setId(property.getId());
                    resource.setScope(property.getScope());
                    resource.setPropertyName(property.getPropertyName());
                    resource.setLabel(property.getLabel());
                    resource.setValueType(property.getValueType());
                    resource.setPaletteRole(property.getPaletteRole());
                    resource.setPaletteIndex(property.getPaletteIndex());
                    resource.setScalarValue(property.getScalarValue());
                    resource.setDefaultProperty(property.getDefaultProperty());
                    resource.setOrdre(property.getOrdre());
                    return resource;
                }).collect(Collectors.toList());
    }

    private String paletteClientId(Paleta palette) {
        if (palette == null) {
            return null;
        }
        if (palette.getClientId() != null) {
            return palette.getClientId();
        }
        if (palette.getId() != null) {
            return String.valueOf(palette.getId());
        }
        return palette.getNom();
    }
}
