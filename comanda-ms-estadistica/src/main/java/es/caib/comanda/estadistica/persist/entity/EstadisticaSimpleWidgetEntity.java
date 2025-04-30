package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaSimpleWidget;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("SIMPLE") // Valor específic al discriminador
public class EstadisticaSimpleWidgetEntity extends EstadisticaWidgetEntity<EstadisticaSimpleWidget> {

    // Text a mostrar després del valor. Ex 20 "dies"
    @Column(name = "unitat", length = 64)
    private String unitat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "indicador_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "view_indicador_fk"),
            nullable = false)
    private IndicadorEntity indicador;

}
