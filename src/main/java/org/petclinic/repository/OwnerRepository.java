package org.petclinic.repository;

import org.petclinic.domain.Owner;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Owner entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {}
