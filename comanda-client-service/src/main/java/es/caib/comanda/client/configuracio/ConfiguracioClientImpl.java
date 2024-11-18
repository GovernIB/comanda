package es.caib.comanda.client.configuracio;

import com.fasterxml.jackson.databind.JsonNode;
import es.caib.comanda.client.configuracio.model.ConfiguracioDto;
import es.caib.comanda.client.configuracio.model.ConfiguracioFiltre;
import es.caib.comanda.client.model.PagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfiguracioClientImpl implements ConfiguracioClient {

	private final String missatgeLog = "Cridant Configuracio Service - ConfiguracioConfiguracio - ";

	private final ConfiguracioFeignClient configuracioFeignClient;

	@Override
	public PagedList<ConfiguracioDto> listConfiguracionsV1(ConfiguracioFiltre configuracioFiltre) {
		
		log.debug(missatgeLog + " llista paginada de configuracions segons codi " + configuracioFiltre.getCodi()
				+ " i nom " + configuracioFiltre.getNom() + " amb filtre " + configuracioFiltre.getFiltre());
		var responseEntity = configuracioFeignClient.listConfiguracionsV1(configuracioFiltre);
		var resultat = Objects.requireNonNull(responseEntity.getBody());
		return resultat;
	}

	@Override
	public Long createConfiguracioV1(ConfiguracioDto configuracioDto) {

		log.debug(missatgeLog + " creant configuracio amb codi " + configuracioDto.getCodi() + " i nom " + configuracioDto.getNom());
		var responseEntity = configuracioFeignClient.createConfiguracioV1(configuracioDto);
		var resultat = Objects.requireNonNull(responseEntity.getBody());
		return resultat;
	}

	@Override
	public void updateConfiguracioV1(Long configuracioId, ConfiguracioDto configuracioDto) {
		
		log.debug(missatgeLog + " put configuracio amb id " + configuracioId + " i nom " + configuracioDto.getNom());;
		configuracioFeignClient.updateConfiguracioV1(configuracioId, configuracioDto);
	}

//	@Override
//	public void patchConfiguracioV1(Long configuracioId, ConfiguracioDto configuracioDto) {
//
//		log.debug(missatgeLog + " patch configuracio amb id " + configuracioId);
//
//		JsonPatchBuilder jpb = Json.createPatchBuilder();
//		if (configuracioDto.getNom() != null) PatchHelper.replaceStringProperty(jpb, "dataTipus", configuracioDto.getNom());
//		if (configuracioDto.getUrlApp() != null) PatchHelper.replaceStringProperty(jpb, "dataUrlApp", configuracioDto.getUrlApp());
//		if (configuracioDto.getUrlSalut() != null) PatchHelper.replaceStringProperty(jpb, "dataUrlSalut", configuracioDto.getUrlSalut());
//		if (configuracioDto.getUrlInfo() != null) PatchHelper.replaceStringProperty(jpb, "dataUrlInfo", configuracioDto.getUrlInfo());
//		if (configuracioDto.getColor() != null) PatchHelper.replaceStringProperty(jpb, "dataColor", configuracioDto.getColor());
//		if (configuracioDto.getVersioActual() != null) PatchHelper.replaceObjectProperty(jpb, "versioActual", configuracioDto.getVersioActual());
//		if (configuracioDto.getVersions() != null) {}
//		if (configuracioDto.getIntegracions() != null) {}
//		if (configuracioDto.getSubsistemes() != null) {}
//		configuracioFeignClient.patchConfiguracioV1(configuracioId, PatchHelper.toJsonNode(jpb));
//	}

	@Override
	public void patchConfiguracioV1(Long configuracioId, JsonNode configuracioJson) {

		log.debug(missatgeLog + " patch configuracio amb id " + configuracioId);
		configuracioFeignClient.patchConfiguracioV1(configuracioId, configuracioJson);
	}

	@Override
	public void deleteConfiguracioV1(Long configuracioId) {
		
		log.debug(missatgeLog + " delete configuracio amb id " + configuracioId);
		configuracioFeignClient.deleteConfiguracioV1(configuracioId);
	}

	@Override
	public ConfiguracioDto getConfiguracioV1(Long configuracioId) {
		
		log.debug(missatgeLog + " obtenir configuracio amb id " + configuracioId);
		var responseEntity = configuracioFeignClient.getConfiguracioV1(configuracioId);
		var resultat = Objects.requireNonNull(responseEntity.getBody());
		return resultat;
	}

}
