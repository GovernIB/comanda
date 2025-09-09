package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves unitàries per l'agrupació per període (setmanal/mensual) extreta.
 * Comentaris en català i estructura AAA.
 */
@ExtendWith(MockitoExtension.class)
public class AgrupacioPeriodeHelperTest {

    @Mock DimensioValorRepository dimensioValorRepository; // no s'usen directament
    @Mock FetRepository fetRepository;
    @Mock IndicadorRepository indicadorRepository;

    @InjectMocks CompactacioHelper helper;

    private FetEntity fet(Long entornId, LocalDate data, Map<String,String> dims){
        FetEntity f = new FetEntity();
        TempsEntity t = new TempsEntity();
        t.setData(data);
        f.setTemps(t);
        f.setEntornAppId(entornId);
        f.setDimensionsJson(dims==null?null:new HashMap<>(dims));
        return f;
    }

    private Object periodeTargetValue(String name) throws Exception {
        Class<?>[] declared = CompactacioHelper.class.getDeclaredClasses();
        for (Class<?> c : declared) {
            if (c.getSimpleName().equals("PeriodeTarget")) {
                for (Object constant : c.getEnumConstants()) {
                    if (((Enum<?>)constant).name().equals(name)) return constant;
                }
            }
        }
        throw new IllegalStateException("No s'ha trobat PeriodeTarget.");
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<Object, List<FetEntity>> agrupar(List<FetEntity> fets, Long entornId, Object target) throws Exception {
        Method m = CompactacioHelper.class.getDeclaredMethod("agruparFetsPerPeriode", List.class, Long.class, target.getClass());
        m.setAccessible(true);
        return (LinkedHashMap<Object, List<FetEntity>>) m.invoke(helper, fets, entornId, target);
    }

    @Test
    void agruparFetsPerPeriode_mensual_mateixMes_mateixaClau() throws Exception {
        // Arrange
        Long entorn = 5L;
        LocalDate d1 = LocalDate.of(2024, 4, 10);
        LocalDate d2 = LocalDate.of(2024, 4, 25);
        var f1 = fet(entorn, d1, Map.of("D","x"));
        var f2 = fet(entorn, d2, Map.of("D","x"));
        Object mensual = periodeTargetValue("MENSUAL");

        // Act
        var map = agrupar(List.of(f1, f2), entorn, mensual);

        // Assert
        assertEquals(1, map.size(), "Mateix mes i dimensions -> una sola clau");
        var clau = map.keySet().iterator().next();
        // La data de la clau ha de ser el primer dia de mes
        // Accedim a camp 'data' via reflexió (és package-private dins ClauDimensio)
        var dataField = clau.getClass().getDeclaredField("data");
        dataField.setAccessible(true);
        LocalDate dataClau = (LocalDate) dataField.get(clau);
        assertEquals(LocalDate.of(2024,4,1), dataClau);
        assertEquals(2, map.values().iterator().next().size());
    }

    @Test
    void agruparFetsPerPeriode_setmanal_mateixaSetmanaISO_mateixaClau() throws Exception {
        // Arrange
        Long entorn = 6L;
        // Triam dues dates de la mateixa setmana ISO (dilluns inici)
        LocalDate monday = LocalDate.of(2024, 7, 1);
        assertEquals(DayOfWeek.MONDAY, monday.getDayOfWeek());
        LocalDate wednesday = monday.plusDays(2);
        var f1 = fet(entorn, monday, Map.of("D","y"));
        var f2 = fet(entorn, wednesday, Map.of("D","y"));
        Object setmanal = periodeTargetValue("SETMANAL");

        // Act
        var map = agrupar(List.of(f1, f2), entorn, setmanal);

        // Assert
        assertEquals(1, map.size(), "Mateixa setmana i dimensions -> una sola clau");
        var clau = map.keySet().iterator().next();
        var dataField = clau.getClass().getDeclaredField("data");
        dataField.setAccessible(true);
        LocalDate dataClau = (LocalDate) dataField.get(clau);
        assertEquals(monday, dataClau, "La data de la clau ha de ser el dilluns corresponent");
        assertEquals(2, map.values().iterator().next().size());
    }

    @Test
    void agruparFetsPerPeriode_dimsDiferents_generenDuesClaus() throws Exception {
        // Arrange
        Long entorn = 7L;
        LocalDate d = LocalDate.of(2024, 5, 10);
        var f1 = fet(entorn, d, Map.of("D","a"));
        var f2 = fet(entorn, d, Map.of("D","b"));
        Object mensual = periodeTargetValue("MENSUAL");

        // Act
        var map = agrupar(List.of(f1, f2), entorn, mensual);

        // Assert
        assertEquals(2, map.size(), "Dimensions diferents -> claus diferents");
    }

    @Test
    void agruparFetsPerPeriode_nullOBuit_retornBuit() throws Exception {
        // Arrange
        Object mensual = periodeTargetValue("MENSUAL");

        // Act
        var buit = agrupar(null, 1L, mensual);
        var buit2 = agrupar(Collections.emptyList(), 1L, mensual);

        // Assert
        assertTrue(buit.isEmpty());
        assertTrue(buit2.isEmpty());
    }
}
