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

import java.util.List;
import java.util.Optional;

import io.quarkus.qute.TemplateInstance;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.system.I18nHelper;
import org.springframework.samples.petclinic.system.Result;
import org.springframework.web.bind.annotation.*;


/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 * @author Antoine Rey
 */
@RestController
@RequestMapping("/owners")
class OwnerController {

	private final OwnerRepository owners;

	private final Validator validator;

	public OwnerController(OwnerRepository owners, Validator validator) {
		this.owners = owners;
		this.validator = validator;
	}

	private Owner findOwner(Integer ownerId) {
		return ownerId == null ? new Owner()
				: this.owners.findById(ownerId)
					.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId
							+ ". Please ensure the ID is correct " + "and the owner exists in the database."));
	}

	@GetMapping("/new")
	public TemplateInstance initCreationForm() {
		return OwnerTemplates.createOrUpdateOwnerForm(new Owner(), Result.empty());
	}

	@PostMapping("/new")
	public TemplateInstance processCreationForm(Owner owner) {
		Result result = Result.from(validator.validate(owner));
		if (result.hasErrors()) {
			return OwnerTemplates.createOrUpdateOwnerForm(owner, result);
		}

		this.owners.save(owner);
		return OwnerTemplates.ownerDetails(owner, Result.success("New Owner Created"));
	}

	@GetMapping("/find")
	public TemplateInstance initFindForm() {
		return OwnerTemplates.findOwners(List.of());
	}

	@GetMapping("/")
	public TemplateInstance processFindForm(@RequestParam(defaultValue = "1") int page, @RequestParam String lastName,
											@HeaderParam("Accept-Language") String language) {
		// allow parameterless GET request for /owners to return all records
		if (lastName == null) {
			lastName = ""; // empty string signifies broadest possible search
		}

		// find owners by last name
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);
		if (ownersResults.isEmpty()) {
			// no owners found
			String notFound = I18nHelper.lookupAppMessages(language).notFound();
			return OwnerTemplates.findOwners(List.of(notFound));
		}

		if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			Owner owner = ownersResults.iterator().next();
			return OwnerTemplates.ownerDetails(owner, Result.empty());
		}

		// multiple owners found
		return OwnerTemplates.ownersList(ownersResults.getContent(), page, ownersResults);
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	@GetMapping("/{ownerId}/edit")
	public TemplateInstance initUpdateOwnerForm(@PathVariable("ownerId") int ownerId) {
		Owner owner = findOwner(ownerId);
		return OwnerTemplates.createOrUpdateOwnerForm(owner, Result.empty());
	}

	@PostMapping("/{ownerId}/edit")
	public TemplateInstance processUpdateOwnerForm(Owner owner, @PathVariable("ownerId") Integer ownerId) {
		Result result = Result.from(validator.validate(owner));
		if (result.hasErrors()) {
			return OwnerTemplates.createOrUpdateOwnerForm(owner, result);
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		return OwnerTemplates.ownerDetails(owner, Result.success("Owner Values Updated"));
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/{ownerId}")
	public TemplateInstance showOwner(@PathVariable("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		return OwnerTemplates.ownerDetails(owner, Result.empty());
	}

}
