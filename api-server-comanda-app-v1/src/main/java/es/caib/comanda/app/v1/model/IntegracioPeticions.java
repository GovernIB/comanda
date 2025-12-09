package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Map;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Mètriques de peticions d&#39;una integració: totals i darrer període
 */

@Schema(name = "IntegracioPeticions", description = "Mètriques de peticions d'una integració: totals i darrer període")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class IntegracioPeticions {

  private Long totalOk;

  private Long totalError;

  private Integer totalTempsMig;

  private Long peticionsOkUltimPeriode;

  private Long peticionsErrorUltimPeriode;

  private Integer tempsMigUltimPeriode;

  private String endpoint;

  @Valid
  private Map<String, IntegracioPeticions> peticionsPerEntorn = new HashMap<>();

  public IntegracioPeticions() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public IntegracioPeticions(Long totalOk, Long totalError, Integer totalTempsMig, Long peticionsOkUltimPeriode, Long peticionsErrorUltimPeriode, Integer tempsMigUltimPeriode) {
    this.totalOk = totalOk;
    this.totalError = totalError;
    this.totalTempsMig = totalTempsMig;
    this.peticionsOkUltimPeriode = peticionsOkUltimPeriode;
    this.peticionsErrorUltimPeriode = peticionsErrorUltimPeriode;
    this.tempsMigUltimPeriode = tempsMigUltimPeriode;
  }

  public IntegracioPeticions totalOk(Long totalOk) {
    this.totalOk = totalOk;
    return this;
  }

  /**
   * Nombre total de peticions amb resultat correcte
   * @return totalOk
   */
  @NotNull 
  @Schema(name = "totalOk", description = "Nombre total de peticions amb resultat correcte", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalOk")
  public Long getTotalOk() {
    return totalOk;
  }

  public void setTotalOk(Long totalOk) {
    this.totalOk = totalOk;
  }

  public IntegracioPeticions totalError(Long totalError) {
    this.totalError = totalError;
    return this;
  }

  /**
   * Nombre total de peticions amb error
   * @return totalError
   */
  @NotNull 
  @Schema(name = "totalError", description = "Nombre total de peticions amb error", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalError")
  public Long getTotalError() {
    return totalError;
  }

  public void setTotalError(Long totalError) {
    this.totalError = totalError;
  }

  public IntegracioPeticions totalTempsMig(Integer totalTempsMig) {
    this.totalTempsMig = totalTempsMig;
    return this;
  }

  /**
   * Temps mig total de resposta (ms)
   * @return totalTempsMig
   */
  @NotNull 
  @Schema(name = "totalTempsMig", description = "Temps mig total de resposta (ms)", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalTempsMig")
  public Integer getTotalTempsMig() {
    return totalTempsMig;
  }

  public void setTotalTempsMig(Integer totalTempsMig) {
    this.totalTempsMig = totalTempsMig;
  }

  public IntegracioPeticions peticionsOkUltimPeriode(Long peticionsOkUltimPeriode) {
    this.peticionsOkUltimPeriode = peticionsOkUltimPeriode;
    return this;
  }

  /**
   * Peticions OK en el darrer període
   * @return peticionsOkUltimPeriode
   */
  @NotNull 
  @Schema(name = "peticionsOkUltimPeriode", description = "Peticions OK en el darrer període", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("peticionsOkUltimPeriode")
  public Long getPeticionsOkUltimPeriode() {
    return peticionsOkUltimPeriode;
  }

  public void setPeticionsOkUltimPeriode(Long peticionsOkUltimPeriode) {
    this.peticionsOkUltimPeriode = peticionsOkUltimPeriode;
  }

  public IntegracioPeticions peticionsErrorUltimPeriode(Long peticionsErrorUltimPeriode) {
    this.peticionsErrorUltimPeriode = peticionsErrorUltimPeriode;
    return this;
  }

  /**
   * Peticions en error en el darrer període
   * @return peticionsErrorUltimPeriode
   */
  @NotNull 
  @Schema(name = "peticionsErrorUltimPeriode", description = "Peticions en error en el darrer període", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("peticionsErrorUltimPeriode")
  public Long getPeticionsErrorUltimPeriode() {
    return peticionsErrorUltimPeriode;
  }

  public void setPeticionsErrorUltimPeriode(Long peticionsErrorUltimPeriode) {
    this.peticionsErrorUltimPeriode = peticionsErrorUltimPeriode;
  }

  public IntegracioPeticions tempsMigUltimPeriode(Integer tempsMigUltimPeriode) {
    this.tempsMigUltimPeriode = tempsMigUltimPeriode;
    return this;
  }

  /**
   * Temps mig de resposta en el darrer període (ms)
   * @return tempsMigUltimPeriode
   */
  @NotNull 
  @Schema(name = "tempsMigUltimPeriode", description = "Temps mig de resposta en el darrer període (ms)", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("tempsMigUltimPeriode")
  public Integer getTempsMigUltimPeriode() {
    return tempsMigUltimPeriode;
  }

  public void setTempsMigUltimPeriode(Integer tempsMigUltimPeriode) {
    this.tempsMigUltimPeriode = tempsMigUltimPeriode;
  }

  public IntegracioPeticions endpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Endpoint concret associat a aquestes mètriques
   * @return endpoint
   */
  @Size(min = 0, max = 255) 
  @Schema(name = "endpoint", example = "/api/v1/notificacions", description = "Endpoint concret associat a aquestes mètriques", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("endpoint")
  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public IntegracioPeticions peticionsPerEntorn(Map<String, IntegracioPeticions> peticionsPerEntorn) {
    this.peticionsPerEntorn = peticionsPerEntorn;
    return this;
  }

  public IntegracioPeticions putPeticionsPerEntornItem(String key, IntegracioPeticions peticionsPerEntornItem) {
    if (this.peticionsPerEntorn == null) {
      this.peticionsPerEntorn = new HashMap<>();
    }
    this.peticionsPerEntorn.put(key, peticionsPerEntornItem);
    return this;
  }

  /**
   * Mètriques per entorn (clau = codi d'entorn)
   * @return peticionsPerEntorn
   */
  @Valid 
  @Schema(name = "peticionsPerEntorn", description = "Mètriques per entorn (clau = codi d'entorn)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("peticionsPerEntorn")
  public Map<String, IntegracioPeticions> getPeticionsPerEntorn() {
    return peticionsPerEntorn;
  }

  public void setPeticionsPerEntorn(Map<String, IntegracioPeticions> peticionsPerEntorn) {
    this.peticionsPerEntorn = peticionsPerEntorn;
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

