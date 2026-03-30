package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DiaSetmanaEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per a TempsMapper")
class TempsMapperTest {

    private final TempsMapper mapper = new TempsMapper() {
        @Override
        public Temps toTemps(TempsEntity entity) {
            if (entity == null) return null;
            return Temps.builder()
                    .data(entity.getData())
                    .anualitat(entity.getAnualitat())
                    .trimestre(entity.getTrimestre())
                    .mes(entity.getMes())
                    .setmana(entity.getSetmana())
                    .diaSetmana(entity.getDiaSetmana())
                    .dia(entity.getDia())
                    .build();
        }

        @Override
        public TempsEntity toTempsEntity(Temps temps) {
            if (temps == null) return null;
            TempsEntity entity = new TempsEntity();
            entity.setId(temps.getId());
            entity.setData(temps.getData());
            entity.setAnualitat(temps.getAnualitat());
            entity.setTrimestre(temps.getTrimestre());
            entity.setMes(temps.getMes());
            entity.setSetmana(temps.getSetmana());
            entity.setDiaSetmana(temps.getDiaSetmana());
            entity.setDia(temps.getDia());
            return entity;
        }
    };

    @Test
    @DisplayName("Dada una entitat TempsEntity, es converteix correctament a model Temps")
    void toTemps_quanEntitatValida_retornaModel() {
        // Arrange
        LocalDate ara = LocalDate.now();
        TempsEntity entity = new TempsEntity(ara);
        entity.setId(1L);

        // Act
        Temps result = mapper.toTemps(entity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getData()).isEqualTo(ara);
        assertThat(result.getAnualitat()).isEqualTo(ara.getYear());
        assertThat(result.getMes()).isEqualTo(ara.getMonthValue());
        assertThat(result.getDia()).isEqualTo(ara.getDayOfMonth());
        assertThat(result.getDiaSetmana()).isEqualTo(DiaSetmanaEnum.valueOfDayOfWeek(ara.getDayOfWeek()));
    }

    @Test
    @DisplayName("Donat un model Temps, es converteix correctament a entitat TempsEntity")
    void toTempsEntity_quanModelValid_retornaEntitat() {
        // Arrange
        LocalDate ara = LocalDate.now();
        Temps temps = Temps.builder()
                .data(ara)
                .anualitat(ara.getYear())
                .trimestre(1)
                .mes(ara.getMonthValue())
                .setmana(10)
                .diaSetmana(DiaSetmanaEnum.DL)
                .dia(ara.getDayOfMonth())
                .build();
        temps.setId(1L);

        // Act
        TempsEntity result = mapper.toTempsEntity(temps);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getData()).isEqualTo(ara);
        assertThat(result.getAnualitat()).isEqualTo(ara.getYear());
        assertThat(result.getDiaSetmana()).isEqualTo(DiaSetmanaEnum.DL);
    }

    @Test
    @DisplayName("Quan l'entitat és nul·la, retorna nul")
    void toTemps_quanNull_retornaNull() {
        assertThat(mapper.toTemps(null)).isNull();
    }

    @Test
    @DisplayName("Quan el model és nul·la, retorna nul")
    void toTempsEntity_quanNull_retornaNull() {
        assertThat(mapper.toTempsEntity(null)).isNull();
    }
}
