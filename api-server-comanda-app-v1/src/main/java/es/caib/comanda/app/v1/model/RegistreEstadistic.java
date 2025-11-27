package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import es.caib.comanda.app.v1.model.Dimensio;
import es.caib.comanda.app.v1.model.Fet;
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
 * Registre d&#39;estadística amb dimensions i fets associats
 */

@Schema(name = "RegistreEstadistic", description = "Registre d'estadística amb dimensions i fets associats")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class RegistreEstadistic {

  @Valid
  private List<@Valid Dimensio> dimensions = new ArrayList<>();

  @Valid
  private List<@Valid Fet> fets = new ArrayList<>();

  public RegistreEstadistic dimensions(List<@Valid Dimensio> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  public RegistreEstadistic addDimensionsItem(Dimensio dimensionsItem) {
    if (this.dimensions == null) {
      this.dimensions = new ArrayList<>();
    }
    this.dimensions.add(dimensionsItem);
    return this;
  }

  /**
   * Dimensions que qualifiquen el registre (p. ex. àrea, tipus, canal)
   * @return dimensions
   */
  @Valid 
  @Schema(name = "dimensions", description = "Dimensions que qualifiquen el registre (p. ex. àrea, tipus, canal)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dimensions")
  public List<@Valid Dimensio> getDimensions() {
    return dimensions;
  }

  public void setDimensions(List<@Valid Dimensio> dimensions) {
    this.dimensions = dimensions;
  }

  public RegistreEstadistic fets(List<@Valid Fet> fets) {
    this.fets = fets;
    return this;
  }

  public RegistreEstadistic addFetsItem(Fet fetsItem) {
    if (this.fets == null) {
      this.fets = new ArrayList<>();
    }
    this.fets.add(fetsItem);
    return this;
  }

  /**
   * Fets o mesures quantitatives associades al registre
   * @return fets
   */
  @Valid 
  @Schema(name = "fets", description = "Fets o mesures quantitatives associades al registre", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("fets")
  public List<@Valid Fet> getFets() {
    return fets;
  }

  public void setFets(List<@Valid Fet> fets) {
    this.fets = fets;
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

