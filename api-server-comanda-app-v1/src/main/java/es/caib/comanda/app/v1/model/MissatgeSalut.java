package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import es.caib.comanda.app.v1.model.SalutNivell;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Missatge informatiu/alerta de salut amb nivell de gravetat
 */

@Schema(name = "MissatgeSalut", description = "Missatge informatiu/alerta de salut amb nivell de gravetat")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class MissatgeSalut {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime data;

  private SalutNivell nivell;

  private String missatge;

  public MissatgeSalut() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public MissatgeSalut(OffsetDateTime data, SalutNivell nivell, String missatge) {
    this.data = data;
    this.nivell = nivell;
    this.missatge = missatge;
  }

  public MissatgeSalut data(OffsetDateTime data) {
    this.data = data;
    return this;
  }

  /**
   * Instant del missatge
   * @return data
   */
  @NotNull @Valid 
  @Schema(name = "data", description = "Instant del missatge", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("data")
  public OffsetDateTime getData() {
    return data;
  }

  public void setData(OffsetDateTime data) {
    this.data = data;
  }

  public MissatgeSalut nivell(SalutNivell nivell) {
    this.nivell = nivell;
    return this;
  }

  /**
   * Get nivell
   * @return nivell
   */
  @NotNull @Valid 
  @Schema(name = "nivell", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nivell")
  public SalutNivell getNivell() {
    return nivell;
  }

  public void setNivell(SalutNivell nivell) {
    this.nivell = nivell;
  }

  public MissatgeSalut missatge(String missatge) {
    this.missatge = missatge;
    return this;
  }

  /**
   * Text del missatge
   * @return missatge
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "missatge", example = "Manteniment programat a les 22:00h", description = "Text del missatge", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("missatge")
  public String getMissatge() {
    return missatge;
  }

  public void setMissatge(String missatge) {
    this.missatge = missatge;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MissatgeSalut missatgeSalut = (MissatgeSalut) o;
    return Objects.equals(this.data, missatgeSalut.data) &&
        Objects.equals(this.nivell, missatgeSalut.nivell) &&
        Objects.equals(this.missatge, missatgeSalut.missatge);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, nivell, missatge);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MissatgeSalut {\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    nivell: ").append(toIndentedString(nivell)).append("\n");
    sb.append("    missatge: ").append(toIndentedString(missatge)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

