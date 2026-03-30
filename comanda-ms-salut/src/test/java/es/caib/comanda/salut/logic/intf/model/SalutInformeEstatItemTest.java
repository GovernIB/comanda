package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.salut.persist.entity.SalutEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SalutInformeEstatItemTest {

    @Test
    void constructor_quanTipusEsMinut_usaLaDataAmbSegonsANulICalculaPercentatges() {
        SalutEntity entity = sampleEntity(TipusRegistreSalut.MINUT, LocalDateTime.of(2026, 3, 16, 10, 11, 59), 4);
        entity.updateAppCountByEstat(SalutEstat.UP);
        entity.updateAppCountByEstat(SalutEstat.UP);
        entity.updateAppCountByEstat(SalutEstat.WARN);
        entity.updateAppCountByEstat(SalutEstat.DOWN);

        SalutInformeEstatItem item = new SalutInformeEstatItem(entity);

        assertThat(item.getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 10, 11, 0));
        assertThat(item.getUpPercent()).isEqualTo(50.0);
        assertThat(item.getWarnPercent()).isEqualTo(25.0);
        assertThat(item.getDownPercent()).isEqualTo(25.0);
        assertThat(item.getNotUpCount()).isEqualTo(1);
        assertThat(item.isAlwaysUp()).isFalse();
        assertThat(item.isAlwaysDown()).isFalse();
    }

    @Test
    void constructor_quanTipusEsHoraODia_normalitzaLaDataISuportaTotalsZero() {
        SalutEntity hora = sampleEntity(TipusRegistreSalut.HORA, LocalDateTime.of(2026, 3, 16, 10, 59, 25), 0);
        SalutEntity dia = sampleEntity(TipusRegistreSalut.DIA, LocalDateTime.of(2026, 3, 16, 10, 59, 25), 100);
        for (int i = 0; i < 30; i++) {
            dia.updateAppCountByEstat(SalutEstat.DOWN);
            dia.updateAppCountByEstat(SalutEstat.ERROR);
        }
        for (int i = 0; i < 20; i++) {
            dia.updateAppCountByEstat(SalutEstat.MAINTENANCE);
            dia.updateAppCountByEstat(SalutEstat.UNKNOWN);
        }

        SalutInformeEstatItem horaItem = new SalutInformeEstatItem(hora);
        SalutInformeEstatItem diaItem = new SalutInformeEstatItem(dia);

        assertThat(horaItem.getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 10, 0, 0));
        assertThat(horaItem.getUpPercent()).isEqualTo(0.0);
        assertThat(diaItem.getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 0, 0, 0));
        assertThat(diaItem.getNotUpCount()).isEqualTo(100);
        assertThat(diaItem.isAlwaysDown()).isTrue();
    }

    private static SalutEntity sampleEntity(TipusRegistreSalut tipus, LocalDateTime data, int numElements) {
        SalutEntity entity = new SalutEntity();
        entity.setTipusRegistre(tipus);
        entity.setData(data);
        entity.setNumElements(numElements);
        return entity;
    }
}
