package es.caib.comanda.model.server.v1.monitoring;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.File;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;



@JsonTypeName("FitxerContingut")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class FitxerContingut   {
  private String nom;
  private Long mida;
  private String mimeType;
  private String dataCreacio;
  private String dataModificacio;
  private byte[] contingut;

  public FitxerContingut() {
  }

  /**
   * Nom del fitxer
   **/
  public FitxerContingut nom(String nom) {
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
  public FitxerContingut mida(Long mida) {
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
  public FitxerContingut mimeType(String mimeType) {
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
  public FitxerContingut dataCreacio(String dataCreacio) {
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
  public FitxerContingut dataModificacio(String dataModificacio) {
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

  /**
   * Contingut del fitxer en format binari comprimit en zip
   **/
  public FitxerContingut contingut(byte[] contingut) {
    this.contingut = contingut;
    return this;
  }

  
  @ApiModelProperty(value = "Contingut del fitxer en format binari comprimit en zip")
  @JsonProperty("contingut")
  public byte[] getContingut() {
    return contingut;
  }

  @JsonProperty("contingut")
  public void setContingut(byte[] contingut) {
    this.contingut = contingut;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FitxerContingut fitxerContingut = (FitxerContingut) o;
    return Objects.equals(this.nom, fitxerContingut.nom) &&
        Objects.equals(this.mida, fitxerContingut.mida) &&
        Objects.equals(this.mimeType, fitxerContingut.mimeType) &&
        Objects.equals(this.dataCreacio, fitxerContingut.dataCreacio) &&
        Objects.equals(this.dataModificacio, fitxerContingut.dataModificacio) &&
        Objects.equals(this.contingut, fitxerContingut.contingut);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nom, mida, mimeType, dataCreacio, dataModificacio, contingut);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FitxerContingut {\n");
    
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
    sb.append("    mida: ").append(toIndentedString(mida)).append("\n");
    sb.append("    mimeType: ").append(toIndentedString(mimeType)).append("\n");
    sb.append("    dataCreacio: ").append(toIndentedString(dataCreacio)).append("\n");
    sb.append("    dataModificacio: ").append(toIndentedString(dataModificacio)).append("\n");
    sb.append("    contingut: ").append(toIndentedString(contingut)).append("\n");
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

