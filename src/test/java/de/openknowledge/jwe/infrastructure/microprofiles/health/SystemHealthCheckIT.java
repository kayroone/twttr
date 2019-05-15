/*
 * Copyright (C) open knowledge GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package de.openknowledge.jwe.infrastructure.microprofiles.health;

import de.openknowledge.jwe.IntegrationTestUtil;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Integration test for the MP-Health {@link SystemHealthCheck}.
 */
public class SystemHealthCheckIT {

  private String uri = IntegrationTestUtil.getHealthCheckURI();

  @Test
  public void checkHealth() {
    RestAssured.given()
        .accept(MediaType.APPLICATION_JSON)
        .when()
        .get(uri)
        .then()
        .contentType(MediaType.APPLICATION_JSON)
        .statusCode(Response.Status.OK.getStatusCode())
        //.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("json/schema/HealthCheck-schema.json"))
        .body("outcome", Matchers.equalTo("UP"))
        .body("checks[1].name", Matchers.equalTo("system"))
        .body("checks[1].state", Matchers.equalTo("UP"))
        .body("checks[1].data", Matchers.notNullValue());
  }
}