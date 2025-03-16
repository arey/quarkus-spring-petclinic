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

package org.springframework.samples.petclinic.system;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

/**
 * Test class for {@link CrashController}
 *
 * @author Colin But
 * @author Alex Lutz
 */
@QuarkusTest
class CrashControllerTests {

	@Test
	void testTriggerException() {
		given()
			.when().get("/oups")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.body(containsString("Expected: controller used to showcase what happens when an exception is thrown"));
	}
}
