package org.springframework.samples.petclinic.system;

import jakarta.ws.rs.ext.ParamConverter;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class LocalDateParamConverter implements ParamConverter<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	// Declare a slf4j logger
	private static final Logger LOG = Logger.getLogger(LocalDateParamConverter.class);

    @Override
    public LocalDate fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value, FORMATTER);
        } catch (DateTimeParseException e) {
			LOG.warn("Invalid date format", e);
			return null;
        }
    }

    @Override
    public String toString(LocalDate value) {
        return value != null ? value.format(FORMATTER) : "";
    }
}
