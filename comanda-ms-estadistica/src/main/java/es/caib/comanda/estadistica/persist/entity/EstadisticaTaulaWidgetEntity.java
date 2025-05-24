package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaTaulaWidget;
import lombok.Getter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Entitat que representa un widget d'estadística tipus taula emmagatzemada a la base de dades.
 *
 * Aquesta classe hereta la funcionalitat bàsica dels widgets d'estadística i afegeix propietats específiques per a la configuració
 * d'un widget de taula. Permet definir columnes, agrupacions i altres criteris per la visualització de dades en format taula.
 *
 * Relacions:
 * - `columnes`: Una llista de columnes associades a aquest widget que seran utilitzades per mostrar dades.
 * - `dimensioAgrupacio`: Defineix la dimensió utilitzada per a agrupar les dades dins de la taula.
 *
 * Propietats:
 * - `columnes`: Llista de tipus `IndicadorTaulaEntity` que representa les columnes de la taula.
 * - `dimensioAgrupacio`: Una referència a l'entitat `DimensioEntity` que defineix l'agrupació principal de les dades.
 * - `titolAgrupament`: Text que especifica el títol de l'agrupació a nivell visual. Té una longitud màxima de 64 caràcters.
 *
 * Ús:
 * Aquesta entitat es fa servir per a persistir la configuració específica dels widgets d'estadística en format taula a la base de dades.
 *
 * Validacions:
 * - El títol de l'agrupament (`titolAgrupament`) no pot superar els 64 caràcters.
 *
 * Autor: Límit Tecnologies
 */
@Getter
@Entity
@DiscriminatorValue("TAULA") // Valor específic al discriminador
public class EstadisticaTaulaWidgetEntity extends EstadisticaWidgetEntity<EstadisticaTaulaWidget> {

    @OneToMany(mappedBy="widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndicadorTaulaEntity> columnes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agrupament_dimensio_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "view_taula_dimensio_fk")
    )
    private DimensioEntity dimensioAgrupacio;
    @Column(name = "agrupament_dimensio_titol", length = 64)
    private String titolAgrupament;
}
