package es.caib.comanda.estadistica.logic.helper;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringFilterHelper {
    private final EntornAppServiceClient entornAppServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    public Filter filterByApp(long appId, String entornAppIdField){
        PagedModel<EntityModel<EntornApp>> entornApps = entornAppServiceClient.find(
                null,
                "app.id:" + appId,
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        List<Filter> idFilters = entornApps.getContent().stream()
                .map(EntityModel::getContent)
                .filter(Objects::nonNull)
                .map(EntornApp::getId)
                .map(id -> FilterBuilder.equal(entornAppIdField, id))
                .collect(Collectors.toList());
        return FilterBuilder.or(idFilters);
    }

    public static String buildOrFilter(String fieldName, Set<Serializable> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
            .sorted(Comparator.comparingLong(id -> Long.parseLong(String.valueOf(id))))
            .map(String::valueOf)
            .map(id -> fieldName + ":" + id)
            .collect(Collectors.joining(" or "));
    }

    public static String and(Object... options) {
        return Arrays.stream(options)
            .filter(obj -> obj != null && !obj.toString().isBlank())
            .map(Object::toString)
            .collect(Collectors.joining(" and "));
    }

    public static String or(Object... options) {
        return Arrays.stream(options)
            .filter(obj -> obj != null && !obj.toString().isBlank())
            .map(Object::toString)
            .collect(Collectors.joining(" or "));
    }
}
