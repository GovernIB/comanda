package es.caib.comanda.model.server.v1.monitoring;

import com.fasterxml.jackson.annotation.JsonTypeName;
import es.caib.comanda.model.server.v1.monitoring.ZoneOffset;
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



@JsonTypeName("OffsetDateTime")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", comments = "Generator version: 7.17.0")
public class ModelOffsetDateTime   {
  private OffsetDateTime dateTime;
  private ZoneOffset offset;

  public ModelOffsetDateTime() {
  }

  /**
   **/
  public ModelOffsetDateTime dateTime(OffsetDateTime dateTime) {
    this.dateTime = dateTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("dateTime")
  public OffsetDateTime getDateTime() {
    return dateTime;
  }

  @JsonProperty("dateTime")
  public void setDateTime(OffsetDateTime dateTime) {
    this.dateTime = dateTime;
  }

  /**
   **/
  public ModelOffsetDateTime offset(ZoneOffset offset) {
    this.offset = offset;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("offset")
  @Valid public ZoneOffset getOffset() {
    return offset;
  }

  @JsonProperty("offset")
  public void setOffset(ZoneOffset offset) {
    this.offset = offset;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModelOffsetDateTime _offsetDateTime = (ModelOffsetDateTime) o;
    return Objects.equals(this.dateTime, _offsetDateTime.dateTime) &&
        Objects.equals(this.offset, _offsetDateTime.offset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dateTime, offset);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ModelOffsetDateTime {\n");
    
    sb.append("    dateTime: ").append(toIndentedString(dateTime)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
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

