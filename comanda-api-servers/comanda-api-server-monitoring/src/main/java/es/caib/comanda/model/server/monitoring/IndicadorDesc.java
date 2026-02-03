package es.caib.comanda.model.server.monitoring;

import es.caib.comanda.model.server.monitoring.Format;
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
 * Descripció d&#39;un indicador/mesura disponible
 **/
@ApiModel(description = "Descripció d'un indicador/mesura disponible")
@JsonTypeName("IndicadorDesc")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class IndicadorDesc   {
  private String codi;
  private String nom;
  private String descripcio;
  private Format format;

  public IndicadorDesc() {
  }

  @JsonCreator
  public IndicadorDesc(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "nom") String nom
  ) {
    this.codi = codi;
    this.nom = nom;
  }

  /**
   * Codi de l&#39;indicador
   **/
  public IndicadorDesc codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(example = "NUM_VIS", required = true, value = "Codi de l'indicador")
  @JsonProperty(required = true, value = "codi")
  @NotNull  @Size(min=1,max=32)public String getCodi() {
    return codi;
  }

  @JsonProperty(required = true, value = "codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   * Nom de l&#39;indicador
   **/
  public IndicadorDesc nom(String nom) {
    this.nom = nom;
    return this;
  }

  
  @ApiModelProperty(example = "Nombre de visites", required = true, value = "Nom de l'indicador")
  @JsonProperty(required = true, value = "nom")
  @NotNull  @Size(min=1,max=64)public String getNom() {
    return nom;
  }

  @JsonProperty(required = true, value = "nom")
  public void setNom(String nom) {
    this.nom = nom;
  }

  /**
   * Descripció funcional de l&#39;indicador
   **/
  public IndicadorDesc descripcio(String descripcio) {
    this.descripcio = descripcio;
    return this;
  }

  
  @ApiModelProperty(example = "Total de visites registrades per període", value = "Descripció funcional de l'indicador")
  @JsonProperty("descripcio")
  public String getDescripcio() {
    return descripcio;
  }

  @JsonProperty("descripcio")
  public void setDescripcio(String descripcio) {
    this.descripcio = descripcio;
  }

  /**
   **/
  public IndicadorDesc format(Format format) {
    this.format = format;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("format")
  public Format getFormat() {
    return format;
  }

  @JsonProperty("format")
  public void setFormat(Format format) {
    this.format = format;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IndicadorDesc indicadorDesc = (IndicadorDesc) o;
    return Objects.equals(this.codi, indicadorDesc.codi) &&
        Objects.equals(this.nom, indicadorDesc.nom) &&
        Objects.equals(this.descripcio, indicadorDesc.descripcio) &&
        Objects.equals(this.format, indicadorDesc.format);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, nom, descripcio, format);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IndicadorDesc {\n");
    
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
    sb.append("    descripcio: ").append(toIndentedString(descripcio)).append("\n");
    sb.append("    format: ").append(toIndentedString(format)).append("\n");
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

