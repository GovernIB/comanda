package es.caib.comanda.estadistica.persist.entity.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entitat que representa un valor associat a una dimensió en el sistema.
 *
 * Aquesta classe és un recurs persistit en base de dades i hereta de BaseEntity.
 * El nom de la seva taula és configurat amb el prefix indicat en BaseConfig.
 *
 * Relacions:
 * - Està associada a l'entitat DimensioEntity mitjançant la propietat `dimensio`.
 *
 * Propietats:
 * - `valor`: Representa una cadena de text associada al valor de la dimensió.
 * - `dimensio`: Fa referència a l'entitat DimensioEntity a la qual aquest valor està relacionat.
 *
 * Ús:
 * Aquesta entitat s'utilitza en el context de les estadístiques per vincular dimensions i els seus valors específics.
 * Permet estructurar i consultar dades relacionades amb les dimensions de manera eficient i escalable.
 *
 * Restriccions:
 * - La propietat `valor` és de longitud màxima de 255 caràcters.
 * - La propietat `dimensio` utilitza una relació ManyToOne Lazy per millorar el rendiment del sistema.
 *
 * Persistència:
 * - El valor `valor` i la relació amb `dimensio` són mapejades a camps de la base de dades identificats
 *   pel prefix definit en les configuracions de BaseConfig.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_dimensio_valor")
@Getter
@Setter
@NoArgsConstructor
public class DimensioValorEntity extends BaseEntity<DimensioValor> {

    @Column(name = "valor", length = 255)
    private String valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dimensio_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "dim_valor_dim_fk"))
    private DimensioEntity dimensio;

}
