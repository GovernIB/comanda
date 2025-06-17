package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * Classe que representa els atributs visuals d'un widget simple.
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
public class AtributsVisualsTitol extends AtributsVisuals implements Serializable {

    private String colorTitol;
    private Integer midaFontTitol;
    private String colorSubtitol;
    private Integer midaFontSubtitol;
    private String colorFons;
    private Boolean mostrarVora;
    private String colorVora;
    private Integer ampleVora;


    public AtributsVisuals merge(AtributsVisuals otherAtributsVisuals) {
        if (otherAtributsVisuals == null || !(otherAtributsVisuals instanceof AtributsVisualsTitol)) {
            return this;
        }

        AtributsVisualsTitol other = (AtributsVisualsTitol) otherAtributsVisuals;
        this.colorTitol = mergeField(this.colorTitol, other.getColorText());
        this.midaFontTitol = mergeField(this.midaFontTitol, other.getMidaFontTitol());
        this.colorSubtitol = mergeField(this.colorSubtitol, other.getColorSubtitol());
        this.midaFontSubtitol = mergeField(this.midaFontSubtitol, other.getMidaFontSubtitol());
        this.colorFons = mergeField(this.colorFons, other.getColorFons());
        this.mostrarVora = mergeField(this.mostrarVora, other.getMostrarVora());
        this.colorVora = mergeField(this.colorVora, other.getColorVora());
        this.ampleVora = mergeField(this.ampleVora, other.getAmpleVora());

        return this;
    }

}