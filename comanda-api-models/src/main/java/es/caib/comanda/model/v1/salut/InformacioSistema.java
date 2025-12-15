package es.caib.comanda.model.v1.salut;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Informació resum de l'estat del sistema on s'executa l'aplicació.
 * Els camps són fixos per facilitar la serialització i la representació al front.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "InformacioSistema", description = "Informació resum de l'estat del sistema on s'executa l'aplicació")
public class InformacioSistema {
    @Schema(description = "Nombre de processadors disponibles", example = "8")
    private Integer processadors;
    @Schema(description = "Càrrega actual del sistema", example = "2.45")
    private String carregaSistema;
    @Schema(description = "Percentatge d'ús de CPU del sistema", example = "45.2%")
    private String cpuSistema;
    @Schema(description = "Memòria total del sistema", example = "16 GB")
    private String memoriaTotal;
    @Schema(description = "Memòria disponible del sistema", example = "8 GB")
    private String memoriaDisponible;
    @Schema(description = "Espai total del disc", example = "500 GB")
    private String espaiDiscTotal;
    @Schema(description = "Espai lliure del disc", example = "250 GB")
    private String espaiDiscLliure;
    @Schema(description = "Sistema operatiu", example = "Linux 5.4.0")
    private String sistemaOperatiu;
    @Schema(description = "Data d'arrencada del sistema", example = "15/01/2024 10:30")
    private String dataArrencada;
    @Schema(description = "Temps de funcionament del sistema", example = "5 dies, 03:25:14")
    private String tempsFuncionant;
}
