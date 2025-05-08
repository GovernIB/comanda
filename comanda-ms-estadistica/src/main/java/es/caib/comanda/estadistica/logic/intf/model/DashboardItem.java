package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;

import javax.validation.constraints.NotNull;

/**
 * Classe que representa un element dins d'un quadre de comandament (Dashboard).
 *
 * Un DashboardItem defineix una configuració específica per presentar un component visual dins d'un Dashboard.
 * Conté informació de posició i dimensió, així com les referències al Dashboard al qual pertany
 * i al component visual específic que ha de mostrar.
 *
 * Propietats:
 * - posX: Posició horitzontal de l'element dins del Dashboard.
 * - posY: Posició vertical de l'element dins del Dashboard.
 * - width: Amplada de l'element dins del Dashboard.
 * - height: Alçada de l'element dins del Dashboard.
 * - dashboard: Referència al Dashboard on es troba aquest element.
 * - view: Referència al component visual (EstadisticaSimpleWidget) que aquest element representa.
 *
 * Aquesta classe hereta de BaseResource, utilitzant un identificador únic del tipus Long proporcionat per la classe base.
 *
 * @author Límit Tecnologies
 */
public class DashboardItem extends BaseResource<Long> {

    @NotNull
    private int posX;
    @NotNull
    private int posY;
    @NotNull
    private int width;
    @NotNull
    private int height;

    @NotNull
    private ResourceReference<Dashboard, Long> dashboard;
    @NotNull
    private ResourceReference<EstadisticaSimpleWidget, Long> view;

}
