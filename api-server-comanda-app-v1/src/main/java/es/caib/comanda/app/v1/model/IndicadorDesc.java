package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import es.caib.comanda.app.v1.model.Format;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Descripció d&#39;un indicador/mesura disponible
 */

@Schema(name = "IndicadorDesc", description = "Descripció d'un indicador/mesura disponible")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class IndicadorDesc {

  private String codi;

  private String nom;

  private String descripcio;

  private Format format;

  public IndicadorDesc() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public IndicadorDesc(String codi, String nom) {
    this.codi = codi;
    this.nom = nom;
  }

  public IndicadorDesc codi(String codi) {
    this.codi = codi;
    return this;
  }

  /**
   * Codi de l'indicador
   * @return codi
   */
  @NotNull @Size(min = 1, max = 32) 
  @Schema(name = "codi", example = "visites", description = "Codi de l'indicador", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  public void setCodi(String codi) {
    this.codi = codi;
  }

  public IndicadorDesc nom(String nom) {
    this.nom = nom;
    return this;
  }

  /**
   * Nom de l'indicador
   * @return nom
   */
  @NotNull @Size(min = 1, max = 64) 
  @Schema(name = "nom", example = "Nombre de visites", description = "Nom de l'indicador", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nom")
  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public IndicadorDesc descripcio(String descripcio) {
    this.descripcio = descripcio;
    return this;
  }

  /**
   * Descripció funcional de l'indicador
   * @return descripcio
   */
  
  @Schema(name = "descripcio", example = "Total de visites registrades per període", description = "Descripció funcional de l'indicador", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("descripcio")
  public String getDescripcio() {
    return descripcio;
  }

  public void setDescripcio(String descripcio) {
    this.descripcio = descripcio;
  }

  public IndicadorDesc format(Format format) {
    this.format = format;
    return this;
  }

  /**
   * Get format
   * @return format
   */
  @Valid 
  @Schema(name = "format", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("format")
  public Format getFormat() {
    return format;
  }

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

