package org.springframework.samples.petclinic.system;

import jakarta.validation.ConstraintViolation;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple result record that can be used to return a success or error message.
 */
public record Result(String message, Boolean success, Set<? extends ConstraintViolation<?>> violations) {

	public Result() {
		this("", null, Collections.emptySet());
	}

	public Result(String successMessage) {
		this(successMessage, true, Collections.emptySet());
	}

	public Result(Set<? extends ConstraintViolation<?>> violations) {
		this(violations.stream()
				.map(ConstraintViolation::getMessage)
				.collect(Collectors.joining(", ")),
			violations.isEmpty(),
			violations);
	}

	public boolean isSuccess() {
		return Boolean.TRUE.equals(success);
	}

	public boolean hasErrors() {
		return Boolean.FALSE.equals(success);
	}

	public boolean hasErrors(String field) {
		return hasErrors() && violations.stream()
			.anyMatch(v -> v.getPropertyPath().toString().equals(field));
	}

	public String getErrorMessage(String field) {
		return violations.stream()
			.filter(v -> v.getPropertyPath().toString().equals(field))
			.map(ConstraintViolation::getMessage)
			.collect(Collectors.joining(", "));
	}

	public static Result empty() {
		return new Result();
	}

	public static Result success(String successMessage) {
		return new Result(successMessage);
	}

	public static Result from(Set<? extends ConstraintViolation<?>> violations) {
		return new Result(violations);
	}
}
