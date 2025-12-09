package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import es.caib.comanda.app.v1.model.Manual;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Context o &#39;namespace&#39; funcional exposat per l&#39;aplicació
 */

@Schema(name = "ContextInfo", description = "Context o 'namespace' funcional exposat per l'aplicació")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class ContextInfo {

  private String codi;

  private String nom;

  private String path;

  @Valid
  private List<@Valid Manual> manuals = new ArrayList<>();

  private String api;

  public ContextInfo() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ContextInfo(String codi, String nom, String path) {
    this.codi = codi;
    this.nom = nom;
    this.path = path;
  }

  public ContextInfo codi(String codi) {
    this.codi = codi;
    return this;
  }

  /**
   * Codi del context
   * @return codi
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "codi", example = "public", description = "Codi del context", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  public void setCodi(String codi) {
    this.codi = codi;
  }

  public ContextInfo nom(String nom) {
    this.nom = nom;
    return this;
  }

  /**
   * Nom descriptiu del context
   * @return nom
   */
  @NotNull @Size(min = 1, max = 255) 
  @Schema(name = "nom", example = "Context públic", description = "Nom descriptiu del context", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nom")
  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public ContextInfo path(String path) {
    this.path = path;
    return this;
  }

  /**
   * Path base del context
   * @return path
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "path", example = "/app/public", description = "Path base del context", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("path")
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public ContextInfo manuals(List<@Valid Manual> manuals) {
    this.manuals = manuals;
    return this;
  }

  public ContextInfo addManualsItem(Manual manualsItem) {
    if (this.manuals == null) {
      this.manuals = new ArrayList<>();
    }
    this.manuals.add(manualsItem);
    return this;
  }

  /**
   * Llista de manuals associats al context
   * @return manuals
   */
  @Valid 
  @Schema(name = "manuals", description = "Llista de manuals associats al context", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("manuals")
  public List<@Valid Manual> getManuals() {
    return manuals;
  }

  public void setManuals(List<@Valid Manual> manuals) {
    this.manuals = manuals;
  }

  public ContextInfo api(String api) {
    this.api = api;
    return this;
  }

  /**
   * URL o especificació OpenAPI del context, si està disponible
   * @return api
   */
  
  @Schema(name = "api", example = "https://dev.caib.es/app/public/openapi.json", description = "URL o especificació OpenAPI del context, si està disponible", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("api")
  public String getApi() {
    return api;
  }

  public void setApi(String api) {
    this.api = api;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ContextInfo contextInfo = (ContextInfo) o;
    return Objects.equals(this.codi, contextInfo.codi) &&
        Objects.equals(this.nom, contextInfo.nom) &&
        Objects.equals(this.path, contextInfo.path) &&
        Objects.equals(this.manuals, contextInfo.manuals) &&
        Objects.equals(this.api, contextInfo.api);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, nom, path, manuals, api);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ContextInfo {\n");
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    manuals: ").append(toIndentedString(manuals)).append("\n");
    sb.append("    api: ").append(toIndentedString(api)).append("\n");
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

