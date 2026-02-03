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

/**
 * Estat de salut i mètriques d&#39;un subsistema intern
 **/
@ApiModel(description = "Estat de salut i mètriques d'un subsistema intern")
@JsonTypeName("SubsistemaSalut")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class SubsistemaSalut   {
  private EstatSalutEnum estat;
  private Integer latencia;
  private String codi;
  private Long totalOk;
  private Long totalError;
  private Integer totalTempsMig;
  private Long peticionsOkUltimPeriode;
  private Long peticionsErrorUltimPeriode;
  private Integer tempsMigUltimPeriode;

  public SubsistemaSalut() {
  }

  @JsonCreator
  public SubsistemaSalut(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "totalOk") Long totalOk,
    @JsonProperty(required = true, value = "totalError") Long totalError,
    @JsonProperty(required = true, value = "totalTempsMig") Integer totalTempsMig,
    @JsonProperty(required = true, value = "peticionsOkUltimPeriode") Long peticionsOkUltimPeriode,
    @JsonProperty(required = true, value = "peticionsErrorUltimPeriode") Long peticionsErrorUltimPeriode,
    @JsonProperty(required = true, value = "tempsMigUltimPeriode") Integer tempsMigUltimPeriode
  ) {
    this.codi = codi;
    this.totalOk = totalOk;
    this.totalError = totalError;
    this.totalTempsMig = totalTempsMig;
    this.peticionsOkUltimPeriode = peticionsOkUltimPeriode;
    this.peticionsErrorUltimPeriode = peticionsErrorUltimPeriode;
    this.tempsMigUltimPeriode = tempsMigUltimPeriode;
  }

  /**
   **/
  public SubsistemaSalut estat(EstatSalutEnum estat) {
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
  public SubsistemaSalut latencia(Integer latencia) {
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
   * Codi del subsistema
   **/
  public SubsistemaSalut codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(example = "BD", required = true, value = "Codi del subsistema")
  @JsonProperty(required = true, value = "codi")
  @NotNull  @Size(min=1)public String getCodi() {
    return codi;
  }

  @JsonProperty(required = true, value = "codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   * Total de peticions amb resultat correcte
   **/
  public SubsistemaSalut totalOk(Long totalOk) {
    this.totalOk = totalOk;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Total de peticions amb resultat correcte")
  @JsonProperty(required = true, value = "totalOk")
  @NotNull public Long getTotalOk() {
    return totalOk;
  }

  @JsonProperty(required = true, value = "totalOk")
  public void setTotalOk(Long totalOk) {
    this.totalOk = totalOk;
  }

  /**
   * Total de peticions amb error
   **/
  public SubsistemaSalut totalError(Long totalError) {
    this.totalError = totalError;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Total de peticions amb error")
  @JsonProperty(required = true, value = "totalError")
  @NotNull public Long getTotalError() {
    return totalError;
  }

  @JsonProperty(required = true, value = "totalError")
  public void setTotalError(Long totalError) {
    this.totalError = totalError;
  }

  /**
   * Temps mig total de resposta (ms)
   **/
  public SubsistemaSalut totalTempsMig(Integer totalTempsMig) {
    this.totalTempsMig = totalTempsMig;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Temps mig total de resposta (ms)")
  @JsonProperty(required = true, value = "totalTempsMig")
  @NotNull public Integer getTotalTempsMig() {
    return totalTempsMig;
  }

  @JsonProperty(required = true, value = "totalTempsMig")
  public void setTotalTempsMig(Integer totalTempsMig) {
    this.totalTempsMig = totalTempsMig;
  }

  /**
   * Peticions OK en el darrer període
   **/
  public SubsistemaSalut peticionsOkUltimPeriode(Long peticionsOkUltimPeriode) {
    this.peticionsOkUltimPeriode = peticionsOkUltimPeriode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Peticions OK en el darrer període")
  @JsonProperty(required = true, value = "peticionsOkUltimPeriode")
  @NotNull public Long getPeticionsOkUltimPeriode() {
    return peticionsOkUltimPeriode;
  }

  @JsonProperty(required = true, value = "peticionsOkUltimPeriode")
  public void setPeticionsOkUltimPeriode(Long peticionsOkUltimPeriode) {
    this.peticionsOkUltimPeriode = peticionsOkUltimPeriode;
  }

  /**
   * Peticions en error en el darrer període
   **/
  public SubsistemaSalut peticionsErrorUltimPeriode(Long peticionsErrorUltimPeriode) {
    this.peticionsErrorUltimPeriode = peticionsErrorUltimPeriode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Peticions en error en el darrer període")
  @JsonProperty(required = true, value = "peticionsErrorUltimPeriode")
  @NotNull public Long getPeticionsErrorUltimPeriode() {
    return peticionsErrorUltimPeriode;
  }

  @JsonProperty(required = true, value = "peticionsErrorUltimPeriode")
  public void setPeticionsErrorUltimPeriode(Long peticionsErrorUltimPeriode) {
    this.peticionsErrorUltimPeriode = peticionsErrorUltimPeriode;
  }

  /**
   * Temps mig de resposta en el darrer període (ms)
   **/
  public SubsistemaSalut tempsMigUltimPeriode(Integer tempsMigUltimPeriode) {
    this.tempsMigUltimPeriode = tempsMigUltimPeriode;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Temps mig de resposta en el darrer període (ms)")
  @JsonProperty(required = true, value = "tempsMigUltimPeriode")
  @NotNull public Integer getTempsMigUltimPeriode() {
    return tempsMigUltimPeriode;
  }

  @JsonProperty(required = true, value = "tempsMigUltimPeriode")
  public void setTempsMigUltimPeriode(Integer tempsMigUltimPeriode) {
    this.tempsMigUltimPeriode = tempsMigUltimPeriode;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubsistemaSalut subsistemaSalut = (SubsistemaSalut) o;
    return Objects.equals(this.estat, subsistemaSalut.estat) &&
        Objects.equals(this.latencia, subsistemaSalut.latencia) &&
        Objects.equals(this.codi, subsistemaSalut.codi) &&
        Objects.equals(this.totalOk, subsistemaSalut.totalOk) &&
        Objects.equals(this.totalError, subsistemaSalut.totalError) &&
        Objects.equals(this.totalTempsMig, subsistemaSalut.totalTempsMig) &&
        Objects.equals(this.peticionsOkUltimPeriode, subsistemaSalut.peticionsOkUltimPeriode) &&
        Objects.equals(this.peticionsErrorUltimPeriode, subsistemaSalut.peticionsErrorUltimPeriode) &&
        Objects.equals(this.tempsMigUltimPeriode, subsistemaSalut.tempsMigUltimPeriode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(estat, latencia, codi, totalOk, totalError, totalTempsMig, peticionsOkUltimPeriode, peticionsErrorUltimPeriode, tempsMigUltimPeriode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubsistemaSalut {\n");
    
    sb.append("    estat: ").append(toIndentedString(estat)).append("\n");
    sb.append("    latencia: ").append(toIndentedString(latencia)).append("\n");
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    totalOk: ").append(toIndentedString(totalOk)).append("\n");
    sb.append("    totalError: ").append(toIndentedString(totalError)).append("\n");
    sb.append("    totalTempsMig: ").append(toIndentedString(totalTempsMig)).append("\n");
    sb.append("    peticionsOkUltimPeriode: ").append(toIndentedString(peticionsOkUltimPeriode)).append("\n");
    sb.append("    peticionsErrorUltimPeriode: ").append(toIndentedString(peticionsErrorUltimPeriode)).append("\n");
    sb.append("    tempsMigUltimPeriode: ").append(toIndentedString(tempsMigUltimPeriode)).append("\n");
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

