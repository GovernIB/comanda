package es.caib.comanda.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalutSubsistema implements Serializable {
    private Long id;
    private String codi;
    private SalutEstat estat;
    private Integer latencia;
}
