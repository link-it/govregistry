/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (6.2.1).
 * https://openapi-generator.tech
 * Do not edit the class manually.
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
public interface UserApi {

    /**
     * GET /users : Retrieve the user list.
     * Retrieve the user list.
     *
     * @param sort Sorting field. (required)
     * @param sortDirection Direction Sorting field. (required)
     * @param limit Max number of provided items. (optional)
     * @param offset offset (zero-based) of indexed results. (optional, default to 0)
     * @param q generic query. (optional)
     * @param enabled filter by status. (optional)
     * @return Successful operation. (status code 200)
     *         or Bad Request. (status code 400)
     *         or Required credentials missing. (status code 401)
     *         or Agent not authorized for the operation. (status code 403)
     *         or Too many requests. (status code 429)
     *         or Service Unavailable. (status code 503)
     *         or Unexpected error. (status code 200)
     */
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


    /**
     * GET /users/{id} : Retrieve a user.
     * Retrieve the user with the provided id.
     *
     * @param id  (required)
     * @return Successful operation. (status code 200)
     *         or Bad Request. (status code 400)
     *         or Required credentials missing. (status code 401)
     *         or Agent not authorized for the operation. (status code 403)
     *         or Not Found. (status code 404)
     *         or Too many requests. (status code 429)
     *         or Service Unavailable. (status code 503)
     *         or Unexpected error. (status code 200)
     */
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
