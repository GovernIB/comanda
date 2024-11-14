package es.caib.comanda.client.configuracio;

import com.fasterxml.jackson.databind.JsonNode;
import es.caib.comanda.client.configuracio.model.ConfiguracioDto;
import es.caib.comanda.client.configuracio.model.ConfiguracioFiltre;
import es.caib.comanda.client.model.PagedList;
import org.springframework.stereotype.Service;

@Service
public interface ConfiguracioClient {

	public PagedList<ConfiguracioDto> listConfiguracionsV1(ConfiguracioFiltre consultaConfiguracio);

	public Long createConfiguracioV1(ConfiguracioDto configuracioDto);

	public ConfiguracioDto getConfiguracioV1(Long configuracioId);

	public void updateConfiguracioV1(Long configuracioId, ConfiguracioDto configuracioDto);

	public void patchConfiguracioV1(Long configuracioId, JsonNode configuracioJson);

	public void deleteConfiguracioV1(Long configuracioId);


}
