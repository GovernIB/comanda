package es.caib.comanda.model.server.monitoring;

import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;
import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumerat d&#39;estats possibles de salut d&#39;un component
 */
public enum EstatSalutEnum {
  
  UP("UP"),
  
  WARN("WARN"),
  
  DEGRADED("DEGRADED"),
  
  DOWN("DOWN"),
  
  MAINTENANCE("MAINTENANCE"),
  
  UNKNOWN("UNKNOWN"),
  
  ERROR("ERROR");

  private String value;

  EstatSalutEnum(String value) {
    this.value = value;
  }

    /**
     * Convert a String into String, as specified in the
     * <a href="https://download.oracle.com/otndocs/jcp/jaxrs-2_0-fr-eval-spec/index.html">See JAX RS 2.0 Specification, section 3.2, p. 12</a>
     */
    public static EstatSalutEnum fromString(String s) {
      for (EstatSalutEnum b : EstatSalutEnum.values()) {
        // using Objects.toString() to be safe if value type non-object type
        // because types like 'int' etc. will be auto-boxed
        if (java.util.Objects.toString(b.value).equals(s)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected string value '" + s + "'");
    }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static EstatSalutEnum fromValue(String value) {
    for (EstatSalutEnum b : EstatSalutEnum.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


