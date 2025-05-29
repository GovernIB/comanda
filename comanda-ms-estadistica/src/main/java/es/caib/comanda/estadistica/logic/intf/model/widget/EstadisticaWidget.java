package es.caib.comanda.estadistica.logic.intf.model.widget;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classe que representa un widget d'estadística tipus taula.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        quickFilterFields = { "titol", "descripcio" },
        descriptionField = "titol")
public class EstadisticaWidget extends WidgetBaseResource<Long> {

}
