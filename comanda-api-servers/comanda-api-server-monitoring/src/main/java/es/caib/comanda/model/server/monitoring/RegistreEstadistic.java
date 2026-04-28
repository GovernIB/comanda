package es.caib.comanda.model.server.monitoring;

import es.caib.comanda.model.server.monitoring.Dimensio;
import es.caib.comanda.model.server.monitoring.Fet;
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
 * Registre d&#39;estadística amb dimensions i fets associats
 **/
@ApiModel(description = "Registre d'estadística amb dimensions i fets associats")
@JsonTypeName("RegistreEstadistic")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class RegistreEstadistic   {
  private @Valid List<@Valid Dimensio> dimensions = new ArrayList<>();
  private @Valid List<@Valid Fet> fets = new ArrayList<>();

  public RegistreEstadistic() {
  }

  /**
   * Dimensions que qualifiquen el registre (p. ex. àrea, tipus, canal)
   **/
  public RegistreEstadistic dimensions(List<@Valid Dimensio> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  
  @ApiModelProperty(example = "[{\"codi\":\"ENTITAT\",\"valor\":\"CAIB\"},{\"codi\":\"ORGAN\",\"valor\":\"A03002345\"},{\"codi\":\"USUARI\",\"valor\":\"u012345\"}]", value = "Dimensions que qualifiquen el registre (p. ex. àrea, tipus, canal)")
  @JsonProperty("dimensions")
  @Valid public List<@Valid Dimensio> getDimensions() {
    return dimensions;
  }

  @JsonProperty("dimensions")
  public void setDimensions(List<@Valid Dimensio> dimensions) {
    this.dimensions = dimensions;
  }

  public RegistreEstadistic addDimensionsItem(Dimensio dimensionsItem) {
    if (this.dimensions == null) {
      this.dimensions = new ArrayList<>();
    }

    this.dimensions.add(dimensionsItem);
    return this;
  }

  public RegistreEstadistic removeDimensionsItem(Dimensio dimensionsItem) {
    if (dimensionsItem != null && this.dimensions != null) {
      this.dimensions.remove(dimensionsItem);
    }

    return this;
  }
  /**
   * Fets o mesures quantitatives associades al registre
   **/
  public RegistreEstadistic fets(List<@Valid Fet> fets) {
    this.fets = fets;
    return this;
  }

  
  @ApiModelProperty(example = "[{\"codi\":\"EXP_CREATS\",\"valor\":10},{\"codi\":\"EXP_TANCATS\",\"valor\":2}]", value = "Fets o mesures quantitatives associades al registre")
  @JsonProperty("fets")
  @Valid public List<@Valid Fet> getFets() {
    return fets;
  }

  @JsonProperty("fets")
  public void setFets(List<@Valid Fet> fets) {
    this.fets = fets;
  }

  public RegistreEstadistic addFetsItem(Fet fetsItem) {
    if (this.fets == null) {
      this.fets = new ArrayList<>();
    }

    this.fets.add(fetsItem);
    return this;
  }

  public RegistreEstadistic removeFetsItem(Fet fetsItem) {
    if (fetsItem != null && this.fets != null) {
      this.fets.remove(fetsItem);
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
    RegistreEstadistic registreEstadistic = (RegistreEstadistic) o;
    return Objects.equals(this.dimensions, registreEstadistic.dimensions) &&
        Objects.equals(this.fets, registreEstadistic.fets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dimensions, fets);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RegistreEstadistic {\n");
    
    sb.append("    dimensions: ").append(toIndentedString(dimensions)).append("\n");
    sb.append("    fets: ").append(toIndentedString(fets)).append("\n");
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

