package es.caib.comanda.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppRef implements Serializable {

	private Long id;
	@JsonProperty("description")
	private String nom;

}
