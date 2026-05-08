package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class InformacioSistema implements Serializable {
    private Integer processadors;
    private String carregaSistema;
    private String cpuSistema;
    private String memoriaTotal;
    private String memoriaDisponible;
    private String espaiDiscTotal;
    private String espaiDiscLliure;
    private String sistemaOperatiu;
    private String dataArrencada;
    private String tempsFuncionant;
}
