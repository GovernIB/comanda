package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorFormulaTerme;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a ValidIndicadorFormulaTermeValidator")
class ValidIndicadorFormulaTermeValidatorTest {

    @Mock
    private IndicadorRepository indicadorRepository;

    @Mock
    private javax.validation.ConstraintValidatorContext context;

    @Mock
    private javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @InjectMocks
    private ValidIndicadorFormulaTermeValidator validator;

    @BeforeEach
    void setUp() {
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }

    private IndicadorEntity indicador(Long id, IndicadorTipus tipus, Long entornAppId) {
        IndicadorEntity i = new IndicadorEntity();
        i.setId(id);
        i.setTipus(tipus);
        i.setEntornAppId(entornAppId);
        return i;
    }

    private IndicadorFormulaTerme terme(Long formulaId, Long componentId) {
        IndicadorFormulaTerme t = new IndicadorFormulaTerme();
        t.setIndicadorFormula(ResourceReference.toResourceReference(formulaId));
        t.setIndicadorComponent(ResourceReference.toResourceReference(componentId));
        t.setOperador(OperadorFormulaEnum.SUMA);
        t.setOrdre(1);
        return t;
    }

    @Test
    @DisplayName("isValid: rebutja quan l'indicador component pertany a un altre entornApp")
    void isValid_quanEntornAppDiferent_llavorsInvalid() {
        when(indicadorRepository.findById(1L)).thenReturn(Optional.of(indicador(1L, IndicadorTipus.FORMULA, 100L)));
        when(indicadorRepository.findById(2L)).thenReturn(Optional.of(indicador(2L, IndicadorTipus.SIMPLE, 200L)));

        assertThat(validator.isValid(terme(1L, 2L), context)).isFalse();
    }

    @Test
    @DisplayName("isValid: rebutja quan l'indicador component és una altra FORMULA (sense niament)")
    void isValid_quanComponentEsFormula_llavorsInvalid() {
        when(indicadorRepository.findById(1L)).thenReturn(Optional.of(indicador(1L, IndicadorTipus.FORMULA, 100L)));
        when(indicadorRepository.findById(2L)).thenReturn(Optional.of(indicador(2L, IndicadorTipus.FORMULA, 100L)));

        assertThat(validator.isValid(terme(1L, 2L), context)).isFalse();
    }

    @Test
    @DisplayName("isValid: rebutja l'auto-referència")
    void isValid_quanAutoReferencia_llavorsInvalid() {
        assertThat(validator.isValid(terme(1L, 1L), context)).isFalse();
    }

    @Test
    @DisplayName("isValid: rebutja quan l'indicador de fórmula no és de tipus FORMULA")
    void isValid_quanIndicadorFormulaNoEsFormula_llavorsInvalid() {
        when(indicadorRepository.findById(1L)).thenReturn(Optional.of(indicador(1L, IndicadorTipus.SIMPLE, 100L)));
        when(indicadorRepository.findById(2L)).thenReturn(Optional.of(indicador(2L, IndicadorTipus.SIMPLE, 100L)));

        assertThat(validator.isValid(terme(1L, 2L), context)).isFalse();
    }

    @Test
    @DisplayName("isValid: accepta un terme vàlid (mateix entornApp, component SIMPLE)")
    void isValid_quanTotEsCorrecte_llavorsValid() {
        when(indicadorRepository.findById(1L)).thenReturn(Optional.of(indicador(1L, IndicadorTipus.FORMULA, 100L)));
        when(indicadorRepository.findById(2L)).thenReturn(Optional.of(indicador(2L, IndicadorTipus.SIMPLE, 100L)));

        assertThat(validator.isValid(terme(1L, 2L), context)).isTrue();
    }
}
