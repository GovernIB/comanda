package es.caib.comanda.api.helper;

import es.caib.comanda.client.AvisServiceClient;
import es.caib.comanda.client.TascaServiceClient;
import es.caib.comanda.client.model.Avis;
import es.caib.comanda.client.model.Tasca;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiClientHelper {

    private final TascaServiceClient tascaServiceClient;
    private final AvisServiceClient avisServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    public Optional<Tasca> getTasca(String identificador, String appCodi, String entornCodi) {
        PagedModel<EntityModel<Tasca>> tasques = tascaServiceClient.find(
                "",
                "app.codi:'" + appCodi + "' and entorn.codi:'" + entornCodi + "' and identificador:'" + identificador + "'",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return tasques.getContent().stream()
                .map(EntityModel::getContent)
                .findFirst();
    }

    public Boolean existTasca(String identificador, String appCodi, String entornCodi) {
        return getTasca(identificador, appCodi, entornCodi).isPresent();
    }

    public List<Tasca> getTasques(Set<String> identificadors, String appCodi, String entornCodi) {
        PagedModel<EntityModel<Tasca>> tasques = tascaServiceClient.find(
                "",
                "app.codi:'" + appCodi + "' and entorn.codi:'" + entornCodi + "' and identificador in [" + identificadors.stream().collect(Collectors.joining(", ")) + "]",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return tasques.getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toUnmodifiableList());
    }

    public PagedModel<EntityModel<Tasca>> getTasques(String quickFilter, String filter, String[] namedQueries, String[] perspectives, String page, Integer size) {
        return tascaServiceClient.find(
                quickFilter,
                filter,
                namedQueries,
                perspectives,
                page,
                size,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
//        return tasques.getContent().stream()
//                .map(EntityModel::getContent)
//                .collect(Collectors.toUnmodifiableList());
    }

    public Optional<Avis> getAvis(String identificador, String appCodi, String entornCodi) {
        PagedModel<EntityModel<Avis>> avisos = avisServiceClient.find(
                "",
                "app.codi:'" + appCodi + "' and entorn.codi:'" + entornCodi + "' and identificador:'" + identificador + "'",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return avisos.getContent().stream()
                .map(EntityModel::getContent)
                .findFirst();
    }

    public Boolean existAvis(String identificador, String appCodi, String entornCodi) {
        return getAvis(identificador, appCodi, entornCodi).isPresent();
    }

    public List<Avis> getAvisos(Set<String> identificadors, String appCodi, String entornCodi) {
        PagedModel<EntityModel<Avis>> avisos = avisServiceClient.find(
                "",
                "app.codi:'" + appCodi + "' and entorn.codi:'" + entornCodi + "' and identificador in [" + identificadors.stream().collect(Collectors.joining(", ")) + "]",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return avisos.getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toUnmodifiableList());
    }

    public PagedModel<EntityModel<Avis>> getAvisos(String quickFilter, String filter, String[] namedQueries, String[] perspectives, String page, Integer size) {
        return avisServiceClient.find(
                quickFilter,
                filter,
                namedQueries,
                perspectives,
                page,
                size,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
    }

}
