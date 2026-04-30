package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.paleta.Plantilla;
import es.caib.comanda.estadistica.logic.intf.service.PlantillaService;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.repository.PaletaRepository;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat Plantilla.
 *
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives a Plantillans,
 * i s'estén de BaseMutableResourceService per oferir operacions bàsiques de lògica empresarial.
 *
 * Les accions específiques que es realitzen en aquesta classe estan directament relacionades amb la interfície PlantillaService
 * i amb l'accés a les dades mitjançant l'entitat PlantillaEntity.
 *
 * La classe utilitza el framework Spring per gestionar la injecció de dependències i s'anota com a servei (@Service),
 * a més d'utilitzar l'anotació @Slf4j per registrar informació de diagnòstic.
 *
 * Aquesta implementació pot ser utilitzada per altres components del sistema per proporcionar funcionalitats específiques relacionades
 * amb l'entitat Plantilla.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PlantillaServiceImpl extends BaseMutableResourceService<Plantilla, Long, PlantillaEntity> implements PlantillaService {

    private void afterSave(PlantillaEntity entity, Plantilla resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        entity.setPaletes(
                resource.getColors().entrySet().stream()
                        .filter((entry) -> entry.getKey() != null && entry.getValue() != null)
                        .map((entry) -> {
                            PaletaEntity paleta = new PaletaEntity();
                            paleta.setKey(entry.getKey());
                            paleta.setValue(entry.getValue()); // o getColor() si renombras el getter
                            paleta.setPlantilla(entity);       // ⚠️ Imprescindible para sincronizar bidireccional
                            return paleta;
                        })
                        .collect(Collectors.toList())
        );
    }

    @Override
    protected void afterCreateSave(PlantillaEntity entity, Plantilla resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        afterSave(entity, resource, answers, anyOrderChanged);
    }

    @Override
    protected void afterUpdateSave(PlantillaEntity entity, Plantilla resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        afterSave(entity, resource, answers, anyOrderChanged);
    }

    @Override
    protected void afterConversion(PlantillaEntity entity, Plantilla resource) {
        resource.setColors(
                entity.getPaletes().stream()
                        .collect(Collectors.toMap(
                                PaletaEntity::getKey,
                                PaletaEntity::getValue,
                                (existing, replacement) -> existing // manejo de claves duplicadas
                        ))
        );
    }
}
