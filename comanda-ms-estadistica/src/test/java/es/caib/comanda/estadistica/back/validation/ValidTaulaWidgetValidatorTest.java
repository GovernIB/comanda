package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaTaulaWidget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class ValidTaulaWidgetValidatorTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext nodeBuilder;

    private ValidTaulaWidgetValidator validator;

    @BeforeEach
    void setUp() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request));

        validator = new ValidTaulaWidgetValidator(messageSource);
        validator.initialize(null);

        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        lenient().when(builder.addNode(anyString())).thenReturn(nodeBuilder);
        lenient().when(nodeBuilder.addConstraintViolation()).thenReturn(context);

        lenient().when(messageSource.getMessage(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenThrow(new org.springframework.context.NoSuchMessageException("mock"));
    }

    private static EstadisticaTaulaWidget newWidget() {
        EstadisticaTaulaWidget widget = new EstadisticaTaulaWidget();
        // dimensioAgrupacio és obligatori (validateDimensioAgrupacio); es fixa aquí perquè cada test
        // només exerciti la particularitat que li pertoca comprovar.
        widget.setDimensioAgrupacio(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(1L));
        return widget;
    }

    private static IndicadorTaula newColumna(long indicadorId, String titol, TableColumnsEnum agregacio) {
        IndicadorTaula ind = new IndicadorTaula();
        ind.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(indicadorId));
        ind.setTitol(titol);
        ind.setAgregacio(agregacio);
        return ind;
    }

    @Test
    void testValidPercentageNotMixed() {
        EstadisticaTaulaWidget widget = newWidget();

        IndicadorTaula ind1 = newColumna(1L, "Visites", TableColumnsEnum.PERCENTAGE);
        IndicadorTaula ind2 = newColumna(2L, "Sessions", TableColumnsEnum.PERCENTAGE);

        widget.setColumnes(Arrays.asList(ind1, ind2));

        assertTrue(validator.isValid(widget, context));
    }

    @Test
    void testValidPercentageMixedWithSumOfDifferentIndicador() {
        // Percentatge i suma d'indicadors diferents no interfereixen entre si: cada columna es calcula
        // de manera independent (veure FetRepositoryCustomImpl.processPercentages/calculatePercentages).
        EstadisticaTaulaWidget widget = newWidget();

        IndicadorTaula ind1 = newColumna(1L, "Visites", TableColumnsEnum.PERCENTAGE);
        IndicadorTaula ind2 = newColumna(2L, "Sessions", TableColumnsEnum.SUM);

        widget.setColumnes(Arrays.asList(ind1, ind2));

        assertTrue(validator.isValid(widget, context));
    }

    @Test
    void testValidPercentageCombinedWithSumOfSameIndicador() {
        // Percentatge que deriva d'una columna base numèrica (SUM) del mateix indicador: vàlid, ja que
        // FetRepositoryCustomImpl.calculateDependentPercentages pot parsejar el valor base com a número.
        EstadisticaTaulaWidget widget = newWidget();

        IndicadorTaula ind1 = newColumna(1L, "Visites", TableColumnsEnum.PERCENTAGE);
        IndicadorTaula ind2 = newColumna(1L, "Visites (total)", TableColumnsEnum.SUM);

        widget.setColumnes(Arrays.asList(ind1, ind2));

        assertTrue(validator.isValid(widget, context));
    }

    @Test
    void testInvalidPercentageCombinedWithFirstSeenOfSameIndicador() {
        // Percentatge que derivaria d'una columna base FIRST_SEEN/LAST_SEEN del mateix indicador (una data,
        // no un número): ha de fallar la validació (veure comentari a ValidTaulaWidgetValidator.validateColumnes).
        EstadisticaTaulaWidget widget = newWidget();

        IndicadorTaula ind1 = newColumna(1L, "Visites", TableColumnsEnum.PERCENTAGE);
        IndicadorTaula ind2 = newColumna(1L, "Primera visita", TableColumnsEnum.FIRST_SEEN);

        widget.setColumnes(Arrays.asList(ind1, ind2));

        assertFalse(validator.isValid(widget, context));
    }

    @Test
    void testInvalidPercentageCombinedWithLastSeenOfSameIndicador() {
        EstadisticaTaulaWidget widget = newWidget();

        IndicadorTaula ind1 = newColumna(1L, "Visites", TableColumnsEnum.PERCENTAGE);
        IndicadorTaula ind2 = newColumna(1L, "Darrera visita", TableColumnsEnum.LAST_SEEN);

        widget.setColumnes(Arrays.asList(ind1, ind2));

        assertFalse(validator.isValid(widget, context));
    }

    @Test
    void testValidSumNotMixed() {
        EstadisticaTaulaWidget widget = newWidget();

        IndicadorTaula ind1 = newColumna(1L, "Visites", TableColumnsEnum.SUM);
        IndicadorTaula ind2 = newColumna(2L, "Sessions", TableColumnsEnum.SUM);

        widget.setColumnes(Arrays.asList(ind1, ind2));

        assertTrue(validator.isValid(widget, context));
    }

    @Test
    void testInvalidDifferentUnits() {
        EstadisticaTaulaWidget widget = newWidget();

        IndicadorTaula ind1 = newColumna(1L, "Visites", TableColumnsEnum.AVERAGE);
        ind1.setUnitatAgregacio(PeriodeUnitat.DIA);

        IndicadorTaula ind2 = newColumna(2L, "Sessions", TableColumnsEnum.AVERAGE);
        ind2.setUnitatAgregacio(PeriodeUnitat.MES);

        widget.setColumnes(Arrays.asList(ind1, ind2));

        assertFalse(validator.isValid(widget, context));
    }

    @Test
    void testInvalidSecondIndicadorMissing() {
        // La segona columna sense indicador seleccionat ha de fallar la validació
        EstadisticaTaulaWidget widget = newWidget();

        IndicadorTaula ind1 = newColumna(1L, "Visites", TableColumnsEnum.SUM);

        IndicadorTaula ind2 = new IndicadorTaula();
        // ind2 sense indicador -> hauria de fallar
        ind2.setTitol("Sessions");
        ind2.setAgregacio(TableColumnsEnum.SUM);

        widget.setColumnes(Arrays.asList(ind1, ind2));

        assertFalse(validator.isValid(widget, context));
    }

    @Test
    void testInvalidMissingDimensioAgrupacio() {
        EstadisticaTaulaWidget widget = new EstadisticaTaulaWidget();
        widget.setDimensioAgrupacio(null);

        IndicadorTaula ind1 = newColumna(1L, "Visites", TableColumnsEnum.SUM);
        widget.setColumnes(Arrays.asList(ind1));

        assertFalse(validator.isValid(widget, context));
    }
}
