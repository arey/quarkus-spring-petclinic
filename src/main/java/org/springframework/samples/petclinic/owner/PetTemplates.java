package org.springframework.samples.petclinic.owner;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import org.springframework.samples.petclinic.system.Result;

import java.util.List;

@CheckedTemplate(basePath = "pets")
public class PetTemplates {

	public static native TemplateInstance createOrUpdatePetForm(Owner owner, Pet pet, List<PetType> petTypes,
			Result result);

	public static native TemplateInstance createOrUpdateVisitForm(Owner owner, Pet pet, Visit visit, Result result);

}
