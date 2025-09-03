package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.model.AppContext;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Informació de salut retornada per una app.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
		accessConstraints = {
				@ResourceAccessConstraint(
						type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
						roles = { BaseConfig.ROLE_ADMIN },
						grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.DELETE }
				),
				@ResourceAccessConstraint(
						type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
						roles = { BaseConfig.ROLE_CONSULTA },
						grantedPermissions = { PermissionEnum.READ }
				)
		},
		artifacts = {
				@ResourceArtifact(type = ResourceArtifactType.REPORT, code = Salut.SALUT_REPORT_LAST, formClass = String.class),
				@ResourceArtifact(type = ResourceArtifactType.REPORT, code = Salut.SALUT_REPORT_ESTAT, formClass = SalutInformeParams.class),
				@ResourceArtifact(type = ResourceArtifactType.REPORT, code = Salut.SALUT_REPORT_ESTATS, formClass = SalutInformeParams.class),
				@ResourceArtifact(type = ResourceArtifactType.REPORT, code = Salut.SALUT_REPORT_LATENCIA, formClass = SalutInformeParams.class),
				@ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = Salut.PERSP_INTEGRACIONS),
				@ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = Salut.PERSP_SUBSISTEMES),
				@ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = Salut.PERSP_CONTEXTS),
				@ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = Salut.PERSP_MISSATGES),
				@ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = Salut.PERSP_DETALLS),
		}
)
public class Salut extends BaseResource<Long> {

	public final static String SALUT_REPORT_LAST = "salut_last";
	public final static String SALUT_REPORT_ESTAT = "estat";
	public final static String SALUT_REPORT_ESTATS = "estats";
	public final static String SALUT_REPORT_LATENCIA = "latencia";
	public static final String PERSP_INTEGRACIONS = "SAL_INTEGRACIONS";
	public static final String PERSP_SUBSISTEMES = "SAL_SUBSISTEMES";
	public static final String PERSP_CONTEXTS = "SAL_CONTEXTS";
	public static final String PERSP_MISSATGES = "SAL_MISSATGES";
	public static final String PERSP_DETALLS = "SAL_DETALLS";

	@NotNull
	private Long entornAppId;
	@NotNull
	private LocalDateTime data;
	@NotNull
	@Size(max = 16)
	private String versio;
	@NotNull
	private SalutEstat appEstat;
	private Integer appLatencia;
	@NotNull
	private SalutEstat bdEstat;
	private Integer bdLatencia;

	private Boolean appUp;
	private Boolean bdUp;

	private Integer integracioUpCount;
	private Integer integracioDownCount;
    private Integer integracioDesconegutCount;
	private Integer subsistemaUpCount;
	private Integer subsistemaDownCount;
	private Integer missatgeErrorCount;
	private Integer missatgeWarnCount;
	private Integer missatgeInfoCount;

	private String year;
	private String yearMonth;
	private String yearMonthDay;
	private String yearMonthDayHour;

	private List<SalutIntegracio> integracions;
	private List<SalutSubsistema> subsistemes;
	private List<AppContext> contexts;
	private List<SalutMissatge> missatges;
	private List<SalutDetall> detalls;

}
