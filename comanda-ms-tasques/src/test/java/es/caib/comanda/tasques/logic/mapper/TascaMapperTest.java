package es.caib.comanda.tasques.logic.mapper;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.model.v1.tasca.Prioritat;
import es.caib.comanda.model.v1.tasca.TascaEstat;
import es.caib.comanda.tasques.logic.helper.TasquesClientHelper;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import es.caib.comanda.tasques.persist.entity.TascaEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TascaMapperTest {

    private final TascaMapper mapper = Mappers.getMapper(TascaMapper.class);

    @Test
    void toTasca_quanBrokerValid_mapaCampsIEntornApp() throws MalformedURLException {
        EntornApp entornApp = new EntornApp();
        entornApp.setId(100L);
        entornApp.setEntorn(EntornRef.builder().id(20L).build());
        entornApp.setApp(AppRef.builder().id(10L).build());
        TasquesClientHelper tasquesClientHelper = new TasquesClientHelperStub(Optional.of(entornApp));
        es.caib.comanda.model.v1.tasca.Tasca tascaBroker = new es.caib.comanda.model.v1.tasca.Tasca();
        tascaBroker.setEntornCodi("ENT");
        tascaBroker.setAppCodi("APP");
        tascaBroker.setIdentificador("ID1");
        tascaBroker.setTipus("TIPUS");
        tascaBroker.setNom("Nom");
        tascaBroker.setDescripcio("Descripcio");
        tascaBroker.setEstat(TascaEstat.PENDENT);
        tascaBroker.setEstatDescripcio("En cua");
        tascaBroker.setNumeroExpedient("EXP-1");
        tascaBroker.setPrioritat(Prioritat.ALTA);
        tascaBroker.setDataInici(OffsetDateTime.of(2026, 3, 20, 10, 0, 0, 0, ZoneOffset.UTC));
        tascaBroker.setDataFi(OffsetDateTime.of(2026, 3, 20, 11, 0, 0, 0, ZoneOffset.UTC));
        tascaBroker.setDataCaducitat(OffsetDateTime.of(2026, 3, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        tascaBroker.setRedireccio(new URL("https://example.org/tasca/1"));
        tascaBroker.setResponsable("usr1");
        tascaBroker.setGrup("GRUP1");
        tascaBroker.setUsuarisAmbPermis(List.of("usr2"));
        tascaBroker.setGrupsAmbPermis(List.of("GRUP2"));

        Tasca result = mapper.toTasca(tasquesClientHelper, tascaBroker);

        assertThat(result.getEntornAppId()).isEqualTo(100L);
        assertThat(result.getEntornId()).isEqualTo(20L);
        assertThat(result.getAppId()).isEqualTo(10L);
        assertThat(result.getIdentificador()).isEqualTo("ID1");
        assertThat(result.getTipus()).isEqualTo("TIPUS");
        assertThat(result.getNom()).isEqualTo("Nom");
        assertThat(result.getDescripcio()).isEqualTo("Descripcio");
        assertThat(result.getEstat()).isEqualTo(TascaEstat.PENDENT);
        assertThat(result.getEstatDescripcio()).isEqualTo("En cua");
        assertThat(result.getNumeroExpedient()).isEqualTo("EXP-1");
        assertThat(result.getPrioritat()).isEqualTo(Prioritat.ALTA);
        assertThat(result.getDataInici()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(result.getDataFi()).isEqualTo(LocalDateTime.of(2026, 3, 20, 11, 0));
        assertThat(result.getDataCaducitat()).isEqualTo(LocalDateTime.of(2026, 3, 25, 0, 0));
        assertThat(result.getUrl()).isEqualTo(new URL("https://example.org/tasca/1"));
        assertThat(result.getResponsable()).isEqualTo("usr1");
        assertThat(result.getGrup()).isEqualTo("GRUP1");
        assertThat(result.getUsuarisAmbPermis()).containsExactly("usr2");
        assertThat(result.getGrupsAmbPermis()).containsExactly("GRUP2");

        TascaEntity entity = mapper.toTascaEntity(result);

        assertThat(entity.getEntornAppId()).isEqualTo(100L);
        assertThat(entity.getEntornId()).isEqualTo(20L);
        assertThat(entity.getAppId()).isEqualTo(10L);
        assertThat(entity.getIdentificador()).isEqualTo("ID1");
        assertThat(entity.getTipus()).isEqualTo("TIPUS");
        assertThat(entity.getNom()).isEqualTo("Nom");
        assertThat(entity.getDescripcio()).isEqualTo("Descripcio");
        assertThat(entity.getEstat()).isEqualTo(TascaEstat.PENDENT);
        assertThat(entity.getEstatDescripcio()).isEqualTo("En cua");
        assertThat(entity.getNumeroExpedient()).isEqualTo("EXP-1");
        assertThat(entity.getPrioritat()).isEqualTo(Prioritat.ALTA);
        assertThat(entity.getDataInici()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(entity.getDataFi()).isEqualTo(LocalDateTime.of(2026, 3, 20, 11, 0));
        assertThat(entity.getDataCaducitat()).isEqualTo(LocalDateTime.of(2026, 3, 25, 0, 0));
        assertThat(entity.getUrl()).isEqualTo(new URL("https://example.org/tasca/1"));
        assertThat(entity.getResponsable()).isEqualTo("usr1");
        assertThat(entity.getGrup()).isEqualTo("GRUP1");
        assertThat(entity.getUsuarisAmbPermis()).containsExactly("usr2");
        assertThat(entity.getGrupsAmbPermis()).containsExactly("GRUP2");
    }

    @Test
    void updateTasca_quanModelValid_actualitzaCampsSenseModificarClaus() throws MalformedURLException {
        Tasca source = new Tasca();
        source.setNom("Nom Nou");
        source.setDescripcio("Nova descripcio");
        source.setTipus("TIPUS_NOU");
        source.setEstat(TascaEstat.INICIADA);
        source.setEstatDescripcio("Executant");
        source.setNumeroExpedient("EXP-2");
        source.setPrioritat(Prioritat.BAIXA);
        source.setDataInici(LocalDateTime.of(2026, 3, 20, 9, 0));
        source.setDataFi(LocalDateTime.of(2026, 3, 20, 12, 0));
        source.setDataCaducitat(LocalDateTime.of(2026, 3, 30, 0, 0));
        source.setUrl(new URL("https://example.org/tasca/2"));
        source.setResponsable("usr2");
        source.setGrup("GRUP2");
        source.setUsuarisAmbPermis(List.of("usr3"));
        source.setGrupsAmbPermis(List.of("GRUP3"));

        TascaEntity target = new TascaEntity();
        target.setId(1L);
        target.setEntornAppId(100L);
        target.setEntornId(20L);
        target.setAppId(10L);
        target.setIdentificador("ID1");

        mapper.updateTasca(source, target);

        assertThat(target.getId()).isEqualTo(1L);
        assertThat(target.getEntornAppId()).isEqualTo(100L);
        assertThat(target.getEntornId()).isEqualTo(20L);
        assertThat(target.getAppId()).isEqualTo(10L);
        assertThat(target.getIdentificador()).isEqualTo("ID1");
        assertThat(target.getNom()).isEqualTo("Nom Nou");
        assertThat(target.getDescripcio()).isEqualTo("Nova descripcio");
        assertThat(target.getTipus()).isEqualTo("TIPUS_NOU");
        assertThat(target.getEstat()).isEqualTo(TascaEstat.INICIADA);
        assertThat(target.getEstatDescripcio()).isEqualTo("Executant");
        assertThat(target.getNumeroExpedient()).isEqualTo("EXP-2");
        assertThat(target.getPrioritat()).isEqualTo(Prioritat.BAIXA);
        assertThat(target.getDataInici()).isEqualTo(LocalDateTime.of(2026, 3, 20, 9, 0));
        assertThat(target.getDataFi()).isEqualTo(LocalDateTime.of(2026, 3, 20, 12, 0));
        assertThat(target.getDataCaducitat()).isEqualTo(LocalDateTime.of(2026, 3, 30, 0, 0));
        assertThat(target.getUrl()).isEqualTo(new URL("https://example.org/tasca/2"));
        assertThat(target.getResponsable()).isEqualTo("usr2");
        assertThat(target.getGrup()).isEqualTo("GRUP2");
        assertThat(target.getUsuarisAmbPermis()).containsExactly("usr3");
        assertThat(target.getGrupsAmbPermis()).containsExactly("GRUP3");
    }

    @Test
    void toTascaEntity_quanModelValid_mapaCampsCorrectament() throws MalformedURLException {
        Tasca tasca = new Tasca();
        tasca.setEntornAppId(100L);
        tasca.setEntornId(20L);
        tasca.setAppId(10L);
        tasca.setIdentificador("ID1");
        tasca.setTipus("TIPUS");
        tasca.setNom("Nom");
        tasca.setDescripcio("Descripcio");
        tasca.setEstat(TascaEstat.PENDENT);
        tasca.setEstatDescripcio("En cua");
        tasca.setNumeroExpedient("EXP-1");
        tasca.setPrioritat(Prioritat.ALTA);
        tasca.setDataInici(LocalDateTime.of(2026, 3, 20, 10, 0));
        tasca.setDataFi(LocalDateTime.of(2026, 3, 20, 11, 0));
        tasca.setDataCaducitat(LocalDateTime.of(2026, 3, 25, 0, 0));
        tasca.setUrl(new URL("https://example.org/tasca/1"));
        tasca.setResponsable("usr1");
        tasca.setGrup("GRUP1");
        tasca.setUsuarisAmbPermis(List.of("usr2"));
        tasca.setGrupsAmbPermis(List.of("GRUP2"));

        TascaEntity entity = mapper.toTascaEntity(tasca);

        assertThat(entity.getEntornAppId()).isEqualTo(100L);
        assertThat(entity.getEntornId()).isEqualTo(20L);
        assertThat(entity.getAppId()).isEqualTo(10L);
        assertThat(entity.getIdentificador()).isEqualTo("ID1");
        assertThat(entity.getTipus()).isEqualTo("TIPUS");
        assertThat(entity.getNom()).isEqualTo("Nom");
        assertThat(entity.getDescripcio()).isEqualTo("Descripcio");
        assertThat(entity.getEstat()).isEqualTo(TascaEstat.PENDENT);
        assertThat(entity.getEstatDescripcio()).isEqualTo("En cua");
        assertThat(entity.getNumeroExpedient()).isEqualTo("EXP-1");
        assertThat(entity.getPrioritat()).isEqualTo(Prioritat.ALTA);
        assertThat(entity.getDataInici()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(entity.getDataFi()).isEqualTo(LocalDateTime.of(2026, 3, 20, 11, 0));
        assertThat(entity.getDataCaducitat()).isEqualTo(LocalDateTime.of(2026, 3, 25, 0, 0));
        assertThat(entity.getUrl()).isEqualTo(new URL("https://example.org/tasca/1"));
        assertThat(entity.getResponsable()).isEqualTo("usr1");
        assertThat(entity.getGrup()).isEqualTo("GRUP1");
        assertThat(entity.getUsuarisAmbPermis()).containsExactly("usr2");
        assertThat(entity.getGrupsAmbPermis()).containsExactly("GRUP2");
    }

    @Test
    void toEntornApp_quanNoExisteix_llancaExcepcio() {
        TasquesClientHelper tasquesClientHelper = new TasquesClientHelperStub(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mapper.toEntornApp(tasquesClientHelper, "ENT", "APP"));
    }

    private static class TasquesClientHelperStub extends TasquesClientHelper {
        private final Optional<EntornApp> entornApp;

        TasquesClientHelperStub(Optional<EntornApp> entornApp) {
            super(null, null, null, null, null);
            this.entornApp = entornApp;
        }

        @Override
        public Optional<EntornApp> entornAppFindByEntornCodiAndAppCodi(String entornCodi, String appCodi) {
            return entornApp;
        }
    }
}
