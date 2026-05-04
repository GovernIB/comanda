package es.caib.comanda.estadistica.persist.entity.paleta;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = BaseConfig.DB_PREFIX + "est_color_palette")
public class PaletaEntity extends BaseAuditableEntity<Paleta> {

    @Column(name = "nom", nullable = false, length = 128)
    private String nom;

    @Column(name = "descripcio", length = 1024)
    private String descripcio;

    @OneToMany(mappedBy = "paleta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicio ASC")
    private List<PaletaColorEntity> colors;
}
