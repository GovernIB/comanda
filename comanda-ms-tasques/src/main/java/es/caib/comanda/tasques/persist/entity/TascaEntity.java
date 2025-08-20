package es.caib.comanda.tasques.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.broker.model.Prioritat;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "tasca",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "tasca_ident_uk", columnNames = { "entorn_app_id", "identificador" })
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TascaEntity extends BaseAuditableEntity<Tasca> {

    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;
    @Column(name = "entorn_id", nullable = false)
    private Long entornId;
    @Column(name = "app_id", nullable = false)
    private Long appId;

    @Column(name = "identificador", length = 64, nullable = false)
    private String identificador;
    @Column(name = "tipus", length = 64, nullable = false)
    private String tipus;
    @Column(name = "nom", length = 255, nullable = false)
    private String nom;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;
    @Column(name = "prioritat", length = 16)
    @Enumerated(EnumType.STRING)
    private Prioritat prioritat;
    @Column(name = "data_inici")
    private LocalDateTime dataInici;
    @Column(name = "data_fi")
    private LocalDateTime dataFi;
    @Column(name = "data_caducitat")
    private LocalDateTime dataCaducitat;
    @Column(name = "url", length = 255, nullable = false)
    private URL url;
    @Column(name = "responsable", length = 128)
    private String responsable;

    @ElementCollection
    @CollectionTable(name = BaseConfig.DB_PREFIX + "tasca_usuari")
    @Column(name = "usuari")
    private List<String> usuarisAmbPermis;
    @ElementCollection
    @CollectionTable(name = BaseConfig.DB_PREFIX + "tasca_grup")
    @Column(name = "grup")
    private List<String> grupsAmbPermis;

}
