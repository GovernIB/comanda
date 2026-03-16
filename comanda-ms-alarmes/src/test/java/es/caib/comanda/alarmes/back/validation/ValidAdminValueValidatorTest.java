package es.caib.comanda.alarmes.back.validation;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidAdminValueValidatorTest {

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private ValidAdminValueValidator validator;

    @BeforeEach
    void setUp() {
        // Assegurem que el mock d'AuthenticationHelper s'injecta a validator (per si InjectMocks no ho fa)
        ReflectionTestUtils.setField(validator, "authenticationHelper", authenticationHelper);
    }

    @Test
    @DisplayName("Quan el valor és fals, el validador sempre ha de retornar cert")
    void isValid_quanValorFals_retornaCert() {
        // Arrange
        Boolean value = false;

        // Act
        boolean result = validator.isValid(value, context);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Quan el valor és verdader i l'usuari és administrador, ha de retornar cert")
    void isValid_quanValorVerdaderIUsuariAdmin_retornaCert() {
        // Arrange
        Boolean value = true;
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        // Act
        boolean result = validator.isValid(value, context);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Quan el valor és verdader i l'usuari no és administrador, ha de retornar fals")
    void isValid_quanValorVerdaderIUsuariNoAdmin_retornaFals() {
        // Arrange
        Boolean value = true;
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);

        // Act
        boolean result = validator.isValid(value, context);

        // Assert
        assertThat(result).isFalse();
    }
}
