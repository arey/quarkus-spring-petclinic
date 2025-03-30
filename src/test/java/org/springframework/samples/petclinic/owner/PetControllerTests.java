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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@QuarkusTest
@TestHTTPEndpoint(PetController.class)
class PetControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@InjectMock
	private OwnerRepository owners;

	@InjectMock
	private PetTypeRepository petTypes;

	Owner owner = new Owner();

	@BeforeEach
	void setup() {
		PetType cat = new PetType();
		cat.setId(3);
		cat.setName("hamster");
		given(this.petTypes.findAllByOrderByName()).willReturn(List.of(cat));

		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Pet pet = new Pet();
		Pet dog = new Pet();
		owner.registerPet(pet);
		owner.registerPet(dog);
		pet.setId(TEST_PET_ID);
		dog.setId(TEST_PET_ID + 1);
		pet.setName("petty");
		pet.setBirthDate(LocalDate.of(2025, 3, 9));
		dog.setName("doggy");
		dog.setBirthDate(LocalDate.of(2025, 2, 23));
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
	}

	@Test
	void testInitCreationForm() {
		when().get("/new", TEST_OWNER_ID)
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body("html.body.form.@id", is("add-pet-form"));
	}

	@Test
	void testProcessCreationFormSuccess() {
		given(this.owners.save(owner)).willReturn(owner);
		given().param("name", "Betty")
			.param("type", "hamster")
			.param("birthDate", "2015-02-12")
			.when()
			.post("/new", TEST_OWNER_ID)
			.then()
			.statusCode(200)
			.body("html.body.div.span", is("New Pet has been Added"));
	}

	@Nested
	class ProcessCreationFormHasErrors {

		@Test
		void testProcessCreationFormWithBlankName() {
			given().param("name", "\t \n")
				.param("birthDate", "2015-02-12")
				.when()
				.post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
				.then()
				.statusCode(200)
				.body("html.body.form.@id", is("add-pet-form"))
				.body(containsString("must not be blank"));
		}

		@Test
		void testProcessCreationFormWithDuplicateName() {
			given().param("name", "petty")
				.param("birthDate", "2015-02-12")
				.param("type", "hamster")
				.when()
				.post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
				.then()
				.statusCode(200)
				.body("html.body.form.@id", is("add-pet-form"))
				.body(containsString("is already in use"));
		}

		@Test
		void testProcessCreationFormWithMissingPetType() {
			given().param("name", "Betty")
				.param("birthDate", "2015-02-12")
				.when()
				.post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
				.then()
				.statusCode(200)
				.body("html.body.form.@id", is("add-pet-form"))
				.body(containsString("must not be null"));
		}

		@Test
		void testProcessCreationFormWithInvalidBirthDate() {
			LocalDate currentDate = LocalDate.now();
			String futureBirthDate = currentDate.plusMonths(1).toString();

			given().param("name", "Betty")
				.param("birthDate", futureBirthDate)
				.param("type", "hamster")
				.when()
				.post("/owners/{ownerId}/pets/new", TEST_OWNER_ID)
				.then()
				.statusCode(200)
				.body("html.body.form.@id", is("add-pet-form"))
				.body(containsString("invalid date"));
		}

	}

	@Test
	void testInitUpdateForm() {
		RestAssured.when()
			.get("/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body("html.body.form.@id", is("add-pet-form"));
	}

	@Test
	void testProcessUpdateFormSuccess() {
		given().param("name", "Betty")
			.param("type", "hamster")
			.param("birthDate", "2015-02-12")
			.when()
			.post("/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
			.then()
			.statusCode(200)
			.body("html.body.div.span", is("Pet details has been edited"));
	}

	@Nested
	class ProcessUpdateFormHasErrors {

		@Test
		void testProcessUpdateFormWithInvalidBirthDate() {
			given().param("name", " ")
				.param("birthDate", "2015/02/12")
				.when()
				.post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
				.then()
				.statusCode(200)
				.body("html.body.form.@id", is("add-pet-form"))
				.body(containsString("must not be null"));
		}

		@Test
		void testProcessUpdateFormWithBlankName() {
			given().param("name", "  ")
				.param("birthDate", "2015-02-12")
				.when()
				.post("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID)
				.then()
				.statusCode(200)
				.body("html.body.form.@id", is("add-pet-form"))
				.body(containsString("must not be blank"));
		}

	}

}
