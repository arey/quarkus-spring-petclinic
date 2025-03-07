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

import java.time.LocalDate;
import java.util.Optional;

import io.quarkus.qute.TemplateInstance;
import jakarta.validation.Validator;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.HeaderParam;
import org.springframework.samples.petclinic.system.I18nHelper;
import org.springframework.samples.petclinic.system.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Wick Dynex
 */
@RestController
@RequestMapping("/owners/{ownerId}/pets")
class PetController {

	private final OwnerRepository owners;

	private final PetTypeRepository petTypes;

	private final Validator validator;

	public PetController(OwnerRepository owners, PetTypeRepository petTypes, Validator validator) {
		this.owners = owners;
		this.petTypes = petTypes;
		this.validator = validator;
	}

	private Owner findOwner(@PathVariable("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		return optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
	}

	private Pet findPet(@PathVariable("ownerId") int ownerId,
			@PathVariable(name = "petId", required = false) Integer petId) {

		if (petId == null) {
			return new Pet();
		}

		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		return owner.getPet(petId);
	}


	@GetMapping("/new")
	public TemplateInstance initCreationForm(@PathVariable("ownerId") int ownerId) {
		Owner owner = findOwner(ownerId);
		Pet pet = new Pet();
		owner.registerPet(pet);
		return PetTemplates.createOrUpdatePetForm(owner, pet, petTypes.findAllByOrderByName(), Result.empty());
	}

	@PostMapping("/new")
	public TemplateInstance processCreationForm(@PathVariable int ownerId, Pet pet,
												@HeaderParam("Accept-Language") @DefaultValue("en") String language) {
		Owner owner = findOwner(ownerId);

		Result result = Result.from(validator.validate(pet));
		if (result.hasErrors()) {
			return PetTemplates.createOrUpdatePetForm(owner, pet, petTypes.findAllByOrderByName(), result);
		}

		if (StringUtils.hasText(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null)
			return PetTemplates.createOrUpdatePetForm(owner, pet, petTypes.findAllByOrderByName(),
				Result.error("name", I18nHelper.lookupAppMessages(language).duplicate()));

		LocalDate currentDate = LocalDate.now();
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {
			result = Result.error("birthDate", I18nHelper.lookupAppMessages(language).typeMismatch_birthDate());
			return PetTemplates.createOrUpdatePetForm(owner, pet, petTypes.findAllByOrderByName(), result);
		}

		owner.registerPet(pet);
		owner = this.owners.save(owner);
		return OwnerTemplates.ownerDetails(owner, Result.success("New Pet has been Added"));
	}

	@GetMapping("/{petId}/edit")
	public TemplateInstance initUpdateForm(@PathVariable("ownerId") int ownerId, @PathVariable(name = "petId", required = false) Integer petId) {
		return PetTemplates.createOrUpdatePetForm(findOwner(1), findPet(ownerId, petId), petTypes.findAllByOrderByName(), Result.empty());
	}

	@PostMapping("/{petId}/edit")
	public TemplateInstance processUpdateForm(@PathVariable int ownerId, @PathVariable int petId, Pet pet,
											  @HeaderParam("Accept-Language") String language) {

		String petName = pet.getName();
		pet.setId(petId);
		Owner owner = findOwner(ownerId);

		Result result = Result.from(validator.validate(pet));
		if (result.hasErrors()) {
			return PetTemplates.createOrUpdatePetForm(owner, pet, petTypes.findAllByOrderByName(), result);
		}

		// checking if the pet name already exist for the owner
		if (StringUtils.hasText(petName)) {
			Pet existingPet = owner.getPet(petName, false);
			if (existingPet != null && !existingPet.getId().equals(pet.getId())) {
				result = Result.error("name", I18nHelper.lookupAppMessages(language).duplicate());
				return PetTemplates.createOrUpdatePetForm(owner, pet, petTypes.findAllByOrderByName(), result);
			}
		}

		LocalDate currentDate = LocalDate.now();
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {
			result = Result.error("birthDate", I18nHelper.lookupAppMessages(language).typeMismatch_birthDate());
			return PetTemplates.createOrUpdatePetForm(owner, pet, petTypes.findAllByOrderByName(), result);
		}

		owner.registerPet(pet);
		this.owners.save(owner);
		return OwnerTemplates.ownerDetails(owner, Result.success("Pet details has been edited"));
	}

}
