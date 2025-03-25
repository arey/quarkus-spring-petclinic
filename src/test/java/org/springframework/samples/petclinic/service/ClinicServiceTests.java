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

package org.springframework.samples.petclinic.service;


import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.*;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

/**
 * Integration test of the Service and the Repository layer.
 * <p>
 * ClinicServiceSpringDataJpaTests subclasses benefit from the following services provided
 * by the Spring TestContext Framework:
 * </p>
 * <ul>
 * <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li>
 * <li><strong>Dependency Injection</strong> of test fixture instances, meaning that we
 * don't need to perform application context lookups. See the use of
 * {@link Autowired @Autowired} on the <code> </code> instance variable, which uses
 * autowiring <em>by type</em>.
 * <li><strong>Transaction management</strong>, meaning each test method is executed in
 * its own transaction, which is automatically rolled back by default. Thus, even if tests
 * insert or otherwise change database state, there is no need for a teardown or cleanup
 * script.
 * <li>An {@link org.springframework.context.ApplicationContext ApplicationContext} is
 * also inherited and can be used for explicit bean lookup if necessary.</li>
 * </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Dave Syer
 */
@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class ClinicServiceTests {

	@Autowired
	protected OwnerRepository owners;

	@Autowired
	protected PetTypeRepository petTypes;

	@Autowired
	protected VetRepository vets;

	Pageable pageable = Pageable.unpaged();

	@Test
	void shouldFindOwnersByLastName() {
		Page<Owner> owners = this.owners.findByLastNameStartingWith("Davis", pageable);
		assertThat(owners.getSize(), is(equalTo(2)));

		owners = this.owners.findByLastNameStartingWith("Daviss", pageable);
		assertThat(owners.isEmpty(), is(true));
	}

	@Test
	void shouldFindSingleOwnerWithPet() {
		Optional<Owner> optionalOwner = this.owners.findById(1);
		assertThat(optionalOwner.isPresent(), is(true));
		Owner owner = optionalOwner.get();
		assertThat(owner.getLastName(), startsWith("Franklin"));
		assertThat(owner.getPets(), hasSize(1));
		assertThat(owner.getPets().get(0).getType(), notNullValue());
		assertThat(owner.getPets().get(0).getType().getName(), is(equalTo("cat")));
	}

	@Test
	@TestTransaction
	void shouldInsertOwner() {
		Page<Owner> owners = this.owners.findByLastNameStartingWith("Schultz", pageable);
		int found = (int) owners.getTotalElements();

		Owner owner = new Owner();
		owner.setFirstName("Sam");
		owner.setLastName("Schultz");
		owner.setAddress("4, Evans Street");
		owner.setCity("Wollongong");
		owner.setTelephone("4444444444");
		this.owners.save(owner);
		assertThat(owner.getId(), is(not(0)));

		owners = this.owners.findByLastNameStartingWith("Schultz", pageable);
		assertThat(owners.getTotalElements(), is(equalTo(found + 1L)));
	}

	@Test
	@TestTransaction
	void shouldUpdateOwner() {
		Optional<Owner> optionalOwner = this.owners.findById(1);
		assertThat(optionalOwner.isPresent(), is(true) );
		Owner owner = optionalOwner.get();
		String oldLastName = owner.getLastName();
		String newLastName = oldLastName + "X";

		owner.setLastName(newLastName);
		this.owners.save(owner);

		// retrieving new name from database
		optionalOwner = this.owners.findById(1);
		assertThat(optionalOwner.isPresent(), is(true));
		owner = optionalOwner.get();
		assertThat(owner.getLastName(), is(newLastName));
	}

	@Test
	void shouldFindAllPetTypes() {
		Collection<PetType> petTypes = this.petTypes.findAll();

		PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
		assertThat(petType1.getName(), is(equalTo("cat")));
		PetType petType4 = EntityUtils.getById(petTypes, PetType.class, 4);
		assertThat(petType4.getName(), is(equalTo("snake")));
	}

	@Test
	@TestTransaction
	void shouldInsertPetIntoDatabaseAndGenerateId() {
		Optional<Owner> optionalOwner = this.owners.findById(6);
		assertThat(optionalOwner.isPresent(), is(true));
		Owner owner6 = optionalOwner.get();

		int found = owner6.getPets().size();

		Pet pet = new Pet();
		pet.setName("bowser");
		Collection<PetType> types = this.petTypes.findAll();
		pet.setType(EntityUtils.getById(types, PetType.class, 2));
		pet.setBirthDate(LocalDate.now());
		owner6.registerPet(pet);
		assertThat(owner6.getPets(), hasSize(found + 1));

		this.owners.save(owner6);

		optionalOwner = this.owners.findById(6);
		assertThat(optionalOwner.isPresent(), is(true));
		owner6 = optionalOwner.get();
		assertThat(owner6.getPets(), hasSize(found + 1));
		// checks that id has been generated
		pet = owner6.getPet("bowser");
		assertThat(pet.getId(), is(notNullValue()));
	}

	@Test
	@TestTransaction
	void shouldUpdatePetName() {
		Optional<Owner> optionalOwner = this.owners.findById(6);
		assertThat(optionalOwner.isPresent(), is(true));
		Owner owner6 = optionalOwner.get();

		Pet pet7 = owner6.getPet(7);
		String oldName = pet7.getName();

		String newName = oldName + "X";
		pet7.setName(newName);
		this.owners.save(owner6);

		optionalOwner = this.owners.findById(6);
		assertThat(optionalOwner.isPresent(), is(true));
		owner6 = optionalOwner.get();
		pet7 = owner6.getPet(7);
		assertThat(pet7.getName(), is(equalTo(newName)));
	}

	@Test
	void shouldFindVets() {
		Collection<Vet> vets = this.vets.findAll();

		Vet vet = EntityUtils.getById(vets, Vet.class, 3);
		assertThat(vet.getLastName(), is(equalTo("Douglas")));
		assertThat(vet.getNrOfSpecialties(), is(equalTo(2)));
		assertThat(vet.getSpecialties().get(0).getName(), is(equalTo("dentistry")));
		assertThat(vet.getSpecialties().get(1).getName(), is(equalTo("surgery")));
	}

	@Test
	@TestTransaction
	void shouldAddNewVisitForPet() {
		Optional<Owner> optionalOwner = this.owners.findById(6);
		assertThat(optionalOwner.isPresent(), is(true));
		Owner owner6 = optionalOwner.get();

		Pet pet7 = owner6.getPet(7);
		int found = pet7.getVisits().size();
		Visit visit = new Visit();
		visit.setDescription("test");

		owner6.addVisit(pet7.getId(), visit);
		this.owners.save(owner6);

		assertThat(pet7.getVisits(), hasSize(found + 1));
		assertThat(pet7.getVisits(), everyItem(hasProperty("id", notNullValue())));
	}

	@Test
	void shouldFindVisitsByPetId() {
		Optional<Owner> optionalOwner = this.owners.findById(6);
		assertThat(optionalOwner.isPresent(), is(true));
		Owner owner6 = optionalOwner.get();

		Pet pet7 = owner6.getPet(7);
		Collection<Visit> visits = pet7.getVisits();

		assertThat(visits, hasSize(2));
		assertThat(visits.iterator().next().getDate(), is(notNullValue()));
	}


}
