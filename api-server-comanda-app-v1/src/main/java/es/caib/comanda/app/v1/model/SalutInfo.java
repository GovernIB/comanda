package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import es.caib.comanda.app.v1.model.DetallSalut;
import es.caib.comanda.app.v1.model.EstatSalut;
import es.caib.comanda.app.v1.model.IntegracioSalut;
import es.caib.comanda.app.v1.model.MissatgeSalut;
import es.caib.comanda.app.v1.model.SubsistemaSalut;
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
 * Estat de salut funcional de l&#39;aplicació i metadades associades
 */

@Schema(name = "SalutInfo", description = "Estat de salut funcional de l'aplicació i metadades associades")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class SalutInfo {

  private String codi;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime data;

  private EstatSalut estat;

  private EstatSalut bd;

  @Valid
  private List<@Valid IntegracioSalut> integracions = new ArrayList<>();

  @Valid
  private List<@Valid DetallSalut> altres = new ArrayList<>();

  @Valid
  private List<@Valid MissatgeSalut> missatges = new ArrayList<>();

  private String versio;

  @Valid
  private List<@Valid SubsistemaSalut> subsistemes = new ArrayList<>();

  public SalutInfo() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SalutInfo(String codi, OffsetDateTime data, EstatSalut estat, EstatSalut bd) {
    this.codi = codi;
    this.data = data;
    this.estat = estat;
    this.bd = bd;
  }

  public SalutInfo codi(String codi) {
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

  public SalutInfo data(OffsetDateTime data) {
    this.data = data;
    return this;
  }

  /**
   * Instant de generació de l'estat de salut
   * @return data
   */
  @NotNull @Valid 
  @Schema(name = "data", description = "Instant de generació de l'estat de salut", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("data")
  public OffsetDateTime getData() {
    return data;
  }

  public void setData(OffsetDateTime data) {
    this.data = data;
  }

  public SalutInfo estat(EstatSalut estat) {
    this.estat = estat;
    return this;
  }

  /**
   * Get estat
   * @return estat
   */
  @NotNull @Valid 
  @Schema(name = "estat", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("estat")
  public EstatSalut getEstat() {
    return estat;
  }

  public void setEstat(EstatSalut estat) {
    this.estat = estat;
  }

  public SalutInfo bd(EstatSalut bd) {
    this.bd = bd;
    return this;
  }

  /**
   * Get bd
   * @return bd
   */
  @NotNull @Valid 
  @Schema(name = "bd", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("bd")
  public EstatSalut getBd() {
    return bd;
  }

  public void setBd(EstatSalut bd) {
    this.bd = bd;
  }

  public SalutInfo integracions(List<@Valid IntegracioSalut> integracions) {
    this.integracions = integracions;
    return this;
  }

  public SalutInfo addIntegracionsItem(IntegracioSalut integracionsItem) {
    if (this.integracions == null) {
      this.integracions = new ArrayList<>();
    }
    this.integracions.add(integracionsItem);
    return this;
  }

  /**
   * Integracions amb el seu estat
   * @return integracions
   */
  @Valid 
  @Schema(name = "integracions", description = "Integracions amb el seu estat", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("integracions")
  public List<@Valid IntegracioSalut> getIntegracions() {
    return integracions;
  }

  public void setIntegracions(List<@Valid IntegracioSalut> integracions) {
    this.integracions = integracions;
  }

  public SalutInfo altres(List<@Valid DetallSalut> altres) {
    this.altres = altres;
    return this;
  }

  public SalutInfo addAltresItem(DetallSalut altresItem) {
    if (this.altres == null) {
      this.altres = new ArrayList<>();
    }
    this.altres.add(altresItem);
    return this;
  }

  /**
   * Altres detalls rellevants de salut
   * @return altres
   */
  @Valid 
  @Schema(name = "altres", description = "Altres detalls rellevants de salut", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("altres")
  public List<@Valid DetallSalut> getAltres() {
    return altres;
  }

  public void setAltres(List<@Valid DetallSalut> altres) {
    this.altres = altres;
  }

  public SalutInfo missatges(List<@Valid MissatgeSalut> missatges) {
    this.missatges = missatges;
    return this;
  }

  public SalutInfo addMissatgesItem(MissatgeSalut missatgesItem) {
    if (this.missatges == null) {
      this.missatges = new ArrayList<>();
    }
    this.missatges.add(missatgesItem);
    return this;
  }

  /**
   * Missatges informatius o d'alerta
   * @return missatges
   */
  @Valid 
  @Schema(name = "missatges", description = "Missatges informatius o d'alerta", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("missatges")
  public List<@Valid MissatgeSalut> getMissatges() {
    return missatges;
  }

  public void setMissatges(List<@Valid MissatgeSalut> missatges) {
    this.missatges = missatges;
  }

  public SalutInfo versio(String versio) {
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

  public SalutInfo subsistemes(List<@Valid SubsistemaSalut> subsistemes) {
    this.subsistemes = subsistemes;
    return this;
  }

  public SalutInfo addSubsistemesItem(SubsistemaSalut subsistemesItem) {
    if (this.subsistemes == null) {
      this.subsistemes = new ArrayList<>();
    }
    this.subsistemes.add(subsistemesItem);
    return this;
  }

  /**
   * Subsistemes interns amb el seu estat
   * @return subsistemes
   */
  @Valid 
  @Schema(name = "subsistemes", description = "Subsistemes interns amb el seu estat", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("subsistemes")
  public List<@Valid SubsistemaSalut> getSubsistemes() {
    return subsistemes;
  }

  public void setSubsistemes(List<@Valid SubsistemaSalut> subsistemes) {
    this.subsistemes = subsistemes;
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
        Objects.equals(this.estat, salutInfo.estat) &&
        Objects.equals(this.bd, salutInfo.bd) &&
        Objects.equals(this.integracions, salutInfo.integracions) &&
        Objects.equals(this.altres, salutInfo.altres) &&
        Objects.equals(this.missatges, salutInfo.missatges) &&
        Objects.equals(this.versio, salutInfo.versio) &&
        Objects.equals(this.subsistemes, salutInfo.subsistemes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, data, estat, bd, integracions, altres, missatges, versio, subsistemes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SalutInfo {\n");
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    estat: ").append(toIndentedString(estat)).append("\n");
    sb.append("    bd: ").append(toIndentedString(bd)).append("\n");
    sb.append("    integracions: ").append(toIndentedString(integracions)).append("\n");
    sb.append("    altres: ").append(toIndentedString(altres)).append("\n");
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

