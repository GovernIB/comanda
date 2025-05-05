package es.caib.comanda.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EntornRef {

	private Long id;
	@JsonProperty("description")
	private String nom;

}
