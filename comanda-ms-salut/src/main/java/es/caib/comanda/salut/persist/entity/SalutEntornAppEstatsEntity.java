package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.salut.logic.intf.model.SalutEntornAppEstats;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entitat de base de dades que emmagatzema informació
 * de salut sobre un entorn aplicació.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "salut_entorn_app_estats")
@Getter
@Setter
@NoArgsConstructor
public class SalutEntornAppEstatsEntity extends BaseEntity<SalutEntornAppEstats> {

    @Column(name = "entorn_app_id", nullable = false, unique = true)
    private Long entornAppId;

    @Column(name = "darrera_activa")
    private LocalDateTime darrerActiu;
    @Column(name = "darrera_advertencia")
    private LocalDateTime darrerAdvertencia;
    @Column(name = "darrera_degradada")
    private LocalDateTime darrerDegradada;
    @Column(name = "darrera_error")
    private LocalDateTime darrerError;
    @Column(name = "darrera_caiguda")
    private LocalDateTime darrerCaiguda;
    @Column(name = "darrera_manteniment")
    private LocalDateTime darrerManteniment;
    @Column(name = "darrera_desconegut")
    private LocalDateTime darrerDesconegut;
    @Column(name = "darrera_peticio_error")
    private LocalDateTime darrerPeticioError;

}
