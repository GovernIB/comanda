package es.caib.comanda.salut.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
public class SalutInfo {
    private String codi;
    private Date data;
    private EstatSalut estat;
    private EstatSalut bd;
    private List<IntegracioSalut> integracions;
    private List<DetallSalut> altres;
    private List<MissatgeSalut> missatges;
    private String versio;
    private List<SubsistemaSalutInfo> subsistemes;
}
