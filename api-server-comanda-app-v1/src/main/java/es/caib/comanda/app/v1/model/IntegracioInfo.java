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
 * Informació d&#39;una integració exposada per l&#39;aplicació
 */

@Schema(name = "IntegracioInfo", description = "Informació d'una integració exposada per l'aplicació")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class IntegracioInfo {

  private String codi;

  private String nom;

  public IntegracioInfo() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public IntegracioInfo(String codi, String nom) {
    this.codi = codi;
    this.nom = nom;
  }

  public IntegracioInfo codi(String codi) {
    this.codi = codi;
    return this;
  }

  /**
   * Codi identificador de la integració
   * @return codi
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "codi", example = "NOTIB", description = "Codi identificador de la integració", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  public void setCodi(String codi) {
    this.codi = codi;
  }

  public IntegracioInfo nom(String nom) {
    this.nom = nom;
    return this;
  }

  /**
   * Nom descriptiu de la integració
   * @return nom
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "nom", example = "NOTIB - Notificacions", description = "Nom descriptiu de la integració", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nom")
  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IntegracioInfo integracioInfo = (IntegracioInfo) o;
    return Objects.equals(this.codi, integracioInfo.codi) &&
        Objects.equals(this.nom, integracioInfo.nom);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, nom);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IntegracioInfo {\n");
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
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

