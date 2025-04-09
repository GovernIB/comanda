package es.caib.comanda.estadistica.persist.entity;

import es.caib.comanda.estadistica.logic.intf.model.DiaSetmanaEnum;
import es.caib.comanda.estadistica.logic.intf.model.Temps;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_temps")
@Getter
@Setter
@NoArgsConstructor
public class TempsEntity extends BaseEntity<Temps> {

    @Column(name = "data", nullable = false)
    private LocalDate data;
    @Column(name = "anualitat", nullable = false)
    private int anualitat;
    @Column(name = "trimestre", nullable = false)
    private int trimestre;
    @Column(name = "mes", nullable = false)
    private int mes;
    @Column(name = "dia", nullable = false)
    private int dia;
    @Column(name = "diaSetmana", length = 2, nullable = false)
    @Enumerated(EnumType.STRING)
    private DiaSetmanaEnum diaSetmana;
    @Column(name = "hora", nullable = false)
    private int hora;

}
