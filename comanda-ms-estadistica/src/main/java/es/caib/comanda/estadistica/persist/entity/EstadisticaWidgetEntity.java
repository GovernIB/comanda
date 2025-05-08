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

/**
 * Representa una entitat abstracta per a widgets d'estadística, emmagatzemada a la base de dades.
 *
 * Aquesta classe defineix la configuració bàsica comuna per a tots els widgets d'estadística.
 * Inclou elements com el títol, la descripció, relacions amb entitats externes i paràmetres
 * per establir el rang periòdic del widget. És la base per altres entitats més específiques.
 *
 * Relacions:
 * - `valorDimensions`: Relació amb l'entitat `DimensioValorEntity` que especifica dimensions i els seus valors associats.
 *
 * Propietats principals:
 * - `titol`: Títol descriptiu del widget amb una longitud màxima de 64 caràcters.
 * - `descripcio`: Descripció opcional del widget amb una longitud de fins a 1024 caràcters.
 * - `entornAppId`: Identificador de l'entorn d'aplicació que està associat al widget.
 * - `iniciUnitat` i `inici`: Especifica la unitat de temps i el nombre d'unitats per determinar el període inicial.
 * - `duracioUnitat` i `duracio`: Defineix la unitat de temps i la duració per establir l'abast del widget.
 *
 * Ús:
 * Aquesta entitat s'ha de fer servir com a classe base per altres widgets específics, implementant funcionalitats particulars
 * mitjançant la discriminació pel camp `widget_type`.
 *
 * Validacions i restriccions:
 * - El camp `titol` és obligatori i únic per entorn d'aplicació.
 * - El període temporal derivat de `inici*` i `duracio*` ha de ser definit correctament per assegurar el seu ús adequat.
 *
 * Autor: Límit Tecnologies
 */
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
