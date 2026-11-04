package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.AppGrup;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

/**
 * Entitat de base de dades que representa un grup d'aplicacions.
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "app_grup",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "app_grup_nom_uk", columnNames = { "nom" })
		})
@Getter
@Setter
@NoArgsConstructor
public class AppGrupEntity extends BaseAuditableEntity<AppGrup> {

	@Column(name = "nom", length = 100, nullable = false)
	private String nom;

	@Column(name = "color_clar", length = 16)
	private String colorClar;

	@Column(name = "color_fosc", length = 16)
	private String colorFosc;

	@Column(name = "ordre")
	private Long ordre;

	@OneToMany(mappedBy = "grup")
	private List<AppEntity> apps;

}
