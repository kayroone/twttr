package de.openknowledge.jwe.application.user;

import de.openknowledge.jwe.domain.model.user.User;
import de.openknowledge.jwe.domain.model.user.UserRole;
import de.openknowledge.jwe.domain.repository.UserRepository;
import de.openknowledge.jwe.infrastructure.constants.Constants;
import de.openknowledge.jwe.infrastructure.domain.entity.EntityNotFoundException;
import de.openknowledge.jwe.infrastructure.domain.error.ApplicationErrorDTO;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.stream.Collectors;

@Path(Constants.USERS_API_URI)
@Produces({MediaType.APPLICATION_JSON})
public class UserResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Inject
    private UserRepository userRepository;

    @Context
    private SecurityContext securityContext;

    @GET
    @PermitAll
    @Operation(description = "Search for user by a given keyword")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @APIResponse(responseCode = "204", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApplicationErrorDTO.class)))
    })
    public Response searchUser(@Parameter(description = "Keyword matching the username")
                               @Size(min = 1, max = 100) @QueryParam("keyword") final String keyword) {

        List<UserListDTO> usersFound = userRepository
                .search(keyword, -1, -1)
                .stream()
                .map(UserListDTO::new)
                .collect(Collectors.toList());

        if (!usersFound.isEmpty()) {
            LOG.info("User found {}", usersFound);
            return Response.status(Response.Status.OK)
                    .entity(new GenericEntity<List<UserListDTO>>(usersFound) {
                    }).build();
        } else {
            LOG.info("No User found with keyword {}", keyword);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    @PUT
    @Transactional
    @RolesAllowed(UserRole.USER)
    @Operation(description = "Follow a given user")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "User followed",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @APIResponse(responseCode = "202", description = "User already followed",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @APIResponse(responseCode = "204", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApplicationErrorDTO.class)))
    })
    public Response followUser(@Parameter(description = "The Id of the user that will be followed")
                               @Min(1) @Max(10000) @QueryParam("id") final Long userId) {

        User user = userRepository
                .getReferenceByUsername(securityContext.getUserPrincipal().getName());

        try {
            User userToFollow = userRepository.find(userId);

            if (userToFollow.hasFollower(user)) {
                LOG.info("User with id {} already followed by user {}. No action required.", userId, user);
                return Response.status(Response.Status.ACCEPTED).build();
            }

            userToFollow.follow(user);
            userRepository.update(userToFollow);

            LOG.info("User with id {} successfully followed by user {}", userId, user);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (EntityNotFoundException e) {
            LOG.warn("User with id {} not found", userId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Transactional
    @RolesAllowed(UserRole.USER)
    @Operation(description = "Unfollow a given user")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "User unfollowed",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @APIResponse(responseCode = "202", description = "User not followed",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @APIResponse(responseCode = "204", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApplicationErrorDTO.class)))
    })
    public Response unfollowUser(@Parameter(description = "The Id of the user that will be unfollowed")
                                 @Min(1) @Max(10000) @QueryParam("id") final Long userId) {

        User user = userRepository
                .getReferenceByUsername(securityContext.getUserPrincipal().getName());

        try {
            User userToUnfollow = userRepository.find(userId);

            if (!userToUnfollow.hasFollower(user)) {
                LOG.info("User with id {} is not followed by user {}. No action required.", userId, user);
                return Response.status(Response.Status.ACCEPTED).build();
            }

            userToUnfollow.unfollow(user);
            userRepository.update(userToUnfollow);

            LOG.info("User with id {} successfully unfollowed by user {}", userId, user);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (EntityNotFoundException e) {
            LOG.warn("User with id {} not found", userId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
