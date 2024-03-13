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

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.govhub.govregistry.commons.api.beans.Problem;
import it.govhub.govregistry.commons.api.beans.Profile;

@Generated(value = "it.govhub.openapi.codegen.GovhubCodegenGenerator", date = "2023-03-30T13:10:58.167825+02:00[Europe/Rome]")
@Validated
@Tag(name = "Profile", description = "the Profile API")
@RequestMapping("/v1")
public interface ProfileApi {

    @Operation(
        operationId = "profile",
        summary = "Retrieve the user profile.",
        tags = { "profile" },
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful operation.", content = {
                @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Profile.class)),
                @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Profile.class))
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
        value = "/profile",
        produces = { "application/hal+json", "application/problem+json" }
    )
    ResponseEntity<Profile> profile(
        
    );

}
