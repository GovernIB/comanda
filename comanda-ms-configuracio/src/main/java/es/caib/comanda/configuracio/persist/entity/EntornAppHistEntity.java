package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.configuracio.logic.intf.model.EntornAppHist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Històric de canvis externs d'entorn d'aplicació.
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "entorn_app_hist")
@Getter
@Setter
@NoArgsConstructor
public class EntornAppHistEntity extends BaseEntity<EntornAppHist> {

    public static final String ENTORN_APP_COLUMN_NAME = "entorn_app_id";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = ENTORN_APP_COLUMN_NAME,
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "entorn_app_hist_fk"))
    private EntornAppEntity entornApp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula("(select ea." + EntornAppEntity.ENTORN_COLUMN_NAME + " from " + EntornAppEntity.TABLE_NAME + " ea where ea.id = " + EntornAppHistEntity.ENTORN_APP_COLUMN_NAME + ")")
    private EntornEntity entorn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula("(select ea." + EntornAppEntity.APP_COLUMN_NAME + " from " + EntornAppEntity.TABLE_NAME + " ea where ea.id = " + EntornAppHistEntity.ENTORN_APP_COLUMN_NAME + ")")
    private AppEntity app;

    @Column(name = "versio", nullable = false)
    private String versio;
    @Column(name = "revisio", nullable = false)
    private String revisio;
    @Column(name = "canvi_versio", nullable = false)
    private boolean canviVersio;
    @Column(name = "data", nullable = false)
    private LocalDateTime data;

}
