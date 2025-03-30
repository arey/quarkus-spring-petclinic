package org.springframework.samples.petclinic.vet;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import org.springframework.data.domain.Page;

import java.util.List;

@CheckedTemplate(basePath = "vets")
public class VetTemplates {

	public static native TemplateInstance vetList(List<Vet> vets, int currentPage, Page<Vet> page);

}
