package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import es.caib.comanda.app.v1.model.EstatSalutEnum;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * EstatSalut
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class EstatSalut {

  private EstatSalutEnum estat;

  private Integer latencia;

  public EstatSalut estat(EstatSalutEnum estat) {
    this.estat = estat;
    return this;
  }

  /**
   * Get estat
   * @return estat
   */
  @Valid 
  @Schema(name = "estat", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("estat")
  public EstatSalutEnum getEstat() {
    return estat;
  }

  public void setEstat(EstatSalutEnum estat) {
    this.estat = estat;
  }

  public EstatSalut latencia(Integer latencia) {
    this.latencia = latencia;
    return this;
  }

  /**
   * Get latencia
   * @return latencia
   */
  
  @Schema(name = "latencia", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("latencia")
  public Integer getLatencia() {
    return latencia;
  }

  public void setLatencia(Integer latencia) {
    this.latencia = latencia;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EstatSalut estatSalut = (EstatSalut) o;
    return Objects.equals(this.estat, estatSalut.estat) &&
        Objects.equals(this.latencia, estatSalut.latencia);
  }

  @Override
  public int hashCode() {
    return Objects.hash(estat, latencia);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EstatSalut {\n");
    sb.append("    estat: ").append(toIndentedString(estat)).append("\n");
    sb.append("    latencia: ").append(toIndentedString(latencia)).append("\n");
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

