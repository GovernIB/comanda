package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Fet
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class Fet {

  private String codi;

  private Double valor;

  public Fet codi(String codi) {
    this.codi = codi;
    return this;
  }

  /**
   * Get codi
   * @return codi
   */
  
  @Schema(name = "codi", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  public void setCodi(String codi) {
    this.codi = codi;
  }

  public Fet valor(Double valor) {
    this.valor = valor;
    return this;
  }

  /**
   * Get valor
   * @return valor
   */
  
  @Schema(name = "valor", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("valor")
  public Double getValor() {
    return valor;
  }

  public void setValor(Double valor) {
    this.valor = valor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Fet fet = (Fet) o;
    return Objects.equals(this.codi, fet.codi) &&
        Objects.equals(this.valor, fet.valor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, valor);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Fet {\n");
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    valor: ").append(toIndentedString(valor)).append("\n");
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

