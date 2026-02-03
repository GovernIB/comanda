package es.caib.comanda.model.server.monitoring;

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
 * Informació d&#39;una integració exposada per l&#39;aplicació
 **/
@ApiModel(description = "Informació d'una integració exposada per l'aplicació")
@JsonTypeName("IntegracioInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class IntegracioInfo   {
  private String codi;
  private String nom;

  public IntegracioInfo() {
  }

  @JsonCreator
  public IntegracioInfo(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "nom") String nom
  ) {
    this.codi = codi;
    this.nom = nom;
  }

  /**
   * Codi identificador de la integració
   **/
  public IntegracioInfo codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(example = "REG", required = true, value = "Codi identificador de la integració")
  @JsonProperty(required = true, value = "codi")
  @NotNull  @Size(min=1)public String getCodi() {
    return codi;
  }

  @JsonProperty(required = true, value = "codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   * Nom descriptiu de la integració
   **/
  public IntegracioInfo nom(String nom) {
    this.nom = nom;
    return this;
  }

  
  @ApiModelProperty(example = "Registre", required = true, value = "Nom descriptiu de la integració")
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

