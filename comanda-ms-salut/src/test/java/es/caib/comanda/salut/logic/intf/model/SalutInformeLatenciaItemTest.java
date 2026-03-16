package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.salut.persist.entity.SalutEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class SalutInformeLatenciaItemTest {

    @Test
    void constructor_quanRepDate_converteixALocalDateTimeIManteLaLatencia() {
        Date data = Date.from(LocalDateTime.of(2026, 3, 16, 10, 15)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        SalutInformeLatenciaItem item = new SalutInformeLatenciaItem(data, 123);

        assertThat(item.getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 10, 15));
        assertThat(item.getLatenciaMitja()).isEqualTo(123);
    }

    @Test
    void constructor_quanRepSalutEntity_normalitzaLaDataSegonsLagregacio() {
        SalutEntity minut = sampleEntity(TipusRegistreSalut.MINUT, LocalDateTime.of(2026, 3, 16, 10, 15, 48), 40);
        SalutEntity hora = sampleEntity(TipusRegistreSalut.HORA, LocalDateTime.of(2026, 3, 16, 10, 15, 48), 50);
        SalutEntity dia = sampleEntity(TipusRegistreSalut.DIA, LocalDateTime.of(2026, 3, 16, 10, 15, 48), 60);

        assertThat(new SalutInformeLatenciaItem(minut).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 10, 15, 0));
        assertThat(new SalutInformeLatenciaItem(hora).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 10, 0, 0));
        assertThat(new SalutInformeLatenciaItem(dia).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 0, 0, 0));
        assertThat(new SalutInformeLatenciaItem(dia).getLatenciaMitja()).isEqualTo(60);
    }

    private static SalutEntity sampleEntity(TipusRegistreSalut tipus, LocalDateTime data, Integer latenciaMitjana) {
        SalutEntity entity = new SalutEntity();
        entity.setTipusRegistre(tipus);
        entity.setData(data);
        entity.setAppLatenciaMitjana(latenciaMitjana);
        return entity;
    }
}
