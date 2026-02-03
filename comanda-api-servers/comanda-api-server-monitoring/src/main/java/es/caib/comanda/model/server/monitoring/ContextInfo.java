package es.caib.comanda.model.server.monitoring;

import es.caib.comanda.model.server.monitoring.Manual;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Context o &#39;namespace&#39; funcional exposat per l&#39;aplicació
 **/
@ApiModel(description = "Context o 'namespace' funcional exposat per l'aplicació")
@JsonTypeName("ContextInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class ContextInfo   {
  private String codi;
  private String nom;
  private String path;
  private @Valid List<@Valid Manual> manuals = new ArrayList<>();
  private String api;

  public ContextInfo() {
  }

  @JsonCreator
  public ContextInfo(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "nom") String nom,
    @JsonProperty(required = true, value = "path") String path
  ) {
    this.codi = codi;
    this.nom = nom;
    this.path = path;
  }

  /**
   * Codi del context
   **/
  public ContextInfo codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(example = "API_INT", required = true, value = "Codi del context")
  @JsonProperty(required = true, value = "codi")
  @NotNull  @Size(min=1,max=64)public String getCodi() {
    return codi;
  }

  @JsonProperty(required = true, value = "codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   * Nom descriptiu del context
   **/
  public ContextInfo nom(String nom) {
    this.nom = nom;
    return this;
  }

  
  @ApiModelProperty(example = "Api interna", required = true, value = "Nom descriptiu del context")
  @JsonProperty(required = true, value = "nom")
  @NotNull  @Size(min=1,max=255)public String getNom() {
    return nom;
  }

  @JsonProperty(required = true, value = "nom")
  public void setNom(String nom) {
    this.nom = nom;
  }

  /**
   * Path base del context
   **/
  public ContextInfo path(String path) {
    this.path = path;
    return this;
  }

  
  @ApiModelProperty(example = "/appapi/interna", required = true, value = "Path base del context")
  @JsonProperty(required = true, value = "path")
  @NotNull  @Size(min=1,max=255)public String getPath() {
    return path;
  }

  @JsonProperty(required = true, value = "path")
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Llista de manuals associats al context
   **/
  public ContextInfo manuals(List<@Valid Manual> manuals) {
    this.manuals = manuals;
    return this;
  }

  
  @ApiModelProperty(value = "Llista de manuals associats al context")
  @JsonProperty("manuals")
  @Valid public List<@Valid Manual> getManuals() {
    return manuals;
  }

  @JsonProperty("manuals")
  public void setManuals(List<@Valid Manual> manuals) {
    this.manuals = manuals;
  }

  public ContextInfo addManualsItem(Manual manualsItem) {
    if (this.manuals == null) {
      this.manuals = new ArrayList<>();
    }

    this.manuals.add(manualsItem);
    return this;
  }

  public ContextInfo removeManualsItem(Manual manualsItem) {
    if (manualsItem != null && this.manuals != null) {
      this.manuals.remove(manualsItem);
    }

    return this;
  }
  /**
   * URL o especificació OpenAPI del context, si està disponible
   **/
  public ContextInfo api(String api) {
    this.api = api;
    return this;
  }

  
  @ApiModelProperty(example = "https://dev.caib.es/app/internaapi/swagger/index.html", value = "URL o especificació OpenAPI del context, si està disponible")
  @JsonProperty("api")
   @Size(min=0,max=255)public String getApi() {
    return api;
  }

  @JsonProperty("api")
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

