package org.springframework.samples.petclinic.owner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PetTypeRepository extends JpaRepository<PetType, Integer> {

	/**
	 * Retrieve all {@link PetType}s from the data store.
	 * @return a Collection of {@link PetType}s.
	 */
	List<PetType> findAllByOrderByName();

	/**
	 * Retrieve all {@link PetType}s from the data store.
	 * @return a Collection of {@link PetType}s.
	 */
	// FIXME : à tester @Query("SELECT ptype FROM PetType ptype ORDER BY ptype.name")
	// List<PetType> findPetTypes();
}
