package es.caib.comanda.estadistica.logic.helper;

//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import es.caib.comanda.estadistica.persist.entity.FetEntity;
//import es.caib.comanda.estadistica.persist.entity.TempsEntity;
//import es.caib.comanda.estadistica.persist.repository.FetRepository;
//import es.caib.comanda.estadistica.persist.repository.TempsRepository;
//import lombok.Data;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;

//@Slf4j
//@Component
//@RequiredArgsConstructor
public class EstadisticaSQLHelper {

//    private final FetRepository fetRepository;
//    private final TempsRepository tempsRepository;
//
//    @Transactional(readOnly = true)
//    public List<ResumPeriodeDTO> getEstadistiquesPeriode(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            NivellAgrupacio nivellAgrupacio) {
//
//        // Get statistics directly from the database using JSON_VALUE and JSON_TABLE
//        List<Object[]> stats = fetRepository.findStatsByEntornAppIdAndPeriod(
//                entornAppId,
//                dataInici,
//                dataFi,
//                nivellAgrupacio.name());
//
//        // Convert to DTOs
//        return convertToResumPeriodeDTOs(stats);
//    }
//
//    private List<ResumPeriodeDTO> convertToResumPeriodeDTOs(List<Object[]> statsRows) {
//        List<ResumPeriodeDTO> result = new ArrayList<>();
//
//        for (Object[] row : statsRows) {
//            try {
//                // Parse the JSON objects from the database
//                ObjectMapper mapper = new ObjectMapper();
//
//                // Parse periode JSON
//                String periodeJson = row[0].toString();
//                Map<String, Object> periodeMap = mapper.readValue(periodeJson, new TypeReference<Map<String, Object>>() {});
//
//                // Create periode DTO
//                ResumPeriodeDTO.PeriodeDTO periodeDTO = new ResumPeriodeDTO.PeriodeDTO();
//                if (periodeMap.containsKey("any")) {
//                    periodeDTO.setAny(((Number) periodeMap.get("any")).intValue());
//                }
//                if (periodeMap.containsKey("trimestre")) {
//                    periodeDTO.setTrimestre(((Number) periodeMap.get("trimestre")).intValue());
//                }
//                if (periodeMap.containsKey("mes")) {
//                    periodeDTO.setMes(((Number) periodeMap.get("mes")).intValue());
//                }
//                if (periodeMap.containsKey("setmana")) {
//                    periodeDTO.setSetmana(((Number) periodeMap.get("setmana")).intValue());
//                }
//                if (periodeMap.containsKey("data")) {
//                    periodeDTO.setData(LocalDate.parse(periodeMap.get("data").toString()));
//                }
//
//                // Generate description
//                periodeDTO.setDescripcio(generarDescripcioPeriode(periodeDTO));
//
//                // Parse dimensions JSON
//                String dimensionsJson = row[1].toString();
//                Map<String, String> dimensions = mapper.readValue(dimensionsJson, new TypeReference<Map<String, String>>() {});
//
//                // Get number of records
//                int numRegistres = ((Number) row[2]).intValue();
//
//                // Parse estadistiques JSON
//                String estadistiquesJson = row[3].toString();
//                Map<String, Map<String, Double>> estadistiquesMap = mapper.readValue(estadistiquesJson,
//                        new TypeReference<Map<String, Map<String, Double>>>() {});
//
//                // Create indicator statistics DTOs
//                List<ResumPeriodeDTO.IndicadorEstadisticDTO> indicadorStats = new ArrayList<>();
//                for (Map.Entry<String, Map<String, Double>> entry : estadistiquesMap.entrySet()) {
//                    String indicadorNom = entry.getKey();
//                    Map<String, Double> statValues = entry.getValue();
//
//                    ResumPeriodeDTO.IndicadorEstadisticDTO indicadorStat = new ResumPeriodeDTO.IndicadorEstadisticDTO();
//                    indicadorStat.setIndicador(indicadorNom);
//                    indicadorStat.setSuma(statValues.get("suma"));
//                    indicadorStat.setMitja(statValues.get("mitja"));
//                    indicadorStat.setMaxim(statValues.get("max"));
//                    indicadorStat.setMinim(statValues.get("min"));
//
//                    indicadorStats.add(indicadorStat);
//                }
//
//                // Create ResumPeriodeDTO
//                ResumPeriodeDTO resumPeriode = new ResumPeriodeDTO();
//                resumPeriode.setPeriode(periodeDTO);
//                resumPeriode.setDimensions(dimensions);
//                resumPeriode.setNumRegistres(numRegistres);
//                resumPeriode.setEstadistiques(indicadorStats);
//
//                result.add(resumPeriode);
//            } catch (Exception e) {
//                log.error("Error converting database result to DTO", e);
//            }
//        }
//
//        return result;
//    }
//
//    private String generarDescripcioPeriode(ResumPeriodeDTO.PeriodeDTO periode) {
//        if (periode.getData() != null) {
//            return periode.getData().format(DateTimeFormatter.ISO_DATE);
//        }
//
//        StringBuilder desc = new StringBuilder();
//        desc.append(periode.getAny());
//
//        if (periode.getTrimestre() != null) {
//            desc.append("-T").append(periode.getTrimestre());
//        }
//
//        if (periode.getMes() != null) {
//            desc.append("-").append(String.format("%02d", periode.getMes()));
//        }
//
//        if (periode.getSetmana() != null) {
//            desc.append("-S").append(String.format("%02d", periode.getSetmana()));
//        }
//
//        return desc.toString();
//    }
//
//    @Transactional(readOnly = true)
//    public List<ResumPeriodeDTO> getEstadistiquesPeriodeAmbDimensions(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            Map<String, List<String>> dimensionsFiltre,
//            NivellAgrupacio nivellAgrupacio) {
//
//        // If no dimensions filter is provided, use the standard method
//        if (dimensionsFiltre == null || dimensionsFiltre.isEmpty()) {
//            return getEstadistiquesPeriode(entornAppId, dataInici, dataFi, nivellAgrupacio);
//        }
//
//        // Convert the dimensions filter map to a format suitable for the query
//        // We need to keep all values for each dimension
//        Map<String, Object> dimensionsMap = new HashMap<>();
//        for (Map.Entry<String, List<String>> entry : dimensionsFiltre.entrySet()) {
//            String dimensioNom = entry.getKey();
//            List<String> dimensioValors = entry.getValue();
//
//            if (dimensioValors != null && !dimensioValors.isEmpty()) {
//                // Add all values for this dimension
//                dimensionsMap.put(dimensioNom, dimensioValors);
//            }
//        }
//
//        // Convert the dimensions map to a JSON string
//        String dimensionsJson = null;
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            dimensionsJson = mapper.writeValueAsString(dimensionsMap);
//        } catch (Exception e) {
//            log.error("Error converting dimensions map to JSON", e);
//            // If there's an error, fall back to the standard method
//            return getEstadistiquesPeriode(entornAppId, dataInici, dataFi, nivellAgrupacio);
//        }
//
//        // Get statistics directly from the database using JSON_VALUE and JSON_TABLE
//        List<Object[]> stats = fetRepository.findStatsByEntornAppIdAndPeriodAndMultipleDimensions(
//                entornAppId,
//                dataInici,
//                dataFi,
//                nivellAgrupacio.name(),
//                dimensionsJson);
//
//        // Convert to DTOs
//        return convertToResumPeriodeDTOs(stats);
//    }
//
//    @Transactional(readOnly = true)
//    public List<ResumAnualDTO> getResumAnual(Long entornAppId) {
//        // Get statistics directly from the database using JSON_VALUE and JSON_TABLE
//        List<Object[]> stats = fetRepository.findAnnualStatsByEntornAppId(entornAppId);
//
//        // Convert to DTOs
//        return convertToResumAnualDTOs(stats);
//    }
//
//    private List<ResumAnualDTO> convertToResumAnualDTOs(List<Object[]> statsRows) {
//        List<ResumAnualDTO> result = new ArrayList<>();
//
//        for (Object[] row : statsRows) {
//            try {
//                // Parse the JSON objects from the database
//                ObjectMapper mapper = new ObjectMapper();
//
//                // Get year
//                int any = ((Number) row[0]).intValue();
//
//                // Parse dimensions JSON
//                String dimensionsJson = row[1].toString();
//                Map<String, String> dimensions = mapper.readValue(dimensionsJson, new TypeReference<Map<String, String>>() {});
//
//                // Get number of records
//                int numRegistres = ((Number) row[2]).intValue();
//
//                // Parse estadistiques JSON
//                String estadistiquesJson = row[3].toString();
//                Map<String, Double> indicadors = mapper.readValue(estadistiquesJson, new TypeReference<Map<String, Double>>() {});
//
//                // Create ResumAnualDTO
//                ResumAnualDTO resumAnual = new ResumAnualDTO();
//                resumAnual.setAny(any);
//                resumAnual.setDimensions(dimensions);
//                resumAnual.setIndicadors(indicadors);
//                resumAnual.setNumRegistres(numRegistres);
//
//                result.add(resumAnual);
//            } catch (Exception e) {
//                log.error("Error converting database result to DTO", e);
//            }
//        }
//
//        return result;
//    }
//
//    private Map<PeriodeKey, List<FetEntity>> agruparPerPeriode(List<FetEntity> fets, NivellAgrupacio nivellAgrupacio) {
//        Map<PeriodeKey, List<FetEntity>> result = new HashMap<>();
//
//        for (FetEntity fet : fets) {
//            TempsEntity temps = fet.getTemps();
//            Map<String, String> dimensions = fet.getDimensionsJson();
//
//            PeriodeKey key = new PeriodeKey();
//            key.setDimensions(dimensions);
//
//            // Set period fields according to nivellAgrupacio
//            switch (nivellAgrupacio) {
//                case ANY:
//                    key.setAny(temps.getAnualitat());
//                    break;
//                case TRIMESTRE:
//                    key.setAny(temps.getAnualitat());
//                    key.setTrimestre(temps.getTrimestre());
//                    break;
//                case MES:
//                    key.setAny(temps.getAnualitat());
//                    key.setMes(temps.getMes());
//                    break;
//                case SETMANA:
//                    key.setAny(temps.getAnualitat());
//                    key.setSetmana(temps.getSetmana());
//                    break;
//                case DIA:
//                    key.setData(temps.getData());
//                    break;
//            }
//
//            result.computeIfAbsent(key, k -> new ArrayList<>()).add(fet);
//        }
//
//        return result;
//    }
//
//    private Map<AnualDimensionsKey, List<FetEntity>> agruparPerAnyDimensions(List<FetEntity> fets) {
//        Map<AnualDimensionsKey, List<FetEntity>> result = new HashMap<>();
//
//        for (FetEntity fet : fets) {
//            TempsEntity temps = fet.getTemps();
//            Map<String, String> dimensions = fet.getDimensionsJson();
//
//            AnualDimensionsKey key = new AnualDimensionsKey();
//            key.setAny(temps.getAnualitat());
//            key.setDimensions(dimensions);
//
//            result.computeIfAbsent(key, k -> new ArrayList<>()).add(fet);
//        }
//
//        return result;
//    }
//
//    private List<FetEntity> filtrarPerDimensions(List<FetEntity> fets, Map<String, List<String>> dimensionsFiltre) {
//        if (dimensionsFiltre == null || dimensionsFiltre.isEmpty()) {
//            return fets;
//        }
//
//        return fets.stream()
//                .filter(fet -> compleixFiltres(fet, dimensionsFiltre))
//                .collect(Collectors.toList());
//    }
//
//    private boolean compleixFiltres(FetEntity fet, Map<String, List<String>> dimensionsFiltre) {
//        Map<String, String> dimensions = fet.getDimensionsJson();
//
//        for (Map.Entry<String, List<String>> entry : dimensionsFiltre.entrySet()) {
//            String dimensioNom = entry.getKey();
//            List<String> valorsPermesos = entry.getValue();
//
//            if (valorsPermesos != null && !valorsPermesos.isEmpty()) {
//                String valorActual = dimensions.get(dimensioNom);
//                if (valorActual == null || !valorsPermesos.contains(valorActual)) {
//                    return false;
//                }
//            }
//        }
//
//        return true;
//    }
//
//    private ResumPeriodeDTO crearResumPeriode(PeriodeKey key, List<FetEntity> fets) {
//        // Create period DTO
//        ResumPeriodeDTO.PeriodeDTO periodeDTO = new ResumPeriodeDTO.PeriodeDTO();
//        periodeDTO.setAny(key.getAny());
//        periodeDTO.setTrimestre(key.getTrimestre());
//        periodeDTO.setMes(key.getMes());
//        periodeDTO.setSetmana(key.getSetmana());
//        periodeDTO.setData(key.getData());
//        periodeDTO.setDescripcio(generarDescripcioPeriode(key));
//
//        // Calculate statistics for each indicator
//        Map<String, Map<String, Double>> indicadorsStats = calcularEstadistiquesIndicadors(fets);
//
//        // Create indicator statistics DTOs
//        List<ResumPeriodeDTO.IndicadorEstadisticDTO> indicadorStats = new ArrayList<>();
//        for (Map.Entry<String, Map<String, Double>> entry : indicadorsStats.entrySet()) {
//            String indicadorNom = entry.getKey();
//            Map<String, Double> stats = entry.getValue();
//
//            ResumPeriodeDTO.IndicadorEstadisticDTO indicadorStat = new ResumPeriodeDTO.IndicadorEstadisticDTO();
//            indicadorStat.setIndicador(indicadorNom);
//            indicadorStat.setSuma(stats.get("suma"));
//            indicadorStat.setMitja(stats.get("mitja"));
//            indicadorStat.setMaxim(stats.get("max"));
//            indicadorStat.setMinim(stats.get("min"));
//
//            indicadorStats.add(indicadorStat);
//        }
//
//        // Create ResumPeriodeDTO
//        ResumPeriodeDTO resumPeriode = new ResumPeriodeDTO();
//        resumPeriode.setPeriode(periodeDTO);
//        resumPeriode.setDimensions(key.getDimensions());
//        resumPeriode.setNumRegistres(fets.size());
//        resumPeriode.setEstadistiques(indicadorStats);
//
//        return resumPeriode;
//    }
//
//    private ResumAnualDTO crearResumAnual(AnualDimensionsKey key, List<FetEntity> fets) {
//        // Calculate average for each indicator
//        Map<String, Double> indicadorsAvg = new HashMap<>();
//
//        for (FetEntity fet : fets) {
//            Map<String, Double> indicadors = fet.getIndicadorsJson();
//
//            for (Map.Entry<String, Double> entry : indicadors.entrySet()) {
//                String indicadorNom = entry.getKey();
//                Double valor = entry.getValue();
//
//                indicadorsAvg.merge(indicadorNom, valor, (oldValue, newValue) -> oldValue + newValue);
//            }
//        }
//
//        // Calculate averages
//        for (Map.Entry<String, Double> entry : indicadorsAvg.entrySet()) {
//            entry.setValue(entry.getValue() / fets.size());
//        }
//
//        // Create ResumAnualDTO
//        ResumAnualDTO resumAnual = new ResumAnualDTO();
//        resumAnual.setAny(key.getAny());
//        resumAnual.setDimensions(key.getDimensions());
//        resumAnual.setIndicadors(indicadorsAvg);
//        resumAnual.setNumRegistres(fets.size());
//
//        return resumAnual;
//    }
//
//    private Map<String, Map<String, Double>> calcularEstadistiquesIndicadors(List<FetEntity> fets) {
//        Map<String, List<Double>> valorsPerIndicador = new HashMap<>();
//
//        // Collect all values for each indicator
//        for (FetEntity fet : fets) {
//            Map<String, Double> indicadors = fet.getIndicadorsJson();
//
//            for (Map.Entry<String, Double> entry : indicadors.entrySet()) {
//                String indicadorNom = entry.getKey();
//                Double valor = entry.getValue();
//
//                valorsPerIndicador.computeIfAbsent(indicadorNom, k -> new ArrayList<>()).add(valor);
//            }
//        }
//
//        // Calculate statistics for each indicator
//        Map<String, Map<String, Double>> result = new HashMap<>();
//
//        for (Map.Entry<String, List<Double>> entry : valorsPerIndicador.entrySet()) {
//            String indicadorNom = entry.getKey();
//            List<Double> valors = entry.getValue();
//
//            Map<String, Double> stats = new HashMap<>();
//
//            // Calculate sum
//            double suma = valors.stream().mapToDouble(Double::doubleValue).sum();
//            stats.put("suma", suma);
//
//            // Calculate average
//            double mitja = suma / valors.size();
//            stats.put("mitja", mitja);
//
//            // Calculate max
//            double max = valors.stream().mapToDouble(Double::doubleValue).max().orElse(0);
//            stats.put("max", max);
//
//            // Calculate min
//            double min = valors.stream().mapToDouble(Double::doubleValue).min().orElse(0);
//            stats.put("min", min);
//
//            result.put(indicadorNom, stats);
//        }
//
//        return result;
//    }
//
//    private String generarDescripcioPeriode(PeriodeKey periode) {
//        if (periode.getData() != null) {
//            return periode.getData().format(DateTimeFormatter.ISO_DATE);
//        }
//
//        StringBuilder desc = new StringBuilder();
//        desc.append(periode.getAny());
//
//        if (periode.getTrimestre() != null) {
//            desc.append("-T").append(periode.getTrimestre());
//        }
//
//        if (periode.getMes() != null) {
//            desc.append("-").append(String.format("%02d", periode.getMes()));
//        }
//
//        if (periode.getSetmana() != null) {
//            desc.append("-S").append(String.format("%02d", periode.getSetmana()));
//        }
//
//        return desc.toString();
//    }
//
//    @Data
//    private static class PeriodeKey {
//        private Integer any;
//        private Integer trimestre;
//        private Integer mes;
//        private Integer setmana;
//        private LocalDate data;
//        private Map<String, String> dimensions;
//    }
//
//    @Data
//    private static class AnualDimensionsKey {
//        private int any;
//        private Map<String, String> dimensions;
//    }
//
//    public enum NivellAgrupacio {
//        ANY,
//        TRIMESTRE,
//        MES,
//        SETMANA,
//        DIA
//    }
//
//    @Data
//    public static class ResumPeriodeDTO {
//        private PeriodeDTO periode;
//        private Map<String, String> dimensions;
//        private int numRegistres;
//        private List<IndicadorEstadisticDTO> estadistiques;
//
//        @Data
//        public static class PeriodeDTO {
//            private Integer any;
//            private Integer trimestre;
//            private Integer mes;
//            private Integer setmana;
//            private LocalDate data;
//            private String descripcio;  // Ex: "2024-T1", "2024-03", etc.
//        }
//
//        @Data
//        public static class IndicadorEstadisticDTO {
//            private String indicador;
//            private Double suma;
//            private Double mitja;
//            private Double maxim;
//            private Double minim;
//        }
//    }
//
//    @Data
//    public static class ResumAnualDTO {
//        private int any;
//        private Map<String, String> dimensions;
//        private Map<String, Double> indicadors;
//        private int numRegistres;
//    }
}
