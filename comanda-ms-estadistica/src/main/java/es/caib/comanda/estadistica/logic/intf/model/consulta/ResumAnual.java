package es.caib.comanda.estadistica.logic.intf.model.consulta;

import lombok.Data;

import java.util.Map;

@Data
public class ResumAnual {
    private int any;
    private Map<String, String> dimensions;
    private Map<String, Double> indicadors;
    private int numRegistres;
}
