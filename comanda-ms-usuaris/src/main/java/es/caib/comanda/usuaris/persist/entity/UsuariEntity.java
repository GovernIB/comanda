package es.caib.comanda.usuaris.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import es.caib.comanda.usuaris.logic.intf.model.LanguageEnum;
import es.caib.comanda.usuaris.logic.intf.model.NumOfElementsPerPageENum;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = BaseConfig.DB_PREFIX + "usuari",
        uniqueConstraints = {
                @UniqueConstraint(name = BaseConfig.DB_PREFIX + "usuari_codi_uk", columnNames = { "codi" })
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UsuariEntity extends BaseAuditableEntity<Usuari> {

    @NaturalId
    @Column(name = "codi", length = 64, nullable = false)
    @Comment("Código del usuario")
    private String codi;
    @Column(name = "nom", length = 255, nullable = false)
    @Comment("Nombre y apellidos del usuario")
    private String nom;
    @Column(name = "nif", length = 10)
    @Comment("Documento identificativo del usuario")
    private String nif;
    @Column(name = "email", length = 255)
    @Comment("Dirección de correo electrónico del usuario")
    private String email;

    @Column(name = "email_alternatiu", length = 200)
    @Comment("Dirección de correo electrónico alternativo del usuario")
    private String emailAlternatiu;
    @Enumerated(EnumType.STRING)
    @Column(name="idioma", length = 2, nullable = false, columnDefinition = "VARCHAR(2) DEFAULT 'CA'")
    @Comment("Idioma del usuario")
    private LanguageEnum idioma;
    @Column(name="tema_obscur")
    private Boolean temaObscur;

    @Enumerated(EnumType.STRING)
    @Column(name = "num_elements_pagina", length = 12, nullable = false, columnDefinition = "VARCHAR(12) DEFAULT 'AUTOMATIC'")
    @Comment("Número de elementos por página")
    private NumOfElementsPerPageENum numElementsPagina;

	@Column(name="alarma_mail", nullable = false)
	private boolean alarmaMail;
	@Column(name="alarma_mail_agrupar", nullable = false)
	private boolean alarmaMailAgrupar;

}
