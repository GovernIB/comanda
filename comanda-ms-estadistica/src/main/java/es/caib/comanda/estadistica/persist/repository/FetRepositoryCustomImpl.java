package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.persist.entity.estadistiues.FetEntity;
import es.caib.comanda.estadistica.persist.repository.dialect.FetRepositoryDialectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Implementació de l'interfície personalitzada FetRepositoryCustom. Proporciona funcionalitats específiques per
 * consultar entitats de tipus FetEntity utilitzant consultes SQL personalitzades segons el dialecte configurat.
 *
 * Aquesta classe utilitza EntityManager per executar consultes natives SQL que inclouen criteris de filtre com
 * l'identificador de l'entorn de l'aplicació, rangs de dates i valors de dimensions especificats.
 *
 * @author Límit Tecnologies
 */
@Repository
public class FetRepositoryCustomImpl implements FetRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FetRepositoryDialectFactory dialectFactory;

    @Override
    public List<FetEntity> findByEntornAppIdAndTempsDataBetweenAndDimensionValue(
            Long entornAppId, 
            LocalDate dataInici, 
            LocalDate dataFi,
            String dimensioCodi,
            String dimensioValor) {

        String sql = dialectFactory.getDialect().getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery();

        Query query = entityManager.createNativeQuery(sql, FetEntity.class);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);
        query.setParameter("dimensioCodi", dimensioCodi);
        query.setParameter("dimensioValor", dimensioValor);

        return query.getResultList();
    }

    @Override
    public List<FetEntity> findByEntornAppIdAndTempsDataBetweenAndDimensionValues(
            Long entornAppId, 
            LocalDate dataInici, 
            LocalDate dataFi,
            String dimensioCodi,
            List<String> valors) {

        String sql = dialectFactory.getDialect().getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery();

        Query query = entityManager.createNativeQuery(sql, FetEntity.class);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);
        query.setParameter("dimensioCodi", dimensioCodi);
        query.setParameter("dimensioValors", valors);

        return query.getResultList();
    }

    @Override
    public List<FetEntity> findByEntornAppIdAndTempsDataAndDimensions(
            Long entornAppId,
            LocalDate data,
            Map<String, List<String>> dimensionsFiltre) {

        String sql = dialectFactory.getDialect().getFindByEntornAppIdAndTempsDataAndDimensionQuery(dimensionsFiltre);

        Query query = entityManager.createNativeQuery(sql, FetEntity.class);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("data", data);

        return query.getResultList();
    }

    @Override
    public List<FetEntity> findByEntornAppIdAndTempsDataBetweenAndDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre) {

        String sql = dialectFactory.getDialect().getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(dimensionsFiltre);

        Query query = entityManager.createNativeQuery(sql, FetEntity.class);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        return query.getResultList();
    }

    @Override
    public Double getAggregatedValue(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            String indicadorCodi,
            TableColumnsEnum agregacio,
            PeriodeUnitat unitatAgregacio) {

        String sql = dialectFactory.getDialect().getAggregatedValueQuery(dimensionsFiltre, indicadorCodi, agregacio, unitatAgregacio);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        Object result = query.getSingleResult();

        if (result == null) {
            return null;
        }

        return Double.valueOf(result.toString());
    }
}
