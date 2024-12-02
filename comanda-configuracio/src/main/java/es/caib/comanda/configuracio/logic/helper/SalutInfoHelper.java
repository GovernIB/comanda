package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.logic.intf.model.SalutEstat;
import es.caib.comanda.configuracio.logic.intf.model.SalutNivell;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.SalutEntity;
import es.caib.comanda.configuracio.persist.entity.SalutIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.SalutMissatgeEntity;
import es.caib.comanda.configuracio.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.configuracio.persist.repository.SalutRepository;
import es.caib.comanda.salut.model.EstatSalutEnum;
import es.caib.comanda.salut.model.IntegracioSalut;
import es.caib.comanda.salut.model.MissatgeSalut;
import es.caib.comanda.salut.model.SalutInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
	private SalutMissatgeRepository salutMissatgeRepository;

	public void getSalutInfo(AppEntity app) {
		log.debug("Consultant informació de salut de l'app {}", app.getCodi());
		RestTemplate restTemplate = new RestTemplate();
		SalutInfo salutInfo = restTemplate.getForObject(app.getSalutUrl(), SalutInfo.class);
		crearSalut(salutInfo);
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
			crearSalutMissatges(saved, info.getMissatges());
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
