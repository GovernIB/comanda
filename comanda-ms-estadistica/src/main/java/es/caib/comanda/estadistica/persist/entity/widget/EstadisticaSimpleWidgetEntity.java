package es.caib.comanda.estadistica.persist.entity.widget;

import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Entitat que representa un widget simple d'estadística dins de la base de dades.
 *
 * Aquesta classe hereta de `EstadisticaWidgetEntity` i està destinada a l'emmagatzematge i gestió de widgets simples d'estadística.
 * Permet configurar un indicador específic i associar informació contextual, com ara unitats i dimensions temporalment definides.
 *
 * Propietats:
 * - `unitat`: Text descriptiu que representa la unitat associada al valor de l'indicador. Per exemple, "dies". Longitud màxima de 64 caràcters.
 * - `indicador`: Referència al camp "IndicadorEntity" associat a aquest widget. L'indicador serveix com a font de dades per al widget.
 *
 * Validacions:
 * - `unitat` ha de complir la longitud màxima especificada i pot ser opcional.
 * - `indicador` és obligatori. Ha d'enllaçar a una entitat "IndicadorEntity" vàlida i present a la base de dades.
 *
 * Relacions:
 * - La propietat `indicador` utilitza una clau forana `view_indicador_fk` que assegura la integritat referencial amb la taula Indicador.
 * - Hereda les relacions i propietats generals definides a la classe base EstadisticaWidgetEntity.
 *
 * Funcionalitat:
 * Aquesta classe és útil per crear vistes senzilles basades en un únic indicador, funcionant com a part d'una solució més àmplia d'estadístiques.
 * Els widgets simples permeten mostrar informació resumida o específica d'un indicador amb un context definit.
 *
 * Persistència:
 * - La classe s'implementa com una entitat JPA i està associada a una taula única conjunta per a tots els tipus de widgets estadístics.
 * - Utilitza el valor discriminant "SIMPLE" per identificar el tipus dins de la jerarquia d'herència d'entitats.
 *
 * Ús:
 * Aquesta classe és especialment utilitzada per generar widgets simples dins d'aplicacions de gestió o report de dades estadístiques.
 * Ofereix suport al disseny, configuració i consulta de widgets associats a indicadors.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@Entity
@DiscriminatorValue("SIMPLE") // Valor específic al discriminador
public class EstadisticaSimpleWidgetEntity extends EstadisticaWidgetEntity<EstadisticaSimpleWidget> {

    @OneToOne(mappedBy = "widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private IndicadorTaulaEntity indicadorInfo;

    // Text a mostrar després del valor. Ex 20 "dies"
    @Column(name = "unitat", length = 64)
    private String unitat;

    // Mostrar la diferència percentual amb el període anterior (opcional)
    @Column(name = "comparar_periode_anterior")
    private boolean compararPeriodeAnterior;

}
