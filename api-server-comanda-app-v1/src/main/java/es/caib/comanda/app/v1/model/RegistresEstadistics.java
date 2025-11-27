package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import es.caib.comanda.app.v1.model.RegistreEstadistic;
import es.caib.comanda.app.v1.model.Temps;
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
 * Registres estadístics d&#39;un instant de temps amb les seves mesures
 */

@Schema(name = "RegistresEstadistics", description = "Registres estadístics d'un instant de temps amb les seves mesures")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class RegistresEstadistics {

  private Temps temps;

  @Valid
  private List<@Valid RegistreEstadistic> fets = new ArrayList<>();

  public RegistresEstadistics() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public RegistresEstadistics(Temps temps) {
    this.temps = temps;
  }

  public RegistresEstadistics temps(Temps temps) {
    this.temps = temps;
    return this;
  }

  /**
   * Get temps
   * @return temps
   */
  @NotNull @Valid 
  @Schema(name = "temps", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("temps")
  public Temps getTemps() {
    return temps;
  }

  public void setTemps(Temps temps) {
    this.temps = temps;
  }

  public RegistresEstadistics fets(List<@Valid RegistreEstadistic> fets) {
    this.fets = fets;
    return this;
  }

  public RegistresEstadistics addFetsItem(RegistreEstadistic fetsItem) {
    if (this.fets == null) {
      this.fets = new ArrayList<>();
    }
    this.fets.add(fetsItem);
    return this;
  }

  /**
   * Llista de registres o fets estadístics recollits en l'instant indicat
   * @return fets
   */
  @Valid 
  @Schema(name = "fets", description = "Llista de registres o fets estadístics recollits en l'instant indicat", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("fets")
  public List<@Valid RegistreEstadistic> getFets() {
    return fets;
  }

  public void setFets(List<@Valid RegistreEstadistic> fets) {
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
    RegistresEstadistics registresEstadistics = (RegistresEstadistics) o;
    return Objects.equals(this.temps, registresEstadistics.temps) &&
        Objects.equals(this.fets, registresEstadistics.fets);
  }

  @Override
  public int hashCode() {
    return Objects.hash(temps, fets);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RegistresEstadistics {\n");
    sb.append("    temps: ").append(toIndentedString(temps)).append("\n");
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

