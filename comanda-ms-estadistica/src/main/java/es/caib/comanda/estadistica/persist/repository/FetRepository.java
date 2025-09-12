package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositori que proporciona accés a operacions específiques relacionades amb l'entitat de fet (FetEntity).
 * Aquest repositori estén la funcionalitat de BaseRepository i afegeix mètodes personalitzats per a manejar
 * consultes específiques i operacions relacionades amb dades estadístiques.
 *
 * Les operacions inclouen:
 * - Eliminació de registres mitjançant criteris definits (temps i entorn d'aplicació).
 * - Consultes per data, identificador d'entorn, i intervals de temps específics.
 * - Execució de procediments SQL per generar consultes personalitzades basades en dimensions i indicadors.
 *
 * Aquest repositori també integra funcionalitats addicionals mitjançant el FetRepositoryCustom.
 *
 * @author Límit Tecnologies
 */
public interface FetRepository extends BaseRepository<FetEntity, Long>, FetRepositoryCustom {

    void deleteAllByTempsAndEntornAppId(TempsEntity temps, Long entornAppId);

    List<FetEntity> findByTempsData(LocalDate data);
    List<FetEntity> findByEntornAppId(Long entornAppId);
    List<FetEntity> findByEntornAppIdAndTempsData(Long entornAppId, LocalDate data);
    List<FetEntity> findByEntornAppIdAndTempsDataBefore(Long entornAppId, LocalDate data);
    List<FetEntity> findByEntornAppIdAndTempsDataBetween(Long entornAppId, LocalDate dataInici, LocalDate dataFi);

    /**
     * Elimina en batch tots els fets d'un entorn determinat amb data anterior a la indicada.
     * Útil per a l'esborrat per retenció sense carregar entitats en memòria.
     * @param entornAppId identificador de l'entorn
     * @param data data llindar (exclusiva)
     * @return nombre de registres eliminats
     */
    long deleteByEntornAppIdAndTempsDataBefore(Long entornAppId, LocalDate data);


    /**
     * Procediment que genera una taula amb les columnes de dimensions ordenades per codi de forma ascendent, seguides
     * per les columnes d'indicadors també ordenades per codi de forma ascendent.
     *
     * @param entornAppId Identificador de l'entorn de l'aplicació que s'està consultant.
     * @param dimensionFilters Filtres aplicats a les dimensions per acotar el resultat de la consulta.
     *                         El filtre tindrà un format SQL.
     *                         Exemple: "'ENT' = '21' AND 'ORG' = 'A04003003' AND 'PRC' IS NULL"
     * @param data Data de referència per a la consulta.
     * @param resultat Llista que conté el resultat del cursor amb les dades de la consulta.
     */
    @Procedure(name = "GENERAR_CONSULTA_ENTORN_APP")
    void generarConsultaEntornApp(
            @Param("p_entorn_app_id") Integer entornAppId,
            @Param("p_dimension_filters") String dimensionFilters,
            @Param("p_data") String data,
            @Param("p_resultat") List<Object[]> resultat // Contingut del cursor
    );

    // Rendiment: mètodes auxiliars per processat paginat/gran volum
    long countByEntornAppId(Long entornAppId);
    Page<FetEntity> findByEntornAppId(Long entornAppId, Pageable pageable);

}
