package es.caib.comanda.model.v1.salut;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import es.caib.comanda.model.v1.deserializer.OffsetDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MissatgeSalut", description = "Missatge informatiu/alerta de salut amb nivell de gravetat")
public class MissatgeSalut {
    @Schema(description = "Instant del missatge", type = "string", format = "date-time")
    @NotNull
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime data;
    @Schema(description = "Nivell de gravetat del missatge")
    @NotNull
    private SalutNivell nivell;
    @Schema(description = "Text del missatge", example = "Manteniment programat a les 22:00h")
    @NotNull @Size(min = 1, max = 2048)
    private String missatge;
}
