package es.caib.comanda.estadistica.persist.repository.mongo;

//import es.caib.comanda.estadistica.persist.document.EstadisticaDocument;
//import es.caib.comanda.estadistica.persist.document.ResumAnual;
//import es.caib.comanda.estadistica.persist.document.ResumPeriode;
//import org.springframework.data.mongodb.repository.Aggregation;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;

// Repositori MongoDB
//@Repository
public interface EstadisticaMongoRepository { //extends MongoRepository<EstadisticaDocument, String> {
//
//    // Agregacions per múltiples indicadors alhora
//    @Aggregation(pipeline = {
//            "{ $match: { entornAppId: ?0 } }",
//            "{ $group: {" +
//                    "    _id: {" +
//                    "      any: '$temps.anualitat'," +
//                    "      dimensions: '$dimensions'" +
//                    "    }," +
//                    "    indicadors: { $last: '$indicadors' }," +
//                    "    numRegistres: { $sum: 1 }" +
//                    "  }}",
//            "{ $sort: { '_id.any': -1 } }"
//    })
//    List<ResumAnual> getResumAnual(Long entornAppId);
//
//    @Aggregation(pipeline = {
//            "{ $match: { " +
//                    "    entornAppId: ?0, " +
//                    "    'temps.data': { $gte: ?1, $lte: ?2 }" +
//                    "}}",
//            "{ $group: {" +
//                    "    _id: {" +
//                    "      periode: { " +
//                    "        $switch: {" +
//                    "          branches: [" +
//                    "            { case: { $eq: ['?3', 'ANY'] }, then: { anualitat: '$temps.anualitat' } }," +
//                    "            { case: { $eq: ['?3', 'TRIMESTRE'] }, then: { " +
//                    "                anualitat: '$temps.anualitat', " +
//                    "                trimestre: '$temps.trimestre' " +
//                    "            }}," +
//                    "            { case: { $eq: ['?3', 'MES'] }, then: { " +
//                    "                anualitat: '$temps.anualitat', " +
//                    "                mes: '$temps.mes' " +
//                    "            }}," +
//                    "            { case: { $eq: ['?3', 'SETMANA'] }, then: { " +
//                    "                anualitat: '$temps.anualitat', " +
//                    "                setmana: '$temps.setmana' " +
//                    "            }}" +
//                    "          ]," +
//                    "          default: { data: '$temps.data' }" +
//                    "        }" +
//                    "      }," +
//                    "      dimensions: '$dimensions'" +
//                    "    }," +
//                    "    numRegistres: { $sum: 1 }," +
//                    "    indicadors: {" +
//                    "      $push: '$indicadors'" +
//                    "    }" +
//                    "  }}",
//            "{ $project: {" +
//                    "    _id: 1," +
//                    "    numRegistres: 1," +
//                    "    estadistiques: {" +
//                    "      $map: {" +
//                    "        input: { $objectToArray: { $first: '$indicadors' } }," +
//                    "        as: 'ind'," +
//                    "        in: {" +
//                    "          indicador: '$$ind.k'," +
//                    "          estadistiques: {" +
//                    "            suma: { $sum: { $map: {" +
//                    "              input: '$indicadors'," +
//                    "              as: 'i'," +
//                    "              in: { $getField: { field: { $toString: '$$ind.k' }, input: '$$i' } }" +
//                    "            }}}," +
//                    "            mitja: { $avg: { $map: {" +
//                    "              input: '$indicadors'," +
//                    "              as: 'i'," +
//                    "              in: { $getField: { field: { $toString: '$$ind.k' }, input: '$$i' } }" +
//                    "            }}}," +
//                    "            max: { $max: { $map: {" +
//                    "              input: '$indicadors'," +
//                    "              as: 'i'," +
//                    "              in: { $getField: { field: { $toString: '$$ind.k' }, input: '$$i' } }" +
//                    "            }}}," +
//                    "            min: { $min: { $map: {" +
//                    "              input: '$indicadors'," +
//                    "              as: 'i'," +
//                    "              in: { $getField: { field: { $toString: '$$ind.k' }, input: '$$i' } }" +
//                    "            }}}" +
//                    "          }" +
//                    "        }" +
//                    "      }" +
//                    "    }" +
//                    "  }}",
//            "{ $sort: { '_id.periode': 1 } }"
//    })
//    List<ResumPeriode> getEstadistiquesPeriode(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            String agrupacio);
//
//    @Aggregation(pipeline = {
//            "{ $match: { " +
//                    "    entornAppId: ?0, " +
//                    "    'temps.data': { $gte: ?1, $lte: ?2 }," +
//                    "    $and: [ ?3 ]" +
//                    "}}",
//            "{ $group: {" +
//                    "    _id: {" +
//                    "      periode: { " +
//                    "        $switch: {" +
//                    "          branches: [" +
//                    "            { case: { $eq: ['?4', 'ANY'] }, then: { anualitat: '$temps.anualitat' } }," +
//                    "            { case: { $eq: ['?4', 'TRIMESTRE'] }, then: { " +
//                    "                anualitat: '$temps.anualitat', " +
//                    "                trimestre: '$temps.trimestre' " +
//                    "            }}," +
//                    "            { case: { $eq: ['?4', 'MES'] }, then: { " +
//                    "                anualitat: '$temps.anualitat', " +
//                    "                mes: '$temps.mes' " +
//                    "            }}," +
//                    "            { case: { $eq: ['?4', 'SETMANA'] }, then: { " +
//                    "                anualitat: '$temps.anualitat', " +
//                    "                setmana: '$temps.setmana' " +
//                    "            }}" +
//                    "          ]," +
//                    "          default: { data: '$temps.data' }" +
//                    "        }" +
//                    "      }," +
//                    "      dimensions: '$dimensions'" +
//                    "    }," +
//                    "    numRegistres: { $sum: 1 }," +
//                    "    indicadors: {" +
//                    "      $push: '$indicadors'" +
//                    "    }" +
//                    "  }}",
//            "{ $project: {" +
//                    "    _id: 1," +
//                    "    numRegistres: 1," +
//                    "    estadistiques: {" +
//                    "      $map: {" +
//                    "        input: { $objectToArray: { $first: '$indicadors' } }," +
//                    "        as: 'ind'," +
//                    "        in: {" +
//                    "          indicador: '$$ind.k'," +
//                    "          estadistiques: {" +
//                    "            suma: { $sum: { $map: {" +
//                    "              input: '$indicadors'," +
//                    "              as: 'i'," +
//                    "              in: { $getField: { field: { $toString: '$$ind.k' }, input: '$$i' } }" +
//                    "            }}}," +
//                    "            mitja: { $avg: { $map: {" +
//                    "              input: '$indicadors'," +
//                    "              as: 'i'," +
//                    "              in: { $getField: { field: { $toString: '$$ind.k' }, input: '$$i' } }" +
//                    "            }}}," +
//                    "            max: { $max: { $map: {" +
//                    "              input: '$indicadors'," +
//                    "              as: 'i'," +
//                    "              in: { $getField: { field: { $toString: '$$ind.k' }, input: '$$i' } }" +
//                    "            }}}," +
//                    "            min: { $min: { $map: {" +
//                    "              input: '$indicadors'," +
//                    "              as: 'i'," +
//                    "              in: { $getField: { field: { $toString: '$$ind.k' }, input: '$$i' } }" +
//                    "            }}}" +
//                    "          }" +
//                    "        }" +
//                    "      }" +
//                    "    }" +
//                    "  }}",
//            "{ $sort: { '_id.periode': 1 } }"
//    })
//    List<ResumPeriode> getEstadistiquesPeriodeAmbDimensions(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            List<Map<String, String>> dimensionsFiltre,
//            String agrupacio);

}