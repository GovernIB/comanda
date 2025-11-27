package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import es.caib.comanda.app.v1.model.DimensioDesc;
import es.caib.comanda.app.v1.model.IndicadorDesc;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Catàleg de dimensions i indicadors d&#39;estadística disponibles per a una APP
 */

@Schema(name = "EstadistiquesInfo", description = "Catàleg de dimensions i indicadors d'estadística disponibles per a una APP")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class EstadistiquesInfo {

  private String codi;

  private String versio;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime data;

  @Valid
  private List<@Valid DimensioDesc> dimensions = new ArrayList<>();

  @Valid
  private List<@Valid IndicadorDesc> indicadors = new ArrayList<>();

  public EstadistiquesInfo() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public EstadistiquesInfo(String codi, List<@Valid DimensioDesc> dimensions, List<@Valid IndicadorDesc> indicadors) {
    this.codi = codi;
    this.dimensions = dimensions;
    this.indicadors = indicadors;
  }

  public EstadistiquesInfo codi(String codi) {
    this.codi = codi;
    return this;
  }

  /**
   * Codi identificador de l'aplicació
   * @return codi
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "codi", example = "NOTIB", description = "Codi identificador de l'aplicació", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  public void setCodi(String codi) {
    this.codi = codi;
  }

  public EstadistiquesInfo versio(String versio) {
    this.versio = versio;
    return this;
  }

  /**
   * Versió de l'aplicació
   * @return versio
   */
  
  @Schema(name = "versio", example = "1.4.3", description = "Versió de l'aplicació", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("versio")
  public String getVersio() {
    return versio;
  }

  public void setVersio(String versio) {
    this.versio = versio;
  }

  public EstadistiquesInfo data(OffsetDateTime data) {
    this.data = data;
    return this;
  }

  /**
   * Data de generació de la informació
   * @return data
   */
  @Valid 
  @Schema(name = "data", description = "Data de generació de la informació", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("data")
  public OffsetDateTime getData() {
    return data;
  }

  public void setData(OffsetDateTime data) {
    this.data = data;
  }

  public EstadistiquesInfo dimensions(List<@Valid DimensioDesc> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  public EstadistiquesInfo addDimensionsItem(DimensioDesc dimensionsItem) {
    if (this.dimensions == null) {
      this.dimensions = new ArrayList<>();
    }
    this.dimensions.add(dimensionsItem);
    return this;
  }

  /**
   * Dimensions estadístiques disponibles
   * @return dimensions
   */
  @NotNull @Valid 
  @Schema(name = "dimensions", description = "Dimensions estadístiques disponibles", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dimensions")
  public List<@Valid DimensioDesc> getDimensions() {
    return dimensions;
  }

  public void setDimensions(List<@Valid DimensioDesc> dimensions) {
    this.dimensions = dimensions;
  }

  public EstadistiquesInfo indicadors(List<@Valid IndicadorDesc> indicadors) {
    this.indicadors = indicadors;
    return this;
  }

  public EstadistiquesInfo addIndicadorsItem(IndicadorDesc indicadorsItem) {
    if (this.indicadors == null) {
      this.indicadors = new ArrayList<>();
    }
    this.indicadors.add(indicadorsItem);
    return this;
  }

  /**
   * Indicadors estadístics disponibles
   * @return indicadors
   */
  @NotNull @Valid 
  @Schema(name = "indicadors", description = "Indicadors estadístics disponibles", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("indicadors")
  public List<@Valid IndicadorDesc> getIndicadors() {
    return indicadors;
  }

  public void setIndicadors(List<@Valid IndicadorDesc> indicadors) {
    this.indicadors = indicadors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EstadistiquesInfo estadistiquesInfo = (EstadistiquesInfo) o;
    return Objects.equals(this.codi, estadistiquesInfo.codi) &&
        Objects.equals(this.versio, estadistiquesInfo.versio) &&
        Objects.equals(this.data, estadistiquesInfo.data) &&
        Objects.equals(this.dimensions, estadistiquesInfo.dimensions) &&
        Objects.equals(this.indicadors, estadistiquesInfo.indicadors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, versio, data, dimensions, indicadors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EstadistiquesInfo {\n");
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    versio: ").append(toIndentedString(versio)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    dimensions: ").append(toIndentedString(dimensions)).append("\n");
    sb.append("    indicadors: ").append(toIndentedString(indicadors)).append("\n");
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

