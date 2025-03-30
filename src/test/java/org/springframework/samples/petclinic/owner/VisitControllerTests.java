/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.owner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@QuarkusTest
@TestHTTPEndpoint(VisitController.class)
class VisitControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@InjectMock
	private OwnerRepository owners;

	@BeforeEach
	void init() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		owner.registerPet(pet);
		pet.setId(TEST_PET_ID);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
	}

	@Test
	void testInitNewVisitForm() {
		when().get("/new", TEST_OWNER_ID, TEST_PET_ID)
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body("html.body.form.@id", is("add-visit-form"));
	}

	@Test
	void testProcessNewVisitFormSuccess() {
		given().param("name", "George")
			.param("description", "Visit Description")
			.when()
			.post("/new", TEST_OWNER_ID, TEST_PET_ID)
			.then()
			.statusCode(200)
			.body("html.body.div.span", is("Your visit has been booked"));
	}

	@Test
	void testProcessNewVisitFormHasErrors() {
		given().param("name", "George")
			.when()
			.post("/new", TEST_OWNER_ID, TEST_PET_ID)
			.then()
			.statusCode(200)
			.body("html.body.form.@id", is("add-visit-form"))
			.body(containsString("must not be blank"));
	}

}
