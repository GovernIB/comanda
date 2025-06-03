package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Lògica per a obtenir i consultar informació per als widgets estadístics simples.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstadisticaSimpleWidgetHelper {

    private final IndicadorRepository indicadorRepository;
    private final IndicadorTaulaRepository indicadorTaulaRepository;

    /** Crea o actualitza l'entitat {@link IndicadorTaulaEntity} amb les dades del widget **/
    public void upsertIndicadorTaula(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource) {
        IndicadorTaulaEntity indicadorTaulaEntity = entity.getIndicadorInfo();
        if (indicadorTaulaEntity == null) {
            indicadorTaulaEntity = new IndicadorTaulaEntity();
            indicadorTaulaEntity.setWidget(entity);
        }
        indicadorTaulaEntity.setTitol(resource.getTitolIndicador());
        indicadorTaulaEntity.setAgregacio(resource.getTipusIndicador());
        indicadorTaulaEntity.setUnitatAgregacio(resource.getPeriodeIndicador());
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

    /** Assigna al widget els atributs provinents de {@link IndicadorTaulaEntity} **/
    public void afterCoversionGetIndicadorTaulaAtributes(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource) {
        if (Objects.nonNull(entity.getIndicadorInfo()) && Objects.nonNull(entity.getIndicadorInfo().getIndicador())) {
            IndicadorTaulaEntity indicadorTaula = entity.getIndicadorInfo();
            IndicadorEntity indicador = indicadorTaula.getIndicador();
            resource.setIndicador(ResourceReference.toResourceReference(indicador.getId(), indicador.getCodi()));
            resource.setTitolIndicador(indicadorTaula.getTitol());
            resource.setTipusIndicador(indicadorTaula.getAgregacio());
            resource.setPeriodeIndicador(indicadorTaula.getUnitatAgregacio());
        }
    }

}
