package es.caib.comanda.client.model.acl;

import es.caib.comanda.client.model.EntornApp;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
public class AclEntry implements Serializable {

	private Long id;
    @NotNull
    private SubjectType subjectType;
    @NotBlank
    private String subjectValue;
    @NotNull
    private ResourceType resourceType;
    @NotNull
    private Long resourceId;
    @NotNull
    private AclAction action;
    @NotNull
    private AclEffect effect;

}
