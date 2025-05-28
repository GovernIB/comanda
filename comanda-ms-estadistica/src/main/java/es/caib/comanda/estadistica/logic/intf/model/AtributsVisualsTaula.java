package es.caib.comanda.estadistica.logic.intf.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.ms.logic.intf.exception.ObjectMappingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * Classe que representa els atributs visuals d'un widget de taula.
 * Aquesta classe s'utilitza per emmagatzemar els atributs visuals en format JSON.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtributsVisualsTaula implements AtributsVisuals {

    // Configuració general de la taula
    private Boolean mostrarCapcalera;     // Indica si s'ha de mostrar la capçalera de la taula
    private Boolean mostrarBordes;        // Indica si s'han de mostrar els bordes de la taula
    private Boolean mostrarAlternancia;   // Indica si s'han d'alternar els colors de les files
    @Size(max = 8)
    private String colorAlternancia;      // Color per a les files alternes

    // Configuració de columnes
    private List<ColumnaEstil> columnesEstils;        // Estils per a les columnes

    // Configuració de cel·les destacades
    private List<CellaDestacada> cellesDestacades;      // Configuració per a destacar cel·les específiques


    public String fromAtributsVisuals() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ObjectMappingException(AtributsVisualsTaula.class, String.class, "Error convertint atributs visuals a JSON");
        }
    }

    public static AtributsVisualsTaula toAtributsVisuals(String json) {

        if (json == null) {
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, AtributsVisualsTaula.class);
        } catch (JsonProcessingException e) {
            log.error("Error convertint JSON a atributs visuals", e);
            throw new ObjectMappingException(String.class, AtributsVisualsTaula.class, "Error convertint JSON a atributs visuals");
        }
    }

    public AtributsVisuals merge(AtributsVisuals otherAtributsVisuals) {
        if (otherAtributsVisuals == null || !(otherAtributsVisuals instanceof AtributsVisualsTaula)) {
            return this;
        }

        AtributsVisualsTaula other = (AtributsVisualsTaula) otherAtributsVisuals;
        this.mostrarCapcalera = mergeField(this.mostrarCapcalera, other.getMostrarCapcalera());
        this.mostrarBordes = mergeField(this.mostrarBordes, other.getMostrarBordes());
        this.mostrarAlternancia = mergeField(this.mostrarAlternancia, other.getMostrarAlternancia());
        this.colorAlternancia = mergeField(this.colorAlternancia, other.getColorAlternancia());
        this.columnesEstils = mergeField(this.columnesEstils, other.getColumnesEstils());
        this.cellesDestacades = mergeField(this.cellesDestacades, other.getCellesDestacades());

        return this;
    }

    /**
     * Classe interna que representa l'estil d'una columna.
     * Permet definir colors i formats específics per a una columna.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnaEstil {
        @Size(max = 64)
        private String codiColumna;           // Codi de la columna a la qual s'aplica l'estil
        @Size(max = 8)
        private String colorText;             // Color del text de la columna
        @Size(max = 8)
        private String colorFons;             // Color de fons de la columna
        private Boolean negreta;              // Indica si el text ha d'estar en negreta
        private Boolean cursiva;              // Indica si el text ha d'estar en cursiva
        private List<RangValor> rangsValors;  // Rangs de valors per aplicar estils condicionals
    }

    /**
     * Classe interna que representa un rang de valors per a l'estil condicional.
     * Permet definir colors i formats específics per a valors dins d'un rang.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RangValor {
        private Double valorMin;      // Valor mínim del rang (inclusiu)
        private Double valorMax;      // Valor màxim del rang (inclusiu)
        @Size(max = 8)
        private String colorText;     // Color del text per a valors dins del rang
        @Size(max = 8)
        private String colorFons;     // Color de fons per a valors dins del rang
        private Boolean negreta;      // Indica si el text ha d'estar en negreta
        private Boolean cursiva;      // Indica si el text ha d'estar en cursiva
    }

    /**
     * Classe interna que representa una cel·la destacada.
     * Permet destacar cel·les específiques basades en condicions.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CellaDestacada {
        @Size(max = 64)
        private String codiColumna;       // Codi de la columna on es troba la cel·la
        @Size(max = 64)
        private String valorDimensio;     // Valor de la dimensió on es troba la cel·la (fila)
        @Size(max = 8)
        private String colorText;         // Color del text de la cel·la
        @Size(max = 8)
        private String colorFons;         // Color de fons de la cel·la
        private Boolean negreta;          // Indica si el text ha d'estar en negreta
        private Boolean cursiva;          // Indica si el text ha d'estar en cursiva
        @Size(max = 64)
        private String iconaPrefix;       // Icona a mostrar abans del valor
        @Size(max = 64)
        private String iconaSufix;        // Icona a mostrar després del valor
    }
}
