package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per a AtributsVisualsTitol.merge()")
class AtributsVisualsTitolTest {

    @Test
    @DisplayName("merge: colorTitol s'agafa de other.colorTitol, no de other.colorText")
    void merge_quanColorTitolPropiEsNull_llavorsAgafaColorTitolDeLAltre() {
        // Arrange
        AtributsVisualsTitol resolved = new AtributsVisualsTitol();
        AtributsVisualsTitol other = new AtributsVisualsTitol();
        other.setColorTitol("#ff0000");
        other.setColorText("#00ff00");

        // Act
        AtributsVisualsTitol result = (AtributsVisualsTitol) resolved.merge(other);

        // Assert
        assertThat(result.getColorTitol()).isEqualTo("#ff0000");
    }
}
