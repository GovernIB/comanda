package es.caib.comanda.model.server.v1.monitoring;

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
 * Referència a un manual o documentació funcional
 **/
@ApiModel(description = "Referència a un manual o documentació funcional")
@JsonTypeName("Manual")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class Manual   {
  private String nom;
  private String path;

  public Manual() {
  }

  @JsonCreator
  public Manual(
    @JsonProperty(required = true, value = "nom") String nom,
    @JsonProperty(required = true, value = "path") String path
  ) {
    this.nom = nom;
    this.path = path;
  }

  /**
   * Nom del manual
   **/
  public Manual nom(String nom) {
    this.nom = nom;
    return this;
  }

  
  @ApiModelProperty(example = "Guia d'usuari", required = true, value = "Nom del manual")
  @JsonProperty(required = true, value = "nom")
  @NotNull  @Size(min=1,max=128)public String getNom() {
    return nom;
  }

  @JsonProperty(required = true, value = "nom")
  public void setNom(String nom) {
    this.nom = nom;
  }

  /**
   * Ruta o URL del manual
   **/
  public Manual path(String path) {
    this.path = path;
    return this;
  }

  
  @ApiModelProperty(example = "https://www.caib.es/docs/guia-usuari.pdf", required = true, value = "Ruta o URL del manual")
  @JsonProperty(required = true, value = "path")
  @NotNull  @Size(min=1,max=255)public String getPath() {
    return path;
  }

  @JsonProperty(required = true, value = "path")
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

