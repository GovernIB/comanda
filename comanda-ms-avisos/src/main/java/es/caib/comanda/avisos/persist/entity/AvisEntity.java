package es.caib.comanda.avisos.persist.entity;

import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.broker.model.AvisTipus;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "avis",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "avis_ident_uk", columnNames = {"entorn_app_id", "identificador"})
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

    @Column(name = "url", length = 255)
    private URL url;
    @Column(name = "responsable", length = 128)
    private String responsable;
    @Column(name = "grup", length = 128)
    private String grup;

    @ElementCollection
    @CollectionTable(
            name = BaseConfig.DB_PREFIX + "avis_usuari",
            joinColumns = @JoinColumn(name = "avis_id"),
            foreignKey = @ForeignKey(name = "com_avisusr_avis_fk"))
    @Column(name = "usuari")
    private List<String> usuarisAmbPermis;
    @ElementCollection
    @CollectionTable(
            name = BaseConfig.DB_PREFIX + "avis_grup",
            joinColumns = @JoinColumn(name = "avis_id"),
            foreignKey = @ForeignKey(name = "com_avisgrp_avis_fk"))
    @Column(name = "grup")
    private List<String> grupsAmbPermis;


    @Builder
    public AvisEntity(Avis avis) {
        this.entornAppId = avis.getEntornAppId();
        this.entornId = avis.getEntornId();
        this.appId = avis.getAppId();
        this.identificador = avis.getIdentificador();
        this.tipus = avis.getTipus();
        this.nom = avis.getNom();
        this.descripcio = avis.getDescripcio();
        this.dataInici = avis.getDataInici();
        this.dataFi = avis.getDataFi();

        this.url = avis.getUrl();
        this.responsable = avis.getResponsable();
        this.grup = avis.getGrup();
        this.usuarisAmbPermis = avis.getUsuarisAmbPermis();
        this.grupsAmbPermis = avis.getGrupsAmbPermis();
    }

}
