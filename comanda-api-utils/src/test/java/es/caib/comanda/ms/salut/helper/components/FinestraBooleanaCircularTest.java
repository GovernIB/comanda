package es.caib.comanda.ms.salut.helper.components;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FinestraBooleanaCircularTest {

    @Test
    void testFinestraBàsica() {
        FinestraBooleanaCircular finestra = new FinestraBooleanaCircular(3);
        assertThat(finestra.esBuida()).isTrue();
        
        finestra.afegeix(true);
        assertThat(finestra.esBuida()).isFalse();
        assertThat(finestra.getOk()).isEqualTo(1);
        assertThat(finestra.getKo()).isEqualTo(0);
        
        finestra.afegeix(false);
        assertThat(finestra.getOk()).isEqualTo(1);
        assertThat(finestra.getKo()).isEqualTo(1);
        
        finestra.afegeix(true);
        assertThat(finestra.getOk()).isEqualTo(2);
        assertThat(finestra.getKo()).isEqualTo(1);
    }

    @Test
    void testFinestraCircularSobreescriu() {
        FinestraBooleanaCircular finestra = new FinestraBooleanaCircular(2);
        
        finestra.afegeix(true);  // [OK]
        finestra.afegeix(true);  // [OK, OK]
        assertThat(finestra.getOk()).isEqualTo(2);
        assertThat(finestra.getKo()).isEqualTo(0);
        
        finestra.afegeix(false); // [KO, OK] (sobreescriu primer OK)
        assertThat(finestra.getOk()).isEqualTo(1);
        assertThat(finestra.getKo()).isEqualTo(1);
        
        finestra.afegeix(false); // [KO, KO] (sobreescriu segon OK)
        assertThat(finestra.getOk()).isEqualTo(0);
        assertThat(finestra.getKo()).isEqualTo(2);
        
        finestra.afegeix(true);  // [OK, KO] (sobreescriu primer KO)
        assertThat(finestra.getOk()).isEqualTo(1);
        assertThat(finestra.getKo()).isEqualTo(1);
    }

    @Test
    void testFinestraMida1() {
        FinestraBooleanaCircular finestra = new FinestraBooleanaCircular(1);
        
        finestra.afegeix(true);
        assertThat(finestra.getOk()).isEqualTo(1);
        assertThat(finestra.getKo()).isEqualTo(0);
        
        finestra.afegeix(false);
        assertThat(finestra.getOk()).isEqualTo(0);
        assertThat(finestra.getKo()).isEqualTo(1);
    }
}
