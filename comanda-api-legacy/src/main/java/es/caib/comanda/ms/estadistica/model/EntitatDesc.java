package es.caib.comanda.ms.estadistica.model;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EntitatDesc {
    private String codi;
    private String nom;
    private String codiDir3;
    private String cif;
}
