package es.caib.comanda.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Salut implements Serializable {

	private Long id;
	private Long entornAppId;
	private LocalDateTime data;
	private String versio;
	private String appEstat;
	private Integer appLatencia;
	private String bdEstat;
	private Integer bdLatencia;
	private boolean peticioError;

}
