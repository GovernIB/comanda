package es.caib.comanda.model.server.v1.monitoring;

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



@JsonTypeName("ZoneOffset")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class ZoneOffset   {
  private Integer totalSeconds;
  private String id;

  public ZoneOffset() {
  }

  /**
   **/
  public ZoneOffset totalSeconds(Integer totalSeconds) {
    this.totalSeconds = totalSeconds;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("totalSeconds")
  public Integer getTotalSeconds() {
    return totalSeconds;
  }

  @JsonProperty("totalSeconds")
  public void setTotalSeconds(Integer totalSeconds) {
    this.totalSeconds = totalSeconds;
  }

  /**
   **/
  public ZoneOffset id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ZoneOffset zoneOffset = (ZoneOffset) o;
    return Objects.equals(this.totalSeconds, zoneOffset.totalSeconds) &&
        Objects.equals(this.id, zoneOffset.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalSeconds, id);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ZoneOffset {\n");
    
    sb.append("    totalSeconds: ").append(toIndentedString(totalSeconds)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

