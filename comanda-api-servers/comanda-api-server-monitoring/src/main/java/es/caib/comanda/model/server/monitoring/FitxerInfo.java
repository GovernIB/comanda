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



@JsonTypeName("FitxerInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class FitxerInfo   {
  private String nom;
  private Long mida;
  private String mimeType;
  private String dataCreacio;
  private String dataModificacio;

  public FitxerInfo() {
  }

  /**
   * Nom del fitxer
   **/
  public FitxerInfo nom(String nom) {
    this.nom = nom;
    return this;
  }

  
  @ApiModelProperty(example = "document.pdf", value = "Nom del fitxer")
  @JsonProperty("nom")
  public String getNom() {
    return nom;
  }

  @JsonProperty("nom")
  public void setNom(String nom) {
    this.nom = nom;
  }

  /**
   * Mida del fitxer en bytes
   **/
  public FitxerInfo mida(Long mida) {
    this.mida = mida;
    return this;
  }

  
  @ApiModelProperty(example = "1024", value = "Mida del fitxer en bytes")
  @JsonProperty("mida")
  public Long getMida() {
    return mida;
  }

  @JsonProperty("mida")
  public void setMida(Long mida) {
    this.mida = mida;
  }

  /**
   * Tipus MIME del fitxer
   **/
  public FitxerInfo mimeType(String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  
  @ApiModelProperty(example = "application/pdf", value = "Tipus MIME del fitxer")
  @JsonProperty("mimeType")
  public String getMimeType() {
    return mimeType;
  }

  @JsonProperty("mimeType")
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Data de creació del fitxer en format dd/MM/yyyy HH:mm:ss
   **/
  public FitxerInfo dataCreacio(String dataCreacio) {
    this.dataCreacio = dataCreacio;
    return this;
  }

  
  @ApiModelProperty(example = "15/01/2024 00:00:00", value = "Data de creació del fitxer en format dd/MM/yyyy HH:mm:ss")
  @JsonProperty("dataCreacio")
  public String getDataCreacio() {
    return dataCreacio;
  }

  @JsonProperty("dataCreacio")
  public void setDataCreacio(String dataCreacio) {
    this.dataCreacio = dataCreacio;
  }

  /**
   * Data de modificació del fitxer en format dd/MM/yyyy HH:mm:ss
   **/
  public FitxerInfo dataModificacio(String dataModificacio) {
    this.dataModificacio = dataModificacio;
    return this;
  }

  
  @ApiModelProperty(example = "15/01/2024 23:59:59", value = "Data de modificació del fitxer en format dd/MM/yyyy HH:mm:ss")
  @JsonProperty("dataModificacio")
  public String getDataModificacio() {
    return dataModificacio;
  }

  @JsonProperty("dataModificacio")
  public void setDataModificacio(String dataModificacio) {
    this.dataModificacio = dataModificacio;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FitxerInfo fitxerInfo = (FitxerInfo) o;
    return Objects.equals(this.nom, fitxerInfo.nom) &&
        Objects.equals(this.mida, fitxerInfo.mida) &&
        Objects.equals(this.mimeType, fitxerInfo.mimeType) &&
        Objects.equals(this.dataCreacio, fitxerInfo.dataCreacio) &&
        Objects.equals(this.dataModificacio, fitxerInfo.dataModificacio);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nom, mida, mimeType, dataCreacio, dataModificacio);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FitxerInfo {\n");
    
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
    sb.append("    mida: ").append(toIndentedString(mida)).append("\n");
    sb.append("    mimeType: ").append(toIndentedString(mimeType)).append("\n");
    sb.append("    dataCreacio: ").append(toIndentedString(dataCreacio)).append("\n");
    sb.append("    dataModificacio: ").append(toIndentedString(dataModificacio)).append("\n");
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

