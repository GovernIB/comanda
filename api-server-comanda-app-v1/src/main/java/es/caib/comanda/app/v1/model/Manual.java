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
 * Referència a un manual o documentació funcional
 */

@Schema(name = "Manual", description = "Referència a un manual o documentació funcional")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class Manual {

  private String nom;

  private String path;

  public Manual() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Manual(String nom, String path) {
    this.nom = nom;
    this.path = path;
  }

  public Manual nom(String nom) {
    this.nom = nom;
    return this;
  }

  /**
   * Nom del manual
   * @return nom
   */
  @NotNull @Size(min = 1, max = 128) 
  @Schema(name = "nom", example = "Guia d'usuari", description = "Nom del manual", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nom")
  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public Manual path(String path) {
    this.path = path;
    return this;
  }

  /**
   * Ruta o URL del manual
   * @return path
   */
  @NotNull @Size(min = 1, max = 255) 
  @Schema(name = "path", example = "https://www.caib.es/docs/guia-usuari.pdf", description = "Ruta o URL del manual", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("path")
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Manual manual = (Manual) o;
    return Objects.equals(this.nom, manual.nom) &&
        Objects.equals(this.path, manual.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nom, path);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Manual {\n");
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
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

