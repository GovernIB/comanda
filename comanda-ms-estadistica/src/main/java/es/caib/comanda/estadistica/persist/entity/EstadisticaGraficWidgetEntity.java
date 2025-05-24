package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaGraficWidget;
import es.caib.comanda.estadistica.logic.intf.model.GraficValueTypeEnum;
import es.caib.comanda.estadistica.logic.intf.model.TipusGraficEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


/**
 * Representa una entitat destinada a emmagatzemar les característiques d'un widget estadístic de tipus gràfic. Aquesta classe hereta de
 * EstadisticaWidgetEntity amb el tipus generificat EstadisticaGraficWidget. Conté propietats específiques per a la configuració de gràfics
 * estadístics, com el tipus de gràfic, el format de valors, la llegenda dels eixos i l'agrupació temporal, així com una relació amb un indicador.
 *
 * Aquesta entitat està anotada amb JPA per a ser emmagatzemada en una taula que compartix estructures amb altres tipus de widget gràfics.
 * El discriminator value per aquesta entitat és "GRAFIC".
 *
 * Aquesta classe és utilitzada en conjunció amb widgets estadístics per proporcionar representacions visuals de les dades.
 *
 * @author Límit Tecnologies
 */
@Getter
@Entity
@DiscriminatorValue("GRAFIC") // Valor específic al discriminador
public class EstadisticaGraficWidgetEntity extends EstadisticaWidgetEntity<EstadisticaGraficWidget> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "indicador_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "view_indicador_fk"),
            nullable = false)
    private IndicadorTaulaEntity indicador;

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
    private PeriodeUnitat tempsAgrupacio;

    // Per crear gràfics compostos, on s'indicarà una dimensió per descomposar.
    // A la gràfica es mostraràn els valors descomposats per la dimensió indicada
    // P. ex. si definim un gràfic de línies per expedients creats l'últim més agrupats per dies,
    // si afegim la dimensió usuari, la gràfica mostrarà una línia amb els expedients creats per a cada usuari
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dimensio_descomposicio_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "view_descomposicio_fk"))
    private DimensioValorEntity dimensioDescomposicio;

    @Column(name = "llegenda_x", length = 64, nullable = false)
    private String llegendaX;

    @Column(name = "llegenda_y", length = 64, nullable = false)
    private String llegendaY;

}
