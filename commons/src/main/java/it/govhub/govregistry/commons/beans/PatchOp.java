package it.govhub.govregistry.commons.beans;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * PatchOp
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-12-13T14:40:47.974452+01:00[Europe/Rome]")
public class PatchOp extends RepresentationModel<PatchOp>  {

  /**
   * Specified operation.
   */
  public enum OpEnum {
    ADD("add"),
    
    REMOVE("remove"),
    
    REPLACE("replace");

    private String value;

    OpEnum(String value) {
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
    public static OpEnum fromValue(String value) {
      for (OpEnum b : OpEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @JsonProperty("op")
  private OpEnum op;

  @JsonProperty("path")
  private String path;

  @JsonProperty("value")
  private Object value;

  /**
   * Specified operation.
   * @return op
  */
  @NotNull 
  @Schema(name = "op", example = "add", description = "Specified operation.", required = true)
  public OpEnum getOp() {
    return op;
  }

  public void setOp(OpEnum op) {
    this.op = op;
  }

  /**
   * jsonPath of the mutating object.
   * @return path
  */
  @NotNull @Pattern(regexp = "^.*$") @Size(max = 4096) 
  @Schema(name = "path", example = "/organizations", description = "jsonPath of the mutating object.", required = true)
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  /**
   * updating value.
   * @return value
  */
  
  @Schema(name = "value", description = "updating value.", required = false)
  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }
}
