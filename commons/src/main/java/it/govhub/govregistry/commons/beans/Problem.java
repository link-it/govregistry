package it.govhub.govregistry.commons.beans;

import java.net.URI;
import java.util.Objects;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Problem
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-12-13T14:40:47.974452+01:00[Europe/Rome]")
public class Problem extends RepresentationModel<Problem>  {

  @JsonProperty("detail")
  private String detail;

  @JsonProperty("instance")
  private URI instance;

  @JsonProperty("status")
  private Integer status;

  @JsonProperty("title")
  private String title;

  @JsonProperty("type")
  private URI type = URI.create("about:blank");

  public Problem detail(String detail) {
    this.detail = detail;
    return this;
  }

  /**
   * A human readable description of the occurred problem.
   * @return detail
  */
  @Pattern(regexp = "^[ -~]+$") @Size(max = 255) 
  @Schema(name = "detail", example = "Connection to database timed out", description = "A human readable description of the occurred problem.", required = false)
  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public Problem instance(URI instance) {
    this.instance = instance;
    return this;
  }

  /**
   * Link to a specific occurence of the problem.
   * @return instance
  */
  @Valid @Size(max = 255) 
  @Schema(name = "instance", description = "Link to a specific occurence of the problem.", required = false)
  public URI getInstance() {
    return instance;
  }

  public void setInstance(URI instance) {
    this.instance = instance;
  }

  public Problem status(Integer status) {
    this.status = status;
    return this;
  }

  /**
   * HTTP Status Code.
   * minimum: 100
   * maximum: 600
   * @return status
  */
  @Min(100) @Max(600) 
  @Schema(name = "status", example = "503", description = "HTTP Status Code.", required = false)
  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Problem title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Short description of the occurred problem.
   * @return title
  */
  @Pattern(regexp = "^[ -~]+$") @Size(max = 255) 
  @Schema(name = "title", example = "Service Unavailable", description = "Short description of the occurred problem.", required = false)
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Problem type(URI type) {
    this.type = type;
    return this;
  }

  /**
   * Absolute URI for the problem description.
   * @return type
  */
  @Valid @Size(max = 255) 
  @Schema(name = "type", example = "https://tools.ietf.org/html/rfc7231#section-6.6.4", description = "Absolute URI for the problem description.", required = false)
  public URI getType() {
    return type;
  }

  public void setType(URI type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Problem problem = (Problem) o;
    return Objects.equals(this.detail, problem.detail) &&
        Objects.equals(this.instance, problem.instance) &&
        Objects.equals(this.status, problem.status) &&
        Objects.equals(this.title, problem.title) &&
        Objects.equals(this.type, problem.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(detail, instance, status, title, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Problem {\n");
    sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
    sb.append("    instance: ").append(toIndentedString(instance)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
