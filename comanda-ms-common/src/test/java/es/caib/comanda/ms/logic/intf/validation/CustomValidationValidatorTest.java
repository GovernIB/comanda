package es.caib.comanda.ms.logic.intf.validation;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomValidationValidatorTest {

    @Test
    void isValid_quanValidatorLocal_validacioIConstraintViolation() {
        // Verifica la validació custom amb un validador local i la propagació de violations.
        CustomValidation annotation = AnnotatedLocal.class.getAnnotation(CustomValidation.class);
        CustomValidationValidator validator = new CustomValidationValidator();
        validator.initialize(annotation);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext node = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        when(context.buildConstraintViolationWithTemplate("local-message")).thenReturn(builder);
        when(builder.addPropertyNode("fieldA")).thenReturn(node);
        when(node.addConstraintViolation()).thenReturn(context);

        boolean valid = validator.isValid(new Object(), context);

        assertThat(valid).isTrue();
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_quanSpringBean_false() {
        // Comprova la resolució del validador com a bean Spring quan la validació falla.
        CustomValidation annotation = AnnotatedSpring.class.getAnnotation(CustomValidation.class);
        CustomValidationValidator validator = new CustomValidationValidator();
        validator.initialize(annotation);

        CustomValidatorLocator locator = new CustomValidatorLocator();
        ReflectionTestUtils.setField(locator, "validators", java.util.List.of(new AlwaysFalseValidator()));
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(CustomValidatorLocator.class)).thenReturn(locator);
        ReflectionTestUtils.setField(CustomValidatorLocator.class, "applicationContext", applicationContext);

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        boolean valid = validator.isValid(new Object(), context);

        assertThat(valid).isFalse();
    }

    @CustomValidation(customValidatorType = AlwaysTrueValidator.class, springBean = false, targetFields = {"fieldA"})
    static class AnnotatedLocal {
    }

    @CustomValidation(customValidatorType = AlwaysFalseValidator.class, springBean = true, targetFields = {})
    static class AnnotatedSpring {
    }

    public static class AlwaysTrueValidator implements CustomValidator<Object> {
        @Override
        public boolean validate(Object value) {
            return true;
        }

        @Override
        public String getMessage() {
            return "local-message";
        }
    }

    public static class AlwaysFalseValidator implements CustomValidator<Object> {
        @Override
        public boolean validate(Object value) {
            return false;
        }
    }
}
