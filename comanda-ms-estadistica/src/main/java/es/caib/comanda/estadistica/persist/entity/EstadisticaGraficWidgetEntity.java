package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaGraficWidget;
import es.caib.comanda.estadistica.logic.intf.model.GraficValueTypeEnum;
import es.caib.comanda.estadistica.logic.intf.model.TipusGraficEnum;
import es.caib.comanda.estadistica.logic.intf.model.UnitatTempsEnum;

import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Entity
@DiscriminatorValue("GRAFIC") // Valor específic al discriminador
public class EstadisticaGraficWidgetEntity extends EstadisticaWidgetEntity<EstadisticaGraficWidget> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "indicador_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "view_indicador_fk"),
            nullable = false)
    private IndicadorEntity indicador;

    // Tipus de vista a generar; numeric, bar_chart, pie_chart, etc.
    @Column(name = "tipus_grafic", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private TipusGraficEnum tipusGrafic;

    // Format dels valors a mostrar: NO_MOSTRAR, NUMERIC, PERCENTATGE
    @Column(name = "tipus_valors", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private GraficValueTypeEnum tipusValors;

    // Quina agrupació de temps utilitzar:
    // hora, dia, setmana, mes, trimestre, any, o dia de la setmana (DL, DM, ...)
    @Column(name = "temps_agrupacio", length = 16)
    @Enumerated(EnumType.STRING)
    private UnitatTempsEnum tempsAgrupacio;

    @Column(name = "llegenda_x", length = 64, nullable = false)
    private String llegendaX;

    @Column(name = "llegenda_y", length = 64, nullable = false)
    private String llegendaY;

}
