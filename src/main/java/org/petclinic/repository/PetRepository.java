package org.petclinic.repository;

import java.util.List;
import java.util.Optional;
import org.petclinic.domain.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Pet entity.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    default Optional<Pet> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Pet> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Pet> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select pet from Pet pet left join fetch pet.type left join fetch pet.owner",
        countQuery = "select count(pet) from Pet pet"
    )
    Page<Pet> findAllWithToOneRelationships(Pageable pageable);

    @Query("select pet from Pet pet left join fetch pet.type left join fetch pet.owner")
    List<Pet> findAllWithToOneRelationships();

    @Query("select pet from Pet pet left join fetch pet.type left join fetch pet.owner where pet.id =:id")
    Optional<Pet> findOneWithToOneRelationships(@Param("id") Long id);
}
