package es.caib.comanda.ms.logic.intf.util;

import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.exception.ResourceFieldNotFoundException;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.Resource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeUtilTest {

    @Test
    void genericAndFieldTypeHelpers_coverBranches() throws Exception {
        // Exercita helpers de genèrics i resolució de tipus de camps simples i múltiples.
        Field singleRef = Holder.class.getDeclaredField("singleRef");
        Field arrayRef = Holder.class.getDeclaredField("arrayRef");
        Field listRef = Holder.class.getDeclaredField("listRef");
        Field resourceField = Holder.class.getDeclaredField("resource");
        Field names = Holder.class.getDeclaredField("names");
        Field ids = Holder.class.getDeclaredField("ids");
        Field required = Holder.class.getDeclaredField("required");

        assertThat(TypeUtil.getReferencedResourceClass(singleRef)).isEqualTo(TestResource.class);
        assertThat(TypeUtil.getReferencedResourceClass(arrayRef)).isEqualTo(TestResource.class);
        assertThat(TypeUtil.getReferencedResourceClass(listRef)).isEqualTo(TestResource.class);
        assertThat(TypeUtil.getReferencedResourceClass(resourceField)).isEqualTo(TestResource.class);

        assertThat(TypeUtil.getMethodSuffixFromFieldName("name")).isEqualTo("Name");
        assertThat(TypeUtil.getMethodSuffixFromField(names)).isEqualTo("Names");
        assertThat(TypeUtil.isNotNullField(required)).isTrue();
        assertThat(TypeUtil.isMultipleFieldType(names)).isTrue();
        assertThat(TypeUtil.isMultipleFieldType(ids)).isTrue();
        assertThat(TypeUtil.getArrayFieldType(ids)).isEqualTo(Long.class);
        assertThat(TypeUtil.getCollectionFieldType(names)).isEqualTo(String.class);
        assertThat(TypeUtil.getFieldTypeMultipleAware(names)).isEqualTo(String.class);
        assertThat(TypeUtil.getFieldTypeMultipleAware(ids)).isEqualTo(Long.class);
        assertThat(TypeUtil.getMultipleFieldType(resourceField)).isNull();
    }

    @Test
    void fieldGetterSetterHelpers_coverFieldAndGetterPaths() throws Exception {
        // Verifica lectura i escriptura per camp o getter/setter a través de TypeUtil.
        AccessHolder holder = new AccessHolder();
        holder.setName("abc");

        Field nameField = AccessHolder.class.getDeclaredField("name");
        assertThat((Object) TypeUtil.getFieldOrGetterValue("name", holder)).isEqualTo("abc");
        assertThat((Object) TypeUtil.getFieldOrGetterValue(nameField, holder)).isEqualTo("abc");
        assertThat(TypeUtil.getFieldOrGetterValue("name", holder, String.class)).isEqualTo("abc");
        assertThat(TypeUtil.getFieldOrGetterValue(nameField, holder, String.class)).isEqualTo("abc");

        TypeUtil.setFieldOrSetterValue(nameField, holder, "xyz");
        assertThat(holder.getName()).isEqualTo("xyz");
        assertThat((Object) TypeUtil.getFieldValue(holder, "name")).isEqualTo("xyz");

        assertThatThrownBy(() -> TypeUtil.getFieldOrGetterValue("name", holder, Integer.class))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TypeUtil.getFieldValue(holder, "missing"))
                .isInstanceOf(ResourceFieldNotFoundException.class);
    }

    @Test
    void assignableClassesAndGenericSuperclass_helpers_work() {
        // Comprova la detecció de classes assignables i la resolució del generic superclass.
        assertThat(TypeUtil.getArgumentClassFromGenericSuperclass(TestChild.class, TestParent.class, 0)).isEqualTo(String.class);
        assertThat(TypeUtil.<HandlerContract>findAssignableClasses(HandlerContract.class, "es.caib.comanda.ms.logic.intf.util"))
                .anyMatch(c -> c.equals(TestAssignable.class));
    }

    static class Holder {
        ResourceReference<TestResource, Long> singleRef;
        ResourceReference<TestResource, Long>[] arrayRef;
        List<ResourceReference<TestResource, Long>> listRef;
        TestResource resource;
        @ResourceField
        List<String> names;
        Long[] ids;
        @NotNull
        String required;
    }

    public static class AccessHolder {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    static class TestResource extends BaseResource<Long> {}

    static class TestParent<T> {}
    static class TestChild extends TestParent<String> {}

    interface HandlerContract extends Serializable {}
    public static class TestAssignable implements HandlerContract {}
}
