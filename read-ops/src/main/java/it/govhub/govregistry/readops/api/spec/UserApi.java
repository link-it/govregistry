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
import it.govhub.govregistry.commons.api.beans.User;
import it.govhub.govregistry.commons.api.beans.UserList;
import it.govhub.govregistry.commons.api.beans.UserOrdering;

@Generated(value = "it.govhub.openapi.codegen.GovhubCodegenGenerator", date = "2023-03-30T13:10:58.167825+02:00[Europe/Rome]")
@Validated
@Tag(name = "User", description = "the User API")
@RequestMapping("/v1")
public interface UserApi {

    @Operation(
        operationId = "listUsers",
        summary = "Retrieve the user list.",
        tags = { "user" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = UserList.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = UserList.class))
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
        value = "/users",
        produces = { "application/hal+json", "application/problem+json" }
    )
    ResponseEntity<UserList> listUsers(
        @NotNull @Parameter(name = "sort", description = "Sorting field.", required = true) @Valid @RequestParam(value = "sort", required = true, defaultValue = "id") UserOrdering sort,
        @NotNull @Parameter(name = "sort_direction", description = "Direction Sorting field.", required = true) @Valid @RequestParam(value = "sort_direction", required = true, defaultValue = "desc") Direction sortDirection,
        @Min(1) @Max(100) @Parameter(name = "limit", description = "Max number of provided items.") @Valid @RequestParam(value = "limit", required = false) Integer limit,
        @Min(0L) @Max(9223372036854775807L) @Parameter(name = "offset", description = "offset (zero-based) of indexed results.") @Valid @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset,
        @Pattern(regexp = "^[^\\u0000]*$") @Size(max = 255) @Parameter(name = "q", description = "generic query.") @Valid @RequestParam(value = "q", required = false) String q,
        @Parameter(name = "enabled", description = "filter by status.") @Valid @RequestParam(value = "enabled", required = false) Boolean enabled
    );


    @Operation(
        operationId = "readUser",
        summary = "Retrieve a user.",
        tags = { "user" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = User.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = User.class))
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
        value = "/users/{id}",
        produces = { "application/hal+json", "application/problem+json" }
    )
    ResponseEntity<User> readUser(
        @Min(0L) @Max(9223372036854775807L) @Parameter(name = "id", description = "", required = true) @PathVariable("id") Long id
    );

}
