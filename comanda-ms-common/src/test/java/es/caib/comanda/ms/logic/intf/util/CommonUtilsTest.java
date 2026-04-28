package es.caib.comanda.ms.logic.intf.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.exception.CompositePkParsingException;
import es.caib.comanda.ms.logic.intf.exception.ResourceFieldNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommonUtilsTest {

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void compositePkUtil_serialitzaIDeserialitza() {
        // Verifica la serialització i deserialització correcta de claus compostes.
        CompositePkUtil util = new CompositePkUtil();
        org.springframework.test.util.ReflectionTestUtils.setField(util, "objectMapper", new ObjectMapper());
        Pk pk = new Pk("A", 1);
        String serialized = util.getSerializedIdFromCompositePk(pk);
        Pk parsed = util.getCompositePkFromSerializedId(serialized, Pk.class);

        assertThat(parsed.a).isEqualTo("A");
        assertThat(parsed.b).isEqualTo(1);
        assertThat(util.isCompositePkClass(Pk.class)).isFalse();
        assertThat(util.isCompositePkClass(String.class)).isFalse();
        assertThat(util.isCompositePkClass(UUID.class)).isFalse();
    }

    @Test
    void compositePkUtil_quanInvalid_throws() {
        // Comprova que una clau composta invàlida genera error.
        CompositePkUtil util = new CompositePkUtil();
        org.springframework.test.util.ReflectionTestUtils.setField(util, "objectMapper", new ObjectMapper());
        assertThatThrownBy(() -> util.getCompositePkFromSerializedId("%%%", Pk.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> util.getSerializedIdFromCompositePk(new Serializable() {}))
                .isInstanceOf(CompositePkParsingException.class);
    }

    @Test
    void halFormsUtil_fieldHelpers_funcionen() {
        // Valida els helpers de HAL-Forms per cercar anotacions i flags d'onChange.
        assertThat(HalFormsUtil.getFieldAnnotation(Annotated.class, "name", ResourceField.class)).isNotNull();
        assertThat(HalFormsUtil.getFieldAnnotation(Annotated.class, "missing", ResourceField.class)).isNull();
        assertThat(HalFormsUtil.isOnChangeActive(Annotated.class, "name")).isTrue();
        assertThat(HalFormsUtil.isOnChangeActive(Annotated.class, "missing")).isFalse();
    }

    @Test
    void jsonUtil_mapAndFill_resourceFields() throws Exception {
        // Exercita la conversió JSON a map i l'emplenat de recursos via JsonUtil.
        JsonUtil util = new JsonUtil();
        org.springframework.test.util.ReflectionTestUtils.setField(util, "objectMapper", new ObjectMapper());
        JsonNodeFactory f = JsonNodeFactory.instance;
        var node = f.objectNode().put("id", 3L).put("name", "abc");

        Map<String, Object> map = util.fromJsonToMap(node, Annotated.class);
        assertThat(map).containsEntry("id", 3L).containsEntry("name", "abc");

        Annotated target = new Annotated();
        util.fillResourceWithFieldsMap(target, Map.of("name", "filled"), null, null);
        assertThat(target.name).isEqualTo("filled");

        Object fieldValue = util.fillResourceWithFieldsMap(new Annotated(), null, "name", f.textNode("x"));
        assertThat(fieldValue).isEqualTo("x");

        assertThatThrownBy(() -> util.fillResourceWithFieldsMap(new Annotated(), null, "missing", f.textNode("x")))
                .isInstanceOf(ResourceFieldNotFoundException.class);
    }

    @Test
    void stringUtil_capitalizeIDecapitalize() {
        // Comprova les utilitats bàsiques de capitalització i decapitalització.
        assertThat(StringUtil.capitalize("abc")).isEqualTo("Abc");
        assertThat(StringUtil.decapitalize("Abc")).isEqualTo("abc");
    }

    @Test
    void httpRequestUtil_requestPresentIAbsent() {
        // Verifica la detecció de request HTTP present i absent al context actual.
        Optional<javax.servlet.http.HttpServletRequest> empty = HttpRequestUtil.getCurrentHttpRequest();
        assertThat(empty).isEmpty();

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/x");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        Optional<javax.servlet.http.HttpServletRequest> present = HttpRequestUtil.getCurrentHttpRequest();
        assertThat(present).isPresent();
    }

    static class Pk implements Serializable {
        public String a;
        public int b;
        public Pk() {}
        Pk(String a, int b) { this.a = a; this.b = b; }
    }

    static class Annotated implements Serializable {
        @ResourceField(onChangeActive = true)
        public String name;
        public Long id;
    }
}
