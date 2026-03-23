package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.model.v1.estadistica.EstadistiquesInfo;
import es.caib.comanda.model.v1.estadistica.RegistresEstadistics;
import es.caib.comanda.model.v1.log.FitxerInfo;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.SalutInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

/** Enumerat dels tipus de url de {@link EntornApp}, relacionant el tipus amb l'objecte que tornara la URL.
 *  Sempre que es modifiqui la resposta esperada haurem de modificar aquest metode. **/
@Getter
@AllArgsConstructor
public enum ExpectedResponseTypeEnum {
    INFO(AppInfo.class, null),
    LOGS(List.class, new ParameterizedTypeReference<List<FitxerInfo>>() {}),
    SALUT(SalutInfo.class, null),
    ESTADISTICA_INFO(EstadistiquesInfo.class, null),
    ESTADISTICA(RegistresEstadistics.class, null),
    BASIC_PING(Void.class, null);

    private final Class<?> rawType;
    private final ParameterizedTypeReference<?> genericType;

    public boolean requiresBodyValidation() {
        return this != BASIC_PING;
    }

}
