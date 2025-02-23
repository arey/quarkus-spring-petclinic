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

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@QuarkusTest
@TestHTTPEndpoint(OwnerController.class)
class OwnerControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@InjectMock
	private OwnerRepository owners;

	private Owner george() {
		Owner george = new Owner();
		george.setId(TEST_OWNER_ID);
		george.setFirstName("George");
		george.setLastName("Franklin");
		george.setAddress("110 W. Liberty St.");
		george.setCity("Madison");
		george.setTelephone("6085551023");
		Pet max = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		max.setType(dog);
		max.setName("Max");
		max.setBirthDate(LocalDate.now());
		george.addPet(max);
		max.setId(1);
		return george;
	}

	@BeforeEach
	void setup() {

		Owner george = george();
		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class)))
			.willReturn(new PageImpl<>(List.of(george)));

		given(this.owners.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of(george)));

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(george));
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		george.getPet("Max").getVisits().add(visit);

	}

	@Test
	void testInitCreationForm() {
		RestAssured.when().get("/new")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body("html.body.form.@id", is("add-owner-form"));
	}

	@Test
	void testProcessCreationFormSuccess() {
		RestAssured.given()
			.param("firstName", "Joe")
			.param("lastName", "Bloggs")
			.param("address", "123 Caramel Street")
			.param("city", "London")
			.param("telephone", "1316761638")
			.when()
			.post("/new")
			.then()
			.statusCode(200)
			.body("html.body.div.span", is("New Owner Created"));
	}

	@Test
	void testProcessCreationFormHasErrors() {
		RestAssured.given()
			.param("firstName", "Joe")
			.param("lastName", "Bloggs")
			.param("city", "London")
			.when()
			.post("/new")
			.then()
			.statusCode(200)
			.body("html.body.form.@id", is("add-owner-form"))
			.body("html.body.form.div[0].div[2].div.span[1]", is("must not be blank"))
			.body("html.body.form.div[0].div[4].div.span[1]", is("must not be blank"));
	}

	@Test
	void testInitFindForm() {
		RestAssured.when().get("/find")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body("html.body.form.@id", is("search-owner-form"));
	}

	@Test
	void testProcessFindFormSuccess() {
		Page<Owner> tasks = new PageImpl<>(List.of(george(), new Owner()), PageRequest.of(1, 1), 10);
		when(this.owners.findByLastNameStartingWith(anyString(), any(Pageable.class))).thenReturn(tasks);

		RestAssured.given()
			.param("page", 1)
			.when()
			.get("/")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body("html.body.div.span[0]", is("Pages:"));
	}

	@Test
	void testProcessFindFormByLastName() {
		Page<Owner> tasks = new PageImpl<>(List.of(george()));
		when(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class))).thenReturn(tasks);

		RestAssured.given()
			.param("page", 1)
			.param("lastName", "Franklin")
			.when()
			.get("/")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body("html.body.h2[0]", is("Owner Information"));
	}

	@Test
	void testProcessFindFormNoOwnersFound() {
		Page<Owner> tasks = new PageImpl<>(List.of());
		when(this.owners.findByLastNameStartingWith(eq("Unknown Surname"), any(Pageable.class))).thenReturn(tasks);

		RestAssured.given()
			.param("page", 1)
			.param("lastName", "Unknown Surname")
			.header("accept-language", "en-US")
			.when()
			.get("/")
			.then()
			.statusCode(200)
			.body("html.body.form.@id", is("search-owner-form"))
			.body("html.body.form.div.div.div.div.p", is("has not been found"));
	}

	@Test
	void testInitUpdateOwnerForm() {
		RestAssured.when().get("/{ownerId}/edit", TEST_OWNER_ID)
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body("html.body.form.@id", is("add-owner-form"))
			.body("html.body.form.div.div.div[0].input.@value", is("George"))
			.body("html.body.form.div.div.div[1].input.@value", is("Franklin"))
			.body("html.body.form.div.div.div[2].input.@value", is("110 W. Liberty St."))
			.body("html.body.form.div.div.div[3].input.@value", is("Madison"))
			.body("html.body.form.div.div.div[4].input.@value", is("6085551023"));
	}

	@Test
	void testProcessUpdateOwnerFormSuccess() {
		RestAssured.given()
			.param("firstName", "Joe")
			.param("lastName", "Bloggs")
			.param("address", "123 Caramel Street")
			.param("city", "London")
			.param("telephone", "1616291589")
			.when()
			.post("/{ownerId}/edit", TEST_OWNER_ID)
			.then()
			.statusCode(200)
			.body("html.body.div.span", is("Owner Values Updated"));
	}


	@Test
	void testProcessUpdateOwnerFormHasErrors() {
		RestAssured.given()
			.param("firstName", "Joe")
			.param("lastName", "Bloggs")
			.param("address", "")
			.param("telephone", "12")
			.when()
			.post("/{ownerId}/edit", TEST_OWNER_ID)
			.then()
			.statusCode(200)
			.body("html.body.form.@id", is("add-owner-form"))
			.body("html.body.form.div[0].div[2].div.span[1]", is("must not be blank"))
			.body("html.body.form.div[0].div[4].div.span[1]", is("Telephone must be a 10-digit number"));
	}

	@Test
	void testShowOwner() {
		RestAssured.when().get("/{ownerId}", TEST_OWNER_ID)
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body("html.body.h2[0]", is("Owner Information"))
			.body("html.body.table[0].tr[0].td.b", is("George Franklin"))
			.body("html.body.table[0].tr[1].td", is("110 W. Liberty St."))
			.body("html.body.table[0].tr[2].td", is("Madison"))
			.body("html.body.table[0].tr[3].td", is("6085551023"))
			.body("html.body.table[1].tr.td.dl.dd[0]", is("Max"))
			.body("html.body.table[1].tr.td.dl.dd[2]", is("dog"));
	}

}
