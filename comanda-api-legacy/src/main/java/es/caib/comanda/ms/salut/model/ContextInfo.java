package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ContextInfo implements Serializable {
    private String codi;
    private String nom;
    private String path;
    private String api;
    private List<Manual> manuals;
}
