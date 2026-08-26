package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio.ChangeTipusActionForm;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.EntitatValorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a ValidDimensioCanviTipusValidator")
class ValidDimensioCanviTipusValidatorTest {

    @Mock
    private DimensioRepository dimensioRepository;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @InjectMocks
    private ValidDimensioCanviTipusValidator validator;

    @BeforeEach
    void setUp() {
        lenient().when(context.buildConstraintViolationWithTemplate(org.mockito.ArgumentMatchers.anyString())).thenReturn(violationBuilder);
        lenient().when(violationBuilder.addPropertyNode(org.mockito.ArgumentMatchers.anyString())).thenReturn(nodeBuilder);
    }

    private DimensioEntity dimensio(Long id, TipusDimensioEnum tipus) {
        DimensioEntity d = new DimensioEntity();
        d.setId(id);
        d.setTipus(tipus);
        return d;
    }

    @Test
    @DisplayName("isValid: rebutja el tipus CONSELLERIA")
    void isValid_quanTipusConselleria_llavorsInvalid() {
        ChangeTipusActionForm form = new ChangeTipusActionForm();
        form.setTipus(TipusDimensioEnum.CONSELLERIA);
        form.setEntornAppId(1L);

        assertThat(validator.isValid(form, context)).isFalse();
    }

    @Test
    @DisplayName("isValid: rebutja si una altra dimensió ja té aquest tipus")
    void isValid_quanAltraDimensioJaTeElTipus_llavorsInvalid() {
        ChangeTipusActionForm form = new ChangeTipusActionForm();
        form.setTipus(TipusDimensioEnum.ENTITAT);
        form.setEntornAppId(1L);
        form.setDimensioId(10L);

        when(dimensioRepository.findByEntornAppId(1L))
            .thenReturn(Collections.singletonList(dimensio(99L, TipusDimensioEnum.ENTITAT)));

        assertThat(validator.isValid(form, context)).isFalse();
    }

    @Test
    @DisplayName("isValid: accepta re-enviar el mateix tipus de la pròpia dimensió (editar el mapeig)")
    void isValid_quanEsLaMateixaDimensioAmbElMateixTipus_llavorsValid() {
        ChangeTipusActionForm form = new ChangeTipusActionForm();
        form.setTipus(TipusDimensioEnum.ENTITAT);
        form.setEntornAppId(1L);
        form.setDimensioId(10L);
        form.setEntitatValorTipus(EntitatValorTipus.CODI_DIR3);

        when(dimensioRepository.findByEntornAppId(1L))
            .thenReturn(Arrays.asList(dimensio(10L, TipusDimensioEnum.ENTITAT), dimensio(11L, TipusDimensioEnum.ORGAN_GESTOR)));

        assertThat(validator.isValid(form, context)).isTrue();
    }

    @Test
    @DisplayName("isValid: accepta un tipus nou quan cap altra dimensió el té")
    void isValid_quanCapAltraDimensioTeElTipus_llavorsValid() {
        ChangeTipusActionForm form = new ChangeTipusActionForm();
        form.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        form.setEntornAppId(1L);

        when(dimensioRepository.findByEntornAppId(1L)).thenReturn(Collections.emptyList());

        assertThat(validator.isValid(form, context)).isTrue();
    }

    @Test
    @DisplayName("isValid: accepta desmarcar (tipus null) sense consultar el repositori")
    void isValid_quanTipusEsNull_llavorsValidSenseConsultar() {
        ChangeTipusActionForm form = new ChangeTipusActionForm();
        form.setEntornAppId(1L);

        assertThat(validator.isValid(form, context)).isTrue();
    }
}
