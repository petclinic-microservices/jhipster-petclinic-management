package org.petclinic.repository;

import org.petclinic.domain.Specialty;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Specialty entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {}
