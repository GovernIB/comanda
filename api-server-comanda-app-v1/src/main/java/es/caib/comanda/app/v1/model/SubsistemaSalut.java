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
 * Estat de salut i mètriques d&#39;un subsistema intern
 */

@Schema(name = "SubsistemaSalut", description = "Estat de salut i mètriques d'un subsistema intern")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class SubsistemaSalut {

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
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SubsistemaSalut(String codi, Long totalOk, Long totalError, Integer totalTempsMig, Long peticionsOkUltimPeriode, Long peticionsErrorUltimPeriode, Integer tempsMigUltimPeriode) {
    this.codi = codi;
    this.totalOk = totalOk;
    this.totalError = totalError;
    this.totalTempsMig = totalTempsMig;
    this.peticionsOkUltimPeriode = peticionsOkUltimPeriode;
    this.peticionsErrorUltimPeriode = peticionsErrorUltimPeriode;
    this.tempsMigUltimPeriode = tempsMigUltimPeriode;
  }

  public SubsistemaSalut estat(EstatSalutEnum estat) {
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

  public SubsistemaSalut latencia(Integer latencia) {
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

  public SubsistemaSalut codi(String codi) {
    this.codi = codi;
    return this;
  }

  /**
   * Codi del subsistema
   * @return codi
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "codi", example = "BD", description = "Codi del subsistema", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  public void setCodi(String codi) {
    this.codi = codi;
  }

  public SubsistemaSalut totalOk(Long totalOk) {
    this.totalOk = totalOk;
    return this;
  }

  /**
   * Total de peticions amb resultat correcte
   * @return totalOk
   */
  @NotNull 
  @Schema(name = "totalOk", description = "Total de peticions amb resultat correcte", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalOk")
  public Long getTotalOk() {
    return totalOk;
  }

  public void setTotalOk(Long totalOk) {
    this.totalOk = totalOk;
  }

  public SubsistemaSalut totalError(Long totalError) {
    this.totalError = totalError;
    return this;
  }

  /**
   * Total de peticions amb error
   * @return totalError
   */
  @NotNull 
  @Schema(name = "totalError", description = "Total de peticions amb error", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("totalError")
  public Long getTotalError() {
    return totalError;
  }

  public void setTotalError(Long totalError) {
    this.totalError = totalError;
  }

  public SubsistemaSalut totalTempsMig(Integer totalTempsMig) {
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

  public SubsistemaSalut peticionsOkUltimPeriode(Long peticionsOkUltimPeriode) {
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

  public SubsistemaSalut peticionsErrorUltimPeriode(Long peticionsErrorUltimPeriode) {
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

  public SubsistemaSalut tempsMigUltimPeriode(Integer tempsMigUltimPeriode) {
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

