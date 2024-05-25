package org.petclinic.repository;

import org.petclinic.domain.VetSpecialty;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the VetSpecialty entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VetSpecialtyRepository extends JpaRepository<VetSpecialty, Long> {}
