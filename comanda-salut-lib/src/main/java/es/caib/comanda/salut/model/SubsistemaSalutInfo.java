package es.caib.comanda.salut.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Builder
@Getter
public class SubsistemaSalutInfo {
    private final String codi;
    private final EstatSalut estat;
}
