package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.ms.salut.model.EstatSalutEnum;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutNivell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SalutInfoHelperPrivateMethodsTest {

    @InjectMocks
    private SalutInfoHelper helper;

    @BeforeEach
    void resetStaticState() {
        // Neteja el mapa estàtic abans de cada test
        SalutInfoHelper.compactacioMap.clear();
    }

    private Object invokePrivate(String name, Class<?>[] types, Object... args) throws Exception {
        Method m = SalutInfoHelper.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(helper, args);
    }

    @Test
    void toSalutEstat_maps_all_values_and_null() throws Exception {
        // Mappeja tots els valors d'EstatSalutEnum a SalutEstat (mateix nom)
        for (EstatSalutEnum e : EstatSalutEnum.values()) {
            Object res = invokePrivate("toSalutEstat", new Class[]{EstatSalutEnum.class}, e);
            assertNotNull(res, "Mapeig no hauria de ser null per " + e);
            assertEquals(e.name(), res.toString());
        }
        // null -> null
        Object unknownRes = invokePrivate("toSalutEstat", new Class[]{EstatSalutEnum.class}, new Object[]{null});
        assertEquals(SalutEstat.UNKNOWN, unknownRes);
    }

    @Test
    void toSalutNivell_maps_variants_and_unknown_returns_null() throws Exception {
        assertEquals(SalutNivell.ERROR, invokePrivate("toSalutNivell", new Class[]{String.class}, "error"));
        assertEquals(SalutNivell.ERROR, invokePrivate("toSalutNivell", new Class[]{String.class}, "ERROR"));
        assertEquals(SalutNivell.WARN, invokePrivate("toSalutNivell", new Class[]{String.class}, "avis"));
        assertEquals(SalutNivell.WARN, invokePrivate("toSalutNivell", new Class[]{String.class}, "WARN"));
        assertEquals(SalutNivell.INFO, invokePrivate("toSalutNivell", new Class[]{String.class}, "info"));
        assertNull(invokePrivate("toSalutNivell", new Class[]{String.class}, "altre"));
        assertNull(invokePrivate("toSalutNivell", new Class[]{String.class}, new Object[]{null}));
    }

    @Test
    void toLocalDateTime_converts_and_handles_null() {
        Date now = new Date();
        LocalDateTime ldt = helper.toLocalDateTime(now);
        assertNotNull(ldt);
        assertNull(helper.toLocalDateTime(null));
    }

    @Test
    void isFirstMinute_helpers_work_and_handle_null() throws Exception {
        LocalDateTime h0m0 = LocalDateTime.of(2025, Month.AUGUST, 1, 0, 0);
        LocalDateTime h1m0 = LocalDateTime.of(2025, Month.AUGUST, 1, 1, 0);
        LocalDateTime h1m1 = LocalDateTime.of(2025, Month.AUGUST, 1, 1, 1);

        assertTrue((Boolean) invokePrivate("isFirstMinuteOfHour", new Class[]{LocalDateTime.class}, h0m0));
        assertTrue((Boolean) invokePrivate("isFirstMinuteOfHour", new Class[]{LocalDateTime.class}, h1m0));
        assertFalse((Boolean) invokePrivate("isFirstMinuteOfHour", new Class[]{LocalDateTime.class}, h1m1));
        assertFalse((Boolean) invokePrivate("isFirstMinuteOfHour", new Class[]{LocalDateTime.class}, new Object[]{null}));

        assertTrue((Boolean) invokePrivate("isFirstMinuteOfDay", new Class[]{LocalDateTime.class}, h0m0));
        assertFalse((Boolean) invokePrivate("isFirstMinuteOfDay", new Class[]{LocalDateTime.class}, h1m0));
        assertFalse((Boolean) invokePrivate("isFirstMinuteOfDay", new Class[]{LocalDateTime.class}, new Object[]{null}));
    }

    @Test
    void actualitzaInfoCompactacio_progresses_per_entorn_and_wraps_mod_4() throws Exception {
        Method m = SalutInfoHelper.class.getDeclaredMethod("actualitzaInfoCompactacio", Long.class);
        m.setAccessible(true);
        // Primera crida crea entrada amb valor 1
        int v1 = (Integer) m.invoke(helper, 10L);
        int v2 = (Integer) m.invoke(helper, 10L);
        int v3 = (Integer) m.invoke(helper, 10L);
        int v4 = (Integer) m.invoke(helper, 10L);
        int v5 = (Integer) m.invoke(helper, 10L);
        assertEquals(1, v1);
        assertEquals(2, v2);
        assertEquals(3, v3);
        assertEquals(0, v4); // 4 % 4 == 0 segons la implementació
        assertEquals(1, v5);
        // Un altre entorn és independent
        int other = (Integer) m.invoke(helper, 20L);
        assertEquals(1, other);
    }
}
