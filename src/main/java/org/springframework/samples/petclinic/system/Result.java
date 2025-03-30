package org.springframework.samples.petclinic.system;

import jakarta.validation.ConstraintViolation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple result record that can be used to return a success or error message.
 */
public record Result(String message, Boolean success, Map<String, FieldError> fieldErrors) {

	public Result() {
		this("", null, Map.of());
	}

	public Result(String successMessage) {
		this(successMessage, true, Map.of());
	}

	public Result(Set<? extends ConstraintViolation<?>> violations) {
		this(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", ")),
				violations.isEmpty(), FieldError.from(violations));
	}

	public boolean isSuccess() {
		return Boolean.TRUE.equals(success);
	}

	public boolean hasErrors() {
		return Boolean.FALSE.equals(success);
	}

	public boolean hasErrors(String fieldName) {
		return hasErrors() && fieldErrors.containsKey(fieldName);
	}

	public String getErrorMessage(String field) {
		return fieldErrors.entrySet()
			.stream()
			.filter(entry -> entry.getKey().equals(field))
			.flatMap(entry -> entry.getValue().messages().stream())
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

	public static Result error(String fieldName, String errorMessage) {
		return new Result(errorMessage, false, Map.of(fieldName, new FieldError(fieldName, Set.of(errorMessage))));
	}

	record FieldError(String field, Set<String> messages) {

		static Map<String, FieldError> from(Set<? extends ConstraintViolation<?>> violations) {
			Map<String, FieldError> fieldErrorMap = new HashMap<>();
			for (ConstraintViolation<?> violation : violations) {
				String field = violation.getPropertyPath().toString();
				String message = violation.getMessage();
				fieldErrorMap.computeIfAbsent(field, k -> new FieldError(field, new HashSet<>()))
					.messages()
					.add(message);
			}
			return fieldErrorMap;
		}
	}
}
