package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * DimensioDesc
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class DimensioDesc {

  private String codi;

  private String nom;

  private String descripcio;

  @Valid
  private List<String> valors = new ArrayList<>();

  public DimensioDesc() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public DimensioDesc(String codi, String nom) {
    this.codi = codi;
    this.nom = nom;
  }

  public DimensioDesc codi(String codi) {
    this.codi = codi;
    return this;
  }

  /**
   * Get codi
   * @return codi
   */
  @NotNull @Size(min = 1, max = 32) 
  @Schema(name = "codi", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  public void setCodi(String codi) {
    this.codi = codi;
  }

  public DimensioDesc nom(String nom) {
    this.nom = nom;
    return this;
  }

  /**
   * Get nom
   * @return nom
   */
  @NotNull @Size(min = 1, max = 64) 
  @Schema(name = "nom", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nom")
  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public DimensioDesc descripcio(String descripcio) {
    this.descripcio = descripcio;
    return this;
  }

  /**
   * Get descripcio
   * @return descripcio
   */
  
  @Schema(name = "descripcio", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("descripcio")
  public String getDescripcio() {
    return descripcio;
  }

  public void setDescripcio(String descripcio) {
    this.descripcio = descripcio;
  }

  public DimensioDesc valors(List<String> valors) {
    this.valors = valors;
    return this;
  }

  public DimensioDesc addValorsItem(String valorsItem) {
    if (this.valors == null) {
      this.valors = new ArrayList<>();
    }
    this.valors.add(valorsItem);
    return this;
  }

  /**
   * Get valors
   * @return valors
   */
  
  @Schema(name = "valors", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("valors")
  public List<String> getValors() {
    return valors;
  }

  public void setValors(List<String> valors) {
    this.valors = valors;
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

