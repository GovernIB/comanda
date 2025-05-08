package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.FetEntity;
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
 * Implementation of FetRepositoryCustom that uses the FetRepositoryDialectFactory
 * to get the appropriate dialect implementation and execute the queries.
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

//    @Override
//    public List<Object[]> findAnnualStatsByEntornAppId(Long entornAppId) {
//        String sql = dialectFactory.getDialect().getFindAnnualStatsByEntornAppIdQuery();
//
//        Query query = entityManager.createNativeQuery(sql);
//        query.setParameter("entornAppId", entornAppId);
//
//        return query.getResultList();
//    }
//
//    @Override
//    public List<Object[]> findStatsByEntornAppIdAndPeriod(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            String nivellAgrupacio) {
//
//        String sql = dialectFactory.getDialect().getFindStatsByEntornAppIdAndPeriodQuery();
//
//        Query query = entityManager.createNativeQuery(sql);
//        query.setParameter("entornAppId", entornAppId);
//        query.setParameter("dataInici", dataInici);
//        query.setParameter("dataFi", dataFi);
//        query.setParameter("nivellAgrupacio", nivellAgrupacio);
//
//        return query.getResultList();
//    }
//
//    @Override
//    public List<Object[]> findStatsByEntornAppIdAndPeriodAndDimensions(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            String nivellAgrupacio,
//            String dimensioNom,
//            List<String> dimensioValors) {
//
//        String sql = dialectFactory.getDialect().getFindStatsByEntornAppIdAndPeriodAndDimensionsQuery();
//
//        Query query = entityManager.createNativeQuery(sql);
//        query.setParameter("entornAppId", entornAppId);
//        query.setParameter("dataInici", dataInici);
//        query.setParameter("dataFi", dataFi);
//        query.setParameter("nivellAgrupacio", nivellAgrupacio);
//        query.setParameter("dimensioNom", dimensioNom);
//        query.setParameter("dimensioValors", dimensioValors);
//
//        return query.getResultList();
//    }
//
//    @Override
//    public List<Object[]> findStatsByEntornAppIdAndPeriodAndMultipleDimensions(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            String nivellAgrupacio,
//            String dimensionsJson) {
//
//        String sql = dialectFactory.getDialect().getFindStatsByEntornAppIdAndPeriodAndMultipleDimensionsQuery();
//
//        Query query = entityManager.createNativeQuery(sql);
//        query.setParameter("entornAppId", entornAppId);
//        query.setParameter("dataInici", dataInici);
//        query.setParameter("dataFi", dataFi);
//        query.setParameter("nivellAgrupacio", nivellAgrupacio);
//        query.setParameter("dimensionsJson", dimensionsJson);
//
//        return query.getResultList();
//    }
}