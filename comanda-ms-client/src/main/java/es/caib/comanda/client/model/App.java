package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
public class App implements Serializable {

	private Long id;
	private String codi;
	private String nom;
	private String descripcio;
	private boolean activa = true;

	private List<EntornApp> entornApps;

}
