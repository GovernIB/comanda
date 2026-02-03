package es.caib.comanda.model.server.monitoring;

import es.caib.comanda.model.server.monitoring.EstatSalut;
import es.caib.comanda.model.server.monitoring.InformacioSistema;
import es.caib.comanda.model.server.monitoring.IntegracioSalut;
import es.caib.comanda.model.server.monitoring.MissatgeSalut;
import es.caib.comanda.model.server.monitoring.SubsistemaSalut;
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
 * Estat de salut funcional de l&#39;aplicació i metadades associades
 **/
@ApiModel(description = "Estat de salut funcional de l'aplicació i metadades associades")
@JsonTypeName("SalutInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class SalutInfo   {
  private String codi;
  private OffsetDateTime data;
  private EstatSalut estatGlobal;
  private EstatSalut estatBaseDeDades;
  private @Valid List<@Valid IntegracioSalut> integracions = new ArrayList<>();
  private InformacioSistema informacioSistema;
  private @Valid List<@Valid MissatgeSalut> missatges = new ArrayList<>();
  private String versio;
  private @Valid List<@Valid SubsistemaSalut> subsistemes = new ArrayList<>();

  public SalutInfo() {
  }

  @JsonCreator
  public SalutInfo(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "data") OffsetDateTime data,
    @JsonProperty(required = true, value = "estatGlobal") EstatSalut estatGlobal,
    @JsonProperty(required = true, value = "estatBaseDeDades") EstatSalut estatBaseDeDades
  ) {
    this.codi = codi;
    this.data = data;
    this.estatGlobal = estatGlobal;
    this.estatBaseDeDades = estatBaseDeDades;
  }

  /**
   * Codi identificador de l&#39;aplicació
   **/
  public SalutInfo codi(String codi) {
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
   **/
  public SalutInfo data(OffsetDateTime data) {
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
   **/
  public SalutInfo estatGlobal(EstatSalut estatGlobal) {
    this.estatGlobal = estatGlobal;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "estatGlobal")
  @NotNull @Valid public EstatSalut getEstatGlobal() {
    return estatGlobal;
  }

  @JsonProperty(required = true, value = "estatGlobal")
  public void setEstatGlobal(EstatSalut estatGlobal) {
    this.estatGlobal = estatGlobal;
  }

  /**
   **/
  public SalutInfo estatBaseDeDades(EstatSalut estatBaseDeDades) {
    this.estatBaseDeDades = estatBaseDeDades;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(required = true, value = "estatBaseDeDades")
  @NotNull @Valid public EstatSalut getEstatBaseDeDades() {
    return estatBaseDeDades;
  }

  @JsonProperty(required = true, value = "estatBaseDeDades")
  public void setEstatBaseDeDades(EstatSalut estatBaseDeDades) {
    this.estatBaseDeDades = estatBaseDeDades;
  }

  /**
   * Integracions amb el seu estat
   **/
  public SalutInfo integracions(List<@Valid IntegracioSalut> integracions) {
    this.integracions = integracions;
    return this;
  }

  
  @ApiModelProperty(value = "Integracions amb el seu estat")
  @JsonProperty("integracions")
  @Valid public List<@Valid IntegracioSalut> getIntegracions() {
    return integracions;
  }

  @JsonProperty("integracions")
  public void setIntegracions(List<@Valid IntegracioSalut> integracions) {
    this.integracions = integracions;
  }

  public SalutInfo addIntegracionsItem(IntegracioSalut integracionsItem) {
    if (this.integracions == null) {
      this.integracions = new ArrayList<>();
    }

    this.integracions.add(integracionsItem);
    return this;
  }

  public SalutInfo removeIntegracionsItem(IntegracioSalut integracionsItem) {
    if (integracionsItem != null && this.integracions != null) {
      this.integracions.remove(integracionsItem);
    }

    return this;
  }
  /**
   **/
  public SalutInfo informacioSistema(InformacioSistema informacioSistema) {
    this.informacioSistema = informacioSistema;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("informacioSistema")
  @Valid public InformacioSistema getInformacioSistema() {
    return informacioSistema;
  }

  @JsonProperty("informacioSistema")
  public void setInformacioSistema(InformacioSistema informacioSistema) {
    this.informacioSistema = informacioSistema;
  }

  /**
   * Missatges informatius o d&#39;alerta
   **/
  public SalutInfo missatges(List<@Valid MissatgeSalut> missatges) {
    this.missatges = missatges;
    return this;
  }

  
  @ApiModelProperty(value = "Missatges informatius o d'alerta")
  @JsonProperty("missatges")
  @Valid public List<@Valid MissatgeSalut> getMissatges() {
    return missatges;
  }

  @JsonProperty("missatges")
  public void setMissatges(List<@Valid MissatgeSalut> missatges) {
    this.missatges = missatges;
  }

  public SalutInfo addMissatgesItem(MissatgeSalut missatgesItem) {
    if (this.missatges == null) {
      this.missatges = new ArrayList<>();
    }

    this.missatges.add(missatgesItem);
    return this;
  }

  public SalutInfo removeMissatgesItem(MissatgeSalut missatgesItem) {
    if (missatgesItem != null && this.missatges != null) {
      this.missatges.remove(missatgesItem);
    }

    return this;
  }
  /**
   * Versió de l&#39;aplicació
   **/
  public SalutInfo versio(String versio) {
    this.versio = versio;
    return this;
  }

  
  @ApiModelProperty(example = "1.4.3", value = "Versió de l'aplicació")
  @JsonProperty("versio")
  public String getVersio() {
    return versio;
  }

  @JsonProperty("versio")
  public void setVersio(String versio) {
    this.versio = versio;
  }

  /**
   * Subsistemes interns amb el seu estat
   **/
  public SalutInfo subsistemes(List<@Valid SubsistemaSalut> subsistemes) {
    this.subsistemes = subsistemes;
    return this;
  }

  
  @ApiModelProperty(value = "Subsistemes interns amb el seu estat")
  @JsonProperty("subsistemes")
  @Valid public List<@Valid SubsistemaSalut> getSubsistemes() {
    return subsistemes;
  }

  @JsonProperty("subsistemes")
  public void setSubsistemes(List<@Valid SubsistemaSalut> subsistemes) {
    this.subsistemes = subsistemes;
  }

  public SalutInfo addSubsistemesItem(SubsistemaSalut subsistemesItem) {
    if (this.subsistemes == null) {
      this.subsistemes = new ArrayList<>();
    }

    this.subsistemes.add(subsistemesItem);
    return this;
  }

  public SalutInfo removeSubsistemesItem(SubsistemaSalut subsistemesItem) {
    if (subsistemesItem != null && this.subsistemes != null) {
      this.subsistemes.remove(subsistemesItem);
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
    SalutInfo salutInfo = (SalutInfo) o;
    return Objects.equals(this.codi, salutInfo.codi) &&
        Objects.equals(this.data, salutInfo.data) &&
        Objects.equals(this.estatGlobal, salutInfo.estatGlobal) &&
        Objects.equals(this.estatBaseDeDades, salutInfo.estatBaseDeDades) &&
        Objects.equals(this.integracions, salutInfo.integracions) &&
        Objects.equals(this.informacioSistema, salutInfo.informacioSistema) &&
        Objects.equals(this.missatges, salutInfo.missatges) &&
        Objects.equals(this.versio, salutInfo.versio) &&
        Objects.equals(this.subsistemes, salutInfo.subsistemes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, data, estatGlobal, estatBaseDeDades, integracions, informacioSistema, missatges, versio, subsistemes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SalutInfo {\n");
    
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    estatGlobal: ").append(toIndentedString(estatGlobal)).append("\n");
    sb.append("    estatBaseDeDades: ").append(toIndentedString(estatBaseDeDades)).append("\n");
    sb.append("    integracions: ").append(toIndentedString(integracions)).append("\n");
    sb.append("    informacioSistema: ").append(toIndentedString(informacioSistema)).append("\n");
    sb.append("    missatges: ").append(toIndentedString(missatges)).append("\n");
    sb.append("    versio: ").append(toIndentedString(versio)).append("\n");
    sb.append("    subsistemes: ").append(toIndentedString(subsistemes)).append("\n");
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

