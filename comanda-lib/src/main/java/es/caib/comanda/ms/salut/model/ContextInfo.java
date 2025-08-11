package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContextInfo {
    private String codi;
    private String nom;
    private String path;
    private List<Manual> manuals;
    private String api;
}
