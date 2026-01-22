package es.caib.comanda.usuaris.logic.intf.service;

import es.caib.comanda.ms.logic.intf.service.MutableResourceService;
import es.caib.comanda.usuaris.logic.intf.model.Usuari;

public interface UsuariService extends MutableResourceService<Usuari, Long> {
    void refreshCurrentUser();
}
