package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateParamConverterTest {

	LocalDateParamConverter converter = new LocalDateParamConverter();

	@Test
	void testFromStringValidDate() {
		LocalDate date = converter.fromString("2023-10-15");
		assertThat(date, is(notNullValue()));
		assertThat(date, is(equalTo(LocalDate.of(2023, 10, 15))));
	}

	@Test
	void testFromStringInvalidDate() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			converter.fromString("invalid-date");
		});
		assertThat(exception.getMessage(), containsString("Invalid date format"));
	}

	@Test
	void testFromStringNull() {
		assertThat(converter.fromString(null), is(nullValue()));
	}

	@Test
	void testFromStringEmpty() {
		assertThat(converter.fromString(""), is(nullValue()));
	}

	@Test
	void testToStringValidDate() {
		String dateString = converter.toString(LocalDate.of(2023, 10, 15));
		assertThat(dateString, is(equalTo("2023-10-15")));
	}

	@Test
	void testToStringNull() {
		assertThat(converter.toString(null), is(equalTo("")));
	}
}
