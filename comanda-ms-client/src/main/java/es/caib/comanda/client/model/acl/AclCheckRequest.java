package es.caib.comanda.client.model.acl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AclCheckRequest {

    public enum Mode { ANY, ALL }

    private String user; // opcional si només s’usen rols

    private List<String> roles; // opcional

    @NotNull
    private ResourceType resourceType;

    @NotNull
    private Long resourceId;

    // Per compatibilitat cap enrere: si s'informa 'action' s'ignora 'actions'
    private AclAction action; // opcional quan s'usen múltiples

    // Nova funcionalitat: comprovar múltiples accions
    private List<AclAction> actions; // opcional

    // Comportament de verificació quan s'indiquen múltiples permisos
    // Per defecte: ANY
    private Mode mode = Mode.ANY;
}
