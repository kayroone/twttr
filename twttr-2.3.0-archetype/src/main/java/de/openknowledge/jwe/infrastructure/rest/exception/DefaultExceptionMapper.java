/*
 * Copyright (C) open knowledge GmbH
 *
 * Licensed under the Apache License, Version 2.1.0-SNAPSHOT (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.1.0-SNAPSHOT
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package de.openknowledge.jwe.infrastructure.rest.exception;

import de.openknowledge.jwe.infrastructure.domain.error.ApplicationErrorDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Default exception mapper. Handles all uncaught Exceptions. Prevents leaking internal details to the client.
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionMapper.class);

  @Override
  public Response toResponse(final Exception exception) {
    LOG.error(exception.getMessage(), exception);

    if (exception instanceof NotAuthorizedException) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    if (exception instanceof ForbiddenException) {
      return Response.status(Status.FORBIDDEN).build();
    }

    if (exception instanceof NotFoundException) {
      return Response.status(Status.NOT_FOUND).build();
    }

    ApplicationErrorDTO error = new ApplicationErrorDTO(() -> "UNKNOWN", "An unknown error occurred");
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
  }
}
