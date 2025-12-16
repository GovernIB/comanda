package es.caib.comanda.ms.estadistica.helper;

import es.caib.comanda.model.v1.estadistica.Dimensio;
import es.caib.comanda.model.v1.estadistica.Fet;
import es.caib.comanda.model.v1.estadistica.GenericDimensio;
import es.caib.comanda.model.v1.estadistica.GenericFet;
import es.caib.comanda.model.v1.estadistica.RegistreEstadistic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utilitats per mapar entitats en estrella (star-schema) cap a models d'estadística consumibles per COMANDA.
 *
 * Proporciona un mètode genèric per convertir qualsevol entitat de domini (provinent de diferents APPs
 * i amb esquemes diferents) a un {@link RegistreEstadistic}, a partir de funcions extractores de
 * dimensions (codi→valor) i fets (codi→valor numèric).
 */
public final class EstadisticaHelper {

    private EstadisticaHelper() {}

    /**
     * Construeix un {@link RegistreEstadistic} generant les dimensions i fets a partir d'un objecte origen.
     *
     * @param entity           entitat d'origen (de qualsevol APP)
     * @param dimensionsMapper funció que extreu un {@code Map<codi, valor>} de dimensions de l'entitat
     * @param fetsMapper       funció que extreu un {@code Map<codi, valor>} numèric dels fets/indicadors
     * @param <E>              tipus de l'entitat d'origen
     * @return {@link RegistreEstadistic} amb les dimensions i fets corresponents
     */
    public static <E> RegistreEstadistic toRegistreEstadistic(
            E entity,
            Function<E, Map<String, String>> dimensionsMapper,
            Function<E, Map<String, ? extends Number>> fetsMapper) {

        Map<String, String> dimMap = safeLinkedMap(dimensionsMapper != null ? dimensionsMapper.apply(entity) : null);
        Map<String, ? extends Number> fetsMap = safeLinkedMap(fetsMapper != null ? fetsMapper.apply(entity) : null);

        List<Dimensio> dimensions = dimMap.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isEmpty())
                .map(e -> GenericDimensio.builder().codi(e.getKey()).valor(e.getValue()).build())
                .collect(Collectors.toList());

        List<Fet> fets = fetsMap.entrySet().stream()
                .filter(e -> e.getKey() != null && !e.getKey().isEmpty())
                .filter(e -> e.getValue() != null)
                .map(e -> GenericFet.builder().codi(e.getKey()).valor(e.getValue().doubleValue()).build())
                .collect(Collectors.toList());

        return RegistreEstadistic.builder()
                .dimensions(dimensions)
                .fets(fets)
                .build();
    }

    /**
     * Construeix una llista de {@link RegistreEstadistic} generant les dimensions i fets a partir d'un objecte origen.
     *
     * @param entities         llistat d'entitats d'origen (de qualsevol APP)
     * @param dimensionsMapper funció que extreu un {@code Map<codi, valor>} de dimensions de l'entitat
     * @param fetsMapper       funció que extreu un {@code Map<codi, valor>} numèric dels fets/indicadors
     * @param <E>              tipus de l'entitat d'origen
     * @return Llista de {@link RegistreEstadistic} amb les dimensions i fets corresponents
     */
    public static <E> List<RegistreEstadistic> toRegistreEstadistic(
            List<E> entities,
            Function<E, Map<String, String>> dimensionsMapper,
            Function<E, Map<String, ? extends Number>> fetsMapper) {
        return entities.stream().map(e -> toRegistreEstadistic(e, dimensionsMapper, fetsMapper)).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Construeix un {@link RegistreEstadistic} a partir de col·leccions ja preparades de dimensions i fets.
     */
    public static RegistreEstadistic toRegistreEstadistic(Collection<Dimensio> dimensions, Collection<Fet> fets) {
        List<Dimensio> dims = dimensions != null ? new ArrayList<>(dimensions) : new ArrayList<>();
        List<Fet> fs = fets != null ? new ArrayList<>(fets) : new ArrayList<>();
        // Neteja elements nuls
        dims.removeIf(Objects::isNull);
        fs.removeIf(Objects::isNull);
        return RegistreEstadistic.builder()
                .dimensions(dims)
                .fets(fs)
                .build();
    }

    /**
     * Construeix un {@link RegistreEstadistic} a partir de mapes de dimensions i fets.
     */
    public static RegistreEstadistic toRegistreEstadistic(Map<String, String> dimensions,
                                                          Map<String, ? extends Number> fets) {
        return toRegistreEstadistic(nullEntity(), e -> dimensions, e -> fets);
    }

    // Helpers
    private static <K, V> Map<K, V> safeLinkedMap(Map<K, V> src) {
        if (src == null || src.isEmpty()) return new LinkedHashMap<>();
        // Preservar l'ordre d'inserció si el Map d'origen no ho garanteix
        LinkedHashMap<K, V> out = new LinkedHashMap<>();
        src.forEach((k, v) -> out.put(k, v));
        return out;
    }

    private static Object nullEntity() { return null; }
}
