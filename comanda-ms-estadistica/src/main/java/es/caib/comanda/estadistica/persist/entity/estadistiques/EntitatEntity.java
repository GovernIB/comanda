package es.caib.comanda.estadistica.persist.entity.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Entitat;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "entitat")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class EntitatEntity extends BaseEntity<Entitat> {

    /**
     * Codi, nom i codiDir3 no són {@code nullable = false}: quan una Entitat es crea automàticament a partir d'un
     * valor de dimensió desconegut (vegeu {@code EntitatResolverHelper.resolveOrCreateEntitat}), només se'n coneix
     * el camp corresponent al {@code entitatValorTipus} de la dimensió; la resta queden buits fins que un
     * administrador els completi.
     */
    @Column(name = "codi", unique = true)
    private String codi;

    @Column(name = "nom")
    private String nom;

    @Column(name = "codi_dir3")
    private String codiDir3;

    @Column(name = "cif")
    private String cif;

}
