package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

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
    @Size(max = 8)
    private String colorTextDestacat;
    private Boolean vora;
    @Size(max = 8)
    private String colorVora;
    private Integer ampleVora;


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
        this.colorTextDestacat = mergeField(this.colorTextDestacat, other.getColorTextDestacat());
        this.vora = mergeField(this.vora, other.getVora());
        this.colorVora = mergeField(this.colorVora, other.getColorVora());
        this.ampleVora = mergeField(this.ampleVora, other.getAmpleVora());

        return this;
    }

}