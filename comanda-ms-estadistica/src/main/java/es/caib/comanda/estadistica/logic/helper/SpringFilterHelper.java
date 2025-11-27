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

import java.util.List;
import java.util.Objects;
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
}
