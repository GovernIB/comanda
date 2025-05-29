package es.caib.comanda.estadistica.persist.entity.estadistiques;

import es.caib.comanda.estadistica.logic.config.DoubleJsonMapConverter;
import es.caib.comanda.estadistica.logic.config.StringJsonMapConverter;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Map;

/**
 * Classe que representa una entitat de fet dins el sistema d'estadístiques.
 * Aquesta entitat emmagatzema informació detallada relacionada amb un fet específic.
 * Inclou associacions amb altres entitats i dades addicionals estructurades en formats JSON.
 *
 * Les dades emmagatzemades inclouen:
 *
 * - Una associació amb l'entitat TempsEntity, que permet identificar i relacionar amb un període de temps determinat.
 * - Informació en format JSON sobre dimensions específiques (Map<String, String>).
 * - Informació en format JSON sobre indicadors específics (Map<String, Double>).
 * - L'identificador de l'entorn d'aplicació on s'està realitzant el registre (entornAppId).
 *
 * Aquesta classe extén la funcionalitat de BaseEntity amb el recurs associat de tipus Fet.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "est_fet")
//@TypeDef(name = "json", typeClass = JsonType.class)
@Getter
@Setter
@NoArgsConstructor
public class FetEntity extends BaseEntity<Fet> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "temps_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "fet_temps_fk"),
            nullable = false)
    private TempsEntity temps;

    @Convert(converter = StringJsonMapConverter.class)
    @Column(name = "dimensions_json", length = 4000)
    private Map<String, String> dimensionsJson;

    @Convert(converter = DoubleJsonMapConverter.class)
    @Column(name = "indicadors_json", length = 4000)
    private Map<String, Double> indicadorsJson;

    @Column(name = "entorn_app_id", nullable = false)
    private Long entornAppId;
}
