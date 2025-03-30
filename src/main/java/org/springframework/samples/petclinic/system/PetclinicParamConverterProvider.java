package org.springframework.samples.petclinic.system;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeFormatter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;

@Provider
public class PetclinicParamConverterProvider implements ParamConverterProvider {

	private final PetTypeFormatter petTypeFormatter;

	public PetclinicParamConverterProvider(PetTypeFormatter petTypeFormatter) {
		this.petTypeFormatter = petTypeFormatter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
		if (rawType.equals(PetType.class)) {
			return (ParamConverter<T>) petTypeFormatter;
		}
		else if (rawType.equals(LocalDate.class)) {
			return (ParamConverter<T>) new LocalDateParamConverter();
		}
		return null;
	}

}
