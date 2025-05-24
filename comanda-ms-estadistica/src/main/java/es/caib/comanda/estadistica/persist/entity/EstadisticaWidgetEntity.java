package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAbsolutTipus;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAlineacio;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAnchor;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeEspecificAny;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeMode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.periode.PresetPeriode;
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
import java.time.LocalDate;
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
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Column(name = "periode_mode")
    @Enumerated(EnumType.STRING)
    private PeriodeMode periodeMode;

    // Camps per mode PRESET
    @Column(name = "preset_periode")
    @Enumerated(EnumType.STRING)
    private PresetPeriode presetPeriode;
    @Column(name = "preset_count")
    private Integer presetCount;

    //    // --- Si mode = "RELATIVE" (relatiu a una data de referència, normalment avui) ---
    //    "relative_settings": {
    //      "reference_point": "TODAY" | "START_OF_THIS_WEEK" | "START_OF_THIS_MONTH" | "START_OF_THIS_YEAR", // Punt de partida base
    //      "direction": "PAST" | "FUTURE", // Per anàlisi històrica, gairebé sempre "PAST"
    //      "last_n": { // Per a "els/les últims/es N..."
    //        "count": 2,                 // Nombre d'unitats
    //        "unit": "DAY" | "WEEK" | "MONTH" | "QUARTER" | "YEAR",
    //        "alignment": "ROLLING" | "COMPLETE_UNITS" // Per a múltiples unitats (ex: últimes 2 setmanes)
    //                                     // ROLLING: 14 dies exactes enrere des del punt de referència (o final del dia anterior)
    //                                     // COMPLETE_UNITS: Dues setmanes completes (Dill-Dum, Dill-Dum) anteriors al punt de referència.
    //      }
    //      // Alternativament a "last_n", podríem tenir altres construccions relatives si cal.
    //    },
    // Camps per mode RELATIVE
    @Column(name = "relatiu_punt_referencia")
    @Enumerated(EnumType.STRING)
    private PeriodeAnchor relatiuPuntReferencia;
    @Column(name = "relatiu_count")
    private Integer relatiuCount;
    @Column(name = "relatiu_unitat")
    @Enumerated(EnumType.STRING)
    private PeriodeUnitat relatiueUnitat;
    @Column(name = "relatiu_alineacio")
    @Enumerated(EnumType.STRING)
    private PeriodeAlineacio relatiuAlineacio;


    //    --- Si mode = "ABSOLUTE" ---
    //    "absolute_settings": {
    //      "type": "DATE_RANGE" | "SPECIFIC_PERIOD_OF_YEAR",
    //
    //      // Si type = "DATE_RANGE"
    //      "start_date": "YYYY-MM-DD", // Data d'inici explícita
    //      "end_date": "YYYY-MM-DD",   // Data de fi explícita
    //
    //      // Si type = "SPECIFIC_PERIOD_OF_YEAR"
    //      "year_reference": "CURRENT_YEAR" | "PREVIOUS_YEAR" | "SPECIFIC_YEAR",
    //      "specific_year_value": null | 2024, // Si year_reference = "SPECIFIC_YEAR"
    //      "period_unit": "WEEK" | "MONTH" | "QUARTER",
    //      "period_values": [1, 2] // Ex: [1,2] per les dues primeres setmanes/mesos/trimestres.
    //                               // O un valor únic si és només un: [3] per la 3a setmana.
    //    }
    // Camps per mode ABSOLUT
    @Column(name = "absolut_tipus")
    @Enumerated(EnumType.STRING)
    private PeriodeAbsolutTipus absolutTipus;
    // Si absolutTipus = DATE_RANGE (Rang de dates)
    @Column(name = "absolut_data_inici")
    private LocalDate absolutDataInici;
    @Column(name = "absolut_data_fi")
    private LocalDate absolutDataFi;
    // Si absolutTipus = SPECIFIC_PERIOD_OF_YEAR (Periode de temps)
    @Column(name = "absolut_any_referencia")
    @Enumerated(EnumType.STRING)
    private PeriodeEspecificAny absolutAnyReferencia;
    @Column(name = "absolut_any_valor")
    private Integer absolutAnyValor;
    @Column(name = "absolut_periode_unitat")
    @Enumerated(EnumType.STRING)
    private PeriodeUnitat absolutPeriodeUnitat;
    @Column(name = "absolut_periode_inici")
    private Integer absolutPeriodeInici;
    @Column(name = "absolut_periode_fi")
    private Integer absolutPeriodeFi;


}
