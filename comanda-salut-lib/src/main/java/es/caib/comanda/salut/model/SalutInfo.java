package es.caib.comanda.salut.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Builder
@Getter
public class SalutInfo {
    private final String codi;
    private final Date data;
    private final EstatSalut estat;
    private final EstatSalut bd;
    private final List<IntegracioSalut> integracions;
    private final List<DetallSalut> altres;
    private final List<MissatgeSalut> missatges;
    private final String versio;
    private final List<SalutInfo> subsistemes;
}
