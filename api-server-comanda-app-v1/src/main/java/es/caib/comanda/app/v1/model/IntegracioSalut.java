package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import es.caib.comanda.app.v1.model.EstatSalutEnum;
import es.caib.comanda.app.v1.model.IntegracioPeticions;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Estat de salut d&#39;una integració concreta
 */

@Schema(name = "IntegracioSalut", description = "Estat de salut d'una integració concreta")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class IntegracioSalut {

  private EstatSalutEnum estat;

  private Integer latencia;

  private String codi;

  private IntegracioPeticions peticions;

  public IntegracioSalut() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public IntegracioSalut(String codi) {
    this.codi = codi;
  }

  public IntegracioSalut estat(EstatSalutEnum estat) {
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

  public IntegracioSalut latencia(Integer latencia) {
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

  public IntegracioSalut codi(String codi) {
    this.codi = codi;
    return this;
  }

  /**
   * Codi de la integració
   * @return codi
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "codi", example = "NOTIB", description = "Codi de la integració", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  public void setCodi(String codi) {
    this.codi = codi;
  }

  public IntegracioSalut peticions(IntegracioPeticions peticions) {
    this.peticions = peticions;
    return this;
  }

  /**
   * Get peticions
   * @return peticions
   */
  @Valid 
  @Schema(name = "peticions", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("peticions")
  public IntegracioPeticions getPeticions() {
    return peticions;
  }

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

