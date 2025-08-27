package es.caib.comanda.tasques.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.broker.model.Prioritat;
import es.caib.comanda.ms.broker.model.TascaEstat;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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
    @Column(name = "estat", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private TascaEstat estat;
    @Column(name = "estat_desc", length = 1024)
    private String estatDescripcio;
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
    @Column(name = "grup", length = 128)
    private String grup;

    @ElementCollection
    @CollectionTable(
            name = BaseConfig.DB_PREFIX + "tasca_usuari",
            joinColumns = @JoinColumn(name = "tasca_id"))
    @Column(name = "usuari")
    private List<String> usuarisAmbPermis;
    @ElementCollection
    @CollectionTable(
            name = BaseConfig.DB_PREFIX + "tasca_grup",
            joinColumns = @JoinColumn(name = "tasca_id"))
    @Column(name = "grup")
    private List<String> grupsAmbPermis;

    @Builder
    public TascaEntity(Tasca tasca) {
        this.entornAppId = tasca.getEntornAppId();
        this.entornId = tasca.getEntornId();
        this.appId = tasca.getAppId();
        this.identificador = tasca.getIdentificador();
        this.tipus = tasca.getTipus();
        this.nom = tasca.getNom();
        this.descripcio = tasca.getDescripcio();
        this.estat = tasca.getEstat();
        this.estatDescripcio = tasca.getEstatDescripcio();
        this.prioritat = tasca.getPrioritat();
        this.dataInici = tasca.getDataInici();
        this.dataFi = tasca.getDataFi();
        this.dataCaducitat = tasca.getDataCaducitat();
        this.url = tasca.getUrl();
        this.responsable = tasca.getResponsable();
        this.grup = tasca.getGrup();
        this.usuarisAmbPermis = tasca.getUsuarisAmbPermis();
        this.grupsAmbPermis = tasca.getGrupsAmbPermis();
    }

}
