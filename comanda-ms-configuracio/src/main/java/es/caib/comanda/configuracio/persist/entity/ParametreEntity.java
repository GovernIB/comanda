package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.client.model.ParamTipus;
import es.caib.comanda.configuracio.logic.intf.model.Parametre;
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
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "parametre",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "parametre_codi_uk", columnNames = { "codi" })
        })
@Getter
@Setter
@NoArgsConstructor
public class ParametreEntity extends BaseEntity<Parametre> {

    @Column(name = "grup", length = 128, nullable = false)
    private String grup;
    @Column(name = "subgrup", length = 128, nullable = false)
    private String subGrup;
    @Column(name = "tipus", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private ParamTipus tipus;
    @Column(name = "codi", length = 128, nullable = false)
    private String codi;
    @Column(name = "nom", length = 128, nullable = false)
    private String nom;
    @Column(name = "descripcio", length = 1024)
    private String descripcio;
    @Column(name = "valor", length = 255)
    private String valor;
    @Column(name = "editable")
    private boolean editable;

    // Camps per multiidioma
    @Column(name = "grup_key", length = 128)
    private String grupI18Key;
    @Column(name = "subgrup_key", length = 128)
    private String subGrupI18Key;
    @Column(name = "nom_key", length = 128)
    private String nomI18Key;
    @Column(name = "descripcio_key", length = 128)
    private String descripcioI18Key;
}
