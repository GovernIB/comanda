package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletaColor;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.estadistica.logic.intf.service.PaletaService;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaColorEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat Paleta.
 *
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives a Paletans,
 * i s'estén de BaseMutableResourceService per oferir operacions bàsiques de lògica empresarial.
 *
 * Les accions específiques que es realitzen en aquesta classe estan directament relacionades amb la interfície PaletaService
 * i amb l'accés a les dades mitjançant l'entitat PaletaEntity.
 *
 * La classe utilitza el framework Spring per gestionar la injecció de dependències i s'anota com a servei (@Service),
 * a més d'utilitzar l'anotació @Slf4j per registrar informació de diagnòstic.
 *
 * Aquesta implementació pot ser utilitzada per altres components del sistema per proporcionar funcionalitats específiques relacionades
 * amb l'entitat Paleta.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PaletaServiceImpl extends BaseMutableResourceService<Paleta, Long, PaletaEntity> implements PaletaService {

    @Override
    protected void beforeCreateSave(PaletaEntity entity, Paleta resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        syncColors(entity, resource);
    }

    @Override
    protected void beforeUpdateSave(PaletaEntity entity, Paleta resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        syncColors(entity, resource);
    }

    @Override
    protected void afterConversion(PaletaEntity entity, Paleta resource) {
        resource.setClientId(entity.getId() != null ? String.valueOf(entity.getId()) : null);
        resource.setColors(toColorResources(entity));
    }

    private void syncColors(PaletaEntity entity, Paleta resource) {
        List<PaletaColorEntity> colors = entity.getColors();
        if (colors == null) {
            colors = new ArrayList<>();
            entity.setColors(colors);
        } else {
            colors.clear();
        }

        List<PaletaColor> resourceColors = resource.getColors();
        if (resourceColors == null) {
            return;
        }
        List<PaletaColorEntity> targetColors = colors;

        resourceColors.stream()
                .filter(color -> color != null && color.getValor() != null)
                .sorted(Comparator.comparing(
                        color -> color.getPosicio() == null ? Integer.MAX_VALUE : color.getPosicio()))
                .forEachOrdered(color -> {
                    PaletaColorEntity colorEntity = new PaletaColorEntity();
                    colorEntity.setPaleta(entity);
                    colorEntity.setPosicio(targetColors.size());
                    colorEntity.setValor(color.getValor());
                    targetColors.add(colorEntity);
                });
    }

    private List<PaletaColor> toColorResources(PaletaEntity entity) {
        List<PaletaColor> result = new ArrayList<>();
        if (entity.getColors() == null) {
            return result;
        }
        entity.getColors().stream()
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
