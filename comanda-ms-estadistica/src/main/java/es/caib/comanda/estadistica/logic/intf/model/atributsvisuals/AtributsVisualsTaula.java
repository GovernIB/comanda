package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Size;
import java.io.Serializable;
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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AtributsVisualsTaula extends AtributsVisuals implements Serializable {

    // Configuració general de la taula
    @Size(max = 8)
    private String colorTextTaula;        // Color del text de la taula (opcional)
    @Size(max = 8)
    private String colorFonsTaula;        // Color de fons per la taula (opcional)

    private Boolean mostrarCapcalera;     // Indica si s'ha de mostrar la capçalera de la taula
    @Size(max = 8)
    private String colorCapcalera;        // Color per la capçalera de la taula (opcional)
    @Size(max = 8)
    private String colorFonsCapcalera;    // Color de fons per la capçalera de la taula (opcional)

    private Boolean mostrarAlternancia;   // Indica si s'han d'alternar els colors de les files
    @Size(max = 8)
    private String colorAlternancia;      // Color per a les files alternes

    private Boolean mostrarVoraTaula;        // Indica si s'han de mostrar els bordes de la taula
    @Size(max = 8)
    private String colorVoraTaula;
    private Integer ampleVoraTaula;

    private Boolean mostrarSeparadorHoritzontal;        // Indica si s'han de mostrar els bordes de la taula
    @Size(max = 8)
    private String colorSeparadorHoritzontal;
    private Integer ampleSeparadorHoritzontal;

    private Boolean mostrarSeparadorVertical;        // Indica si s'han de mostrar els bordes de la taula
    @Size(max = 8)
    private String colorSeparadorVertical;
    private Integer ampleSeparadorVertical;

    private Boolean paginada;

    // Configuració de columnes
    private List<ColumnaEstil> columnesEstils;        // Estils per a les columnes

    // Configuració de cel·les destacades
    private List<CellaDestacada> cellesDestacades;      // Configuració per a destacar cel·les específiques


    public AtributsVisuals merge(AtributsVisuals otherAtributsVisuals) {
        if (otherAtributsVisuals == null || !(otherAtributsVisuals instanceof AtributsVisualsTaula)) {
            return this;
        }

        AtributsVisualsTaula other = (AtributsVisualsTaula) otherAtributsVisuals;
        this.colorText = mergeField(this.colorText, other.getColorText());
        this.colorFons = mergeField(this.colorFons, other.getColorFons());
        this.mostrarVora = mergeField(this.mostrarVora, other.getMostrarVora());
        this.colorVora = mergeField(this.colorVora, other.getColorVora());
        this.ampleVora = mergeField(this.ampleVora, other.getAmpleVora());
        this.colorTextTaula = mergeField(this.colorTextTaula, other.getColorTextTaula());
        this.colorFonsTaula = mergeField(this.colorFonsTaula, other.getColorFonsTaula());
        this.mostrarCapcalera = mergeField(this.mostrarCapcalera, other.getMostrarCapcalera());
        this.colorCapcalera = mergeField(this.colorCapcalera, other.getColorCapcalera());
        this.colorFonsCapcalera = mergeField(this.colorFonsCapcalera, other.getColorFonsCapcalera());
        this.mostrarAlternancia = mergeField(this.mostrarAlternancia, other.getMostrarAlternancia());
        this.colorAlternancia = mergeField(this.colorAlternancia, other.getColorAlternancia());
        this.mostrarVoraTaula = mergeField(this.mostrarVoraTaula, other.getMostrarVoraTaula());
        this.colorVoraTaula = mergeField(this.colorVoraTaula, other.getColorVoraTaula());
        this.ampleVoraTaula = mergeField(this.ampleVoraTaula, other.getAmpleVoraTaula());
        this.mostrarSeparadorHoritzontal = mergeField(this.mostrarSeparadorHoritzontal, other.getMostrarSeparadorHoritzontal());
        this.colorSeparadorHoritzontal = mergeField(this.colorSeparadorHoritzontal, other.getColorSeparadorHoritzontal());
        this.ampleSeparadorHoritzontal = mergeField(this.ampleSeparadorHoritzontal, other.getAmpleSeparadorHoritzontal());
        this.mostrarSeparadorVertical = mergeField(this.mostrarSeparadorVertical, other.getMostrarSeparadorVertical());
        this.colorSeparadorVertical = mergeField(this.colorSeparadorVertical, other.getColorSeparadorVertical());
        this.ampleSeparadorVertical = mergeField(this.ampleSeparadorVertical, other.getAmpleSeparadorVertical());
        this.paginada = mergeField(this.paginada, other.getPaginada());

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
