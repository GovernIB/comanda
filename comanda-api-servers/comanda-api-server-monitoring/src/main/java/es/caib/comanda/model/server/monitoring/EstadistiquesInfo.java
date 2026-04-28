package es.caib.comanda.model.server.monitoring;

import es.caib.comanda.model.server.monitoring.DimensioDesc;
import es.caib.comanda.model.server.monitoring.IndicadorDesc;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
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
 * Catàleg de dimensions i indicadors d&#39;estadística disponibles per a una APP
 **/
@ApiModel(description = "Catàleg de dimensions i indicadors d'estadística disponibles per a una APP")
@JsonTypeName("EstadistiquesInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class EstadistiquesInfo   {
  private String codi;
  private String versio;
  private OffsetDateTime data;
  private @Valid List<@Valid DimensioDesc> dimensions = new ArrayList<>();
  private @Valid List<@Valid IndicadorDesc> indicadors = new ArrayList<>();

  public EstadistiquesInfo() {
  }

  @JsonCreator
  public EstadistiquesInfo(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "dimensions") List<@Valid DimensioDesc> dimensions,
    @JsonProperty(required = true, value = "indicadors") List<@Valid IndicadorDesc> indicadors
  ) {
    this.codi = codi;
    this.dimensions = dimensions;
    this.indicadors = indicadors;
  }

  /**
   * Codi identificador de l&#39;aplicació
   **/
  public EstadistiquesInfo codi(String codi) {
    this.codi = codi;
    return this;
  }

  
  @ApiModelProperty(example = "APP", required = true, value = "Codi identificador de l'aplicació")
  @JsonProperty(required = true, value = "codi")
  @NotNull  @Size(min=1)public String getCodi() {
    return codi;
  }

  @JsonProperty(required = true, value = "codi")
  public void setCodi(String codi) {
    this.codi = codi;
  }

  /**
   * Versió de l&#39;aplicació
   **/
  public EstadistiquesInfo versio(String versio) {
    this.versio = versio;
    return this;
  }

  
  @ApiModelProperty(example = "2.1.0", value = "Versió de l'aplicació")
  @JsonProperty("versio")
  public String getVersio() {
    return versio;
  }

  @JsonProperty("versio")
  public void setVersio(String versio) {
    this.versio = versio;
  }

  /**
   **/
  public EstadistiquesInfo data(OffsetDateTime data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("data")
  @Valid public OffsetDateTime getData() {
    return data;
  }

  @JsonProperty("data")
  public void setData(OffsetDateTime data) {
    this.data = data;
  }

  /**
   * Dimensions estadístiques disponibles
   **/
  public EstadistiquesInfo dimensions(List<@Valid DimensioDesc> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Dimensions estadístiques disponibles")
  @JsonProperty(required = true, value = "dimensions")
  @NotNull @Valid public List<@Valid DimensioDesc> getDimensions() {
    return dimensions;
  }

  @JsonProperty(required = true, value = "dimensions")
  public void setDimensions(List<@Valid DimensioDesc> dimensions) {
    this.dimensions = dimensions;
  }

  public EstadistiquesInfo addDimensionsItem(DimensioDesc dimensionsItem) {
    if (this.dimensions == null) {
      this.dimensions = new ArrayList<>();
    }

    this.dimensions.add(dimensionsItem);
    return this;
  }

  public EstadistiquesInfo removeDimensionsItem(DimensioDesc dimensionsItem) {
    if (dimensionsItem != null && this.dimensions != null) {
      this.dimensions.remove(dimensionsItem);
    }

    return this;
  }
  /**
   * Indicadors estadístics disponibles
   **/
  public EstadistiquesInfo indicadors(List<@Valid IndicadorDesc> indicadors) {
    this.indicadors = indicadors;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Indicadors estadístics disponibles")
  @JsonProperty(required = true, value = "indicadors")
  @NotNull @Valid public List<@Valid IndicadorDesc> getIndicadors() {
    return indicadors;
  }

  @JsonProperty(required = true, value = "indicadors")
  public void setIndicadors(List<@Valid IndicadorDesc> indicadors) {
    this.indicadors = indicadors;
  }

  public EstadistiquesInfo addIndicadorsItem(IndicadorDesc indicadorsItem) {
    if (this.indicadors == null) {
      this.indicadors = new ArrayList<>();
    }

    this.indicadors.add(indicadorsItem);
    return this;
  }

  public EstadistiquesInfo removeIndicadorsItem(IndicadorDesc indicadorsItem) {
    if (indicadorsItem != null && this.indicadors != null) {
      this.indicadors.remove(indicadorsItem);
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

