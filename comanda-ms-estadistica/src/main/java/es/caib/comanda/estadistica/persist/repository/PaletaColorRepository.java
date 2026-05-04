package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.paleta.PaletaColorEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

public interface PaletaColorRepository extends BaseRepository<PaletaColorEntity, Long> {
    List<PaletaColorEntity> findByPaletaIdOrderByPosicioAsc(Long paletaId);
}
