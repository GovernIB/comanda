package es.caib.comanda.model.server.monitoring;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
import es.caib.comanda.model.server.monitoring.IntegracioPeticions;
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

/**
 * Estat de salut d&#39;una integració concreta
 **/
@ApiModel(description = "Estat de salut d'una integració concreta")
@JsonTypeName("IntegracioSalut")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class IntegracioSalut   {
  private EstatSalutEnum estat;
  private Integer latencia;
  private String codi;
  private IntegracioPeticions peticions;

  public IntegracioSalut() {
  }

  @JsonCreator
  public IntegracioSalut(
    @JsonProperty(required = true, value = "codi") String codi
  ) {
    this.codi = codi;
  }

  /**
   **/
  public IntegracioSalut estat(EstatSalutEnum estat) {
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
  public IntegracioSalut latencia(Integer latencia) {
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

  /**
   * Codi de la integració
   **/
  public IntegracioSalut codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(example = "REG", required = true, value = "Codi de la integració")
  @JsonProperty(required = true, value = "codi")
  @NotNull  @Size(min=1,max=32)public String getCodi() {
    return codi;
  }

  @JsonProperty(required = true, value = "codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   **/
  public IntegracioSalut peticions(IntegracioPeticions peticions) {
    this.peticions = peticions;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("peticions")
  @Valid public IntegracioPeticions getPeticions() {
    return peticions;
  }

  @JsonProperty("peticions")
  public void setPeticions(IntegracioPeticions peticions) {
    this.peticions = peticions;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IntegracioSalut integracioSalut = (IntegracioSalut) o;
    return Objects.equals(this.estat, integracioSalut.estat) &&
        Objects.equals(this.latencia, integracioSalut.latencia) &&
        Objects.equals(this.codi, integracioSalut.codi) &&
        Objects.equals(this.peticions, integracioSalut.peticions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(estat, latencia, codi, peticions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IntegracioSalut {\n");
    
    sb.append("    estat: ").append(toIndentedString(estat)).append("\n");
    sb.append("    latencia: ").append(toIndentedString(latencia)).append("\n");
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    peticions: ").append(toIndentedString(peticions)).append("\n");
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

