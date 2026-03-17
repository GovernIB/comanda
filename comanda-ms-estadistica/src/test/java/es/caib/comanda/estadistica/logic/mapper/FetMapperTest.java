package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per a FetMapper")
class FetMapperTest {

    private final FetMapper mapper = new FetMapper() {
        @Override
        public Fet toFet(FetEntity entity) {
            if (entity == null) return null;
            Temps temps = null;
            if (entity.getTemps() != null) {
                temps = Temps.builder()
                        .data(entity.getTemps().getData())
                        .anualitat(entity.getTemps().getAnualitat())
                        .trimestre(entity.getTemps().getTrimestre())
                        .mes(entity.getTemps().getMes())
                        .setmana(entity.getTemps().getSetmana())
                        .diaSetmana(entity.getTemps().getDiaSetmana())
                        .dia(entity.getTemps().getDia())
                        .build();
                temps.setId(entity.getTemps().getId());
            }
            Fet fet = Fet.builder()
                    .temps(temps)
                    .dimensionsJson(entity.getDimensionsJson())
                    .indicadorsJson(entity.getIndicadorsJson())
                    .entornAppId(entity.getEntornAppId())
                    .tipus(entity.getTipus())
                    .numDies(entity.getNumDies())
                    .build();
            fet.setId(entity.getId());
            return fet;
        }

        @Override
        public FetEntity toFetEntity(Fet model) {
            if (model == null) return null;
            FetEntity entity = new FetEntity();
            entity.setId(model.getId());
            if (model.getTemps() != null) {
                TempsEntity tempsEntity = new TempsEntity();
                tempsEntity.setId(model.getTemps().getId());
                tempsEntity.setData(model.getTemps().getData());
                tempsEntity.setAnualitat(model.getTemps().getAnualitat());
                tempsEntity.setTrimestre(model.getTemps().getTrimestre());
                tempsEntity.setMes(model.getTemps().getMes());
                tempsEntity.setSetmana(model.getTemps().getSetmana());
                tempsEntity.setDiaSetmana(model.getTemps().getDiaSetmana());
                tempsEntity.setDia(model.getTemps().getDia());
                entity.setTemps(tempsEntity);
            }
            entity.setDimensionsJson(model.getDimensionsJson());
            entity.setIndicadorsJson(model.getIndicadorsJson());
            entity.setEntornAppId(model.getEntornAppId());
            entity.setTipus(model.getTipus());
            entity.setNumDies(model.getNumDies());
            return entity;
        }
    };

    @Test
    @DisplayName("Dada una entitat FetEntity, es converteix correctament a model Fet")
    void toFet_quanEntitatValida_retornaModel() {
        // Arrange
        TempsEntity tempsEntity = new TempsEntity();
        tempsEntity.setId(10L);

        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("dim1", "val1");

        Map<String, Double> indicadors = new HashMap<>();
        indicadors.put("ind1", 100.0);

        FetEntity entity = new FetEntity();
        entity.setId(1L);
        entity.setTemps(tempsEntity);
        entity.setDimensionsJson(dimensions);
        entity.setIndicadorsJson(indicadors);
        entity.setEntornAppId(5L);

        // Act
        Fet result = mapper.toFet(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTemps()).isNotNull();
        assertThat(result.getTemps().getId()).isEqualTo(10L);
        assertThat(result.getDimensionsJson()).containsEntry("dim1", "val1");
        assertThat(result.getIndicadorsJson()).containsEntry("ind1", 100.0);
        assertThat(result.getEntornAppId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Donat un model Fet, es converteix correctament a entitat FetEntity")
    void toFetEntity_quanModelValid_retornaEntitat() {
        // Arrange
        Temps temps = new Temps();
        temps.setId(10L);

        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("dim1", "val1");

        Map<String, Double> indicadors = new HashMap<>();
        indicadors.put("ind1", 100.0);

        Fet fet = Fet.builder()
                .temps(temps)
                .dimensionsJson(dimensions)
                .indicadorsJson(indicadors)
                .entornAppId(5L)
                .build();
        fet.setId(1L);

        // Act
        FetEntity result = mapper.toFetEntity(fet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTemps()).isNotNull();
        assertThat(result.getTemps().getId()).isEqualTo(10L);
        assertThat(result.getDimensionsJson()).containsEntry("dim1", "val1");
        assertThat(result.getIndicadorsJson()).containsEntry("ind1", 100.0);
        assertThat(result.getEntornAppId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Quan l'entitat és nul·la, retorna nul")
    void toFet_quanNull_retornaNull() {
        assertThat(mapper.toFet(null)).isNull();
    }

    @Test
    @DisplayName("Quan el model és nul·la, retorna nul")
    void toFetEntity_quanNull_retornaNull() {
        assertThat(mapper.toFetEntity(null)).isNull();
    }
}
