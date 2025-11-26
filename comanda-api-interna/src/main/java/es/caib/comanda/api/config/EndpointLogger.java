package es.caib.comanda.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EndpointLogger implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(EndpointLogger.class);

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;
    @Autowired
    private Environment env;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("======== ENDPOINTS PUBLICATS ========");

        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String baseUrl = "http://localhost:" + port + contextPath;

        log.info("baseUrl: {}", baseUrl);

        handlerMapping.getHandlerMethods().forEach((mappingInfo, handlerMethod) -> {

            // 1) Mètodes HTTP
            String methods = mappingInfo.getMethodsCondition()
                    .getMethods()
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));

            if (methods.isEmpty()) {
                methods = "[ALL]";
            }

            // 2) Paths: suportar Ant + PathPattern
            List<String> paths = new ArrayList<>();

            if (mappingInfo.getPatternsCondition() != null) {
                // AntPathMatcher (clàssic)
                paths.addAll(mappingInfo.getPatternsCondition()
                        .getPatterns()
                        .stream()
                        .collect(Collectors.toList()));
            } else if (mappingInfo.getPathPatternsCondition() != null) {
                // PathPatternParser (nou)
                mappingInfo.getPathPatternsCondition()
                        .getPatterns()
                        .forEach(p -> paths.add(p.getPatternString()));
            } else {
                // per si de cas
                paths.add("[NO PATH]");
            }

            String pathStr = String.join(", ", paths);

            log.info("{} {}  →  {}#{}",
                    methods,
                    pathStr,
                    handlerMethod.getBeanType().getSimpleName(),
                    handlerMethod.getMethod().getName()
            );
        });

        log.info("=====================================");
    }

}
