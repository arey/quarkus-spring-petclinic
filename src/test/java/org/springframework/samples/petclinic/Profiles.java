package org.springframework.samples.petclinic;

import io.quarkus.test.junit.QuarkusTestProfile;

public class Profiles {

	public static class Postgres implements QuarkusTestProfile {

		@Override
		public String getConfigProfile() {
			return "postgres-it";
		}

	}

	public static class MySQL implements QuarkusTestProfile {

		@Override
		public String getConfigProfile() {
			return "mysql-it";
		}

	}

}
