package es.caib.comanda.acl.logic.intf.dto;

import es.caib.comanda.acl.persist.enums.AclAction;
import es.caib.comanda.acl.persist.enums.ResourceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AclCheckRequest {

    private String user; // opcional si només s’usen rols

    private List<String> roles; // opcional

    @NotNull
    private ResourceType resourceType;

    @NotNull
    private Long resourceId;

    @NotNull
    private AclAction action;
}
