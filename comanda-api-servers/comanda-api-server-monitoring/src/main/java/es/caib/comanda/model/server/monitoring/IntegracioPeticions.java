package es.caib.comanda.model.server.monitoring;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Mètriques de peticions d&#39;una integració: totals i darrer període
 **/
@ApiModel(description = "Mètriques de peticions d'una integració: totals i darrer període")
@JsonTypeName("IntegracioPeticions")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class IntegracioPeticions   {
  private Long totalOk;
  private Long totalError;
  private Integer totalTempsMig;
  private Long peticionsOkUltimPeriode;
  private Long peticionsErrorUltimPeriode;
  private Integer tempsMigUltimPeriode;
  private String endpoint;
  private @Valid Map<String, IntegracioPeticions> peticionsPerEntorn = new HashMap<>();

  public IntegracioPeticions() {
  }

  @JsonCreator
  public IntegracioPeticions(
    @JsonProperty(required = true, value = "totalOk") Long totalOk,
    @JsonProperty(required = true, value = "totalError") Long totalError,
    @JsonProperty(required = true, value = "totalTempsMig") Integer totalTempsMig,
    @JsonProperty(required = true, value = "peticionsOkUltimPeriode") Long peticionsOkUltimPeriode,
    @JsonProperty(required = true, value = "peticionsErrorUltimPeriode") Long peticionsErrorUltimPeriode,
    @JsonProperty(required = true, value = "tempsMigUltimPeriode") Integer tempsMigUltimPeriode
  ) {
    this.totalOk = totalOk;
    this.totalError = totalError;
    this.totalTempsMig = totalTempsMig;
    this.peticionsOkUltimPeriode = peticionsOkUltimPeriode;
    this.peticionsErrorUltimPeriode = peticionsErrorUltimPeriode;
    this.tempsMigUltimPeriode = tempsMigUltimPeriode;
  }

  /**
   * Nombre total de peticions amb resultat correcte
   **/
  public IntegracioPeticions totalOk(Long totalOk) {
    this.totalOk = totalOk;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Nombre total de peticions amb resultat correcte")
  @JsonProperty(required = true, value = "totalOk")
  @NotNull public Long getTotalOk() {
    return totalOk;
  }

  @JsonProperty(required = true, value = "totalOk")
  public void setTotalOk(Long totalOk) {
    this.totalOk = totalOk;
  }

  /**
   * Nombre total de peticions amb error
   **/
  public IntegracioPeticions totalError(Long totalError) {
    this.totalError = totalError;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Nombre total de peticions amb error")
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
  public IntegracioPeticions totalTempsMig(Integer totalTempsMig) {
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
  public IntegracioPeticions peticionsOkUltimPeriode(Long peticionsOkUltimPeriode) {
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
  public IntegracioPeticions peticionsErrorUltimPeriode(Long peticionsErrorUltimPeriode) {
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
  public IntegracioPeticions tempsMigUltimPeriode(Integer tempsMigUltimPeriode) {
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

  /**
   * Endpoint concret associat a aquestes mètriques
   **/
  public IntegracioPeticions endpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  
  @ApiModelProperty(example = "https://path/to/integracio", value = "Endpoint concret associat a aquestes mètriques")
  @JsonProperty("endpoint")
   @Size(min=0,max=255)public String getEndpoint() {
    return endpoint;
  }

  @JsonProperty("endpoint")
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * Mètriques per entorn (clau &#x3D; codi d&#39;entorn)
   **/
  public IntegracioPeticions peticionsPerEntorn(Map<String, IntegracioPeticions> peticionsPerEntorn) {
    this.peticionsPerEntorn = peticionsPerEntorn;
    return this;
  }

  
  @ApiModelProperty(example = "{\"ENT1\":{\"totalOk\":0,\"totalError\":0,\"totalTempsMig\":0,\"peticionsOkUltimPeriode\":0,\"peticionsErrorUltimPeriode\":0,\"tempsMigUltimPeriode\":0,\"endpoint\":\"https://path/to/entorn1\"},\"ENT2\":{\"totalOk\":0,\"totalError\":0,\"totalTempsMig\":0,\"peticionsOkUltimPeriode\":0,\"peticionsErrorUltimPeriode\":0,\"tempsMigUltimPeriode\":0,\"endpoint\":\"https://path/to/entorn2\"}}", value = "Mètriques per entorn (clau = codi d'entorn)")
  @JsonProperty("peticionsPerEntorn")
  @Valid public Map<String, IntegracioPeticions> getPeticionsPerEntorn() {
    return peticionsPerEntorn;
  }

  @JsonProperty("peticionsPerEntorn")
  public void setPeticionsPerEntorn(Map<String, IntegracioPeticions> peticionsPerEntorn) {
    this.peticionsPerEntorn = peticionsPerEntorn;
  }

  public IntegracioPeticions putPeticionsPerEntornItem(String key, IntegracioPeticions peticionsPerEntornItem) {
    if (this.peticionsPerEntorn == null) {
      this.peticionsPerEntorn = new HashMap<>();
    }

    this.peticionsPerEntorn.put(key, peticionsPerEntornItem);
    return this;
  }

  public IntegracioPeticions removePeticionsPerEntornItem(String key) {
    if (this.peticionsPerEntorn != null) {
      this.peticionsPerEntorn.remove(key);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IntegracioPeticions integracioPeticions = (IntegracioPeticions) o;
    return Objects.equals(this.totalOk, integracioPeticions.totalOk) &&
        Objects.equals(this.totalError, integracioPeticions.totalError) &&
        Objects.equals(this.totalTempsMig, integracioPeticions.totalTempsMig) &&
        Objects.equals(this.peticionsOkUltimPeriode, integracioPeticions.peticionsOkUltimPeriode) &&
        Objects.equals(this.peticionsErrorUltimPeriode, integracioPeticions.peticionsErrorUltimPeriode) &&
        Objects.equals(this.tempsMigUltimPeriode, integracioPeticions.tempsMigUltimPeriode) &&
        Objects.equals(this.endpoint, integracioPeticions.endpoint) &&
        Objects.equals(this.peticionsPerEntorn, integracioPeticions.peticionsPerEntorn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalOk, totalError, totalTempsMig, peticionsOkUltimPeriode, peticionsErrorUltimPeriode, tempsMigUltimPeriode, endpoint, peticionsPerEntorn);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IntegracioPeticions {\n");
    
    sb.append("    totalOk: ").append(toIndentedString(totalOk)).append("\n");
    sb.append("    totalError: ").append(toIndentedString(totalError)).append("\n");
    sb.append("    totalTempsMig: ").append(toIndentedString(totalTempsMig)).append("\n");
    sb.append("    peticionsOkUltimPeriode: ").append(toIndentedString(peticionsOkUltimPeriode)).append("\n");
    sb.append("    peticionsErrorUltimPeriode: ").append(toIndentedString(peticionsErrorUltimPeriode)).append("\n");
    sb.append("    tempsMigUltimPeriode: ").append(toIndentedString(tempsMigUltimPeriode)).append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
    sb.append("    peticionsPerEntorn: ").append(toIndentedString(peticionsPerEntorn)).append("\n");
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

