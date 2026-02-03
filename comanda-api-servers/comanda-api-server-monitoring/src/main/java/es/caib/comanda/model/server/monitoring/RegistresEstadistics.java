package es.caib.comanda.model.server.monitoring;

import es.caib.comanda.model.server.monitoring.RegistreEstadistic;
import es.caib.comanda.model.server.monitoring.Temps;
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
 * Registres estadístics d&#39;un instant de temps amb les seves mesures
 **/
@ApiModel(description = "Registres estadístics d'un instant de temps amb les seves mesures")
@JsonTypeName("RegistresEstadistics")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class RegistresEstadistics   {
  private Temps temps;
  private @Valid List<@Valid RegistreEstadistic> fets = new ArrayList<>();

  public RegistresEstadistics() {
  }

  @JsonCreator
  public RegistresEstadistics(
    @JsonProperty(required = true, value = "temps") Temps temps
  ) {
    this.temps = temps;
  }

  /**
   **/
  public RegistresEstadistics temps(Temps temps) {
    this.temps = temps;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "temps")
  @NotNull @Valid public Temps getTemps() {
    return temps;
  }

  @JsonProperty(required = true, value = "temps")
  public void setTemps(Temps temps) {
    this.temps = temps;
  }

  /**
   * Llista de registres o fets estadístics recollits en l&#39;instant indicat
   **/
  public RegistresEstadistics fets(List<@Valid RegistreEstadistic> fets) {
    this.fets = fets;
    return this;
  }

  
  @ApiModelProperty(value = "Llista de registres o fets estadístics recollits en l'instant indicat")
  @JsonProperty("fets")
  @Valid public List<@Valid RegistreEstadistic> getFets() {
    return fets;
  }

  @JsonProperty("fets")
  public void setFets(List<@Valid RegistreEstadistic> fets) {
    this.fets = fets;
  }

  public RegistresEstadistics addFetsItem(RegistreEstadistic fetsItem) {
    if (this.fets == null) {
      this.fets = new ArrayList<>();
    }

    this.fets.add(fetsItem);
    return this;
  }

  public RegistresEstadistics removeFetsItem(RegistreEstadistic fetsItem) {
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

