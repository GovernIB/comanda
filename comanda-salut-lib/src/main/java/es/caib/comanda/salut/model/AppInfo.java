package es.caib.comanda.salut.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Builder
@Getter
public class AppInfo {
    private final String codi;
    private final String nom;
    private final String versio;
    private final Date data;
    private final List<IntegracioInfo> integracions;
    private final List<AppInfo> subsistemes;
}
