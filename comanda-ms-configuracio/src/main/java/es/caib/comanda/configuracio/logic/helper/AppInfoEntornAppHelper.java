package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppHistEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppHistRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppInfoEntornAppHelper {
	private final EntornAppRepository entornAppRepository;
    private final EntornAppHistRepository entornAppHistRepository;

	/**
	 * Actualitza la informació de l'aplicació associada a un entorn concret.
	 * <p>
	 * Aquesta actualització es realitza mitjançant una crida HTTP al servei monitoritzat
	 * de l'aplicació, obtenint la seva versió, data de desplegament i integracions/subsistemes.
	 * La informació obtinguda es desa a la base de dades per mantenir actualitzat l'estat
	 * de les aplicacions en cada entorn.
	 * </p>
	 * <p>
	 * En cas d'error en la comunicació, es registra un avís i es continua l'execució sense interrompre el procés global.
	 * </p>
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void storeAppInfo(AppInfo appInfo, Long entornAppId) {
		EntornAppEntity entornAppEntity = entornAppRepository.findById(entornAppId)
				.orElseThrow(() -> new ResourceNotFoundException(EntornApp.class, entornAppId.toString()));
		if (appInfo != null) {
            logicaCanviVersioRevisio(entornAppEntity, entornAppEntity.getVersio(), entornAppEntity.getRevisio(), appInfo.getVersio(), appInfo.getRevisio());
            entornAppEntity.setVersio(appInfo.getVersio());
			entornAppEntity.setInfoData(appInfo.getData().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
			entornAppEntity.setRevisio(appInfo.getRevisio());
			entornAppEntity.setJdkVersion(appInfo.getJdkVersion());
		}
	}

    /**
     * Detecta canvis en la versió o revisió i registra un històric si es necessari.
     * @param entornAppEntity Entitat pare a la que es relaciona l'historic
     * @param versioAntiga Valor de versió actual a la base de dades (abans de l'actualització)
     * @param revisioAntiga Valor de revisió actual a la base de dades (abans de l'actualització)
     * @param versioNova Nou valor de versió rebut de l'API externa
     * @param revisioNova Nou valor de revisió rebut de l'API externa
     */
    private void logicaCanviVersioRevisio(EntornAppEntity entornAppEntity, String versioAntiga, String revisioAntiga, String versioNova, String revisioNova) {
        boolean haCanviatVersio = !Objects.equals(versioAntiga, versioNova);
        boolean haCanviatRevisio = !Objects.equals(revisioAntiga, revisioNova);
        if (haCanviatVersio || haCanviatRevisio) {
            log.debug("Canvi detectat per entornAppId={}: versio({}→{}) revisio({}→{})",
                    entornAppEntity.getId(), versioAntiga, versioNova, revisioAntiga, revisioNova);
            registrarHistoric(entornAppEntity, versioNova, revisioNova, haCanviatVersio);
        }
    }

    /**
     * Crea i guarda un registre a la taula d'històric.
     * @param entornApp Entitat pare a la que es relaciona l'historic
     * @param versio Valor de versió a guardar (el nou)
     * @param revisio Valor de revisió a guardar (el nou)
     * @param canviVersio true si el canvi que ha disparat l'historic és de versió
     */
    private void registrarHistoric(EntornAppEntity entornApp, String versio, String revisio, boolean canviVersio) {
        EntornAppHistEntity historic = new EntornAppHistEntity();
        historic.setEntornApp(entornApp);
        historic.setVersio(versio);
        historic.setRevisio(revisio);
        historic.setCanviVersio(canviVersio);
        historic.setData(LocalDateTime.now());

        entornAppHistRepository.save(historic);
    }

}
