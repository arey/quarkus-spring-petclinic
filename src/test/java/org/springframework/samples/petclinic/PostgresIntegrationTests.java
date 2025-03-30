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

package org.springframework.samples.petclinic;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.vet.VetRepository;

import static org.hamcrest.Matchers.containsString;

@QuarkusTest
@TestProfile(Profiles.Postgres.class)
class PostgresIntegrationTests {


	@Autowired
	private VetRepository vets;

	@Test
	void testFindAll() {
		vets.findAll();
	}

	@Test
	void testOwnerDetails() {
		RestAssured.when().get("/owners/1")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body(containsString("Owner Information"))
			.body(containsString("George Franklin"))
			.body(containsString("110 W. Liberty St."))
			.body(containsString("Madison"))
			.body(containsString("6085551023"))
			.body(containsString("Leo"))
			.body(containsString("cat"));
	}
}




