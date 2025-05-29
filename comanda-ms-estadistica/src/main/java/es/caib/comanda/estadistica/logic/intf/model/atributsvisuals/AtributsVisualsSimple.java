package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

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

/**
 * Classe que representa els atributs visuals d'un widget simple.
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
public class AtributsVisualsSimple implements AtributsVisuals {

    // Nom de la icona MUI a visualitzar (opcional)
    @Size(max = 64)
    private String icona;
    
    // Estils per defecte del widget (opcionals)
    @Size(max = 8)
    private String colorText;
    @Size(max = 8)
    private String colorFons;
    @Size(max = 8)
    private String colorIcona;
    @Size(max = 8)
    private String colorFonsIcona;
    private Boolean destacat;
    @Size(max = 8)
    private String colorTextDestacat;
    @Size(max = 8)
    private String colorFonsDestacat;
    private Boolean borde;
    @Size(max = 8)
    private String colorBorde;


    public String fromAtributsVisuals() {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("Error convertint atributs visuals a JSON", e);
            throw new ObjectMappingException(AtributsVisualsSimple.class, String.class, "Error convertint atributs visuals a JSON");
        }
    }
    
    public static AtributsVisualsSimple toAtributsVisuals(String json) {

        if (json == null) {
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, AtributsVisualsSimple.class);
        } catch (JsonProcessingException e) {
            log.error("Error convertint JSON a atributs visuals", e);
            throw new ObjectMappingException(String.class, AtributsVisualsSimple.class, "Error convertint JSON a atributs visuals");
        }
    }

    public AtributsVisuals merge(AtributsVisuals otherAtributsVisuals) {
        if (otherAtributsVisuals == null || !(otherAtributsVisuals instanceof AtributsVisualsSimple)) {
            return this;
        }

        AtributsVisualsSimple other = (AtributsVisualsSimple) otherAtributsVisuals;
        this.icona = mergeField(this.icona, other.getIcona());
        this.colorText = mergeField(this.colorText, other.getColorText());
        this.colorFons = mergeField(this.colorFons, other.getColorFons());
        this.colorIcona = mergeField(this.colorIcona, other.getColorIcona());
        this.colorFonsIcona = mergeField(this.colorFonsIcona, other.getColorFonsIcona());
        this.destacat = mergeField(this.destacat, other.getDestacat());
        this.colorTextDestacat = mergeField(this.colorTextDestacat, other.getColorTextDestacat());
        this.colorFonsDestacat = mergeField(this.colorFonsDestacat, other.getColorFonsDestacat());
        this.borde = mergeField(this.borde, other.getBorde());
        this.colorBorde = mergeField(this.colorBorde, other.getColorBorde());

        return this;
    }

}