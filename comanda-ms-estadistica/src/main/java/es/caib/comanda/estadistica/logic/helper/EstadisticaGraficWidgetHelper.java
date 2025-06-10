package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaGraficWidget;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorTaulaRepository;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Lògica per a obtenir i consultar informació per als widgets estadístics simples.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstadisticaGraficWidgetHelper {

    private final IndicadorRepository indicadorRepository;
    private final IndicadorTaulaRepository indicadorTaulaRepository;

    public void upsertColumnes(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource) {

        if (TipusGraficDataEnum.VARIS_INDICADORS.equals(entity.getTipusDades())) {
            // Eliminar indicadors existents
            if (entity.getIndicadorsInfo() != null && !entity.getIndicadorsInfo().isEmpty()) {
                indicadorTaulaRepository.deleteAll(entity.getIndicadorsInfo());
                entity.getIndicadorsInfo().clear();
            }

            if (resource.getIndicadorsInfo() == null) {
                return;
            }

            // Crear indicadors noves
            var indicadors = resource.getIndicadorsInfo().stream()
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
            indicadorTaulaRepository.saveAll(indicadors);
        } else {
            IndicadorTaulaEntity indicadorTaulaEntity = entity.getIndicadorInfo();
            if (indicadorTaulaEntity == null) {
                indicadorTaulaEntity = new IndicadorTaulaEntity();
                indicadorTaulaEntity.setWidget(entity);
            }
            indicadorTaulaEntity.setTitol(resource.getTitolIndicador());
            indicadorTaulaEntity.setAgregacio(resource.getAgregacio());
            indicadorTaulaEntity.setUnitatAgregacio(resource.getTempsAgrupacio());
            if (resource.getIndicador() != null && resource.getIndicador().getId() != null) {
                if (Objects.isNull(indicadorTaulaEntity.getIndicadorId()) ||
                        !Objects.equals(indicadorTaulaEntity.getIndicadorId(), resource.getIndicador().getId())) {
                    indicadorRepository.findById(resource.getIndicador().getId())
                            .ifPresent(indicadorTaulaEntity::setIndicador);
                }
            }
            indicadorTaulaEntity = indicadorTaulaRepository.save(indicadorTaulaEntity);
            entity.setIndicadorInfo(indicadorTaulaEntity);
        }

        if (TipusGraficDataEnum.DOS_INDICADORS.equals(entity.getTipusDades())) {
            // TODO: Segon indicador
        }
    }

    public void afterCoversionGetColumnes(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource) {
        if (TipusGraficDataEnum.VARIS_INDICADORS.equals(entity.getTipusDades())) {
            if (entity.getIndicadorsInfo() == null || entity.getIndicadorsInfo().isEmpty())
                return;

            var indicadorsResource = entity.getIndicadorsInfo().stream()
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
            resource.setIndicadorsInfo(indicadorsResource);
        } else {
            if (Objects.nonNull(entity.getIndicadorInfo()) && Objects.nonNull(entity.getIndicadorInfo().getIndicador())) {
                IndicadorTaulaEntity indicadorTaula = entity.getIndicadorInfo();
                IndicadorEntity indicador = indicadorTaula.getIndicador();
                resource.setIndicador(ResourceReference.toResourceReference(indicador.getId(), indicador.getCodi()));
                resource.setTitolIndicador(indicadorTaula.getTitol());
                resource.setAgregacio(indicadorTaula.getAgregacio());
                resource.setTempsAgrupacio(indicadorTaula.getUnitatAgregacio());
            }
        }

        if (TipusGraficDataEnum.DOS_INDICADORS.equals(entity.getTipusDades())) {
            // TODO: Segon indicador
        }
    }
}
