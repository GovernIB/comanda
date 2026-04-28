package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutHist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Històric de canvis d'estat de salut per entorn d'aplicació.
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "salut_hist")
@Getter
@Setter
@NoArgsConstructor
public class SalutHistEntity extends BaseEntity<SalutHist> {

    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Enumerated(EnumType.STRING)
    @Column(name = "app_estat", nullable = false)
    private SalutEstat appEstat;

    @Column(name = "peticio_error", nullable = false)
    private boolean peticioError;
}
