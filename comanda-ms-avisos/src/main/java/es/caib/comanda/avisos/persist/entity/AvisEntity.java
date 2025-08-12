package es.caib.comanda.avisos.persist.entity;

import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.ms.broker.model.AvisTipus;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "avis",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "avis_ident_uk", columnNames = { "entorn_app_id", "identificador" })
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AvisEntity extends BaseAuditableEntity<Avis> {

        @Column(name = "entorn_app_id", nullable = false)
        private Long entornAppId;
        @Column(name = "entorn_id", nullable = false)
        private Long entornId;
        @Column(name = "app_id", nullable = false)
        private Long appId;

        @Column(name = "identificador", length = 64, nullable = false)
        private String identificador;
        @Column(name = "tipus", length = 16, nullable = false)
        @Enumerated(EnumType.STRING)
        private AvisTipus tipus;
        @Column(name = "nom", length = 255, nullable = false)
        private String nom;
        @Column(name = "descripcio", length = 1024)
        private String descripcio;
        @Column(name = "data_inici")
        private LocalDateTime dataInici;
        @Column(name = "data_fi")
        private LocalDateTime dataFi;

}
