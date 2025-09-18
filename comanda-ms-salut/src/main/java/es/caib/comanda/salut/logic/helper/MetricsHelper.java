package es.caib.comanda.salut.logic.helper;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mètodes per a obtenir les instàncies de les mètriques internes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsHelper {

	private static final String SALUT_INFO_GLOBAL_LATENCIA = "salut_info_global_latencia_milliseconds";

	private static final String SALUT_LAST_GLOBAL_LATENCIA = "salut_last_global_latencia_milliseconds";
	private static final String SALUT_LAST_ENTORNAPPS_LATENCIA = "salut_last_entornapps_latencia_milliseconds";
	private static final String SALUT_LAST_DADES_LATENCIA = "salut_last_dades_latencia_milliseconds";

	private final MeterRegistry meterRegistry;

	public Timer getSalutInfoGlobalTimer(String entornNom, String appNom) {
		Timer.Builder builder = Timer.builder(SALUT_INFO_GLOBAL_LATENCIA).
				description("Temps transcorregut per a consultar la informació de salut");
		if (entornNom != null) {
			builder.tag("entorn", entornNom);
		}
		if (appNom != null) {
			builder.tag("app", appNom);
		}
		return builder.
				publishPercentiles(0.5, 0.75, 0.95).
				register(meterRegistry);
	}

	public Timer getSalutLastGlobalTimer() {
		return Timer.builder(SALUT_LAST_GLOBAL_LATENCIA).
				description("Temps transcorregut per a la consulta de les darreres dades de salut").
				publishPercentiles(0.5, 0.75, 0.95).
				register(meterRegistry);
	}
	public Timer getSalutLastEntornAppsTimer() {
		return Timer.builder(SALUT_LAST_ENTORNAPPS_LATENCIA).
				description("Temps transcorregut per a la consulta dels entorn-apps per a les darreres dades de salut").
				publishPercentiles(0.5, 0.75, 0.95).
				register(meterRegistry);
	}
	public Timer getSalutLastDadesTimer() {
		return Timer.builder(SALUT_LAST_DADES_LATENCIA).
				description("Temps transcorregut per a la consulta de les dades per a les darreres dades de salut").
				publishPercentiles(0.5, 0.75, 0.95).
				register(meterRegistry);
	}

}
