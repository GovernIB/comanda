package es.caib.comanda.model.server.monitoring;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
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



@JsonTypeName("EstatSalut")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class EstatSalut   {
  private EstatSalutEnum estat;
  private Integer latencia;

  public EstatSalut() {
  }

  /**
   **/
  public EstatSalut estat(EstatSalutEnum estat) {
    this.estat = estat;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("estat")
  public EstatSalutEnum getEstat() {
    return estat;
  }

  @JsonProperty("estat")
  public void setEstat(EstatSalutEnum estat) {
    this.estat = estat;
  }

  /**
   **/
  public EstatSalut latencia(Integer latencia) {
    this.latencia = latencia;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("latencia")
  public Integer getLatencia() {
    return latencia;
  }

  @JsonProperty("latencia")
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

