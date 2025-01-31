package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppInfo {
    private String codi;
    private String nom;
    private String versio;
    private Date data;
    private List<IntegracioInfo> integracions;
    private List<AppInfo> subsistemes;
}
