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
 * Descripció d&#39;una entitat disponible
 **/
@ApiModel(description = "Descripció d'una entitat disponible")
@JsonTypeName("EntitatDesc")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class EntitatDesc   {
  private String codi;
  private String nom;
  private String codiDir3;
  private String cif;

  public EntitatDesc() {
  }

  @JsonCreator
  public EntitatDesc(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "nom") String nom,
    @JsonProperty(required = true, value = "codiDir3") String codiDir3,
    @JsonProperty(required = true, value = "cif") String cif
  ) {
    this.codi = codi;
    this.nom = nom;
    this.codiDir3 = codiDir3;
    this.cif = cif;
  }

  /**
   * Codi de la entitat
   **/
  public EntitatDesc codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(example = "GOIB", required = true, value = "Codi de la entitat")
  @JsonProperty(required = true, value = "codi")
  @NotNull  @Size(min=1,max=32)public String getCodi() {
    return codi;
  }

  @JsonProperty(required = true, value = "codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   * Nom de la entitat
   **/
  public EntitatDesc nom(String nom) {
    this.nom = nom;
    return this;
  }

  
  @ApiModelProperty(example = "Govern de les Illes Balears", required = true, value = "Nom de la entitat")
  @JsonProperty(required = true, value = "nom")
  @NotNull  @Size(min=1,max=255)public String getNom() {
    return nom;
  }

  @JsonProperty(required = true, value = "nom")
  public void setNom(String nom) {
    this.nom = nom;
  }

  /**
   * Codi Dir3 de la entitat
   **/
  public EntitatDesc codiDir3(String codiDir3) {
    this.codiDir3 = codiDir3;
    return this;
  }

  
  @ApiModelProperty(example = "A04003003", required = true, value = "Codi Dir3 de la entitat")
  @JsonProperty(required = true, value = "codiDir3")
  @NotNull  @Size(min=1,max=64)public String getCodiDir3() {
    return codiDir3;
  }

  @JsonProperty(required = true, value = "codiDir3")
  public void setCodiDir3(String codiDir3) {
    this.codiDir3 = codiDir3;
  }

  /**
   * CIF de la entitat
   **/
  public EntitatDesc cif(String cif) {
    this.cif = cif;
    return this;
  }

  
  @ApiModelProperty(example = "Entitat", required = true, value = "CIF de la entitat")
  @JsonProperty(required = true, value = "cif")
  @NotNull  @Size(min=1,max=16)public String getCif() {
    return cif;
  }

  @JsonProperty(required = true, value = "cif")
  public void setCif(String cif) {
    this.cif = cif;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EntitatDesc entitatDesc = (EntitatDesc) o;
    return Objects.equals(this.codi, entitatDesc.codi) &&
        Objects.equals(this.nom, entitatDesc.nom) &&
        Objects.equals(this.codiDir3, entitatDesc.codiDir3) &&
        Objects.equals(this.cif, entitatDesc.cif);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, nom, codiDir3, cif);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EntitatDesc {\n");
    
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
    sb.append("    codiDir3: ").append(toIndentedString(codiDir3)).append("\n");
    sb.append("    cif: ").append(toIndentedString(cif)).append("\n");
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

