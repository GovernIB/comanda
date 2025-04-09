package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaView;
import es.caib.comanda.estadistica.logic.intf.model.Indicador;
import es.caib.comanda.estadistica.logic.intf.model.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.TipusVistaEnum;
import es.caib.comanda.estadistica.logic.intf.model.UnitatTempsEnum;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;

public class EstadisticaViewEntity extends BaseEntity<EstadisticaView> {

    @Column(name = "titol", length = 64, nullable = false)
    private String titol;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;
    // Text a mostrar després del valor. Ex 20 "dies"
    @Column(name = "unitat", length = 64)
    private String unitat;

    @Column(name = "aplicacio_codi", length = 16, nullable = false)
    private String aplicacioCodi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "indicador_id",
            referencedColumnName = "id",
            foreignKey = @javax.persistence.ForeignKey(name = "view_indicador_fk"),
            nullable = false)
    private Indicador indicador;

    // Tipus de vista a generar; numeric, bar_chart, pie_chart, etc.
    @Column(name = "tipus_vista", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private TipusVistaEnum tipusVista;
    @ManyToMany
    @JoinTable(
            name = BaseConfig.DB_PREFIX + "est_view_dimensio_valor",
            joinColumns = @JoinColumn(name = "view_id"),
            inverseJoinColumns = @JoinColumn(name = "dimensio_valor_id")
    )
    private List<DimensioValorEntity> dimensionsaValorFiltre;

    // Periode de temps que es vol mostrar
    @Column(name = "periode_inici_unitat", length = 16)
    @Enumerated(EnumType.STRING)
    private UnitatTempsEnum periodeIniciUnitat;
    // Número de unitats a retrocedir per iniciar el període de la vista
    @Column(name = "periode_inici")
    private Integer periodeInici;
    @Column(name = "periode_unitat", length = 16)
    @Enumerated(EnumType.STRING)
    private UnitatTempsEnum periodeUnitat;
    @Column(name = "periode")
    private Integer periode;

    // En cas de gràfic, quina agrupació de temps utilitzar:
    // hora, dia, setmana, mes, trimestre, any, o dia de la setmana (DL, DM, ...)
    @Column(name = "temps_agrupacio", length = 16)
    @Enumerated(EnumType.STRING)
    private UnitatTempsEnum tempsAgrupacio;

    // Camps en cas de vist tipus TABLE
    // first_seen, last_seen, avg_interval, average, etc.
    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(
            name = BaseConfig.DB_PREFIX + "est_view_taula_columnes",
            joinColumns = @JoinColumn(name = "view_id"),
            foreignKey = @ForeignKey(name = "view_taula_columnes_fk"))
    @Column(name = "columna", length = 16)
    private List<TableColumnsEnum> taulaColumnes;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "taula_dimensio_id",
            referencedColumnName = "id",
            foreignKey = @javax.persistence.ForeignKey(name = "view_taula_dimensio_fk")
    )
    private DimensioEntity taulaDimensio;
}
