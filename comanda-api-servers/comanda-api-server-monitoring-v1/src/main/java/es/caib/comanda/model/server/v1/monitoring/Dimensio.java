package es.caib.comanda.model.server.v1.monitoring;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("Dimensio")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class Dimensio   {
  private String codi;
  private String valor;

  public Dimensio() {
  }

  /**
   **/
  public Dimensio codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  @JsonProperty("codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   **/
  public Dimensio valor(String valor) {
    this.valor = valor;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("valor")
  public String getValor() {
    return valor;
  }

  @JsonProperty("valor")
  public void setValor(String valor) {
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
    Dimensio dimensio = (Dimensio) o;
    return Objects.equals(this.codi, dimensio.codi) &&
        Objects.equals(this.valor, dimensio.valor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, valor);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Dimensio {\n");
    
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

