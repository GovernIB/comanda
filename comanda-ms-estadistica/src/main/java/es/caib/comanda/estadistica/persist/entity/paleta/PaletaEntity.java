package es.caib.comanda.estadistica.persist.entity.paleta;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = BaseConfig.DB_PREFIX + "paleta")
public class PaletaEntity extends BaseEntity<Paleta> {

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "valor", nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "plantilla_id",
            referencedColumnName = "id")
    private PlantillaEntity plantilla;
}
