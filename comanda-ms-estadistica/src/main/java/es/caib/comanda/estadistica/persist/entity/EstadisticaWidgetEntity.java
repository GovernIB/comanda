package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.UnitatTempsEnum;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "widget_type", discriminatorType = DiscriminatorType.STRING) // Identifica el tipus d'entitat
@Table(
        name = BaseConfig.DB_PREFIX + "est_widget",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "widget_titol_uk", columnNames = { "titol", "entorn_app_id" })
        })
public abstract  class EstadisticaWidgetEntity<E> extends BaseAuditableEntity<E> {

    @Column(name = "titol", length = 64, nullable = false)
    private String titol;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;

    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;

    @ManyToMany
    @JoinTable(
            name = BaseConfig.DB_PREFIX + "est_widget_dim_valor",
            joinColumns = @JoinColumn(name = "widget_id"),
            inverseJoinColumns = @JoinColumn(name = "dimensio_valor_id")
    )
    private List<DimensioValorEntity> valorDimensions;

    // Periode de temps que es vol mostrar
    @Column(name = "inici_unitat", length = 16)
    @Enumerated(EnumType.STRING)
    private UnitatTempsEnum iniciUnitat;
    // Número de unitats a retrocedir per iniciar el període de la vista
    @Column(name = "inici")
    private Integer inici;
    @Column(name = "duracio_unitat", length = 16)
    @Enumerated(EnumType.STRING)
    private UnitatTempsEnum duracioUnitat;
    @Column(name = "duracio")
    private Integer duracio;

}
