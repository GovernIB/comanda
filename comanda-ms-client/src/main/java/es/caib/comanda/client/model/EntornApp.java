package es.caib.comanda.client.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntornApp implements Serializable {

    private Long id;
    private EntornRef entorn;
    private AppRef app;

    private String infoUrl;
    private LocalDateTime infoData;
    private String versio;
    private boolean activa;

    private String salutUrl;

    private Integer integracioCount;
    private Integer subsistemaCount;

    private List<AppIntegracio> integracions;
    private List<AppSubsistema> subsistemes;
    private List<AppContext> contexts;

    private String estadisticaInfoUrl;
    private String estadisticaUrl;
    private String estadisticaCron;

    private boolean estadisticaAuth;
    private boolean salutAuth;

    @Builder.Default
    private Boolean compactable = false;
    private Integer compactacioSetmanalMesos;
    private Integer compactacioMensualMesos;
    private Integer eliminacioMesos;

    private String alarmesEmail;
}
