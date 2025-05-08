package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.IndicadorEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositori per gestionar l'entitat IndicadorEntity en el sistema.
 *
 * Aquesta interfície defineix els mètodes necessaris per interactuar amb la base de dades relacionada amb
 * els indicadors, permetent realitzar operacions de lectura específiques a més de les que proporciona la interfície
 * genèrica BaseRepository.
 *
 * @author Límit Tecnologies
 */
public interface IndicadorRepository extends BaseRepository<IndicadorEntity, Long> {

    Optional<IndicadorEntity> findByCodiAndEntornAppId(String codi, Long entornAppId);

    List<IndicadorEntity> findByEntornAppId(Long entornAppId);

}
