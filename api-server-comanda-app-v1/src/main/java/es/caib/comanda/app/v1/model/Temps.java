package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import es.caib.comanda.app.v1.model.DiaSetmanaEnum;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Desglossament temporal associat als registres d&#39;estadística
 */

@Schema(name = "Temps", description = "Desglossament temporal associat als registres d'estadística")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class Temps {

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime data;

  private Integer anualitat;

  private Integer trimestre;

  private Integer mes;

  private Integer setmana;

  private DiaSetmanaEnum diaSetmana;

  private Integer dia;

  public Temps() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Temps(OffsetDateTime data) {
    this.data = data;
  }

  public Temps data(OffsetDateTime data) {
    this.data = data;
    return this;
  }

  /**
   * Instant temporal de referència
   * @return data
   */
  @NotNull @Valid 
  @Schema(name = "data", description = "Instant temporal de referència", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("data")
  public OffsetDateTime getData() {
    return data;
  }

  public void setData(OffsetDateTime data) {
    this.data = data;
  }

  public Temps anualitat(Integer anualitat) {
    this.anualitat = anualitat;
    return this;
  }

  /**
   * Any corresponent a la data
   * @return anualitat
   */
  
  @Schema(name = "anualitat", example = "2025", description = "Any corresponent a la data", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("anualitat")
  public Integer getAnualitat() {
    return anualitat;
  }

  public void setAnualitat(Integer anualitat) {
    this.anualitat = anualitat;
  }

  public Temps trimestre(Integer trimestre) {
    this.trimestre = trimestre;
    return this;
  }

  /**
   * Trimestre (1-4) derivat de la data
   * @return trimestre
   */
  
  @Schema(name = "trimestre", example = "4", description = "Trimestre (1-4) derivat de la data", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("trimestre")
  public Integer getTrimestre() {
    return trimestre;
  }

  public void setTrimestre(Integer trimestre) {
    this.trimestre = trimestre;
  }

  public Temps mes(Integer mes) {
    this.mes = mes;
    return this;
  }

  /**
   * Mes (1-12) derivat de la data
   * @return mes
   */
  
  @Schema(name = "mes", example = "11", description = "Mes (1-12) derivat de la data", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("mes")
  public Integer getMes() {
    return mes;
  }

  public void setMes(Integer mes) {
    this.mes = mes;
  }

  public Temps setmana(Integer setmana) {
    this.setmana = setmana;
    return this;
  }

  /**
   * Setmana de l'any derivada de la data
   * @return setmana
   */
  
  @Schema(name = "setmana", example = "47", description = "Setmana de l'any derivada de la data", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("setmana")
  public Integer getSetmana() {
    return setmana;
  }

  public void setSetmana(Integer setmana) {
    this.setmana = setmana;
  }

  public Temps diaSetmana(DiaSetmanaEnum diaSetmana) {
    this.diaSetmana = diaSetmana;
    return this;
  }

  /**
   * Get diaSetmana
   * @return diaSetmana
   */
  @Valid 
  @Schema(name = "diaSetmana", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("diaSetmana")
  public DiaSetmanaEnum getDiaSetmana() {
    return diaSetmana;
  }

  public void setDiaSetmana(DiaSetmanaEnum diaSetmana) {
    this.diaSetmana = diaSetmana;
  }

  public Temps dia(Integer dia) {
    this.dia = dia;
    return this;
  }

  /**
   * Dia del mes
   * @return dia
   */
  
  @Schema(name = "dia", example = "25", description = "Dia del mes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dia")
  public Integer getDia() {
    return dia;
  }

  public void setDia(Integer dia) {
    this.dia = dia;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Temps temps = (Temps) o;
    return Objects.equals(this.data, temps.data) &&
        Objects.equals(this.anualitat, temps.anualitat) &&
        Objects.equals(this.trimestre, temps.trimestre) &&
        Objects.equals(this.mes, temps.mes) &&
        Objects.equals(this.setmana, temps.setmana) &&
        Objects.equals(this.diaSetmana, temps.diaSetmana) &&
        Objects.equals(this.dia, temps.dia);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, anualitat, trimestre, mes, setmana, diaSetmana, dia);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Temps {\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    anualitat: ").append(toIndentedString(anualitat)).append("\n");
    sb.append("    trimestre: ").append(toIndentedString(trimestre)).append("\n");
    sb.append("    mes: ").append(toIndentedString(mes)).append("\n");
    sb.append("    setmana: ").append(toIndentedString(setmana)).append("\n");
    sb.append("    diaSetmana: ").append(toIndentedString(diaSetmana)).append("\n");
    sb.append("    dia: ").append(toIndentedString(dia)).append("\n");
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

