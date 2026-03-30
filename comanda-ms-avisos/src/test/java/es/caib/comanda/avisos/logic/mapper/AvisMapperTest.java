package es.caib.comanda.avisos.logic.mapper;

import es.caib.comanda.avisos.logic.helper.AvisClientHelper;
import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.model.v1.avis.AvisTipus;
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

class AvisMapperTest {

    private final AvisMapper mapper = Mappers.getMapper(AvisMapper.class);

    @Test
    void toAvis_quanBrokerValid_mapaCampsIEntornApp() throws MalformedURLException {
        EntornApp entornApp = new EntornApp();
        entornApp.setId(100L);
        entornApp.setEntorn(EntornRef.builder().id(20L).build());
        entornApp.setApp(AppRef.builder().id(10L).build());
        AvisClientHelper avisClientHelper = new AvisClientHelperStub(Optional.of(entornApp));
        es.caib.comanda.model.v1.avis.Avis avisBroker = new es.caib.comanda.model.v1.avis.Avis();
        avisBroker.setEntornCodi("ENT");
        avisBroker.setAppCodi("APP");
        avisBroker.setIdentificador("ID1");
        avisBroker.setTipus(AvisTipus.INFO);
        avisBroker.setNom("Nom");
        avisBroker.setDescripcio("Descripcio");
        avisBroker.setDataInici(OffsetDateTime.of(2026, 3, 20, 10, 0, 0, 0, ZoneOffset.UTC));
        avisBroker.setDataFi(OffsetDateTime.of(2026, 3, 21, 10, 0, 0, 0, ZoneOffset.UTC));
        avisBroker.setRedireccio(new URL("https://example.org/avis/1"));
        avisBroker.setResponsable("usr1");
        avisBroker.setGrup("GRUP1");
        avisBroker.setUsuarisAmbPermis(List.of("usr2"));
        avisBroker.setGrupsAmbPermis(List.of("GRUP2"));

        Avis result = mapper.toAvis(avisClientHelper, avisBroker);

        assertThat(result.getEntornAppId()).isEqualTo(100L);
        assertThat(result.getEntornId()).isEqualTo(20L);
        assertThat(result.getAppId()).isEqualTo(10L);
        assertThat(result.getIdentificador()).isEqualTo("ID1");
        assertThat(result.getTipus()).isEqualTo(AvisTipus.INFO);
        assertThat(result.getNom()).isEqualTo("Nom");
        assertThat(result.getDescripcio()).isEqualTo("Descripcio");
        assertThat(result.getDataInici()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(result.getDataFi()).isEqualTo(LocalDateTime.of(2026, 3, 21, 10, 0));
        assertThat(result.getUrl()).isEqualTo(new URL("https://example.org/avis/1"));
        assertThat(result.getResponsable()).isEqualTo("usr1");
        assertThat(result.getGrup()).isEqualTo("GRUP1");
        assertThat(result.getUsuarisAmbPermis()).containsExactly("usr2");
        assertThat(result.getGrupsAmbPermis()).containsExactly("GRUP2");

        AvisEntity entity = mapper.toAvisEntity(result);

        assertThat(entity.getEntornAppId()).isEqualTo(100L);
        assertThat(entity.getEntornId()).isEqualTo(20L);
        assertThat(entity.getAppId()).isEqualTo(10L);
        assertThat(entity.getIdentificador()).isEqualTo("ID1");
        assertThat(entity.getTipus()).isEqualTo(AvisTipus.INFO);
        assertThat(entity.getNom()).isEqualTo("Nom");
        assertThat(entity.getDescripcio()).isEqualTo("Descripcio");
        assertThat(entity.getDataInici()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(entity.getDataFi()).isEqualTo(LocalDateTime.of(2026, 3, 21, 10, 0));
        assertThat(entity.getUrl()).isEqualTo(new URL("https://example.org/avis/1"));
        assertThat(entity.getResponsable()).isEqualTo("usr1");
        assertThat(entity.getGrup()).isEqualTo("GRUP1");
        assertThat(entity.getUsuarisAmbPermis()).containsExactly("usr2");
        assertThat(entity.getGrupsAmbPermis()).containsExactly("GRUP2");
    }

    @Test
    void updateAvis_quanBrokerValid_actualitzaCampsSenseModificarClaus() throws MalformedURLException {
        es.caib.comanda.model.v1.avis.Avis source = new es.caib.comanda.model.v1.avis.Avis();
        source.setNom("Nom Nou");
        source.setDescripcio("Nova descripcio");
        source.setTipus(AvisTipus.ERROR);
        source.setDataInici(OffsetDateTime.of(2026, 3, 20, 10, 0, 0, 0, ZoneOffset.UTC));
        source.setDataFi(OffsetDateTime.of(2026, 3, 21, 10, 0, 0, 0, ZoneOffset.UTC));
        source.setRedireccio(new URL("https://example.org/avis/2"));
        source.setResponsable("usr2");
        source.setGrup("GRUP2");
        source.setUsuarisAmbPermis(List.of("usr3"));
        source.setGrupsAmbPermis(List.of("GRUP3"));

        AvisEntity target = new AvisEntity();
        target.setId(1L);
        target.setEntornAppId(100L);
        target.setEntornId(20L);
        target.setAppId(10L);
        target.setIdentificador("ID1");

        mapper.updateAvis(source, target);

        assertThat(target.getId()).isEqualTo(1L);
        assertThat(target.getEntornAppId()).isEqualTo(100L);
        assertThat(target.getEntornId()).isEqualTo(20L);
        assertThat(target.getAppId()).isEqualTo(10L);
        assertThat(target.getIdentificador()).isEqualTo("ID1");
        assertThat(target.getNom()).isEqualTo("Nom Nou");
        assertThat(target.getDescripcio()).isEqualTo("Nova descripcio");
        assertThat(target.getTipus()).isEqualTo(AvisTipus.ERROR);
        assertThat(target.getDataInici()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(target.getDataFi()).isEqualTo(LocalDateTime.of(2026, 3, 21, 10, 0));
        assertThat(target.getUrl()).isEqualTo(new URL("https://example.org/avis/2"));
        assertThat(target.getResponsable()).isEqualTo("usr2");
        assertThat(target.getGrup()).isEqualTo("GRUP2");
        assertThat(target.getUsuarisAmbPermis()).containsExactly("usr3");
        assertThat(target.getGrupsAmbPermis()).containsExactly("GRUP3");
    }

    @Test
    void toAvisEntity_quanModelValid_mapaCampsCorrectament() throws MalformedURLException {
        Avis avis = new Avis();
        avis.setEntornAppId(100L);
        avis.setEntornId(20L);
        avis.setAppId(10L);
        avis.setIdentificador("ID1");
        avis.setTipus(AvisTipus.INFO);
        avis.setNom("Nom");
        avis.setDescripcio("Descripcio");
        avis.setDataInici(LocalDateTime.of(2026, 3, 20, 10, 0));
        avis.setDataFi(LocalDateTime.of(2026, 3, 21, 10, 0));
        avis.setUrl(new URL("https://example.org/avis/1"));
        avis.setResponsable("usr1");
        avis.setGrup("GRUP1");
        avis.setUsuarisAmbPermis(List.of("usr2"));
        avis.setGrupsAmbPermis(List.of("GRUP2"));

        AvisEntity entity = mapper.toAvisEntity(avis);

        assertThat(entity.getEntornAppId()).isEqualTo(100L);
        assertThat(entity.getEntornId()).isEqualTo(20L);
        assertThat(entity.getAppId()).isEqualTo(10L);
        assertThat(entity.getIdentificador()).isEqualTo("ID1");
        assertThat(entity.getTipus()).isEqualTo(AvisTipus.INFO);
        assertThat(entity.getNom()).isEqualTo("Nom");
        assertThat(entity.getDescripcio()).isEqualTo("Descripcio");
        assertThat(entity.getDataInici()).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 0));
        assertThat(entity.getDataFi()).isEqualTo(LocalDateTime.of(2026, 3, 21, 10, 0));
        assertThat(entity.getUrl()).isEqualTo(new URL("https://example.org/avis/1"));
        assertThat(entity.getResponsable()).isEqualTo("usr1");
        assertThat(entity.getGrup()).isEqualTo("GRUP1");
        assertThat(entity.getUsuarisAmbPermis()).containsExactly("usr2");
        assertThat(entity.getGrupsAmbPermis()).containsExactly("GRUP2");
    }

    @Test
    void toEntornApp_quanNoExisteix_llancaExcepcio() {
        AvisClientHelper avisClientHelper = new AvisClientHelperStub(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mapper.toEntornApp(avisClientHelper, "ENT", "APP"));
    }

    private static class AvisClientHelperStub extends AvisClientHelper {
        private final Optional<EntornApp> entornApp;

        AvisClientHelperStub(Optional<EntornApp> entornApp) {
            super(null, null, null, null, null);
            this.entornApp = entornApp;
        }

        @Override
        public Optional<EntornApp> entornAppFindByEntornCodiAndAppCodi(String entornCodi, String appCodi) {
            return entornApp;
        }
    }
}
