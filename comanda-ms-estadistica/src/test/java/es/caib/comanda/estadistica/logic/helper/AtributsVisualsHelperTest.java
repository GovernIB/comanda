package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.ms.logic.intf.exception.ObjectMappingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a AtributsVisualsHelper")
class AtributsVisualsHelperTest {

    // Instanciem directament ja que no té dependències injectades per constructor (només camp estàtic)
    private final AtributsVisualsHelper helper = new AtributsVisualsHelper();

    // Classe dummy per a les proves de serialització/deserialització real amb Jackson
    private static class DummyAtributsVisuals extends AtributsVisuals {
        private String valorProva;

        public String getValorProva() {
            return valorProva;
        }

        public void setValorProva(String valorProva) {
            this.valorProva = valorProva;
        }

        @Override
        public AtributsVisuals merge(AtributsVisuals other) {
            return null;
        }

        @Override
        public boolean hasOverrides() {
            return valorProva != null;
        }
    }

    // ========================================================================
    // 1. TESTOS PER A getAtributsVisuals(EstadisticaWidgetEntity)
    // ========================================================================

    @Test
    @DisplayName("getAtributsVisuals (Widget): retorna null quan l'entitat és null")
    void getAtributsVisualsWidget_quanEntitatEsNull_llavorsRetornaNull() {
        // Act
        AtributsVisuals result = helper.getAtributsVisuals((EstadisticaWidgetEntity) null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAtributsVisuals (Widget): retorna null quan el JSON és null")
    void getAtributsVisualsWidget_quanJsonEsNull_llavorsRetornaNull() {
        // Arrange
        EstadisticaWidgetEntity entity = mock(EstadisticaWidgetEntity.class);
        when(entity.getAtributsVisualsJson()).thenReturn(null);

        // Act
        AtributsVisuals result = helper.getAtributsVisuals(entity);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAtributsVisuals (Widget): deserialitza correctament el JSON a l'objecte especificat")
    void getAtributsVisualsWidget_quanJsonEsValid_llavorsDeserialitzaCorrectament() {
        // Arrange
        String jsonValid = "{\"valorProva\":\"test123\"}";
        EstadisticaWidgetEntity entity = mock(EstadisticaWidgetEntity.class);
        when(entity.getAtributsVisualsJson()).thenReturn(jsonValid);
        // Forcem el tipus retornat a la nostra classe dummy per a la prova
        when(entity.getAtributsVisualsType()).thenReturn((Class<? extends AtributsVisuals>) DummyAtributsVisuals.class);

        // Act
        AtributsVisuals result = helper.getAtributsVisuals(entity);

        // Assert
        assertThat(result).isInstanceOf(DummyAtributsVisuals.class);
        assertThat(((DummyAtributsVisuals) result).getValorProva()).isEqualTo("test123");
    }

    @Test
    @DisplayName("getAtributsVisuals (Widget): llança ObjectMappingException quan el JSON és invàlid")
    void getAtributsVisualsWidget_quanJsonEsInvalid_llavorsLlancaExcepcio() {
        // Arrange
        String jsonInvalid = "{ json mal format }";
        EstadisticaWidgetEntity entity = mock(EstadisticaWidgetEntity.class);
        when(entity.getAtributsVisualsJson()).thenReturn(jsonInvalid);
        when(entity.getAtributsVisualsType()).thenReturn((Class<? extends AtributsVisuals>) DummyAtributsVisuals.class);

        // Act & Assert
        assertThatThrownBy(() -> helper.getAtributsVisuals(entity))
            .isInstanceOf(ObjectMappingException.class)
            .hasMessageContaining("Error al deserialitzar la informació d'atributs visuals del widget");
    }

    // ========================================================================
    // 2. TESTOS PER A getAtributsVisuals(DashboardItemEntity)
    // ========================================================================

    @Test
    @DisplayName("getAtributsVisuals (DashboardItem): retorna null quan l'entitat és null")
    void getAtributsVisualsDashboardItem_quanEntitatEsNull_llavorsRetornaNull() {
        // Act
        AtributsVisuals result = helper.getAtributsVisuals((DashboardItemEntity) null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAtributsVisuals (DashboardItem): retorna null quan el JSON és null")
    void getAtributsVisualsDashboardItem_quanJsonEsNull_llavorsRetornaNull() {
        // Arrange
        DashboardItemEntity entity = mock(DashboardItemEntity.class);
        when(entity.getAtributsVisualsJson()).thenReturn(null);

        // Act
        AtributsVisuals result = helper.getAtributsVisuals(entity);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAtributsVisuals (DashboardItem): deserialitza correctament el JSON a l'objecte especificat")
    void getAtributsVisualsDashboardItem_quanJsonEsValid_llavorsDeserialitzaCorrectament() {
        // Arrange
        String jsonValid = "{\"valorProva\":\"dashboard456\"}";
        DashboardItemEntity entity = mock(DashboardItemEntity.class);
        EstadisticaWidgetEntity widget = mock(EstadisticaWidgetEntity.class);

        when(entity.getAtributsVisualsJson()).thenReturn(jsonValid);
        when(entity.getWidget()).thenReturn(widget);
        when(widget.getAtributsVisualsType()).thenReturn((Class<? extends AtributsVisuals>) DummyAtributsVisuals.class);

        // Act
        AtributsVisuals result = helper.getAtributsVisuals(entity);

        // Assert
        assertThat(result).isInstanceOf(DummyAtributsVisuals.class);
        assertThat(((DummyAtributsVisuals) result).getValorProva()).isEqualTo("dashboard456");
    }

    @Test
    @DisplayName("getAtributsVisuals (DashboardItem): llança ObjectMappingException quan el JSON és invàlid")
    void getAtributsVisualsDashboardItem_quanJsonEsInvalid_llavorsLlancaExcepcio() {
        // Arrange
        String jsonInvalid = "{ json mal format }";
        DashboardItemEntity entity = mock(DashboardItemEntity.class);
        EstadisticaWidgetEntity widget = mock(EstadisticaWidgetEntity.class);

        when(entity.getAtributsVisualsJson()).thenReturn(jsonInvalid);
        when(entity.getWidget()).thenReturn(widget);
        when(widget.getAtributsVisualsType()).thenReturn((Class<? extends AtributsVisuals>) DummyAtributsVisuals.class);

        // Act & Assert
        assertThatThrownBy(() -> helper.getAtributsVisuals(entity))
            .isInstanceOf(ObjectMappingException.class)
            .hasMessageContaining("Error al deserialitzar la informació d'atributs visuals del dashboardItem");
    }

    // ========================================================================
    // 3. TESTOS PER A getAtributsVisualsJson
    // ========================================================================

    @Test
    @DisplayName("getAtributsVisualsJson: retorna null quan l'objecte és null")
    void getAtributsVisualsJson_quanObjecteEsNull_llavorsRetornaNull() {
        // Act
        String result = helper.getAtributsVisualsJson(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getAtributsVisualsJson: serialitza correctament l'objecte a JSON")
    void getAtributsVisualsJson_quanObjecteEsValid_llavorsSerialitzaCorrectament() {
        // Arrange
        DummyAtributsVisuals atributs = new DummyAtributsVisuals();
        atributs.setValorProva("serialitzatOK");

        // Act
        String result = helper.getAtributsVisualsJson(atributs);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("\"valorProva\":\"serialitzatOK\"");
    }
}
