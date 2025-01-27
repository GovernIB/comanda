package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.ms.salut.model.*;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutNivell;
import es.caib.comanda.salut.persist.entity.*;
import es.caib.comanda.salut.persist.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
@Component
public class SalutInfoHelper {

	@Autowired
	private SalutRepository salutRepository;
	@Autowired
	private SalutIntegracioRepository salutIntegracioRepository;
	@Autowired
	private SalutSubsistemaRepository salutSubsistemaRepository;
	@Autowired
	private SalutMissatgeRepository salutMissatgeRepository;
	@Autowired
	private SalutDetallRepository salutDetallRepository;

	public void getSalutInfo(String appCodi, String salutUrl) {
		log.debug("Consultant informació de salut de l'app {}", appCodi);
		RestTemplate restTemplate = new RestTemplate();
		try {
			SalutInfo salutInfo = restTemplate.getForObject(salutUrl, SalutInfo.class);
			crearSalut(salutInfo);
		} catch (RestClientException ex) {
			SalutEntity salut = new SalutEntity();
			salut.setCodi(appCodi);
			salut.setData(LocalDateTime.now());
			salut.setAppEstat(SalutEstat.UNKNOWN);
			salutRepository.save(salut);
			log.warn("No s'ha pogut obtenir informació de salut de l'app {}: {}",
					appCodi,
					ex.getLocalizedMessage());
		}
	}

	private void crearSalut(SalutInfo info) {
		if (info != null) {
			SalutEntity salut = new SalutEntity();
			salut.setCodi(info.getCodi());
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
				salutIntegracio.setTotalOk(i.getPeticions().getTotalOk());
				salutIntegracio.setTotalError(i.getPeticions().getTotalError());
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
