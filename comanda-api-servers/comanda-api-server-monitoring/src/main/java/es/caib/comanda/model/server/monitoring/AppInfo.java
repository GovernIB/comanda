package es.caib.comanda.model.server.monitoring;

import es.caib.comanda.model.server.monitoring.ContextInfo;
import es.caib.comanda.model.server.monitoring.IntegracioInfo;
import es.caib.comanda.model.server.monitoring.SubsistemaInfo;
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
 * Informació bàsica de l&#39;aplicació consultada per COMANDA
 **/
@ApiModel(description = "Informació bàsica de l'aplicació consultada per COMANDA")
@JsonTypeName("AppInfo")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class AppInfo   {
  private String codi;
  private String nom;
  private String versio;
  private OffsetDateTime data;
  private String revisio;
  private String jdkVersion;
  private String versioJboss;
  private @Valid List<@Valid IntegracioInfo> integracions = new ArrayList<>();
  private @Valid List<@Valid SubsistemaInfo> subsistemes = new ArrayList<>();
  private @Valid List<@Valid ContextInfo> contexts = new ArrayList<>();

  public AppInfo() {
  }

  @JsonCreator
  public AppInfo(
    @JsonProperty(required = true, value = "codi") String codi,
    @JsonProperty(required = true, value = "nom") String nom,
    @JsonProperty(required = true, value = "versio") String versio,
    @JsonProperty(required = true, value = "data") OffsetDateTime data
  ) {
    this.codi = codi;
    this.nom = nom;
    this.versio = versio;
    this.data = data;
  }

  /**
   * Codi identificador de l&#39;aplicació
   **/
  public AppInfo codi(String codi) {
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
   * Nom complet de l&#39;aplicació
   **/
  public AppInfo nom(String nom) {
    this.nom = nom;
    return this;
  }

  
  @ApiModelProperty(example = "APLICACIO", required = true, value = "Nom complet de l'aplicació")
  @JsonProperty(required = true, value = "nom")
  @NotNull  @Size(min=1,max=100)public String getNom() {
    return nom;
  }

  @JsonProperty(required = true, value = "nom")
  public void setNom(String nom) {
    this.nom = nom;
  }

  /**
   * Versió desplegada de l&#39;aplicació
   **/
  public AppInfo versio(String versio) {
    this.versio = versio;
    return this;
  }

  
  @ApiModelProperty(example = "2.1.0", required = true, value = "Versió desplegada de l'aplicació")
  @JsonProperty(required = true, value = "versio")
  @NotNull  @Size(min=1)public String getVersio() {
    return versio;
  }

  @JsonProperty(required = true, value = "versio")
  public void setVersio(String versio) {
    this.versio = versio;
  }

  /**
   **/
  public AppInfo data(OffsetDateTime data) {
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
   * Revisió o identificador de commit de la build
   **/
  public AppInfo revisio(String revisio) {
    this.revisio = revisio;
    return this;
  }

  
  @ApiModelProperty(example = "a1b2c3d", value = "Revisió o identificador de commit de la build")
  @JsonProperty("revisio")
  public String getRevisio() {
    return revisio;
  }

  @JsonProperty("revisio")
  public void setRevisio(String revisio) {
    this.revisio = revisio;
  }

  /**
   * Versió de JDK amb la qual s&#39;executa l&#39;aplicació
   **/
  public AppInfo jdkVersion(String jdkVersion) {
    this.jdkVersion = jdkVersion;
    return this;
  }

  
  @ApiModelProperty(example = "Temurin-17.0.9", value = "Versió de JDK amb la qual s'executa l'aplicació")
  @JsonProperty("jdkVersion")
  public String getJdkVersion() {
    return jdkVersion;
  }

  @JsonProperty("jdkVersion")
  public void setJdkVersion(String jdkVersion) {
    this.jdkVersion = jdkVersion;
  }

  /**
   * Versió de JBoss/WildFly amb la qual s&#39;executa l&#39;aplicació
   **/
  public AppInfo versioJboss(String versioJboss) {
    this.versioJboss = versioJboss;
    return this;
  }

  
  @ApiModelProperty(example = "JBoss EAP 7.2", value = "Versió de JBoss/WildFly amb la qual s'executa l'aplicació")
  @JsonProperty("versioJboss")
  public String getVersioJboss() {
    return versioJboss;
  }

  @JsonProperty("versioJboss")
  public void setVersioJboss(String versioJboss) {
    this.versioJboss = versioJboss;
  }

  /**
   * Llista d&#39;integracions exposades per l&#39;aplicació
   **/
  public AppInfo integracions(List<@Valid IntegracioInfo> integracions) {
    this.integracions = integracions;
    return this;
  }

  
  @ApiModelProperty(value = "Llista d'integracions exposades per l'aplicació")
  @JsonProperty("integracions")
  @Valid public List<@Valid IntegracioInfo> getIntegracions() {
    return integracions;
  }

  @JsonProperty("integracions")
  public void setIntegracions(List<@Valid IntegracioInfo> integracions) {
    this.integracions = integracions;
  }

  public AppInfo addIntegracionsItem(IntegracioInfo integracionsItem) {
    if (this.integracions == null) {
      this.integracions = new ArrayList<>();
    }

    this.integracions.add(integracionsItem);
    return this;
  }

  public AppInfo removeIntegracionsItem(IntegracioInfo integracionsItem) {
    if (integracionsItem != null && this.integracions != null) {
      this.integracions.remove(integracionsItem);
    }

    return this;
  }
  /**
   * Llista de subsistemes interns amb el seu estat
   **/
  public AppInfo subsistemes(List<@Valid SubsistemaInfo> subsistemes) {
    this.subsistemes = subsistemes;
    return this;
  }

  
  @ApiModelProperty(value = "Llista de subsistemes interns amb el seu estat")
  @JsonProperty("subsistemes")
  @Valid public List<@Valid SubsistemaInfo> getSubsistemes() {
    return subsistemes;
  }

  @JsonProperty("subsistemes")
  public void setSubsistemes(List<@Valid SubsistemaInfo> subsistemes) {
    this.subsistemes = subsistemes;
  }

  public AppInfo addSubsistemesItem(SubsistemaInfo subsistemesItem) {
    if (this.subsistemes == null) {
      this.subsistemes = new ArrayList<>();
    }

    this.subsistemes.add(subsistemesItem);
    return this;
  }

  public AppInfo removeSubsistemesItem(SubsistemaInfo subsistemesItem) {
    if (subsistemesItem != null && this.subsistemes != null) {
      this.subsistemes.remove(subsistemesItem);
    }

    return this;
  }
  /**
   * Contextos o endpoints base exposats per l&#39;aplicació
   **/
  public AppInfo contexts(List<@Valid ContextInfo> contexts) {
    this.contexts = contexts;
    return this;
  }

  
  @ApiModelProperty(value = "Contextos o endpoints base exposats per l'aplicació")
  @JsonProperty("contexts")
  @Valid public List<@Valid ContextInfo> getContexts() {
    return contexts;
  }

  @JsonProperty("contexts")
  public void setContexts(List<@Valid ContextInfo> contexts) {
    this.contexts = contexts;
  }

  public AppInfo addContextsItem(ContextInfo contextsItem) {
    if (this.contexts == null) {
      this.contexts = new ArrayList<>();
    }

    this.contexts.add(contextsItem);
    return this;
  }

  public AppInfo removeContextsItem(ContextInfo contextsItem) {
    if (contextsItem != null && this.contexts != null) {
      this.contexts.remove(contextsItem);
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
    AppInfo appInfo = (AppInfo) o;
    return Objects.equals(this.codi, appInfo.codi) &&
        Objects.equals(this.nom, appInfo.nom) &&
        Objects.equals(this.versio, appInfo.versio) &&
        Objects.equals(this.data, appInfo.data) &&
        Objects.equals(this.revisio, appInfo.revisio) &&
        Objects.equals(this.jdkVersion, appInfo.jdkVersion) &&
        Objects.equals(this.versioJboss, appInfo.versioJboss) &&
        Objects.equals(this.integracions, appInfo.integracions) &&
        Objects.equals(this.subsistemes, appInfo.subsistemes) &&
        Objects.equals(this.contexts, appInfo.contexts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, nom, versio, data, revisio, jdkVersion, versioJboss, integracions, subsistemes, contexts);
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
    sb.append("    versioJboss: ").append(toIndentedString(versioJboss)).append("\n");
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

