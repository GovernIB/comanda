package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.ms.persist.entity.BaseAuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entitat de base de dades que representa una aplicació a monitoritzar.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "entorn_app",
		uniqueConstraints = {
				@UniqueConstraint(name = BaseConfig.DB_PREFIX + "entorn_app_entapp_uk", columnNames = { "entorn_id", "app_id" })
		})
@Getter
@Setter
@NoArgsConstructor
public class EntornAppEntity extends BaseAuditableEntity<EntornApp> {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "entorn_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "entorn_app_entorn_fk"))
	private EntornEntity entorn;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "app_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "entorn_app_app_fk"))
	private AppEntity app;

	// Informació de l'aplicació en l'entorn concret
	@Column(name = "info_url", length = 200, nullable = false)
	private String infoUrl;
	@Column(name = "info_data")
	private LocalDateTime infoData;
	@Column(name = "versio", length = 10)
	private String versio;
	@Column(name = "revisio", length = 64)
	private String revisio;
	@Column(name = "jdk_version", length = 10)
	private String jdkVersion;
	@Column(name = "activa", nullable = false)
	private boolean activa;

	// Informació de salut
	@Column(name = "salut_url", length = 200, nullable = false)
	private String salutUrl;

	@OneToMany(mappedBy= "entornApp", cascade = CascadeType.ALL)
	private Set<AppIntegracioEntity> appIntegracions;
	@OneToMany(mappedBy="entornApp", cascade = CascadeType.ALL)
	private Set<AppSubsistemaEntity> appSubsistemes;
	@OneToMany(mappedBy="entornApp", cascade = CascadeType.ALL)
	private Set<AppContextEntity> appContexts;

	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "app_integracio int where int.entorn_app_id = id)")
	private Integer integracioCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "app_subsistema sub where sub.entorn_app_id = id)")
	private Integer subsistemaCount;

	// Informació d'estadístiques
	@Column(name = "estadistica_info_url", length = 200)
	private String estadisticaInfoUrl;
	@Column(name = "estadistica_url", length = 200)
	private String estadisticaUrl;
	@Column(name = "estadistica_cron", length = 40)
	private String estadisticaCron;

	// Si les estadístiques requereixen autenticació bàsica
	@Column(name = "estadistica_auth", nullable = false)
	private boolean estadisticaAuth = false;

	// Si les peticions de salut requereixen autenticació bàsica
	@Column(name = "salut_auth", nullable = false)
	private boolean salutAuth = false;

    // Dades de compactació
    @Column(name = "compactable", nullable = false)
    private Boolean compactable = false;
    @Column(name = "compactacio_setmanal")
    private Integer compactacioSetmanalMesos;
    @Column(name = "compactacio_mensual")
    private Integer compactacioMensualMesos;
    @Column(name = "eliminacio")
    private Integer eliminacioMesos;

}
