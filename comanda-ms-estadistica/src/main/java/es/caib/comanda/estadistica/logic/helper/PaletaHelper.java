package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletaColor;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaColorEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.repository.PaletaColorRepository;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/** Helper per a la gestió de la lògica de negoci relacionada amb les paletes de colors **/
@Slf4j
@Component
@RequiredArgsConstructor
public class PaletaHelper {

    private final PaletaColorRepository paletaColorRepository;

    /**
     * Sincronitza els colors d'una paleta entre l'entitat de persistència i el recurs rebut del front.
     * <p>
     * El procés segueix aquests passos:
     * <ol>
     *   <li>Normalitza les posicions dels colors del recurs per garantir una seqüència correlativa [0, 1, 2, ...]</li>
     *   <li>Carrega els colors existents de la base de dades per a la paleta indicada</li>
     *   <li>Actualitza els colors existents que coincideixen per posició</li>
     *   <li>Crea nous colors per a les posicions que no existien prèviament</li>
     *   <li>Elimina els colors de la BD que ja no apareixen al recurs (orphaned)</li>
     *   <li>Actualitza la col·lecció de l'entitat perquè Hibernate gestioni correctament la relació</li>
     * </ol>
     * <p>
     * Aquest mètode gestiona explícitament les operacions de persistència (deleteAll, saveAll)
     * per evitar conflictes amb la restricció única (palette_id, posicio).
     */
    public void syncColors(PaletaEntity entity, Paleta resource) {
        List<PaletaColor> normalizedColors = normalizePositions(resource.getColors());
        List<PaletaColorEntity> existingColors = paletaColorRepository.findByPaletaId(entity.getId());
        Map<Integer, PaletaColorEntity> existingByPosicio = existingColors.stream()
                .filter(c -> c.getPosicio() != null)
                .collect(Collectors.toMap(PaletaColorEntity::getPosicio, c -> c));
        List<PaletaColorEntity> toKeep = new ArrayList<>();
        for (int pos = 0; pos < normalizedColors.size(); pos++) {
            PaletaColor colorResource = normalizedColors.get(pos);
            if (colorResource == null || colorResource.getValor() == null) {
                continue;
            }
            PaletaColorEntity colorEntity;
            if (existingByPosicio.containsKey(pos)) {
                colorEntity = existingByPosicio.get(pos);
                existingByPosicio.remove(pos);
                log.debug("Actualizando color en posicio={} valor={}", pos, colorResource.getValor());
            } else {
                colorEntity = new PaletaColorEntity();
                colorEntity.setPaleta(entity);
                colorEntity.setPosicio(pos);
                log.debug("Creando color nuevo en posicio={} valor={}", pos, colorResource.getValor());
            }
            colorEntity.setValor(colorResource.getValor());
            toKeep.add(colorEntity);
        }
        List<PaletaColorEntity> toDelete = new ArrayList<>(existingByPosicio.values());
        if (!toDelete.isEmpty()) {
            log.debug("Eliminando {} colores en posiciones no presentes en el resource: {}",
                    toDelete.size(),
                    toDelete.stream().map(PaletaColorEntity::getPosicio).collect(Collectors.toList()));
            paletaColorRepository.deleteAll(toDelete);
        }
        if (!toKeep.isEmpty()) {
            log.debug("Guardando {} colores (actualizados + nuevos)", toKeep.size());
            paletaColorRepository.saveAll(toKeep);
        }
        entity.setColors(toKeep);
    }

    /** Normaliza la lista de colores para garantizar posiciones correlativas [0, 1, 2, ...]. */
    private List<PaletaColor> normalizePositions(List<PaletaColor> colors) {
        if (colors == null || colors.isEmpty()) {
            return Collections.emptyList();
        }
        return colors.stream()
            .filter(c -> c != null && c.getValor() != null)
            .sorted(Comparator.comparing(
                    c -> c.getPosicio() == null ? Integer.MAX_VALUE : c.getPosicio()))
            .peek(c -> c.setPosicio(null))
            .collect(Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                        // Reasignar posiciones [0, 1, 2, ...]
                        for (int i = 0; i < list.size(); i++) {
                            list.get(i).setPosicio(i);
                        }
                        return list;
                    }
            ));
    }

    /** Converteix una entitat {@link PaletaEntity} a una llista de recursos {@link PaletaColor}. **/
    public List<PaletaColor> paletaEntitytoColorResources(PaletaEntity entity) {
        List<PaletaColor> result = new ArrayList<>();
        List<PaletaColorEntity> existingColors = paletaColorRepository.findByPaletaIdOrderByPosicioAsc(entity.getId());
        if (existingColors == null) {
            return result;
        }
        existingColors.stream()
                .sorted(Comparator.comparing(color -> color.getPosicio() == null ? Integer.MAX_VALUE : color.getPosicio()))
                .forEach(color -> {
                    PaletaColor resource = new PaletaColor();
                    resource.setId(color.getId());
                    resource.setPaleta(ResourceReference.toResourceReference(entity.getId(), entity.getNom()));
                    resource.setPosicio(color.getPosicio());
                    resource.setValor(color.getValor());
                    result.add(resource);
                });
        return result;
    }
}
