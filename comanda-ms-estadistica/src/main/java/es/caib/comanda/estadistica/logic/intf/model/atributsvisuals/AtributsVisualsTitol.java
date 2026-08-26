package es.caib.comanda.estadistica.logic.intf.model.atributsvisuals;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.PosicioSubtitol;
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
    private PosicioSubtitol posicioSubtitol;
    private Integer separacioSubtitol;
    private Boolean mostrarVoraTop;
    private String colorVoraTop;
    private Integer ampleVoraTop;
    private Boolean mostrarVoraRight;
    private String colorVoraRight;
    private Integer ampleVoraRight;
    private Boolean mostrarVoraBottom;
    private String colorVoraBottom;
    private Integer ampleVoraBottom;
    private Boolean mostrarVoraLeft;
    private String colorVoraLeft;
    private Integer ampleVoraLeft;


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
        this.posicioSubtitol = mergeField(this.posicioSubtitol, other.getPosicioSubtitol());
        this.separacioSubtitol = mergeField(this.separacioSubtitol, other.getSeparacioSubtitol());
        this.mostrarVoraTop = mergeField(this.mostrarVoraTop, other.getMostrarVoraTop());
        this.colorVoraTop = mergeField(this.colorVoraTop, other.getColorVoraTop());
        this.ampleVoraTop = mergeField(this.ampleVoraTop, other.getAmpleVoraTop());
        this.mostrarVoraRight = mergeField(this.mostrarVoraRight, other.getMostrarVoraRight());
        this.colorVoraRight = mergeField(this.colorVoraRight, other.getColorVoraRight());
        this.ampleVoraRight = mergeField(this.ampleVoraRight, other.getAmpleVoraRight());
        this.mostrarVoraBottom = mergeField(this.mostrarVoraBottom, other.getMostrarVoraBottom());
        this.colorVoraBottom = mergeField(this.colorVoraBottom, other.getColorVoraBottom());
        this.ampleVoraBottom = mergeField(this.ampleVoraBottom, other.getAmpleVoraBottom());
        this.mostrarVoraLeft = mergeField(this.mostrarVoraLeft, other.getMostrarVoraLeft());
        this.colorVoraLeft = mergeField(this.colorVoraLeft, other.getColorVoraLeft());
        this.ampleVoraLeft = mergeField(this.ampleVoraLeft, other.getAmpleVoraLeft());

        return this;
    }

}
