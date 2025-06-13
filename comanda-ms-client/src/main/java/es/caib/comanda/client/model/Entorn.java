package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class Entorn implements Serializable {

	private Long id;
	private String codi;
	private String nom;

}
