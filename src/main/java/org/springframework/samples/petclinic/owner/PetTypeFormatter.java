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

import jakarta.ws.rs.ext.ParamConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Instructs JAX-RS on how to parse and print elements of type 'PetType'.
 * This class implements the ParamConverter interface from JAX-RS.
 * <p>
 * Previously, this class was used in a Spring MVC context as a Formatter.
 * It has been migrated to Quarkus and now implements the JAX-RS ParamConverter interface.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Michael Isvy
 */
@Component
public class PetTypeFormatter implements ParamConverter<PetType> {

	private final PetTypeRepository petTypes;

	public PetTypeFormatter(PetTypeRepository petTypes) {
		this.petTypes = petTypes;
	}

	@Override
	public String toString(PetType petType) {
		return petType.getName();
	}

	@Override
	public PetType fromString(String text) {
		Collection<PetType> findPetTypes = this.petTypes.findAllByOrderByName();
		for (PetType type : findPetTypes) {
			if (type.getName().equals(text)) {
				return type;
			}
		}
		throw new IllegalArgumentException("type not found: " + text);
	}

}
