package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletaColor;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.repository.PaletaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per a PlantillaServiceImpl")
class PlantillaServiceImplTest {

    @Test
    @DisplayName("savePaletteResources actualitza una paleta existent quan arriba amb clientId numèric però sense id")
    @SuppressWarnings("unchecked")
    void savePaletteResources_quanClientIdNumericSenseId_actualitzaPaletaExistent() {
        Paleta palette = palette("910001", "Institucional clar widget", "#ffffff");
        PaletaEntity existing = paletteEntity(910001L, "Institucional clar widget");
        RecordingPaletaRepository repository = new RecordingPaletaRepository();
        repository.byId.put(910001L, existing);
        PlantillaServiceImpl service = new PlantillaServiceImpl(repository.proxy());

        Map<String, PaletaEntity> result = ReflectionTestUtils.invokeMethod(
                service,
                "savePaletteResources",
                Collections.singletonList(palette));

        assertThat(result).containsEntry("910001", existing);
        assertThat(existing.getColors()).hasSize(1);
        assertThat(existing.getColors().get(0).getValor()).isEqualTo("#ffffff");
        assertThat(repository.findByNomCalls).isZero();
        assertThat(repository.saved).containsExactly(existing);
    }

    @Test
    @DisplayName("savePaletteResources reutilitza una paleta existent pel nom abans de crear una entitat nova")
    @SuppressWarnings("unchecked")
    void savePaletteResources_quanSenseIdINomExisteix_actualitzaPaletaExistent() {
        Paleta palette = palette(null, "Institucional clar widget", "#f8fafc");
        PaletaEntity existing = paletteEntity(910001L, "Institucional clar widget");
        RecordingPaletaRepository repository = new RecordingPaletaRepository();
        repository.byNom.put("Institucional clar widget", existing);
        PlantillaServiceImpl service = new PlantillaServiceImpl(repository.proxy());

        Map<String, PaletaEntity> result = ReflectionTestUtils.invokeMethod(
                service,
                "savePaletteResources",
                Collections.singletonList(palette));

        assertThat(result).containsEntry("Institucional clar widget", existing);
        assertThat(result).containsEntry("910001", existing);
        assertThat(existing.getColors()).hasSize(1);
        assertThat(existing.getColors().get(0).getValor()).isEqualTo("#f8fafc");
        assertThat(repository.findByNomCalls).isEqualTo(1);
        assertThat(repository.saved).containsExactly(existing);
    }

    private Paleta palette(String clientId, String nom, String color) {
        Paleta palette = new Paleta();
        palette.setClientId(clientId);
        palette.setNom(nom);
        palette.setColors(Collections.singletonList(color(0, color)));
        return palette;
    }

    private PaletaColor color(int posicio, String valor) {
        PaletaColor color = new PaletaColor();
        color.setPosicio(posicio);
        color.setValor(valor);
        return color;
    }

    private PaletaEntity paletteEntity(Long id, String nom) {
        PaletaEntity entity = new PaletaEntity();
        entity.setId(id);
        entity.setNom(nom);
        return entity;
    }

    private static class RecordingPaletaRepository implements InvocationHandler {
        private final Map<Long, PaletaEntity> byId = new HashMap<>();
        private final Map<String, PaletaEntity> byNom = new HashMap<>();
        private final ArrayList<PaletaEntity> saved = new ArrayList<>();
        private int findByNomCalls;

        private PaletaRepository proxy() {
            return (PaletaRepository) Proxy.newProxyInstance(
                    PaletaRepository.class.getClassLoader(),
                    new Class<?>[] { PaletaRepository.class },
                    this);
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            switch (method.getName()) {
                case "findById":
                    return Optional.ofNullable(byId.get((Long) args[0]));
                case "findByNom":
                    findByNomCalls++;
                    return Optional.ofNullable(byNom.get((String) args[0]));
                case "saveAndFlush":
                    PaletaEntity entity = (PaletaEntity) args[0];
                    saved.add(entity);
                    return entity;
                case "toString":
                    return "RecordingPaletaRepository";
                default:
                    throw new UnsupportedOperationException("Unexpected repository call: " + method.getName());
            }
        }
    }
}
