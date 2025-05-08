package es.caib.comanda.estadistica.logic.helper;

// Commented out as part of the MongoDB removal
/*
import es.caib.comanda.estadistica.persist.document.EstadisticaDocument;
import es.caib.comanda.estadistica.persist.document.ResumAnual;
import es.caib.comanda.estadistica.persist.document.ResumPeriode;
import es.caib.comanda.estadistica.persist.document.TempsDocument;
import es.caib.comanda.estadistica.persist.entity.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.FetEntity;
import es.caib.comanda.estadistica.persist.entity.TempsEntity;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.mongo.EstadisticaMongoRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstadisticaMongoHelper {

    private final EstadisticaMongoRepository estadisticaMongoRepository;
    private final FetRepository fetRepository;
    @Qualifier("mainTransactionManager")
    private final PlatformTransactionManager mainTransactionManager;
    @Qualifier("mongoTransactionManager")
    private final PlatformTransactionManager mongoTransactionManager;


//    @Transactional("mainTransactionManager")
//    public void migrarDades(Long entornAppId, LocalDate data) {
//        // Obtenir tots els fets per data i entornAppId
//        List<FetEntity> fets = fetRepository.findByTempsDataAndIndicador_EntornAppId(data, entornAppId);
//
//        // Processar el lot actual utilitzant parallel stream per l'agrupació
//        Map<String, List<FetEntity>> fetsAgrupats = fets
//                .parallelStream()
//                .collect(Collectors.groupingByConcurrent(this::crearClauAgrupacio));
//
//        // Crear documents en parallel
//        List<EstadisticaDocument> documents = fetsAgrupats.values()
//                .parallelStream()
//                .map(this::crearDocument)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        // Guardar documents en lots
//        if (!documents.isEmpty()) {
//            TransactionTemplate mongoTx = new TransactionTemplate(mongoTransactionManager);
//            mongoTx.execute(status -> {
//                // Utilitzar BulkOperations per inserció massiva
//                estadisticaMongoRepository.saveAll(documents);
//                return null;
//            });
//        }
//    }

    @Transactional("mainTransactionManager")
    public void migrarDades(Long entornAppId, LocalDate data) {
        // Obtenir tots els fets per data i entornAppId
        List<FetEntity> fets = fetRepository.findByTempsDataAndIndicador_EntornAppId(data, entornAppId);

        // Agrupar per la combinació de dimensions i temps
        Map<String, List<FetEntity>> fetsAgrupats = fets.stream()
                .collect(Collectors.groupingBy(fet -> crearClauAgrupacio(fet)));

        // Crear documents MongoDB
        List<EstadisticaDocument> documents = fetsAgrupats.values().stream()
                .map(this::crearDocument)
                .collect(Collectors.toList());

        // Guardar documents
//        TransactionTemplate mongoTx = new TransactionTemplate(mongoTransactionManager);
//        mongoTx.execute(status -> {
            estadisticaMongoRepository.saveAll(documents);
//            return null;
//        });
    }

    private String crearClauAgrupacio(FetEntity fet) {
        // Crear una clau única per la combinació de temps i dimensions
        StringBuilder clau = new StringBuilder();
        clau.append(fet.getTemps().getData());

        // Ordenar les dimensions per nom per assegurar consistència
//        fet.getDimensioValors().stream()
//                .sorted(Comparator.comparing(dv -> dv.getDimensio().getNom()))
//                .forEach(dv -> {
//                    clau.append("|")
//                            .append(dv.getDimensio().getNom())
//                            .append("=")
//                            .append(dv.getValor());
//                });
        // Pre-ordenar les dimensions i utilitzar un stream seqüencial
        List<DimensioValorEntity> dimensionsOrdenades = new ArrayList<>(fet.getDimensioValors());
        dimensionsOrdenades.sort(Comparator.comparing(dv -> dv.getDimensio().getNom()));

        for (DimensioValorEntity dv : dimensionsOrdenades) {
            clau.append('|')
                    .append(dv.getDimensio().getNom())
                    .append('=')
                    .append(dv.getValor());
        }


        return clau.toString();
    }

    private EstadisticaDocument crearDocument(List<FetEntity> fetsList) {
        if (fetsList.isEmpty()) return null;

        FetEntity primerFet = fetsList.get(0);
        EstadisticaDocument doc = new EstadisticaDocument();

        // Inicialitzar els maps amb la capacitat adequada
        Map<String, String> dimensions = new HashMap<>(primerFet.getDimensioValors().size());
        Map<String, Double> indicadors = new HashMap<>(fetsList.size());


        // Configurar temps
        TempsEntity temps = primerFet.getTemps();
        TempsDocument tempsDoc = new TempsDocument();
        tempsDoc.setData(temps.getData());
        tempsDoc.setAnualitat(temps.getAnualitat());
        tempsDoc.setTrimestre(temps.getTrimestre());
        tempsDoc.setMes(temps.getMes());
        tempsDoc.setSetmana(temps.getSetmana());
        tempsDoc.setDia(temps.getDia());
        tempsDoc.setDiaSetmana(temps.getDiaSetmana().toString());
        doc.setTemps(tempsDoc);

        // Omplir dimensions sense stream
        for (DimensioValorEntity dv : primerFet.getDimensioValors()) {
            dimensions.put(dv.getDimensio().getNom(), dv.getValor());
        }

        // Omplir indicadors sense stream
        for (FetEntity fet : fetsList) {
            indicadors.put(fet.getIndicador().getNom(), fet.getValor());
        }

        // Configurar dimensions
//        Map<String, String> dimensions = primerFet.getDimensioValors().stream()
//                .collect(Collectors.toMap(
//                        dv -> dv.getDimensio().getNom(),
//                        DimensioValorEntity::getValor
//                ));
        doc.setDimensions(dimensions);

        // Configurar entornAppId
        doc.setEntornAppId(primerFet.getIndicador().getEntornAppId());

        // Configurar indicadors
//        Map<String, Double> indicadors = fetsList.stream()
//                .collect(Collectors.toMap(
//                        fet -> fet.getIndicador().getNom(),
//                        FetEntity::getValor
//                ));
        doc.setIndicadors(indicadors);

        return doc;
    }



    // EXEMPLES DE CONSULTES
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Exemple per obtenir un resum per periode
    @Transactional(value = "mongoTransactionManager", readOnly = true)
    public List<ResumPeriodeDTO> getEstadistiquesPeriode(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            NivellAgrupacio nivellAgrupacio) {

        List<ResumPeriode> resum = estadisticaMongoRepository.getEstadistiquesPeriode(
                entornAppId,
                dataInici,
                dataFi,
                nivellAgrupacio.name());

        return resum.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Exemple per obtenir un resum per periode filtrat per dimensions
    @Transactional(value = "mongoTransactionManager", readOnly = true)
    public List<ResumPeriodeDTO> getEstadistiquesPeriodeAmbDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            NivellAgrupacio nivellAgrupacio) {

        List<Map<String, String>> filtres = convertirFiltresDimensions(dimensionsFiltre);

        List<ResumPeriode> resum = estadisticaMongoRepository.getEstadistiquesPeriodeAmbDimensions(
                entornAppId,
                dataInici,
                dataFi,
                filtres,
                nivellAgrupacio.name());

        return resum.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // Exemple per obtenir un resum anual
    @Transactional(value = "mongoTransactionManager", readOnly = true)
    public List<ResumAnualDTO> getResumAnual(Long entornAppId) {
        List<ResumAnual> resumAnual = estadisticaMongoRepository.getResumAnual(entornAppId);
        return resumAnual.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private ResumAnualDTO convertToDTO(ResumAnual resum) {
        return ResumAnualDTO.builder()
                .any(resum.get_id().getAny())
                .dimensions(resum.get_id().getDimensions())
                .indicadors(resum.getIndicadors())
                .numRegistres(resum.getNumRegistres())
                .build();
    }

    private ResumPeriodeDTO convertToDTO(ResumPeriode resum) {
        return ResumPeriodeDTO.builder()
                .periode(convertPeriode(resum.get_id().getPeriode()))
                .dimensions(resum.get_id().getDimensions())
                .numRegistres(resum.getNumRegistres())
                .estadistiques(convertEstadistiques(resum.getEstadistiques()))
                .build();
    }

    private ResumPeriodeDTO.PeriodeDTO convertPeriode(ResumPeriode.PeriodeAgrupat periode) {
        String descripcio = generarDescripcioPeriode(periode);

        return ResumPeriodeDTO.PeriodeDTO.builder()
                .any(periode.getAnualitat())
                .trimestre(periode.getTrimestre())
                .mes(periode.getMes())
                .setmana(periode.getSetmana())
                .data(periode.getData())
                .descripcio(descripcio)
                .build();
    }

    private String generarDescripcioPeriode(ResumPeriode.PeriodeAgrupat periode) {
        if (periode.getData() != null) {
            return periode.getData().format(DateTimeFormatter.ISO_DATE);
        }

        StringBuilder desc = new StringBuilder();
        desc.append(periode.getAnualitat());

        if (periode.getTrimestre() != null) {
            desc.append("-T").append(periode.getTrimestre());
        }

        if (periode.getMes() != null) {
            desc.append("-").append(String.format("%02d", periode.getMes()));
        }

        if (periode.getSetmana() != null) {
            desc.append("-S").append(String.format("%02d", periode.getSetmana()));
        }

        return desc.toString();
    }


    private List<ResumPeriodeDTO.IndicadorEstadisticDTO> convertEstadistiques(
            List<ResumPeriode.IndicadorEstadistic> estadistiques) {
        return estadistiques.stream()
                .map(est -> ResumPeriodeDTO.IndicadorEstadisticDTO.builder()
                        .indicador(est.getIndicador())
                        .suma(est.getEstadistiques().getSuma())
                        .mitja(est.getEstadistiques().getMitja())
                        .maxim(est.getEstadistiques().getMax())
                        .minim(est.getEstadistiques().getMin())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Map<String, String>> convertirFiltresDimensions(Map<String, List<String>> dimensionsFiltre) {
        List<Map<String, String>> filtres = new ArrayList<>();

        // Per cada dimensió
        for (Map.Entry<String, List<String>> entrada : dimensionsFiltre.entrySet()) {
            String clauDimensio = entrada.getKey();
            List<String> valors = entrada.getValue();

            // Si hi ha valors per aquesta dimensió
            if (valors != null && !valors.isEmpty()) {
                // Creem el filtre per aquesta dimensió
                Map<String, String> filtre = new HashMap<>();
                filtre.put("dimensions." + clauDimensio,
                        new Document("$in", valors).toJson());
                filtres.add(filtre);
            }
        }

        return filtres;
    }

    public enum NivellAgrupacio {
        ANY,
        TRIMESTRE,
        MES,
        SETMANA,
        DIA
    }

    @Data
    @Builder
    public static class ResumPeriodeDTO {
        private PeriodeDTO periode;
        private Map<String, String> dimensions;
        private int numRegistres;
        private List<IndicadorEstadisticDTO> estadistiques;

        @Data
        @Builder
        public static class PeriodeDTO {
            private Integer any;
            private Integer trimestre;
            private Integer mes;
            private Integer setmana;
            private LocalDate data;
            private String descripcio;  // Ex: "2024-T1", "2024-03", etc.
        }

        @Data
        @Builder
        public static class IndicadorEstadisticDTO {
            private String indicador;
            private Double suma;
            private Double mitja;
            private Double maxim;
            private Double minim;
        }
    }

    @Data
    @Builder
    public static class ResumAnualDTO {
        private int any;
        private Map<String, String> dimensions;
        private Map<String, Double> indicadors;
        private int numRegistres;
    }

}
*/

// Empty class to replace the MongoDB helper
public class EstadisticaMongoHelper {
    // This class has been commented out as part of the MongoDB removal
}
