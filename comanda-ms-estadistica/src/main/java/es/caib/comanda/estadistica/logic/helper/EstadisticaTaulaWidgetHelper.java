package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaTaulaWidget;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorTaulaRepository;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Lògica per a obtenir i consultar informació per als widgets estadístics simples.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstadisticaTaulaWidgetHelper {

    private final IndicadorRepository indicadorRepository;
    private final IndicadorTaulaRepository indicadorTaulaRepository;

    public void upsertColumnes(EstadisticaTaulaWidgetEntity entity, EstadisticaTaulaWidget resource) {

        // Eliminar columnes existents
        if (entity.getColumnes() != null && !entity.getColumnes().isEmpty()) {
            indicadorTaulaRepository.deleteAll(entity.getColumnes());
            entity.getColumnes().clear();
        }

        if (resource.getColumnes() == null) {
            return;
        }

        // Crear columnes noves
        var columnes = resource.getColumnes().stream()
                .map(columnaResource -> {
                    var columna = new IndicadorTaulaEntity();
                    columna.setWidget(entity);
                    columna.setTitol(columnaResource.getTitol());
                    columna.setAgregacio(columnaResource.getAgregacio());
                    columna.setUnitatAgregacio(columnaResource.getUnitatAgregacio());
                    if (columnaResource.getIndicador() != null && columnaResource.getIndicador().getId() != null) {
                        indicadorRepository.findById(columnaResource.getIndicador().getId())
                                .ifPresent(columna::setIndicador);
                    }
                    return columna;
                }).collect(Collectors.toList());
        indicadorTaulaRepository.saveAll(columnes);
    }

    public void afterCoversionGetColumnes(EstadisticaTaulaWidgetEntity entity, EstadisticaTaulaWidget resource) {
        if (entity.getColumnes() == null || entity.getColumnes().isEmpty() )
            return;

        var columnesResource = entity.getColumnes().stream()
                .map(columna -> {
                    var columnaResource = new IndicadorTaula();
                    columnaResource.setTitol(columna.getTitol());
                    columnaResource.setAgregacio(columna.getAgregacio());
                    columnaResource.setUnitatAgregacio(columna.getUnitatAgregacio());
                    columnaResource.setIndicador(ResourceReference.toResourceReference(
                            columna.getIndicador().getId(),
                            columna.getIndicador().getCodi()));
                    return columnaResource;
                }).collect(Collectors.toList());
        resource.setColumnes(columnesResource);
    }
}
