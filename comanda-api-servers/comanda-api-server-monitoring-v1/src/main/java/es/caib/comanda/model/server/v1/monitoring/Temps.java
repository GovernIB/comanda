package es.caib.comanda.model.server.v1.monitoring;

import es.caib.comanda.model.server.v1.monitoring.DiaSetmanaEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Desglossament temporal associat als registres d&#39;estadística
 **/
@ApiModel(description = "Desglossament temporal associat als registres d'estadística")
@JsonTypeName("Temps")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class Temps   {
  private OffsetDateTime data;
  private Integer anualitat;
  private Integer trimestre;
  private Integer mes;
  private Integer setmana;
  private DiaSetmanaEnum diaSetmana;
  private Integer dia;

  public Temps() {
  }

  @JsonCreator
  public Temps(
    @JsonProperty(required = true, value = "data") OffsetDateTime data
  ) {
    this.data = data;
  }

  /**
   **/
  public Temps data(OffsetDateTime data) {
    this.data = data;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "data")
  @NotNull @Valid public OffsetDateTime getData() {
    return data;
  }

  @JsonProperty(required = true, value = "data")
  public void setData(OffsetDateTime data) {
    this.data = data;
  }

  /**
   * Any corresponent a la data
   **/
  public Temps anualitat(Integer anualitat) {
    this.anualitat = anualitat;
    return this;
  }

  
  @ApiModelProperty(example = "2025", value = "Any corresponent a la data")
  @JsonProperty("anualitat")
  public Integer getAnualitat() {
    return anualitat;
  }

  @JsonProperty("anualitat")
  public void setAnualitat(Integer anualitat) {
    this.anualitat = anualitat;
  }

  /**
   * Trimestre (1-4) derivat de la data
   **/
  public Temps trimestre(Integer trimestre) {
    this.trimestre = trimestre;
    return this;
  }

  
  @ApiModelProperty(example = "4", value = "Trimestre (1-4) derivat de la data")
  @JsonProperty("trimestre")
  public Integer getTrimestre() {
    return trimestre;
  }

  @JsonProperty("trimestre")
  public void setTrimestre(Integer trimestre) {
    this.trimestre = trimestre;
  }

  /**
   * Mes (1-12) derivat de la data
   **/
  public Temps mes(Integer mes) {
    this.mes = mes;
    return this;
  }

  
  @ApiModelProperty(example = "11", value = "Mes (1-12) derivat de la data")
  @JsonProperty("mes")
  public Integer getMes() {
    return mes;
  }

  @JsonProperty("mes")
  public void setMes(Integer mes) {
    this.mes = mes;
  }

  /**
   * Setmana de l&#39;any derivada de la data
   **/
  public Temps setmana(Integer setmana) {
    this.setmana = setmana;
    return this;
  }

  
  @ApiModelProperty(example = "47", value = "Setmana de l'any derivada de la data")
  @JsonProperty("setmana")
  public Integer getSetmana() {
    return setmana;
  }

  @JsonProperty("setmana")
  public void setSetmana(Integer setmana) {
    this.setmana = setmana;
  }

  /**
   **/
  public Temps diaSetmana(DiaSetmanaEnum diaSetmana) {
    this.diaSetmana = diaSetmana;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("diaSetmana")
  public DiaSetmanaEnum getDiaSetmana() {
    return diaSetmana;
  }

  @JsonProperty("diaSetmana")
  public void setDiaSetmana(DiaSetmanaEnum diaSetmana) {
    this.diaSetmana = diaSetmana;
  }

  /**
   * Dia del mes
   **/
  public Temps dia(Integer dia) {
    this.dia = dia;
    return this;
  }

  
  @ApiModelProperty(example = "25", value = "Dia del mes")
  @JsonProperty("dia")
  public Integer getDia() {
    return dia;
  }

  @JsonProperty("dia")
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

