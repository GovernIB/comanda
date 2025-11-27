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
 * Informació d&#39;un subsistema intern de l&#39;aplicació
 */

@Schema(name = "SubsistemaInfo", description = "Informació d'un subsistema intern de l'aplicació")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class SubsistemaInfo {

  private String codi;

  private String nom;

  public SubsistemaInfo() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SubsistemaInfo(String codi, String nom) {
    this.codi = codi;
    this.nom = nom;
  }

  public SubsistemaInfo codi(String codi) {
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

  public SubsistemaInfo nom(String nom) {
    this.nom = nom;
    return this;
  }

  /**
   * Nom del subsistema
   * @return nom
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "nom", example = "Base de Dades", description = "Nom del subsistema", requiredMode = Schema.RequiredMode.REQUIRED)
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
    SubsistemaInfo subsistemaInfo = (SubsistemaInfo) o;
    return Objects.equals(this.codi, subsistemaInfo.codi) &&
        Objects.equals(this.nom, subsistemaInfo.nom);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, nom);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubsistemaInfo {\n");
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

