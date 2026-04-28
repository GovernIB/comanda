package es.caib.comanda.ms.logic.intf.util;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.lang.reflect.Field;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class I18nUtilTest {

    @Test
    void getI18nMessageAndEnumDescription_coverFallbacks() throws Exception {
        // Comprova la resolució de missatges i els fallback de descripcions d'enums.
        StaticMessageSource ms = new StaticMessageSource();
        ms.addMessage("code.test", Locale.getDefault(), "translated");
        ms.addMessage(Holder.class.getName() + ".status.OK", Locale.getDefault(), "Field translation");

        I18nUtil util = new I18nUtil();
        org.springframework.test.util.ReflectionTestUtils.setField(util, "messageSource", ms);

        assertThat(util.getI18nMessage("code.test")).isEqualTo("translated");
        assertThat(util.getI18nMessage("missing")).isEqualTo("missing");

        Field field = Holder.class.getDeclaredField("status");
        assertThat(util.getI18nEnumDescription(field, "OK")).isEqualTo("Field translation");
        assertThat(util.getI18nEnumDescription(field, "MISS")).isEqualTo("MISS");
    }

    static class Holder {
        Status status;
    }

    enum Status {
        OK
    }
}
