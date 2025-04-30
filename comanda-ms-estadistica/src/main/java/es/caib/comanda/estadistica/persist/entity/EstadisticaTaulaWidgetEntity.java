package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaTaulaWidget;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@DiscriminatorValue("TAULA") // Valor específic al discriminador
public class EstadisticaTaulaWidgetEntity extends EstadisticaWidgetEntity<EstadisticaTaulaWidget> {

    @OneToMany(mappedBy="widget", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndicadorTaulaEntity> columnes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "agrupament_dimensio_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "view_taula_dimensio_fk")
    )
    private DimensioEntity dimensioAgrupacio;
    @Column(name = "agrupament_dimensio_titol", length = 64)
    private String titolAgrupament;
}
