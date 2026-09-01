package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.IndicadorRolEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
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

import java.util.ArrayList;
import java.util.List;
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
                    .filter(columnaResource -> columnaResource.getIndicador() != null && columnaResource.getIndicador().getId() != null)
                    .map(columnaResource -> {
                        var columna = new IndicadorTaulaEntity();
                        columna.setWidget(entity);
                        columna.setTitol(columnaResource.getTitol());
                        columna.setAgregacio(columnaResource.getAgregacio());
                        columna.setUnitatAgregacio(TableColumnsEnum.AVERAGE.equals(columnaResource.getAgregacio()) ? columnaResource.getUnitatAgregacio() : null);
                        indicadorRepository.findById(columnaResource.getIndicador().getId())
                                .ifPresent(columna::setIndicador);
                        return columna;
                    }).collect(Collectors.toList());
            indicadorTaulaRepository.saveAll(indicadors);
            entity.setIndicadorsInfo(indicadors);
        } else {
            IndicadorTaulaEntity indicadorTaulaEntity = findIndicadorTaulaByRol(entity, IndicadorRolEnum.VALOR);
            if (indicadorTaulaEntity == null) {
                // Compatibilitat amb UN_INDICADOR/UN_INDICADOR_AMB_DESCOMPOSICIO, que no usen 'rol':
                // la fila de valor és senzillament la primera (única) fila de la llista.
                indicadorTaulaEntity = entity.getIndicadorsInfo() == null || entity.getIndicadorsInfo().isEmpty()
                        ? null
                        : entity.getIndicadorsInfo().get(0);
            }
            if (indicadorTaulaEntity == null) {
                indicadorTaulaEntity = new IndicadorTaulaEntity();
                indicadorTaulaEntity.setWidget(entity);
            }
            indicadorTaulaEntity.setTitol(resource.getTitolIndicador());
            indicadorTaulaEntity.setAgregacio(resource.getAgregacio());
            indicadorTaulaEntity.setUnitatAgregacio(TableColumnsEnum.AVERAGE.equals(resource.getAgregacio()) ? resource.getUnitatAgregacio() : null);
            indicadorTaulaEntity.setRol(TipusGraficDataEnum.DOS_INDICADORS.equals(entity.getTipusDades()) ? IndicadorRolEnum.VALOR : null);
            if (resource.getIndicador() != null && resource.getIndicador().getId() != null) {
                if (Objects.isNull(indicadorTaulaEntity.getIndicadorId()) ||
                        !Objects.equals(indicadorTaulaEntity.getIndicadorId(), resource.getIndicador().getId())) {
                    indicadorRepository.findById(resource.getIndicador().getId())
                            .ifPresent(indicadorTaulaEntity::setIndicador);
                }
            }
            indicadorTaulaEntity = indicadorTaulaRepository.save(indicadorTaulaEntity);

            List<IndicadorTaulaEntity> novaLlista = new ArrayList<>();
            novaLlista.add(indicadorTaulaEntity);

            if (TipusGraficDataEnum.DOS_INDICADORS.equals(entity.getTipusDades())) {
                IndicadorTaulaEntity indicadorMaxEntity = upsertIndicadorMax(entity, resource);
                if (indicadorMaxEntity != null) {
                    novaLlista.add(indicadorMaxEntity);
                }
            } else {
                // Ja no és DOS_INDICADORS: eliminar una possible fila MAXIM residual d'una configuració anterior.
                IndicadorTaulaEntity maxResidual = findIndicadorTaulaByRol(entity, IndicadorRolEnum.MAXIM);
                if (maxResidual != null) {
                    indicadorTaulaRepository.delete(maxResidual);
                }
            }
            entity.setIndicadorsInfo(novaLlista);
        }
    }

    private IndicadorTaulaEntity upsertIndicadorMax(EstadisticaGraficWidgetEntity entity, EstadisticaGraficWidget resource) {
        if (resource.getIndicadorMax() == null || resource.getIndicadorMax().getId() == null) {
            return null;
        }
        IndicadorTaulaEntity indicadorMaxEntity = findIndicadorTaulaByRol(entity, IndicadorRolEnum.MAXIM);
        if (indicadorMaxEntity == null) {
            indicadorMaxEntity = new IndicadorTaulaEntity();
            indicadorMaxEntity.setWidget(entity);
        }
        indicadorMaxEntity.setTitol(resource.getTitolIndicadorMax());
        indicadorMaxEntity.setAgregacio(resource.getAgregacioMax());
        indicadorMaxEntity.setUnitatAgregacio(TableColumnsEnum.AVERAGE.equals(resource.getAgregacioMax()) ? resource.getUnitatAgregacioMax() : null);
        indicadorMaxEntity.setRol(IndicadorRolEnum.MAXIM);
        if (Objects.isNull(indicadorMaxEntity.getIndicadorId()) ||
                !Objects.equals(indicadorMaxEntity.getIndicadorId(), resource.getIndicadorMax().getId())) {
            indicadorRepository.findById(resource.getIndicadorMax().getId())
                    .ifPresent(indicadorMaxEntity::setIndicador);
        }
        return indicadorTaulaRepository.save(indicadorMaxEntity);
    }

    private IndicadorTaulaEntity findIndicadorTaulaByRol(EstadisticaGraficWidgetEntity entity, IndicadorRolEnum rol) {
        if (entity.getIndicadorsInfo() == null) {
            return null;
        }
        return entity.getIndicadorsInfo().stream()
                .filter(ind -> rol.equals(ind.getRol()))
                .findFirst()
                .orElse(null);
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
                                columna.getIndicador().getCodiNomDescription()));
                        return columnaResource;
                    }).collect(Collectors.toList());
            resource.setIndicadorsInfo(indicadorsResource);
        } else {
            IndicadorTaulaEntity indicadorTaula = findIndicadorTaulaByRol(entity, IndicadorRolEnum.VALOR);
            if (indicadorTaula == null && entity.getIndicadorsInfo() != null && !entity.getIndicadorsInfo().isEmpty()) {
                indicadorTaula = entity.getIndicadorsInfo().get(0);
            }
            if (indicadorTaula != null) {
                IndicadorEntity indicador = indicadorTaula.getIndicador();
                resource.setIndicador(ResourceReference.toResourceReference(indicador.getId(), indicador.getCodiNomDescription()));
                resource.setTitolIndicador(indicadorTaula.getTitol());
                resource.setAgregacio(indicadorTaula.getAgregacio());
                resource.setUnitatAgregacio(indicadorTaula.getUnitatAgregacio());
            }

            if (TipusGraficDataEnum.DOS_INDICADORS.equals(entity.getTipusDades())) {
                IndicadorTaulaEntity indicadorMaxTaula = findIndicadorTaulaByRol(entity, IndicadorRolEnum.MAXIM);
                if (indicadorMaxTaula != null) {
                    IndicadorEntity indicadorMax = indicadorMaxTaula.getIndicador();
                    resource.setIndicadorMax(ResourceReference.toResourceReference(indicadorMax.getId(), indicadorMax.getCodiNomDescription()));
                    resource.setTitolIndicadorMax(indicadorMaxTaula.getTitol());
                    resource.setAgregacioMax(indicadorMaxTaula.getAgregacio());
                    resource.setUnitatAgregacioMax(indicadorMaxTaula.getUnitatAgregacio());
                }
            }
        }
    }
}
