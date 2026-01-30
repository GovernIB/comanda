package es.caib.comanda.model.server.v1.monitoring;

import es.caib.comanda.model.server.v1.monitoring.SalutNivell;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Missatge informatiu/alerta de salut amb nivell de gravetat
 **/
@ApiModel(description = "Missatge informatiu/alerta de salut amb nivell de gravetat")
@JsonTypeName("MissatgeSalut")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class MissatgeSalut   {
  private OffsetDateTime data;
  private SalutNivell nivell;
  private String missatge;

  public MissatgeSalut() {
  }

  @JsonCreator
  public MissatgeSalut(
    @JsonProperty(required = true, value = "data") OffsetDateTime data,
    @JsonProperty(required = true, value = "nivell") SalutNivell nivell,
    @JsonProperty(required = true, value = "missatge") String missatge
  ) {
    this.data = data;
    this.nivell = nivell;
    this.missatge = missatge;
  }

  /**
   **/
  public MissatgeSalut data(OffsetDateTime data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "data")
  @NotNull @Valid public OffsetDateTime getData() {
    return data;
  }

  @JsonProperty(required = true, value = "data")
  public void setData(OffsetDateTime data) {
    this.data = data;
  }

  /**
   **/
  public MissatgeSalut nivell(SalutNivell nivell) {
    this.nivell = nivell;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "nivell")
  @NotNull public SalutNivell getNivell() {
    return nivell;
  }

  @JsonProperty(required = true, value = "nivell")
  public void setNivell(SalutNivell nivell) {
    this.nivell = nivell;
  }

  /**
   * Text del missatge
   **/
  public MissatgeSalut missatge(String missatge) {
    this.missatge = missatge;
    return this;
  }

  
  @ApiModelProperty(example = "Manteniment programat a les 22:00h", required = true, value = "Text del missatge")
  @JsonProperty(required = true, value = "missatge")
  @NotNull  @Size(min=1)public String getMissatge() {
    return missatge;
  }

  @JsonProperty(required = true, value = "missatge")
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

