package es.caib.comanda.model.server.monitoring;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;
import javax.validation.Valid;

import io.swagger.annotations.*;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Informació resum de l&#39;estat del sistema on s&#39;executa l&#39;aplicació
 **/
@ApiModel(description = "Informació resum de l'estat del sistema on s'executa l'aplicació")
@JsonTypeName("InformacioSistema")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class InformacioSistema   {
  private Integer processadors;
  private String carregaSistema;
  private String cpuSistema;
  private String memoriaTotal;
  private String memoriaDisponible;
  private String espaiDiscTotal;
  private String espaiDiscLliure;
  private String sistemaOperatiu;
  private String dataArrencada;
  private String tempsFuncionant;

  public InformacioSistema() {
  }

  /**
   * Nombre de processadors disponibles
   **/
  public InformacioSistema processadors(Integer processadors) {
    this.processadors = processadors;
    return this;
  }

  
  @ApiModelProperty(example = "8", value = "Nombre de processadors disponibles")
  @JsonProperty("processadors")
  public Integer getProcessadors() {
    return processadors;
  }

  @JsonProperty("processadors")
  public void setProcessadors(Integer processadors) {
    this.processadors = processadors;
  }

  /**
   * Càrrega actual del sistema
   **/
  public InformacioSistema carregaSistema(String carregaSistema) {
    this.carregaSistema = carregaSistema;
    return this;
  }

  
  @ApiModelProperty(example = "2.45", value = "Càrrega actual del sistema")
  @JsonProperty("carregaSistema")
  public String getCarregaSistema() {
    return carregaSistema;
  }

  @JsonProperty("carregaSistema")
  public void setCarregaSistema(String carregaSistema) {
    this.carregaSistema = carregaSistema;
  }

  /**
   * Percentatge d&#39;ús de CPU del sistema
   **/
  public InformacioSistema cpuSistema(String cpuSistema) {
    this.cpuSistema = cpuSistema;
    return this;
  }

  
  @ApiModelProperty(example = "45.2%", value = "Percentatge d'ús de CPU del sistema")
  @JsonProperty("cpuSistema")
  public String getCpuSistema() {
    return cpuSistema;
  }

  @JsonProperty("cpuSistema")
  public void setCpuSistema(String cpuSistema) {
    this.cpuSistema = cpuSistema;
  }

  /**
   * Memòria total del sistema
   **/
  public InformacioSistema memoriaTotal(String memoriaTotal) {
    this.memoriaTotal = memoriaTotal;
    return this;
  }

  
  @ApiModelProperty(example = "16 GB", value = "Memòria total del sistema")
  @JsonProperty("memoriaTotal")
  public String getMemoriaTotal() {
    return memoriaTotal;
  }

  @JsonProperty("memoriaTotal")
  public void setMemoriaTotal(String memoriaTotal) {
    this.memoriaTotal = memoriaTotal;
  }

  /**
   * Memòria disponible del sistema
   **/
  public InformacioSistema memoriaDisponible(String memoriaDisponible) {
    this.memoriaDisponible = memoriaDisponible;
    return this;
  }

  
  @ApiModelProperty(example = "8 GB", value = "Memòria disponible del sistema")
  @JsonProperty("memoriaDisponible")
  public String getMemoriaDisponible() {
    return memoriaDisponible;
  }

  @JsonProperty("memoriaDisponible")
  public void setMemoriaDisponible(String memoriaDisponible) {
    this.memoriaDisponible = memoriaDisponible;
  }

  /**
   * Espai total del disc
   **/
  public InformacioSistema espaiDiscTotal(String espaiDiscTotal) {
    this.espaiDiscTotal = espaiDiscTotal;
    return this;
  }

  
  @ApiModelProperty(example = "500 GB", value = "Espai total del disc")
  @JsonProperty("espaiDiscTotal")
  public String getEspaiDiscTotal() {
    return espaiDiscTotal;
  }

  @JsonProperty("espaiDiscTotal")
  public void setEspaiDiscTotal(String espaiDiscTotal) {
    this.espaiDiscTotal = espaiDiscTotal;
  }

  /**
   * Espai lliure del disc
   **/
  public InformacioSistema espaiDiscLliure(String espaiDiscLliure) {
    this.espaiDiscLliure = espaiDiscLliure;
    return this;
  }

  
  @ApiModelProperty(example = "250 GB", value = "Espai lliure del disc")
  @JsonProperty("espaiDiscLliure")
  public String getEspaiDiscLliure() {
    return espaiDiscLliure;
  }

  @JsonProperty("espaiDiscLliure")
  public void setEspaiDiscLliure(String espaiDiscLliure) {
    this.espaiDiscLliure = espaiDiscLliure;
  }

  /**
   * Sistema operatiu
   **/
  public InformacioSistema sistemaOperatiu(String sistemaOperatiu) {
    this.sistemaOperatiu = sistemaOperatiu;
    return this;
  }

  
  @ApiModelProperty(example = "Linux 5.4.0", value = "Sistema operatiu")
  @JsonProperty("sistemaOperatiu")
  public String getSistemaOperatiu() {
    return sistemaOperatiu;
  }

  @JsonProperty("sistemaOperatiu")
  public void setSistemaOperatiu(String sistemaOperatiu) {
    this.sistemaOperatiu = sistemaOperatiu;
  }

  /**
   * Data d&#39;arrencada del sistema
   **/
  public InformacioSistema dataArrencada(String dataArrencada) {
    this.dataArrencada = dataArrencada;
    return this;
  }

  
  @ApiModelProperty(example = "15/01/2024 10:30", value = "Data d'arrencada del sistema")
  @JsonProperty("dataArrencada")
  public String getDataArrencada() {
    return dataArrencada;
  }

  @JsonProperty("dataArrencada")
  public void setDataArrencada(String dataArrencada) {
    this.dataArrencada = dataArrencada;
  }

  /**
   * Temps de funcionament del sistema
   **/
  public InformacioSistema tempsFuncionant(String tempsFuncionant) {
    this.tempsFuncionant = tempsFuncionant;
    return this;
  }

  
  @ApiModelProperty(example = "5 dies, 03:25:14", value = "Temps de funcionament del sistema")
  @JsonProperty("tempsFuncionant")
  public String getTempsFuncionant() {
    return tempsFuncionant;
  }

  @JsonProperty("tempsFuncionant")
  public void setTempsFuncionant(String tempsFuncionant) {
    this.tempsFuncionant = tempsFuncionant;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InformacioSistema informacioSistema = (InformacioSistema) o;
    return Objects.equals(this.processadors, informacioSistema.processadors) &&
        Objects.equals(this.carregaSistema, informacioSistema.carregaSistema) &&
        Objects.equals(this.cpuSistema, informacioSistema.cpuSistema) &&
        Objects.equals(this.memoriaTotal, informacioSistema.memoriaTotal) &&
        Objects.equals(this.memoriaDisponible, informacioSistema.memoriaDisponible) &&
        Objects.equals(this.espaiDiscTotal, informacioSistema.espaiDiscTotal) &&
        Objects.equals(this.espaiDiscLliure, informacioSistema.espaiDiscLliure) &&
        Objects.equals(this.sistemaOperatiu, informacioSistema.sistemaOperatiu) &&
        Objects.equals(this.dataArrencada, informacioSistema.dataArrencada) &&
        Objects.equals(this.tempsFuncionant, informacioSistema.tempsFuncionant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(processadors, carregaSistema, cpuSistema, memoriaTotal, memoriaDisponible, espaiDiscTotal, espaiDiscLliure, sistemaOperatiu, dataArrencada, tempsFuncionant);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InformacioSistema {\n");
    
    sb.append("    processadors: ").append(toIndentedString(processadors)).append("\n");
    sb.append("    carregaSistema: ").append(toIndentedString(carregaSistema)).append("\n");
    sb.append("    cpuSistema: ").append(toIndentedString(cpuSistema)).append("\n");
    sb.append("    memoriaTotal: ").append(toIndentedString(memoriaTotal)).append("\n");
    sb.append("    memoriaDisponible: ").append(toIndentedString(memoriaDisponible)).append("\n");
    sb.append("    espaiDiscTotal: ").append(toIndentedString(espaiDiscTotal)).append("\n");
    sb.append("    espaiDiscLliure: ").append(toIndentedString(espaiDiscLliure)).append("\n");
    sb.append("    sistemaOperatiu: ").append(toIndentedString(sistemaOperatiu)).append("\n");
    sb.append("    dataArrencada: ").append(toIndentedString(dataArrencada)).append("\n");
    sb.append("    tempsFuncionant: ").append(toIndentedString(tempsFuncionant)).append("\n");
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

