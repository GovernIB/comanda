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
 * Informació d&#39;un subsistema intern de l&#39;aplicació
 **/
@ApiModel(description = "Informació d'un subsistema intern de l'aplicació")
@JsonTypeName("SubsistemaInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class SubsistemaInfo   {
  private String codi;
  private String nom;

  public SubsistemaInfo() {
  }

  @JsonCreator
  public SubsistemaInfo(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "nom") String nom
  ) {
    this.codi = codi;
    this.nom = nom;
  }

  /**
   * Codi del subsistema
   **/
  public SubsistemaInfo codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(example = "ALTA_REST", required = true, value = "Codi del subsistema")
  @JsonProperty(required = true, value = "codi")
  @NotNull  @Size(min=1,max=64)public String getCodi() {
    return codi;
  }

  @JsonProperty(required = true, value = "codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   * Nom del subsistema
   **/
  public SubsistemaInfo nom(String nom) {
    this.nom = nom;
    return this;
  }

  
  @ApiModelProperty(example = "Alta de elements via REST", required = true, value = "Nom del subsistema")
  @JsonProperty(required = true, value = "nom")
  @NotNull  @Size(min=1,max=255)public String getNom() {
    return nom;
  }

  @JsonProperty(required = true, value = "nom")
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

