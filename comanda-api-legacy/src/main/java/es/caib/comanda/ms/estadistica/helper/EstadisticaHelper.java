package es.caib.comanda.ms.estadistica.helper;

import es.caib.comanda.ms.estadistica.model.Dimensio;
import es.caib.comanda.ms.estadistica.model.Fet;
import es.caib.comanda.ms.estadistica.model.RegistreEstadistic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EstadisticaHelper {

    private EstadisticaHelper() {
    }

    public interface DimensionsMapper<E> {
        Map<String, String> map(E entity);
    }

    public interface FetsMapper<E> {
        Map<String, ? extends Number> map(E entity);
    }

    public static <E> RegistreEstadistic toRegistreEstadistic(E entity,
                                                              DimensionsMapper<E> dimensionsMapper,
                                                              FetsMapper<E> fetsMapper) {
        Map<String, String> dimMap = safeLinkedMap(dimensionsMapper != null ? dimensionsMapper.map(entity) : null);
        Map<String, ? extends Number> fetsMap = safeLinkedMap(fetsMapper != null ? fetsMapper.map(entity) : null);

        List<Dimensio> dimensions = new ArrayList<Dimensio>();
        for (Map.Entry<String, String> entry : dimMap.entrySet()) {
            if (isValidCode(entry.getKey())) {
                dimensions.add(new Dimensio().setCodi(entry.getKey()).setValor(entry.getValue()));
            }
        }

        List<Fet> fets = new ArrayList<Fet>();
        for (Map.Entry<String, ? extends Number> entry : fetsMap.entrySet()) {
            if (isValidCode(entry.getKey()) && entry.getValue() != null) {
                fets.add(new Fet().setCodi(entry.getKey()).setValor(Double.valueOf(entry.getValue().doubleValue())));
            }
        }

        return new RegistreEstadistic().setDimensions(dimensions).setFets(fets);
    }

    public static <E> List<RegistreEstadistic> toRegistreEstadistic(List<E> entities,
                                                                     DimensionsMapper<E> dimensionsMapper,
                                                                     FetsMapper<E> fetsMapper) {
        List<RegistreEstadistic> result = new ArrayList<RegistreEstadistic>();
        if (entities == null) {
            return result;
        }
        for (E entity : entities) {
            result.add(toRegistreEstadistic(entity, dimensionsMapper, fetsMapper));
        }
        return result;
    }

    public static RegistreEstadistic toRegistreEstadistic(Collection<Dimensio> dimensions, Collection<Fet> fets) {
        List<Dimensio> dims = new ArrayList<Dimensio>();
        List<Fet> fs = new ArrayList<Fet>();
        if (dimensions != null) {
            for (Dimensio dimensio : dimensions) {
                if (dimensio != null) {
                    dims.add(dimensio);
                }
            }
        }
        if (fets != null) {
            for (Fet fet : fets) {
                if (fet != null) {
                    fs.add(fet);
                }
            }
        }
        return new RegistreEstadistic().setDimensions(dims).setFets(fs);
    }

    public static RegistreEstadistic toRegistreEstadistic(final Map<String, String> dimensions,
                                                          final Map<String, ? extends Number> fets) {
        return toRegistreEstadistic(new Object(),
                new DimensionsMapper<Object>() {
                    @Override
                    public Map<String, String> map(Object entity) {
                        return dimensions;
                    }
                },
                new FetsMapper<Object>() {
                    @Override
                    public Map<String, ? extends Number> map(Object entity) {
                        return fets;
                    }
                });
    }

    private static boolean isValidCode(String code) {
        return code != null && code.length() > 0;
    }

    private static <K, V> Map<K, V> safeLinkedMap(Map<K, V> source) {
        LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
        if (source != null) {
            result.putAll(source);
        }
        return result;
    }
}
