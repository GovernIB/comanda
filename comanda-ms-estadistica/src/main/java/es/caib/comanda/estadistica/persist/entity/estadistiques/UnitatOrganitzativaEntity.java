package es.caib.comanda.estadistica.persist.entity.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UOEstatEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UnitatOrganitzativa;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Locale;

@Entity
@Table(name = BaseConfig.DB_PREFIX + "unitat_org")
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UnitatOrganitzativaEntity extends BaseAuditableEntity<UnitatOrganitzativa> {

    @Column(name = "codi", unique = true, nullable = false)
    private String codi;

    @JsonIgnore
    @Column(name = "denominacio_es", nullable = false)
    private String denominacioEs;

    @JsonIgnore
    @Column(name = "denominacio_ca", nullable = false)
    private String denominacioCa;

    @Column(name = "nif_cif")
    private String nifCif;

    @Column(name = "codi_unitat_superior")
    private String codiUnitatSuperior;

    @Column(name = "codi_unitat_arrel")
    private String codiUnitatArrel;

    @Column(name = "estat")
    private UOEstatEnum estat;

    @Transient
    private String denominacio;

    public String getDenominacio() {
        Locale locale = LocaleContextHolder.getLocale();
        if ("es".equals(locale.getLanguage())) {
            return denominacioEs;
        }
        return denominacioCa;
    }
}
