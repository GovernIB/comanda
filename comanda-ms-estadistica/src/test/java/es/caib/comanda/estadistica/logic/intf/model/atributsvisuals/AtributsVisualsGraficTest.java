package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per a AtributsVisualsGrafic.merge()")
class AtributsVisualsGraficTest {

    @Test
    @DisplayName("merge: quan els camps propis són null, s'agafen tots els valors de l'altre objecte")
    void merge_quanCampsPropisSonNull_llavorsAgafaValorsDeLAltre() {
        // Arrange
        AtributsVisualsGrafic resolved = new AtributsVisualsGrafic();
        AtributsVisualsGrafic other = new AtributsVisualsGrafic();
        other.setArea(true);
        other.setPieDonut(true);
        other.setPieShowLabels(false);
        other.setOuterRadius(100);
        other.setInnerRadius(50);
        other.setLabelSize(14);

        // Act
        AtributsVisualsGrafic result = (AtributsVisualsGrafic) resolved.merge(other);

        // Assert
        assertThat(result.getArea()).isEqualTo(true);
        assertThat(result.getPieDonut()).isEqualTo(true);
        assertThat(result.getPieShowLabels()).isEqualTo(false);
        assertThat(result.getOuterRadius()).isEqualTo(100);
        assertThat(result.getInnerRadius()).isEqualTo(50);
        assertThat(result.getLabelSize()).isEqualTo(14);
    }
}
