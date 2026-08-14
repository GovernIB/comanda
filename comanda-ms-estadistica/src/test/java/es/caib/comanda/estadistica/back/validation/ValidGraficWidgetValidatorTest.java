package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaGraficWidget;
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
public class ValidGraficWidgetValidatorTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext nodeBuilder;

    private ValidGraficWidgetValidator validator;

    @BeforeEach
    void setUp() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request));

        validator = new ValidGraficWidgetValidator(messageSource);
        validator.initialize(null);

        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        lenient().when(builder.addNode(anyString())).thenReturn(nodeBuilder);
        lenient().when(nodeBuilder.addConstraintViolation()).thenReturn(context);

        lenient().when(messageSource.getMessage(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenThrow(new org.springframework.context.NoSuchMessageException("mock"));
    }

    @Test
    void testValidPercentageNotMixed() {
        EstadisticaGraficWidget widget = new EstadisticaGraficWidget();
        widget.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);
        widget.setTempsAgrupacio(PeriodeUnitat.MES);

        IndicadorTaula ind1 = new IndicadorTaula();
        ind1.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(1L));
        ind1.setTitol("Visites");
        ind1.setAgregacio(TableColumnsEnum.PERCENTAGE);

        IndicadorTaula ind2 = new IndicadorTaula();
        ind2.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(2L));
        ind2.setTitol("Sessions");
        ind2.setAgregacio(TableColumnsEnum.PERCENTAGE);

        widget.setIndicadorsInfo(Arrays.asList(ind1, ind2));

        assertTrue(validator.isValid(widget, context));
    }

    @Test
    void testInvalidPercentageMixedWithSum() {
        EstadisticaGraficWidget widget = new EstadisticaGraficWidget();
        widget.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);
        widget.setTempsAgrupacio(PeriodeUnitat.MES);

        IndicadorTaula ind1 = new IndicadorTaula();
        ind1.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(1L));
        ind1.setTitol("Visites");
        ind1.setAgregacio(TableColumnsEnum.PERCENTAGE);

        IndicadorTaula ind2 = new IndicadorTaula();
        ind2.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(2L));
        ind2.setTitol("Sessions");
        ind2.setAgregacio(TableColumnsEnum.SUM);

        widget.setIndicadorsInfo(Arrays.asList(ind1, ind2));

        assertFalse(validator.isValid(widget, context));
    }

    @Test
    void testValidSumNotMixed() {
        EstadisticaGraficWidget widget = new EstadisticaGraficWidget();
        widget.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);
        widget.setTempsAgrupacio(PeriodeUnitat.MES);

        IndicadorTaula ind1 = new IndicadorTaula();
        ind1.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(1L));
        ind1.setTitol("Visites");
        ind1.setAgregacio(TableColumnsEnum.SUM);

        IndicadorTaula ind2 = new IndicadorTaula();
        ind2.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(2L));
        ind2.setTitol("Sessions");
        ind2.setAgregacio(TableColumnsEnum.SUM);

        widget.setIndicadorsInfo(Arrays.asList(ind1, ind2));

        assertTrue(validator.isValid(widget, context));
    }

    @Test
    void testInvalidSecondIndicadorMissing() {
        // El segon indicador sense indicador seleccionat ha de fallar la validació
        EstadisticaGraficWidget widget = new EstadisticaGraficWidget();
        widget.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);
        widget.setTempsAgrupacio(PeriodeUnitat.MES);

        IndicadorTaula ind1 = new IndicadorTaula();
        ind1.setIndicador(es.caib.comanda.ms.logic.intf.model.ResourceReference.toResourceReference(1L));
        ind1.setTitol("Visites");
        ind1.setAgregacio(TableColumnsEnum.SUM);

        IndicadorTaula ind2 = new IndicadorTaula();
        // ind2 sense indicador -> hauria de fallar
        ind2.setTitol("Sessions");
        ind2.setAgregacio(TableColumnsEnum.SUM);

        widget.setIndicadorsInfo(Arrays.asList(ind1, ind2));

        assertFalse(validator.isValid(widget, context));
    }
}
