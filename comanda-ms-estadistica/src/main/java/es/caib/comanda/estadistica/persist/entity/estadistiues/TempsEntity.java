package es.caib.comanda.estadistica.persist.entity.estadistiues;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DiaSetmanaEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.temporal.WeekFields;

/**
 * Classe d'entitat que representa la dimensió temporal en el sistema de persistència.
 *
 * Aquesta entitat defineix una lògica temporal basada en la data, especificant diverses propietats derivades com
 * l'anualitat, trimestre, mes, setmana, dia i el dia de la setmana. És útil per fer consultes i organitzar dades
 * temporalment dins del context d'anàlisi o estadístiques.
 *
 * Aquesta classe està anotada com a entitat JPA amb la taula específica prefixada segons la configuració
 * `BaseConfig.DB_PREFIX` i el nom de taula `est_temps`.
 *
 * Propietats principals:
 * - data: Data precisa en format LocalDate. Ha de ser obligatòria.
 * - anualitat: Any associat a la data.
 * - trimestre: Trimestre de l'any associat a la data (1-4).
 * - mes: El mes (1-12) associat a la data. Derivat de la propietat `data`.
 * - setmana: Número de la setmana a l'any segons `WeekFields.ISO`.
 * - dia: Número del dia dins del mes (1-31).
 * - diaSetmana: Valor enumerat que representa el dia de la setmana.
 *
 * Constructor especial:
 * Aquesta classe inclou un constructor que accepta un objecte LocalDate per calcular i inicialitzar
 * automàticament les propietats derivades com l'any, trimestre, mes, setmana, dia i el dia de la setmana.
 *
 * Ús principal:
 * Aquesta entitat s'utilitza principalment per emmagatzemar i consultar registres amb informació temporal,
 * proporcionant flexibilitat i granularitat en l'explotació de dades.
 *
 * Validacions:
 * Les columnes marcades amb `nullable = false` són obligatòries. L'ús d’aquestes validacions es tradueix en
 * garanties d'integritat a nivell de base de dades.
 *
 * Anotacions:
 * - `@Entity`: Marca aquesta classe com una entitat JPA.
 * - `@Table`: Defineix el nom de la taula corresponent a la base de dades.
 * - Anotacions de Lombok inclouen `@Getter`, `@Setter`, `@NoArgsConstructor` i `@AllArgsConstructor` per reduir
 * la generació de codi repetitiu com getters, setters i constructors.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_temps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TempsEntity extends BaseEntity<Temps> {

    @Column(name = "data", nullable = false)
    private LocalDate data;
    @Column(name = "anualitat", nullable = false)
    private int anualitat;
    @Column(name = "trimestre", nullable = false)
    private int trimestre;
    @Column(name = "mes", nullable = false)
    private int mes;
    @Column(name = "setmana", nullable = false)
    private int setmana;
    @Column(name = "dia", nullable = false)
    private int dia;
    @Column(name = "diaSetmana", length = 2, nullable = false)
    @Enumerated(EnumType.STRING)
    private DiaSetmanaEnum diaSetmana;

    public TempsEntity(LocalDate data) {
        this.data = data;
        this.anualitat = data.getYear();
        this.trimestre = data.getMonthValue() / 3;
        this.mes = data.getMonthValue();
        this.setmana = data.get(WeekFields.ISO.weekOfWeekBasedYear());
        this.diaSetmana = DiaSetmanaEnum.valueOfDayOfWeek(data.getDayOfWeek());
        this.dia = data.getDayOfMonth();
    }

}
