package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Detall específic d&#39;estat de salut (parella clau-valor)
 */

@Schema(name = "DetallSalut", description = "Detall específic d'estat de salut (parella clau-valor)")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
public class DetallSalut {

  private String codi;

  private String nom;

  private String valor;

  public DetallSalut() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public DetallSalut(String codi, String nom, String valor) {
    this.codi = codi;
    this.nom = nom;
    this.valor = valor;
  }

  public DetallSalut codi(String codi) {
    this.codi = codi;
    return this;
  }

  /**
   * Codi del detall
   * @return codi
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "codi", example = "latencia-db", description = "Codi del detall", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("codi")
  public String getCodi() {
    return codi;
  }

  public void setCodi(String codi) {
    this.codi = codi;
  }

  public DetallSalut nom(String nom) {
    this.nom = nom;
    return this;
  }

  /**
   * Nom del detall
   * @return nom
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "nom", example = "Latència BD", description = "Nom del detall", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("nom")
  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public DetallSalut valor(String valor) {
    this.valor = valor;
    return this;
  }

  /**
   * Valor del detall
   * @return valor
   */
  @NotNull @Size(min = 1) 
  @Schema(name = "valor", example = "120ms", description = "Valor del detall", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("valor")
  public String getValor() {
    return valor;
  }

  public void setValor(String valor) {
    this.valor = valor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DetallSalut detallSalut = (DetallSalut) o;
    return Objects.equals(this.codi, detallSalut.codi) &&
        Objects.equals(this.nom, detallSalut.nom) &&
        Objects.equals(this.valor, detallSalut.valor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(codi, nom, valor);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DetallSalut {\n");
    sb.append("    codi: ").append(toIndentedString(codi)).append("\n");
    sb.append("    nom: ").append(toIndentedString(nom)).append("\n");
    sb.append("    valor: ").append(toIndentedString(valor)).append("\n");
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

