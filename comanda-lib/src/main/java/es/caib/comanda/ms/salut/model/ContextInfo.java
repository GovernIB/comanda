package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContextInfo {
    @NotNull @Size(min = 1, max = 64)
    private String codi;
    @NotNull @Size(min = 1, max = 100)
    private String nom;
    @NotNull @Size(min = 1, max = 255)
    private String path;
    @Valid
    private List<Manual> manuals;
    @Size(max = 255)
    private String api;
}
