package org.springframework.samples.petclinic.owner;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.CheckedTemplate;
import org.springframework.data.domain.Page;
import org.springframework.samples.petclinic.system.Result;

import java.util.List;

@CheckedTemplate(basePath = "owners")
public class OwnerTemplates {

    public static native TemplateInstance findOwners(List<String> errors);

	public static native TemplateInstance ownersList(List<Owner> owners, int currentPage, Page<Owner> page);

	public static native TemplateInstance ownerDetails(Owner owner, Result result);

	public static native TemplateInstance createOrUpdateOwnerForm(Owner owner, Result result);

}
