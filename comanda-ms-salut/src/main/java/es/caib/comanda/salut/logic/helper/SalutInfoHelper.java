package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.ms.salut.model.DetallSalut;
import es.caib.comanda.ms.salut.model.EstatSalutEnum;
import es.caib.comanda.ms.salut.model.IntegracioSalut;
import es.caib.comanda.ms.salut.model.MissatgeSalut;
import es.caib.comanda.ms.salut.model.SalutInfo;
import es.caib.comanda.ms.salut.model.SubsistemaSalut;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutNivell;
import es.caib.comanda.salut.persist.entity.SalutDetallEntity;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutIntegracioEntity;
import es.caib.comanda.salut.persist.entity.SalutMissatgeEntity;
import es.caib.comanda.salut.persist.entity.SalutSubsistemaEntity;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Lògica comuna per a consultar la informació de salut de les apps.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SalutInfoHelper {

	private final SalutRepository salutRepository;
	private final SalutIntegracioRepository salutIntegracioRepository;
	private final SalutSubsistemaRepository salutSubsistemaRepository;
	private final SalutMissatgeRepository salutMissatgeRepository;
	private final SalutDetallRepository salutDetallRepository;

	private final KeycloakHelper keycloakHelper;
	private final MonitorServiceClient monitorServiceClient;

	@Transactional
	public void getSalutInfo(EntornApp entornApp) {
		log.debug("Obtenint dades de salut de l'app {}, entorn {}",
				entornApp.getApp().getNom(),
				entornApp.getEntorn().getNom());
		RestTemplate restTemplate = new RestTemplate();
		MonitorSalut monitorSalut = new MonitorSalut(
				entornApp.getId(),
				entornApp.getSalutUrl(),
				monitorServiceClient,
				keycloakHelper.getAuthorizationHeader());

		try {
			// Obtenir dades de salut de l'aplicació
			monitorSalut.startAction();
			SalutInfo salutInfo = restTemplate.getForObject(entornApp.getSalutUrl(), SalutInfo.class);
			monitorSalut.endAction();
			// Guardar les dades de salut a la base de dades
			crearSalut(salutInfo, entornApp.getId());
		} catch (RestClientException ex) {
			SalutEntity salut = new SalutEntity();
			salut.setEntornAppId(entornApp.getId());
			salut.setData(LocalDateTime.now());
			salut.setAppEstat(SalutEstat.UNKNOWN);
			salutRepository.save(salut);
			log.warn("No s'han pogut obtenir dades de salut de l'app {}, entorn {}: {}",
					entornApp.getApp().getNom(),
					entornApp.getEntorn().getNom(),
					ex.getLocalizedMessage());
			if (!monitorSalut.isFinishedAction()) {
				monitorSalut.endAction(ex);
			}
		}
	}

	private void crearSalut(SalutInfo info, Long entornAppId) {
		if (info != null) {
			SalutEntity salut = new SalutEntity();
			salut.setEntornAppId(entornAppId);
			salut.setData(toLocalDateTime(info.getData()));
			salut.setAppEstat(toSalutEstat(info.getEstat().getEstat()));
			salut.setAppLatencia(info.getEstat().getLatencia());
			salut.setBdEstat(toSalutEstat(info.getBd().getEstat()));
			salut.setBdLatencia(info.getBd().getLatencia());
			SalutEntity saved = salutRepository.save(salut);
			crearSalutIntegracions(saved, info.getIntegracions());
			crearSalutSubsistemes(saved, info.getSubsistemes());
			crearSalutMissatges(saved, info.getMissatges());
			crearSalutDetalls(saved, info.getAltres());
		}
	}

	private void crearSalutIntegracions(SalutEntity salut, List<IntegracioSalut> integracions) {
		if (integracions != null) {
			integracions.forEach(i -> {
				SalutIntegracioEntity salutIntegracio = new SalutIntegracioEntity();
				salutIntegracio.setCodi(i.getCodi());
				salutIntegracio.setEstat(toSalutEstat(i.getEstat()));
				salutIntegracio.setLatencia(i.getLatencia());
				salutIntegracio.setTotalOk(i.getPeticions() != null ? i.getPeticions().getTotalOk() : 0L);
				salutIntegracio.setTotalError(i.getPeticions() != null ? i.getPeticions().getTotalError() : 0L);
				salutIntegracio.setSalut(salut);
				salutIntegracioRepository.save(salutIntegracio);
			});
		}
	}

	private void crearSalutSubsistemes(SalutEntity salut, List<SubsistemaSalut> subsistemes) {
		if (subsistemes != null) {
			subsistemes.forEach(s -> {
				SalutSubsistemaEntity salutSubsistema = new SalutSubsistemaEntity();
				salutSubsistema.setCodi(s.getCodi());
				salutSubsistema.setEstat(toSalutEstat(s.getEstat()));
				salutSubsistema.setLatencia(s.getLatencia());
				salutSubsistema.setSalut(salut);
				salutSubsistemaRepository.save(salutSubsistema);
			});
		}
	}

	private void crearSalutMissatges(SalutEntity salut, List<MissatgeSalut> missatges) {
		if (missatges != null) {
			missatges.forEach(m -> {
				SalutMissatgeEntity salutMissatge = new SalutMissatgeEntity();
				salutMissatge.setData(toLocalDateTime(m.getData()));
				salutMissatge.setNivell(toSalutNivell(m.getNivell()));
				salutMissatge.setMissatge(m.getMissatge());
				salutMissatge.setSalut(salut);
				salutMissatgeRepository.save(salutMissatge);
			});
		}
	}

	private void crearSalutDetalls(SalutEntity salut, List<DetallSalut> missatges) {
		if (missatges != null) {
			missatges.forEach(d -> {
				SalutDetallEntity salutDetall = new SalutDetallEntity();
				salutDetall.setCodi(d.getCodi());
				salutDetall.setNom(d.getNom());
				salutDetall.setValor(d.getValor());
				salutDetall.setSalut(salut);
				salutDetallRepository.save(salutDetall);
			});
		}
	}

	private SalutEstat toSalutEstat(EstatSalutEnum estatSalut) {
		return estatSalut != null ? SalutEstat.valueOf(estatSalut.name()) : null;
	}

	private SalutNivell toSalutNivell(String nivell) {
		if ("error".equalsIgnoreCase(nivell)) {
			return SalutNivell.ERROR;
		} else if ("avis".equalsIgnoreCase(nivell) || "warn".equalsIgnoreCase(nivell)) {
			return SalutNivell.WARN;
		} else if ("info".equalsIgnoreCase(nivell)) {
			return SalutNivell.INFO;
		} else {
			return null;
		}
	}

	public LocalDateTime toLocalDateTime(Date dateToConvert) {
		return dateToConvert != null ? LocalDateTime.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault()) : null;
	}

}
