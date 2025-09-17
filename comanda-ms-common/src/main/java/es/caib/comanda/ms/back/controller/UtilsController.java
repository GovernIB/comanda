package es.caib.comanda.ms.back.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.back.config.WebSecurityConfig;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Controller amb les utilitats.
 * 
 * @author Límit Tecnologies
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UtilsController extends BaseUtilsController {

	private final MeterRegistry meterRegistry;

	@GetMapping(value = "/metrics", produces = "application/json")
	public ResponseEntity<String> metrics() throws JsonProcessingException {
		return ResponseEntity.ok(metriquesAsJson());
	}

	protected boolean isReactAppMappedFrontProperty(String propertyName) {
		return propertyName.startsWith(BaseConfig.PROPERTY_PREFIX_FRONT) && BaseConfig.REACT_APP_PROPS_MAP.containsKey(propertyName);
	}
	protected String getReactAppMappedFrontProperty(String propertyName) {
		return BaseConfig.REACT_APP_PROPS_MAP.get(propertyName);
	}
	protected boolean isViteMappedFrontProperty(String propertyName) {
		return propertyName.startsWith(BaseConfig.PROPERTY_PREFIX_FRONT) && BaseConfig.VITE_PROPS_MAP.containsKey(propertyName);
	}
	protected String getViteMappedFrontProperty(String propertyName) {
		return BaseConfig.VITE_PROPS_MAP.get(propertyName);
	}

	@Override
	protected String getAuthToken() {
		ServletRequestAttributes attrs = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			throw new IllegalStateException("No current request attributes found");
		}
		HttpServletRequest request = attrs.getRequest();
		Principal principal = request.getUserPrincipal();
		if (principal instanceof PreAuthenticatedAuthenticationToken) {
			PreAuthenticatedAuthenticationToken token = ((PreAuthenticatedAuthenticationToken) request.getUserPrincipal());
			if (token.getDetails() instanceof WebSecurityConfig.PreauthWebAuthenticationDetails) {
				WebSecurityConfig.PreauthWebAuthenticationDetails tokenDetails = (WebSecurityConfig.PreauthWebAuthenticationDetails) token.getDetails();
				return tokenDetails.getJwtToken();
			}
		}
		return null;
	}

	public String metriquesAsJson() throws JsonProcessingException {
		List<Map<String, Object>> metricsList = new ArrayList<>();
		for (Meter meter: meterRegistry.getMeters()) {
			Map<String, Object> metricMap = new LinkedHashMap<>();
			metricMap.put("name", meter.getId().getName());
			Map<String, String> tags = new HashMap<>();
			for (Tag tag: meter.getId().getTags()) {
				tags.put(tag.getKey(), tag.getValue());
			}
			metricMap.put("tags", tags);
			if (meter instanceof io.micrometer.core.instrument.Timer) {
				io.micrometer.core.instrument.Timer timer = (io.micrometer.core.instrument.Timer)meter;
				HistogramSnapshot snapshot = timer.takeSnapshot();
				Map<Double, Double> percentilesMs = new HashMap<>();
				for (ValueAtPercentile v: snapshot.percentileValues()) {
					percentilesMs.put(v.percentile(), v.value(TimeUnit.MILLISECONDS));
				}
				double p50 = percentilesMs.getOrDefault(0.5, Double.NaN);
				double p75 = percentilesMs.getOrDefault(0.75, Double.NaN);
				double p95 = percentilesMs.getOrDefault(0.95, Double.NaN);
				Map<String, Object> timerData = new LinkedHashMap<>();
				timerData.put("count", snapshot.count());
				timerData.put("duration_units", "milliseconds");
				timerData.put("max", snapshot.max(TimeUnit.MILLISECONDS));
				timerData.put("mean", snapshot.mean(TimeUnit.MILLISECONDS));
				timerData.put("p50", p50);
				timerData.put("p75", p75);
				timerData.put("p95", p95);
				timerData.put("rate_units", "calls/second");
				metricMap.put("type", "timer");
				metricMap.put("data", timerData);
			} else if (meter instanceof io.micrometer.core.instrument.Counter) {
				io.micrometer.core.instrument.Counter counter = (io.micrometer.core.instrument.Counter) meter;
				Map<String, Object> counterData = new LinkedHashMap<>();
				counterData.put("count", counter.count());
				metricMap.put("type", "counter");
				metricMap.put("data", counterData);
			} else {
				List<Map<String, Object>> measurements = new ArrayList<>();
				for (Measurement m: meter.measure()) {
					measurements.add(Map.of(
							"statistic", m.getStatistic().name(),
							"value", m.getValue()
					));
				}
				metricMap.put("type", meter.getId().getType().name().toLowerCase());
				metricMap.put("data", Map.of("measurements", measurements));
			}
			metricsList.add(metricMap);
		}
		metricsList.sort(Comparator.comparing(m -> {
			String suffix = null;
			Object tags = m.get("tags");
			if (tags instanceof Map) {
				suffix = (String)((Map<?, ?>)tags).get("phi");
			}
			return m.get("name") + (suffix != null ? suffix : "");
		}));
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(metricsList);
	}

}
