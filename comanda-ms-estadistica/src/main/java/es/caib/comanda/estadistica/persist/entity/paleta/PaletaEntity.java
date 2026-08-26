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

    public static final int NOM_MAX_LENGTH = 128;
    public static final int DESCRIPCIO_MAX_LENGTH = 1024;

    @Column(name = "nom", nullable = false, length = NOM_MAX_LENGTH)
    private String nom;

    @Column(name = "descripcio", length = DESCRIPCIO_MAX_LENGTH)
    private String descripcio;

    @OneToMany(mappedBy = "paleta", cascade = CascadeType.ALL)
    @OrderBy("posicio ASC")
    private List<PaletaColorEntity> colors;
}
