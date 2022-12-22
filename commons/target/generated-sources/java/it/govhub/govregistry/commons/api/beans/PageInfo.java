package it.govhub.govregistry.commons.api.beans;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import org.hibernate.validator.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.hateoas.RepresentationModel;

import java.util.*;
import javax.annotation.Generated;

/**
 * PageInfo
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-12-22T16:51:50.588360+01:00[Europe/Rome]")
public class PageInfo extends RepresentationModel<PageInfo>  {

  @JsonProperty("offset")
  private Long offset;

  @JsonProperty("limit")
  private Integer limit;

  @JsonProperty("total")
  private Long total;

  public PageInfo offset(Long offset) {
    this.offset = offset;
    return this;
  }

  /**
   * Offset value (zero-based) for the results.
   * minimum: 0
   * maximum: 9223372036854775807
   * @return offset
  */
  @NotNull @Min(0L) @Max(9223372036854775807L) 
  @Schema(name = "offset", example = "20", description = "Offset value (zero-based) for the results.", required = true)
  public Long getOffset() {
    return offset;
  }

  public void setOffset(Long offset) {
    this.offset = offset;
  }

  public PageInfo limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  /**
   * numero massimo di elementi restituiti.
   * minimum: 0
   * maximum: 2147483647
   * @return limit
  */
  @NotNull @Min(0) @Max(2147483647) 
  @Schema(name = "limit", example = "25", description = "numero massimo di elementi restituiti.", required = true)
  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public PageInfo total(Long total) {
    this.total = total;
    return this;
  }

  /**
   * Number of found elements.
   * minimum: 0
   * maximum: 9223372036854775807
   * @return total
  */
  @Min(0L) @Max(9223372036854775807L) 
  @Schema(name = "total", example = "32", description = "Number of found elements.", required = false)
  public Long getTotal() {
    return total;
  }

  public void setTotal(Long total) {
    this.total = total;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PageInfo pageInfo = (PageInfo) o;
    return Objects.equals(this.offset, pageInfo.offset) &&
        Objects.equals(this.limit, pageInfo.limit) &&
        Objects.equals(this.total, pageInfo.total);
  }

  @Override
  public int hashCode() {
    return Objects.hash(offset, limit, total);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PageInfo {\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
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

