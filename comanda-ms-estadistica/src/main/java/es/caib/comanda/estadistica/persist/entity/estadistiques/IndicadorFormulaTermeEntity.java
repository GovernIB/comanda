package es.caib.comanda.estadistica.persist.entity.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorFormulaTerme;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entitat JPA que representa un terme d'una fórmula d'indicador (vegeu {@link IndicadorFormulaTerme}).
 *
 * Correspon a la taula "com_est_indicador_formula_terme". Cada fila enllaça un indicador de tipus FORMULA
 * (indicadorFormula) amb un dels indicadors SIMPLE que en formen part (indicadorComponent), l'operador amb
 * què s'aplica (SUMA/RESTA) i la posició dins la fórmula (ordre).
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_indicador_formula_terme")
@Getter
@Setter
@NoArgsConstructor
public class IndicadorFormulaTermeEntity extends BaseEntity<IndicadorFormulaTerme> {

    @ManyToOne
    @JoinColumn(
            name = "indicador_formula_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "ind_formula_terme_formula_fk"))
    private IndicadorEntity indicadorFormula;

    @ManyToOne
    @JoinColumn(
            name = "indicador_component_id",
            referencedColumnName = "id",
            nullable = false,
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "ind_formula_terme_component_fk"))
    private IndicadorEntity indicadorComponent;

    @Column(name = "operador", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private OperadorFormulaEnum operador;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;

}
