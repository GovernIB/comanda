package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests per a {@link AtributsVisualsTaula}, centrats en el comportament de {@code merge} i
 * {@code hasOverrides}, en particular pel camp {@code compacte}.
 *
 * @author Límit Tecnologies
 */
@DisplayName("Tests per a AtributsVisualsTaula")
class AtributsVisualsTaulaTest {

    // ========================================================================
    // 1. TESTOS PER A hasOverrides
    // ========================================================================

    @Test
    @DisplayName("hasOverrides: retorna false quan cap camp és no nul")
    void hasOverrides_quanCapCampEsNoNul_retornaFalse() {
        AtributsVisualsTaula atributsVisuals = AtributsVisualsTaula.builder().build();

        assertThat(atributsVisuals.hasOverrides()).isFalse();
    }

    @Test
    @DisplayName("hasOverrides: retorna true quan compacte és no nul")
    void hasOverrides_quanCompacteEsNoNul_retornaTrue() {
        AtributsVisualsTaula atributsVisuals = AtributsVisualsTaula.builder().compacte(true).build();

        assertThat(atributsVisuals.hasOverrides()).isTrue();
    }

    @Test
    @DisplayName("hasOverrides: retorna true quan midaFontTaula és no nul")
    void hasOverrides_quanMidaFontTaulaEsNoNul_retornaTrue() {
        AtributsVisualsTaula atributsVisuals = AtributsVisualsTaula.builder().midaFontTaula(16).build();

        assertThat(atributsVisuals.hasOverrides()).isTrue();
    }

    // ========================================================================
    // 2. TESTOS PER A merge
    // ========================================================================

    @Test
    @DisplayName("merge: quan compacte no està definit al widget, s'usa el valor de la plantilla")
    void merge_quanCompacteNoDefinitAlWidget_usaElValorDeLaPlantilla() {
        AtributsVisualsTaula widget = AtributsVisualsTaula.builder().build();
        AtributsVisualsTaula plantilla = AtributsVisualsTaula.builder().compacte(true).build();

        AtributsVisualsTaula result = (AtributsVisualsTaula) widget.merge(plantilla);

        assertThat(result.getCompacte()).isTrue();
    }

    @Test
    @DisplayName("merge: quan compacte està definit al widget, es manté i no es sobreescriu amb la plantilla")
    void merge_quanCompacteDefinitAlWidget_esManteIgnorantLaPlantilla() {
        AtributsVisualsTaula widget = AtributsVisualsTaula.builder().compacte(false).build();
        AtributsVisualsTaula plantilla = AtributsVisualsTaula.builder().compacte(true).build();

        AtributsVisualsTaula result = (AtributsVisualsTaula) widget.merge(plantilla);

        assertThat(result.getCompacte()).isFalse();
    }

    @Test
    @DisplayName("merge: quan midaFontTaula no està definit al widget, s'usa el valor de la plantilla")
    void merge_quanMidaFontTaulaNoDefinitAlWidget_usaElValorDeLaPlantilla() {
        AtributsVisualsTaula widget = AtributsVisualsTaula.builder().build();
        AtributsVisualsTaula plantilla = AtributsVisualsTaula.builder().midaFontTaula(16).build();

        AtributsVisualsTaula result = (AtributsVisualsTaula) widget.merge(plantilla);

        assertThat(result.getMidaFontTaula()).isEqualTo(16);
    }

    @Test
    @DisplayName("merge: quan midaFontTaula està definit al widget, es manté i no es sobreescriu amb la plantilla")
    void merge_quanMidaFontTaulaDefinitAlWidget_esManteIgnorantLaPlantilla() {
        AtributsVisualsTaula widget = AtributsVisualsTaula.builder().midaFontTaula(20).build();
        AtributsVisualsTaula plantilla = AtributsVisualsTaula.builder().midaFontTaula(16).build();

        AtributsVisualsTaula result = (AtributsVisualsTaula) widget.merge(plantilla);

        assertThat(result.getMidaFontTaula()).isEqualTo(20);
    }

    @Test
    @DisplayName("merge: quan otherAtributsVisuals no és una AtributsVisualsTaula, retorna this sense modificar")
    void merge_quanOtherNoEsAtributsVisualsTaula_retornaThisSenseModificar() {
        AtributsVisualsTaula widget = AtributsVisualsTaula.builder().compacte(true).build();
        AtributsVisualsSimple other = AtributsVisualsSimple.builder().build();

        AtributsVisuals result = widget.merge(other);

        assertThat(result).isSameAs(widget);
        assertThat(((AtributsVisualsTaula) result).getCompacte()).isTrue();
    }
}
