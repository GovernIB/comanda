package es.caib.comanda.model.server.monitoring;

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
 * Descripció d&#39;una dimensió disponible (les dimenstions són els camps pels quals es pot filtrar la informació estadística
 **/
@ApiModel(description = "Descripció d'una dimensió disponible (les dimenstions són els camps pels quals es pot filtrar la informació estadística")
@JsonTypeName("DimensioDesc")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class DimensioDesc   {
  private String codi;
  private String nom;
  private String descripcio;
  private @Valid List<String> valors = new ArrayList<>();

  public DimensioDesc() {
  }

  @JsonCreator
  public DimensioDesc(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "nom") String nom
  ) {
    this.codi = codi;
    this.nom = nom;
  }

  /**
   * Codi de la dimensió
   **/
  public DimensioDesc codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(example = "ENT", required = true, value = "Codi de la dimensió")
  @JsonProperty(required = true, value = "codi")
  @NotNull  @Size(min=1,max=32)public String getCodi() {
    return codi;
  }

  @JsonProperty(required = true, value = "codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   * Nom de la dimensió
   **/
  public DimensioDesc nom(String nom) {
    this.nom = nom;
    return this;
  }

  
  @ApiModelProperty(example = "Entitat", required = true, value = "Nom de la dimensió")
  @JsonProperty(required = true, value = "nom")
  @NotNull  @Size(min=1,max=64)public String getNom() {
    return nom;
  }

  @JsonProperty(required = true, value = "nom")
  public void setNom(String nom) {
    this.nom = nom;
  }

  /**
   * Descripció funcional de la dimensió
   **/
  public DimensioDesc descripcio(String descripcio) {
    this.descripcio = descripcio;
    return this;
  }

  
  @ApiModelProperty(example = "Entitat de la que s'ha generat la informació estadística", value = "Descripció funcional de la dimensió")
  @JsonProperty("descripcio")
   @Size(min=0,max=1024)public String getDescripcio() {
    return descripcio;
  }

  @JsonProperty("descripcio")
  public void setDescripcio(String descripcio) {
    this.descripcio = descripcio;
  }

  /**
   * Llista dels possibles valors que pot tenir assignada la dimensió
   **/
  public DimensioDesc valors(List<String> valors) {
    this.valors = valors;
    return this;
  }

  
  @ApiModelProperty(example = "[CAIB, TEST]", value = "Llista dels possibles valors que pot tenir assignada la dimensió")
  @JsonProperty("valors")
  public List<String> getValors() {
    return valors;
  }

  @JsonProperty("valors")
  public void setValors(List<String> valors) {
    this.valors = valors;
  }

  public DimensioDesc addValorsItem(String valorsItem) {
    if (this.valors == null) {
      this.valors = new ArrayList<>();
    }

    this.valors.add(valorsItem);
    return this;
  }

  public DimensioDesc removeValorsItem(String valorsItem) {
    if (valorsItem != null && this.valors != null) {
      this.valors.remove(valorsItem);
    }

    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DimensioDesc dimensioDesc = (DimensioDesc) o;
    return Objects.equals(this.codi, dimensioDesc.codi) &&
        Objects.equals(this.nom, dimensioDesc.nom) &&
        Objects.equals(this.descripcio, dimensioDesc.descripcio) &&
        Objects.equals(this.valors, dimensioDesc.valors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, nom, descripcio, valors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DimensioDesc {\n");
    
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
    sb.append("    descripcio: ").append(toIndentedString(descripcio)).append("\n");
    sb.append("    valors: ").append(toIndentedString(valors)).append("\n");
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

