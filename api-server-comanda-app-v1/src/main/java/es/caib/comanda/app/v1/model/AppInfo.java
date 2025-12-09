package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import es.caib.comanda.app.v1.model.ContextInfo;
import es.caib.comanda.app.v1.model.IntegracioInfo;
import es.caib.comanda.app.v1.model.SubsistemaInfo;
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
 * Informació bàsica de l&#39;aplicació consultada per COMANDA
 */

@Schema(name = "AppInfo", description = "Informació bàsica de l'aplicació consultada per COMANDA")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class AppInfo {

  private String codi;

  private String nom;

  private String versio;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime data;

  private String revisio;

  private String jdkVersion;

  @Valid
  private List<@Valid IntegracioInfo> integracions = new ArrayList<>();

  @Valid
  private List<@Valid SubsistemaInfo> subsistemes = new ArrayList<>();

  @Valid
  private List<@Valid ContextInfo> contexts = new ArrayList<>();

  public AppInfo() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public AppInfo(String codi, String nom, String versio, OffsetDateTime data) {
    this.codi = codi;
    this.nom = nom;
    this.versio = versio;
    this.data = data;
  }

  public AppInfo codi(String codi) {
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

  public AppInfo nom(String nom) {
    this.nom = nom;
    return this;
  }

  /**
   * Nom complet de l'aplicació
   * @return nom
   */
  @NotNull @Size(min = 1, max = 100) 
  @Schema(name = "nom", example = "NOTIB - Notificacions", description = "Nom complet de l'aplicació", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nom")
  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public AppInfo versio(String versio) {
    this.versio = versio;
    return this;
  }

  /**
   * Versió desplegada de l'aplicació
   * @return versio
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "versio", example = "1.4.3", description = "Versió desplegada de l'aplicació", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("versio")
  public String getVersio() {
    return versio;
  }

  public void setVersio(String versio) {
    this.versio = versio;
  }

  public AppInfo data(OffsetDateTime data) {
    this.data = data;
    return this;
  }

  /**
   * Data de compilació o de la informació reportada
   * @return data
   */
  @NotNull @Valid 
  @Schema(name = "data", description = "Data de compilació o de la informació reportada", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("data")
  public OffsetDateTime getData() {
    return data;
  }

  public void setData(OffsetDateTime data) {
    this.data = data;
  }

  public AppInfo revisio(String revisio) {
    this.revisio = revisio;
    return this;
  }

  /**
   * Revisió o identificador de commit de la build
   * @return revisio
   */
  
  @Schema(name = "revisio", example = "a1b2c3d", description = "Revisió o identificador de commit de la build", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("revisio")
  public String getRevisio() {
    return revisio;
  }

  public void setRevisio(String revisio) {
    this.revisio = revisio;
  }

  public AppInfo jdkVersion(String jdkVersion) {
    this.jdkVersion = jdkVersion;
    return this;
  }

  /**
   * Versió de JDK amb la qual s'executa l'aplicació
   * @return jdkVersion
   */
  
  @Schema(name = "jdkVersion", example = "Temurin-17.0.9", description = "Versió de JDK amb la qual s'executa l'aplicació", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("jdkVersion")
  public String getJdkVersion() {
    return jdkVersion;
  }

  public void setJdkVersion(String jdkVersion) {
    this.jdkVersion = jdkVersion;
  }

  public AppInfo integracions(List<@Valid IntegracioInfo> integracions) {
    this.integracions = integracions;
    return this;
  }

  public AppInfo addIntegracionsItem(IntegracioInfo integracionsItem) {
    if (this.integracions == null) {
      this.integracions = new ArrayList<>();
    }
    this.integracions.add(integracionsItem);
    return this;
  }

  /**
   * Llista d'integracions exposades per l'aplicació
   * @return integracions
   */
  @Valid 
  @Schema(name = "integracions", description = "Llista d'integracions exposades per l'aplicació", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("integracions")
  public List<@Valid IntegracioInfo> getIntegracions() {
    return integracions;
  }

  public void setIntegracions(List<@Valid IntegracioInfo> integracions) {
    this.integracions = integracions;
  }

  public AppInfo subsistemes(List<@Valid SubsistemaInfo> subsistemes) {
    this.subsistemes = subsistemes;
    return this;
  }

  public AppInfo addSubsistemesItem(SubsistemaInfo subsistemesItem) {
    if (this.subsistemes == null) {
      this.subsistemes = new ArrayList<>();
    }
    this.subsistemes.add(subsistemesItem);
    return this;
  }

  /**
   * Llista de subsistemes interns amb el seu estat
   * @return subsistemes
   */
  @Valid 
  @Schema(name = "subsistemes", description = "Llista de subsistemes interns amb el seu estat", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("subsistemes")
  public List<@Valid SubsistemaInfo> getSubsistemes() {
    return subsistemes;
  }

  public void setSubsistemes(List<@Valid SubsistemaInfo> subsistemes) {
    this.subsistemes = subsistemes;
  }

  public AppInfo contexts(List<@Valid ContextInfo> contexts) {
    this.contexts = contexts;
    return this;
  }

  public AppInfo addContextsItem(ContextInfo contextsItem) {
    if (this.contexts == null) {
      this.contexts = new ArrayList<>();
    }
    this.contexts.add(contextsItem);
    return this;
  }

  /**
   * Contextos o endpoints base exposats per l'aplicació
   * @return contexts
   */
  @Valid 
  @Schema(name = "contexts", description = "Contextos o endpoints base exposats per l'aplicació", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("contexts")
  public List<@Valid ContextInfo> getContexts() {
    return contexts;
  }

  public void setContexts(List<@Valid ContextInfo> contexts) {
    this.contexts = contexts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AppInfo appInfo = (AppInfo) o;
    return Objects.equals(this.codi, appInfo.codi) &&
        Objects.equals(this.nom, appInfo.nom) &&
        Objects.equals(this.versio, appInfo.versio) &&
        Objects.equals(this.data, appInfo.data) &&
        Objects.equals(this.revisio, appInfo.revisio) &&
        Objects.equals(this.jdkVersion, appInfo.jdkVersion) &&
        Objects.equals(this.integracions, appInfo.integracions) &&
        Objects.equals(this.subsistemes, appInfo.subsistemes) &&
        Objects.equals(this.contexts, appInfo.contexts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, nom, versio, data, revisio, jdkVersion, integracions, subsistemes, contexts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppInfo {\n");
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
    sb.append("    versio: ").append(toIndentedString(versio)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    revisio: ").append(toIndentedString(revisio)).append("\n");
    sb.append("    jdkVersion: ").append(toIndentedString(jdkVersion)).append("\n");
    sb.append("    integracions: ").append(toIndentedString(integracions)).append("\n");
    sb.append("    subsistemes: ").append(toIndentedString(subsistemes)).append("\n");
    sb.append("    contexts: ").append(toIndentedString(contexts)).append("\n");
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

