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
        // Durant updateEntityWithResource, el camp 'indicadorInfo' de l'entitat es posa a null
        // perquè el frontend envia els camps de l'indicador individualment (indicador, titolIndicador, etc.)
        // en lloc d'una referència a IndicadorTaula.
        // Si el widget ja existeix a la BD (entity.getId() != null), recuperem l'IndicadorTaulaEntity existent
        // per widget_id per actualitzar-lo en lloc d'inserir un duplicat, evitant errors de bloqueig optimista
        // i de files duplicades en el refresh()/merge() del BaseMutableResourceService.
        if (indicadorTaulaEntity == null && entity.getId() != null) {
            indicadorTaulaEntity = indicadorTaulaRepository.findByWidgetId(entity.getId());
        }
        if (indicadorTaulaEntity == null) {
            indicadorTaulaEntity = new IndicadorTaulaEntity();
            indicadorTaulaEntity.setWidget(entity);
        }
        indicadorTaulaEntity.setTitol(resource.getTitolIndicador());
        indicadorTaulaEntity.setAgregacio(resource.getTipusIndicador());
        indicadorTaulaEntity.setUnitatAgregacio(TableColumnsEnum.AVERAGE.equals(resource.getTipusIndicador()) ? resource.getPeriodeIndicador() : null);
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
            resource.setIndicador(ResourceReference.toResourceReference(indicador.getId(), indicador.getCodiNomDescription()));
            resource.setTitolIndicador(indicadorTaula.getTitol());
            resource.setTipusIndicador(indicadorTaula.getAgregacio());
            resource.setPeriodeIndicador(indicadorTaula.getUnitatAgregacio());
        }
    }

}
