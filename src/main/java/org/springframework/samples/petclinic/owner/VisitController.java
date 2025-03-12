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

import java.util.Optional;

import io.quarkus.qute.TemplateInstance;
import jakarta.validation.Validator;
import org.springframework.samples.petclinic.system.Result;
import org.springframework.web.bind.annotation.*;


/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Wick Dynex
 * @author Antoine Rey
 */
@RestController
@RequestMapping("/owners/{ownerId}/pets/{petId}/visits")
class VisitController {

	private final OwnerRepository owners;

	private final Validator validator;

	public VisitController(OwnerRepository owners, Validator validator) {
		this.owners = owners;
		this.validator = validator;
	}

	@GetMapping("/new")
	public TemplateInstance initNewVisitForm(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId) {
		Owner owner = loadOwner(ownerId);
		Pet pet = owner.getPet(petId);
		Visit visit = new Visit();
		pet.addVisit(visit);
		return PetTemplates.createOrUpdateVisitForm(owner, pet, visit, Result.empty());
	}

	@PostMapping("/new")
	public TemplateInstance processNewVisitForm(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId, Visit visit) {
		Owner owner = loadOwner(ownerId);
		Pet pet = owner.getPet(petId);
		Result result = Result.from(validator.validate(visit));
		if (result.hasErrors()) {
			return PetTemplates.createOrUpdateVisitForm(owner, pet, visit, result);
		}

		owner.addVisit(petId, visit);
		this.owners.save(owner);
		return OwnerTemplates.ownerDetails(owner, Result.success("Your visit has been booked"));
	}

	private Owner loadOwner(int ownerId) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		return optionalOwner.orElseThrow(() -> new IllegalArgumentException(
			"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
	}
}
