package es.caib.comanda.app.v1.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumerat d'estats possibles de salut d'un component
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.10.0")
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

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
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

