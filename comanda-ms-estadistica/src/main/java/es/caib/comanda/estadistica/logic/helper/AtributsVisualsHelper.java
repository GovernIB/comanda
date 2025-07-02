package es.caib.comanda.estadistica.logic.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaWidget;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.ms.logic.intf.exception.ObjectMappingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Slf4j
@Component
@RequiredArgsConstructor
public class AtributsVisualsHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public AtributsVisuals getAtributsVisuals(EstadisticaWidgetEntity entity) {
        if (entity == null || entity.getAtributsVisualsJson() == null) return null;
        try {
            Class<? extends AtributsVisuals> atributsVisualsType = entity.getAtributsVisualsType();
            AtributsVisuals atributsVisuals = objectMapper.readValue(
                    entity.getAtributsVisualsJson(),
                    new TypeReference<>() {
                        @Override
                        public Type getType() {
                            return atributsVisualsType;
                        }
                    }
            );
            return atributsVisuals;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ObjectMappingException(
                    EstadisticaWidgetEntity.class,
                    EstadisticaWidget.class,
                    "Error al deserialitzar la informació d'atributs visuals del widget: " + e.getMessage());
        }
    }

    public AtributsVisuals getAtributsVisuals(DashboardItemEntity entity) {
        if (entity == null || entity.getAtributsVisualsJson() == null) return null;
        try {
            Class<? extends AtributsVisuals> atributsVisualsType = entity.getWidget().getAtributsVisualsType();
            AtributsVisuals atributsVisuals = objectMapper.readValue(
                    entity.getAtributsVisualsJson(),
                    new TypeReference<>() {
                        @Override
                        public Type getType() {
                            return atributsVisualsType;
                        }
                    }
            );
            return atributsVisuals;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ObjectMappingException(
                    DashboardItemEntity.class,
                    DashboardItem.class,
                    "Error al deserialitzar la informació d'atributs visuals del dashboardItem: " + e.getMessage());
        }
    }

    public String getAtributsVisualsJson(AtributsVisuals atributsVisuals) {
        if (atributsVisuals == null) return null;
        try {
            return objectMapper.writeValueAsString(atributsVisuals);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ObjectMappingException(
                    AtributsVisuals.class,
                    AtributsVisuals.class,
                    "Error al serialitzar la informació d'atributs visuals: " + e.getMessage());
        }
    }
}
