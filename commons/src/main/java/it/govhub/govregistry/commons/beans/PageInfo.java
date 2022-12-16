package it.govhub.govregistry.commons.beans;

import javax.annotation.Generated;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * PageInfo
 * Generato con OpenAPI Generator
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-12-13T14:40:47.974452+01:00[Europe/Rome]")
public class PageInfo extends RepresentationModel<PageInfo>  {

  @JsonProperty("offset")
  private Long offset;

  @JsonProperty("limit")
  private Integer limit;

  @JsonProperty("total")
  private Long total;

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
}
