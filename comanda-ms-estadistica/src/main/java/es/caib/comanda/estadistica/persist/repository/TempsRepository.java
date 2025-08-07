package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfície de repositori per a la gestió de l'entitat TempsEntity.
 *
 * Aquesta interfície proporciona operacions específiques per accedir i gestionar dades relacionades amb l'entitat de dimensió temporal,
 * a més de les funcionalitats estàndard heretades del BaseRepository.
 *
 * @author Límit Tecnologies
 */
public interface TempsRepository extends BaseRepository<TempsEntity, Long> {

    TempsEntity findByData(LocalDate data);
    
    /**
     * Troba totes les entitats TempsEntity associades a un entornAppId específic.
     * 
     * @param entornAppId Identificador de l'entorn d'aplicació.
     * @return Llista d'entitats TempsEntity associades a l'entornAppId.
     */
    @Query("SELECT DISTINCT f.temps FROM FetEntity f WHERE f.entornAppId = :entornAppId")
    List<TempsEntity> findByEntornAppId(@Param("entornAppId") Long entornAppId);

}
