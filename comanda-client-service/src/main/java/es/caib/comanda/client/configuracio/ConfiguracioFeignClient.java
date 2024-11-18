package es.caib.comanda.client.configuracio;

import com.fasterxml.jackson.databind.JsonNode;
import es.caib.comanda.client.configuracio.model.ConfiguracioDto;
import es.caib.comanda.client.configuracio.model.ConfiguracioFiltre;
import es.caib.comanda.client.model.PagedList;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

public interface ConfiguracioFeignClient {
	
	@RequestMapping(method = RequestMethod.GET, value = ConfiguracioApiPath.LIST_CONFIGURACIONS)
	public ResponseEntity<PagedList<ConfiguracioDto>> listConfiguracionsV1(
			@SpringQueryMap ConfiguracioFiltre configuracioFiltre);
	

	@RequestMapping(method = RequestMethod.GET, value = ConfiguracioApiPath.CREATE_CONFIGURACIO)
	public ResponseEntity<Long> createConfiguracioV1(@RequestBody ConfiguracioDto configuracioDto);

	@RequestMapping(method = RequestMethod.GET, value = ConfiguracioApiPath.GET_CONFIGURACIO)
	public ResponseEntity<ConfiguracioDto> getConfiguracioV1(@PathVariable("configuracioId") Long configuracioId);

	@RequestMapping(method = RequestMethod.PUT, value = ConfiguracioApiPath.UPDATE_CONFIGURACIO)
	public ResponseEntity<Void> updateConfiguracioV1(
            @PathVariable("configuracioId") Long configuracioId,
            @Valid @RequestBody ConfiguracioDto configuracioDto);

	@RequestMapping(method = RequestMethod.PATCH, value = ConfiguracioApiPath.PATCH_CONFIGURACIO)
	public ResponseEntity<Void> patchConfiguracioV1(
//          HttpServletRequest request,
          @PathVariable("configuracioId") Long configuracioId,
          @RequestBody JsonNode configuracioJson);

	@RequestMapping(method = RequestMethod.DELETE, value = ConfiguracioApiPath.DELETE_CONFIGURACIO)
	public ResponseEntity<Void> deleteConfiguracioV1(@PathVariable("configuracioId") Long configuracioId);

}
