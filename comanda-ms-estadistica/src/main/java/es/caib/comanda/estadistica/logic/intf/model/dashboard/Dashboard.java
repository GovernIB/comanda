package es.caib.comanda.estadistica.logic.intf.model.dashboard;

import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Classe que representa un quadre de comandament (Dashboard).
 *
 * Un quadre de comandament conté la informació necessària per organitzar elements visuals (DashboardItems)
 * que proporcionen estadístiques o informes de dades rellevants.
 * Cada Dashboard està vinculat a un conjunt d'elements amb configuracions predefinides dins de les seves propietats.
 *
 * Propietats:
 * - titol: El títol del quadre de comandament, limitat a 64 caràcters.
 * - descripcio: Una descripció opcional del quadre de comandament, limitada a 1024 caràcters.
 * - items: Una llista d'elements (DashboardItem) que defineixen els components visuals del Dashboard.
 *
 * Aquesta classe hereta de BaseResource i inclou un identificador únic del tipus Long proporcionat per la classe base.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        descriptionField = "titol",
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.REPORT, code = Dashboard.WIDGETS_REPORT),
        }
)
public class Dashboard extends BaseResource<Long> {

    public final static String WIDGETS_REPORT = "widgets_data";

    @NotNull
    @Size(max = 64)
    private String titol;
    @Size(max = 1024)
    private String descripcio;

    @NotEmpty
    private List<DashboardItem> items;
    
}
