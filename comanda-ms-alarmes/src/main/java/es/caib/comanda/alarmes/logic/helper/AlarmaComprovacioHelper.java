package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfigTipus;
import es.caib.comanda.alarmes.logic.intf.model.AlarmaEstat;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import es.caib.comanda.alarmes.persist.repository.AlarmaRepository;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.UsuariServiceClient;
import es.caib.comanda.client.model.Salut;
import es.caib.comanda.client.model.Usuari;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Comprovacions i creació d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaComprovacioHelper {

	@Value("${" + BaseConfig.PROP_ALARMA_MAIL_ADMIN + ":false}")
	private boolean alarmaMailAdmin;
	@Value("${" + BaseConfig.PROP_ALARMA_MAIL_ADMIN_AGRUPAR + ":false}")
	private boolean alarmaMailAdminAgrupar;

	private final AlarmaRepository alarmaRepository;
	private final SalutServiceClient salutServiceClient;
	private final UsuariServiceClient usuariServiceClient;
	private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
	private final AlarmaMailHelper alarmaMailHelper;

	public boolean comprovar(AlarmaConfigEntity alarmaConfig) {
		if (alarmaConfig.getTipus() == AlarmaConfigTipus.APP_CAIGUDA) {
			return comprovarAppCaiguda(alarmaConfig);
		} else if (alarmaConfig.getTipus() == AlarmaConfigTipus.APP_LATENCIA) {
			return comprovarAppLatencia(alarmaConfig);
		} else {
			log.error("Tipus d'alarma no suportat: {}", alarmaConfig.getTipus());
			return false;
		}
	}

	private boolean comprovarAppCaiguda(AlarmaConfigEntity alarmaConfig) {
		Salut salut = findSalutLast(alarmaConfig.getEntornAppId());
		boolean alarma = false;
		if (salut != null) {
			alarma = salut.getAppEstat().equals("DOWN");
		}
		if (alarma) {
			crearAlarma(alarmaConfig);
			return true;
		} else {
			eliminarEsborranys(alarmaConfig);
			return false;
		}
	}

	private boolean comprovarAppLatencia(AlarmaConfigEntity alarmaConfig) {
		Salut salut = findSalutLast(alarmaConfig.getEntornAppId());
		boolean alarma = false;
		if (salut != null) {
			Integer latencia = salut.getAppLatencia();
			if (latencia != null && alarmaConfig.getCondicio() != null) {
				switch (alarmaConfig.getCondicio()) {
					case MAJOR:
						alarma = latencia > alarmaConfig.getValor().intValue();
						break;
					case MAJOR_IGUAL:
						alarma = latencia >= alarmaConfig.getValor().intValue();
						break;
					case MENOR:
						alarma = latencia < alarmaConfig.getValor().intValue();
						break;
					case MENOR_IGUAL:
						alarma = latencia <= alarmaConfig.getValor().intValue();
						break;
				}
			 }
		}
		if (alarma) {
			crearAlarma(alarmaConfig);
			return true;
		} else {
			eliminarEsborranys(alarmaConfig);
			return false;
		}
	}

	private void crearAlarma(AlarmaConfigEntity alarmaConfig) {
		AlarmaEntity alarmaActivada = null;
		if (alarmaConfig.getPeriodeValor() != null && alarmaConfig.getPeriodeUnitat() != null) {
			Optional<AlarmaEntity> alarmaEsborrany = alarmaRepository.findTopByAlarmaConfigAndEstatOrderByIdDesc(
					alarmaConfig,
					AlarmaEstat.ESBORRANY);
			if (alarmaEsborrany.isPresent()) {
				Duration duration = Duration.between(alarmaEsborrany.get().getCreatedDate(), LocalDateTime.now());
				boolean activar = false;
				switch (alarmaConfig.getPeriodeUnitat()) {
					case SEGONS:
						activar = duration.getSeconds() > alarmaConfig.getPeriodeValor().intValue();
						break;
					case MINUTS:
						activar = duration.getSeconds() / 60 > alarmaConfig.getPeriodeValor().intValue();
						break;
					case HORES:
						activar = duration.getSeconds() / 3600 > alarmaConfig.getPeriodeValor().intValue();
						break;
					case DIES:
						activar = duration.getSeconds() / 3600 * 24 > alarmaConfig.getPeriodeValor().intValue();
						break;
				}
				if (activar) {
					alarmaEsborrany.get().setEstat(AlarmaEstat.ACTIVA);
					alarmaEsborrany.get().setDataActivacio(LocalDateTime.now());
					alarmaActivada = alarmaEsborrany.get();
					log.debug("Alarma de tipus esborrany activada (configId={}, configNom={}, destinatari={}",
							alarmaConfig.getId(),
							alarmaConfig.getNom(),
							alarmaConfig.isAdmin() ? "[ADMIN]" : alarmaConfig.getCreatedBy());
				}
			} else {
				Alarma alarma = new Alarma();
				alarma.setEntornAppId(alarmaConfig.getEntornAppId());
				alarma.setEstat(AlarmaEstat.ESBORRANY);
				alarma.setMissatge(alarmaConfig.getMissatge());
				alarmaRepository.save(
						AlarmaEntity.builder().
								alarma(alarma).
								alarmaConfig(alarmaConfig).
								build());
				log.debug("Nova alarma de tipus esborrany creada (configId={}, configNom={}, destinatari={}",
						alarmaConfig.getId(),
						alarmaConfig.getNom(),
						alarmaConfig.isAdmin() ? "[ADMIN]" : alarmaConfig.getCreatedBy());
			}
		} else {
			Alarma alarma = new Alarma();
			alarma.setEntornAppId(alarmaConfig.getEntornAppId());
			alarma.setEstat(AlarmaEstat.ACTIVA);
			alarma.setMissatge(alarmaConfig.getMissatge());
			alarma.setDataActivacio(LocalDateTime.now());
			alarmaActivada = alarmaRepository.save(
					AlarmaEntity.builder().
							alarma(alarma).
							alarmaConfig(alarmaConfig).
							build());
			log.debug("Nova alarma activa creada (configId={}, configNom={}, destinatari={}",
					alarmaConfig.getId(),
					alarmaConfig.getNom(),
					alarmaConfig.isAdmin() ? "[ADMIN]" : alarmaConfig.getCreatedBy());
		}
		if (alarmaActivada != null) {
			enviarCorreuAlarma(alarmaActivada);
		}
	}

	private void eliminarEsborranys(AlarmaConfigEntity alarmaConfig) {
		alarmaRepository.deleteAll(
				alarmaRepository.findByAlarmaConfigAndEstat(
						alarmaConfig,
						AlarmaEstat.ESBORRANY));
	}

	private void enviarCorreuAlarma(AlarmaEntity alarma) {
		boolean enviarMail = false;
		if (alarma.getAlarmaConfig().isAdmin()) {
			enviarMail = alarmaMailAdmin && !alarmaMailAdminAgrupar;
		} else {
			Usuari usuari = usuariFindByUsername(alarma.getAlarmaConfig().getCreatedBy());
			if (usuari != null) {
				enviarMail = usuari.isAlarmaMail() && !usuari.isAlarmaMailAgrupar();
			}
		}
		if (enviarMail) {
			alarmaMailHelper.sendAlarma(alarma);
		}
	}

	private Salut findSalutLast(Long entornAppId) {
		PagedModel<EntityModel<Salut>> saluts = salutServiceClient.find(
				null,
				"entornAppId:" + entornAppId,
				null,
				null,
				"0",
				1,
				new String[] { "id,desc" },
				httpAuthorizationHeaderHelper.getAuthorizationHeader());
		if (saluts == null) return null;
		return saluts.getContent().stream().
				findFirst().
				map(EntityModel::getContent).
				orElse(null);
	}

	private Usuari usuariFindByUsername(String username) {
		PagedModel<EntityModel<Usuari>> usuaris = usuariServiceClient.find(
				null,
				"codi:'" + username + "'",
				null,
				null,
				"0",
				1,
				httpAuthorizationHeaderHelper.getAuthorizationHeader());
		if (usuaris == null) return null;
		return usuaris.getContent().stream().
				findFirst().
				map(EntityModel::getContent).
				orElse(null);
	}

}
