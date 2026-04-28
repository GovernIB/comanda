package es.caib.comanda.avisos.persist.entity;

import es.caib.comanda.avisos.logic.intf.model.AvisLlegit;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "avis_llegit")
@Getter
@Setter
@NoArgsConstructor
@FieldNameConstants
public class AvisLlegitEntity extends BaseAuditableEntity<AvisLlegit> {

    @Column(name = "usuari", length = 255, nullable = false)
    private String usuari;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "avis_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "avis_llegit_avis_fk"))
    private AvisEntity avis;

    @Builder
    public AvisLlegitEntity(AvisLlegit avis, AvisEntity avisEntity) {
        this.usuari = avis.getUsuari();

        this.avis = avisEntity;
    }

}
