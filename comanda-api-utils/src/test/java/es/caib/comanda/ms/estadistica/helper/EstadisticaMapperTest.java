package es.caib.comanda.ms.estadistica.helper;

import es.caib.comanda.model.server.monitoring.Dimensio;
import es.caib.comanda.model.server.monitoring.Fet;
import es.caib.comanda.model.server.monitoring.RegistreEstadistic;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class EstadisticaMapperTest {

    private static class DummyEntity {
        String app; String canal; String tipus; Integer visites; Long errors; Double temps;
        DummyEntity(String app, String canal, String tipus, Integer visites, Long errors, Double temps) {
            this.app = app; this.canal = canal; this.tipus = tipus; this.visites = visites; this.errors = errors; this.temps = temps;
        }
    }

    @Test
    void toRegistreEstadistic_fromEntity_functions_filtersAndOrders() {
        DummyEntity e = new DummyEntity("APP", "WEB", "CONSULTA", 10, 2L, 3.5);

        Function<DummyEntity, Map<String, String>> dims = ent -> {
            LinkedHashMap<String, String> m = new LinkedHashMap<>();
            m.put("aplicacio", ent.app);
            m.put("", "IGNORED_EMPTY_KEY"); // s'ha d'ignorar per clau buida
            m.put("canal", ent.canal);
            m.put("tipus", ent.tipus);
            m.put("nullable", null); // permès (dimensions poden tenir valor null)
            return m;
        };
        Function<DummyEntity, Map<String, ? extends Number>> fets = ent -> {
            LinkedHashMap<String, Number> m = new LinkedHashMap<>();
            m.put("visites", ent.visites);
            m.put("errors", ent.errors);
            m.put("temps", ent.temps);
            m.put("", 99); // s'ha d'ignorar (clau buida)
            m.put("nul", null); // s'ha d'ignorar (valor nul)
            return m;
        };

        RegistreEstadistic re = EstadisticaHelper.toRegistreEstadistic(e, dims, fets);

        assertThat(re).isNotNull();
        assertThat(re.getDimensions()).extracting(Dimensio::getCodi)
                .containsExactly("aplicacio", "canal", "tipus", "nullable");
        assertThat(re.getDimensions()).extracting(Dimensio::getValor)
                .containsExactly("APP", "WEB", "CONSULTA", null);

        assertThat(re.getFets()).extracting(Fet::getCodi)
                .containsExactly("visites", "errors", "temps");
        assertThat(re.getFets()).extracting(Fet::getValor)
                .containsExactly(10.0, 2.0, 3.5);
    }

    @Test
    void toRegistreEstadistic_fromMaps_preservesInsertionOrder_andConvertsNumbers() {
        LinkedHashMap<String, String> dims = new LinkedHashMap<>();
        dims.put("a", "1");
        dims.put("b", "2");

        LinkedHashMap<String, Number> fets = new LinkedHashMap<>();
        fets.put("i", 1); // Integer
        fets.put("l", 2L); // Long
        fets.put("bd", new BigDecimal("3.25")); // BigDecimal → double

        RegistreEstadistic re = EstadisticaHelper.toRegistreEstadistic(dims, fets);
        assertThat(re.getDimensions()).extracting(Dimensio::getCodi).containsExactly("a", "b");
        assertThat(re.getDimensions()).extracting(Dimensio::getValor).containsExactly("1", "2");
        assertThat(re.getFets()).extracting(Fet::getCodi).containsExactly("i", "l", "bd");
        assertThat(re.getFets()).extracting(Fet::getValor).containsExactly(1.0, 2.0, 3.25);
    }

    @Test
    void toRegistreEstadistic_fromCollections_handlesNullCollectionsAndElements() {
        Dimensio d = new Dimensio();
        d.setCodi("x");
        d.setValor("X");
        List<Dimensio> dims = new ArrayList<>(Arrays.asList(d, null));

        Fet f = new Fet();
        f.setCodi("y");
        f.setValor(7.0);
        List<Fet> fets = new ArrayList<>(Arrays.asList(f));

        RegistreEstadistic re1 = EstadisticaHelper.toRegistreEstadistic(dims, fets);
        assertThat(re1.getDimensions()).hasSize(1);
        assertThat(re1.getDimensions().get(0).getCodi()).isEqualTo("x");
        assertThat(re1.getFets()).hasSize(1);
        assertThat(re1.getFets().get(0).getCodi()).isEqualTo("y");

        RegistreEstadistic re2 = EstadisticaHelper.toRegistreEstadistic((Collection<Dimensio>) null, (Collection<Fet>) null);
        assertThat(re2.getDimensions()).isNotNull().isEmpty();
        assertThat(re2.getFets()).isNotNull().isEmpty();
    }

    @Test
    void toRegistreEstadistic_allowsNullMappers_producesEmptyLists() {
        RegistreEstadistic re = EstadisticaHelper.toRegistreEstadistic(new Object(), null, null);
        assertThat(re.getDimensions()).isNotNull().isEmpty();
        assertThat(re.getFets()).isNotNull().isEmpty();
    }
}
