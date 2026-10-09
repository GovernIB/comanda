package es.caib.comanda.estadistica.logic.intf.validation;

import es.caib.comanda.estadistica.logic.helper.DashboardImportHelper;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OverwriteEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.Conflict;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidConflictValidatorTest {

    @Mock
    private DashboardImportHelper dashboardImportHelper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext nodeBuilder;

    @InjectMocks
    private ValidConflictValidator validator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(I18nUtil.getInstance(), "messageSource", messageSource);
        lenient().when(messageSource.getMessage(anyString(), any(), anyString(), any())).thenAnswer(i -> i.getArgument(0));

        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        lenient().when(builder.addNode(anyString())).thenReturn(nodeBuilder);
        lenient().when(nodeBuilder.addConstraintViolation()).thenReturn(context);
        lenient().when(builder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    @DisplayName("isValid: retorna false quan el conflicte és bloquejant")
    void isValid_quanConflicteBloquejant_retornaFalse() {
        Conflict conflict = new Conflict("IND1", "IndicadorExport");
        conflict.setBloquejant(true);
        conflict.setMissatgeError("Indicador no trobat");

        boolean result = validator.isValid(conflict, context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Indicador no trobat");
    }

    @Test
    @DisplayName("isValid: retorna false quan DashboardExport té EMPRAR_EXISTENT")
    void isValid_quanDashboardExportTeEmprarExistent_retornaFalse() {
        Conflict conflict = new Conflict("Dash", DashboardExport.class.getSimpleName());
        conflict.setOverwrite(OverwriteEnum.EMPRAR_EXISTENT);

        boolean result = validator.isValid(conflict, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid: retorna true quan és un conflicte vàlid no bloquejant")
    void isValid_quanConflicteValid_retornaTrue() {
        Conflict conflict = new Conflict("Widget1", "EstadisticaWidgetExport");
        conflict.setOverwrite(OverwriteEnum.EMPRAR_EXISTENT);

        boolean result = validator.isValid(conflict, context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isValid: retorna false quan IndicadorExport té CREAR_AMB_ALTRE_NOM")
    void isValid_quanIndicadorExportTeCrearAmbAltreNom_retornaFalse() {
        Conflict conflict = new Conflict("IND1", "IndicadorExport");
        conflict.setOverwrite(OverwriteEnum.CREAR_AMB_ALTRE_NOM);

        boolean result = validator.isValid(conflict, context);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isValid: retorna true quan IndicadorExport té SOBRESCRIURE")
    void isValid_quanIndicadorExportTeSobrescriure_retornaTrue() {
        Conflict conflict = new Conflict("IND1", "IndicadorExport");
        conflict.setOverwrite(OverwriteEnum.SOBRESCRIURE);

        boolean result = validator.isValid(conflict, context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isValid: retorna true quan IndicadorExport té EMPRAR_EXISTENT")
    void isValid_quanIndicadorExportTeEmprarExistent_retornaTrue() {
        Conflict conflict = new Conflict("IND1", "IndicadorExport");
        conflict.setOverwrite(OverwriteEnum.EMPRAR_EXISTENT);

        boolean result = validator.isValid(conflict, context);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isValid: retorna false quan element que no és IndicadorExport té SOBRESCRIURE")
    void isValid_quanNoIndicadorExportTeSobrescriure_retornaFalse() {
        Conflict widgetConflict = new Conflict("Widget1", "EstadisticaWidgetExport");
        widgetConflict.setOverwrite(OverwriteEnum.SOBRESCRIURE);
        assertThat(validator.isValid(widgetConflict, context)).isFalse();

        Conflict dashConflict = new Conflict("Dash1", DashboardExport.class.getSimpleName());
        dashConflict.setOverwrite(OverwriteEnum.SOBRESCRIURE);
        assertThat(validator.isValid(dashConflict, context)).isFalse();
    }
}
