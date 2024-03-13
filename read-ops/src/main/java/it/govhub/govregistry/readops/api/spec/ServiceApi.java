/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govregistry.readops.api.spec;

import java.util.List;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.govhub.govregistry.commons.api.beans.Problem;
import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.beans.ServiceList;
import it.govhub.govregistry.commons.api.beans.ServiceOrdering;

@Generated(value = "it.govhub.openapi.codegen.GovhubCodegenGenerator", date = "2023-03-30T13:10:58.167825+02:00[Europe/Rome]")
@Validated
@Tag(name = "Service", description = "the Service API")
@RequestMapping("/v1")
public interface ServiceApi {

    @Operation(
        operationId = "downloadServiceLogo",
        summary = "Retrieve the service logo",
        tags = { "service" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = org.springframework.core.io.Resource.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.core.io.Resource.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "401", description = "Required credentials missing.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "403", description = "Agent not authorized for the operation.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "503", description = "Service Unavailable.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "200", description = "Unexpected error.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/services/{id}/logo",
        produces = { "application/octet-stream", "application/problem+json" }
    )
    ResponseEntity<org.springframework.core.io.Resource> downloadServiceLogo(
        @Min(0L) @Max(9223372036854775807L) @Parameter(name = "id", description = "", required = true) @PathVariable("id") Long id
    );


    @Operation(
        operationId = "downloadServiceLogoMiniature",
        summary = "Retrieve the service logo miniature",
        tags = { "service" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = org.springframework.core.io.Resource.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = org.springframework.core.io.Resource.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "401", description = "Required credentials missing.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "403", description = "Agent not authorized for the operation.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "503", description = "Service Unavailable.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "200", description = "Unexpected error.", content = {
                @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/services/{id}/logo-miniature",
        produces = { "application/octet-stream", "application/problem+json" }
    )
    ResponseEntity<org.springframework.core.io.Resource> downloadServiceLogoMiniature(
        @Min(0L) @Max(9223372036854775807L) @Parameter(name = "id", description = "", required = true) @PathVariable("id") Long id
    );


    @Operation(
        operationId = "listServices",
        summary = "Retrieve the service list.",
        tags = { "service" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = ServiceList.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ServiceList.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "401", description = "Required credentials missing.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "403", description = "Agent not authorized for the operation.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "503", description = "Service Unavailable.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "200", description = "Unexpected error.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/services",
        produces = { "application/hal+json", "application/problem+json" }
    )
    ResponseEntity<ServiceList> listServices(
        @NotNull @Parameter(name = "sort", description = "Sorting field.", required = true) @Valid @RequestParam(value = "sort", required = true, defaultValue = "id") ServiceOrdering sort,
        @NotNull @Parameter(name = "sort_direction", description = "Direction Sorting field.", required = true) @Valid @RequestParam(value = "sort_direction", required = true, defaultValue = "desc") Direction sortDirection,
        @Min(1) @Max(100) @Parameter(name = "limit", description = "Max number of provided items.") @Valid @RequestParam(value = "limit", required = false) Integer limit,
        @Min(0L) @Max(9223372036854775807L) @Parameter(name = "offset", description = "offset (zero-based) of indexed results.") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset,
        @Pattern(regexp = "^[^\\u0000]*$") @Size(max = 255) @Parameter(name = "q", description = "generic query.") @Valid @RequestParam(value = "q", required = false) String q,
        @Parameter(name = "with_roles", description = "Retrieve the items for whose the principal has the roles.") @Valid @RequestParam(value = "with_roles", required = false) List<String> withRoles
    );


    @Operation(
        operationId = "readService",
        summary = "Retrieve a service.",
        tags = { "service" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Service.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Service.class))
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "401", description = "Required credentials missing.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "403", description = "Agent not authorized for the operation.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "404", description = "Not Found.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "503", description = "Service Unavailable.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            }),
            @ApiResponse(responseCode = "200", description = "Unexpected error.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Problem.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))
            })
        }
    )
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/services/{id}",
        produces = { "application/hal+json", "application/problem+json" }
    )
    ResponseEntity<Service> readService(
        @Min(0L) @Max(9223372036854775807L) @Parameter(name = "id", description = "", required = true) @PathVariable("id") Long id
    );

}
