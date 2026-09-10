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

    public static String buildOrFilter(String fieldName, Collection<? extends Serializable> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
            .sorted(Comparator.comparingLong(id -> Long.parseLong(String.valueOf(id))))
            .map(String::valueOf)
            .distinct()
            .map(id -> fieldName + ":" + id)
            .collect(Collectors.joining(" or "));
    }

    public static String and(Object... options) {
        return Arrays.stream(options)
            .filter(obj -> obj != null && !obj.toString().isBlank())
            .map(Object::toString)
            .map(SpringFilterHelper::wrapIfTopLevelOr)
            .collect(Collectors.joining(" and "));
    }

    public static String or(Object... options) {
        return Arrays.stream(options)
            .filter(obj -> obj != null && !obj.toString().isBlank())
            .map(Object::toString)
            .collect(Collectors.joining(" or "));
    }

    /**
     * Embolcalla una expressió entre parèntesis només si conté operadors OR de nivell superior (profunditat 0)
     * no protegits per parèntesis externs, evitant així problemes de precedència en combinar-la amb AND.
     */
    public static String wrapIfTopLevelOr(String s) {
        if (s == null || s.isBlank()) {
            return s;
        }
        String trimmed = s.trim();
        return hasTopLevelOr(trimmed) ? "(" + trimmed + ")" : trimmed;
    }

    /**
     * Comprova si una expressió conté l'operador lògic OR a nivell superior (fora de parèntesis i de cometes).
     */
    public static boolean hasTopLevelOr(String s) {
        if (s == null) {
            return false;
        }
        String lower = s.toLowerCase();
        if (!lower.contains("or")) {
            return false;
        }
        int depth = 0;
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (inSingleQuotes) {
                if (c == '\'') {
                    inSingleQuotes = false;
                }
            } else if (inDoubleQuotes) {
                if (c == '"') {
                    inDoubleQuotes = false;
                }
            } else if (c == '\'') {
                inSingleQuotes = true;
            } else if (c == '"') {
                inDoubleQuotes = true;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth = Math.max(0, depth - 1);
            } else if (depth == 0) {
                if ((i == 0 || Character.isWhitespace(lower.charAt(i - 1)))
                        && lower.startsWith("or", i)
                        && (i + 2 == len || Character.isWhitespace(lower.charAt(i + 2)))) {
                    return true;
                }
            }
        }
        return false;
    }
}
