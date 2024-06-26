package org.petclinic.service;

import java.util.Optional;
import org.petclinic.domain.Pet;
import org.petclinic.repository.PetRepository;
import org.petclinic.repository.search.PetSearchRepository;
import org.petclinic.service.dto.PetDTO;
import org.petclinic.service.mapper.PetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.petclinic.domain.Pet}.
 */
@Service
@Transactional
public class PetService {

    private final Logger log = LoggerFactory.getLogger(PetService.class);

    private final PetRepository petRepository;

    private final PetMapper petMapper;

    private final PetSearchRepository petSearchRepository;

    public PetService(PetRepository petRepository, PetMapper petMapper, PetSearchRepository petSearchRepository) {
        this.petRepository = petRepository;
        this.petMapper = petMapper;
        this.petSearchRepository = petSearchRepository;
    }

    /**
     * Save a pet.
     *
     * @param petDTO the entity to save.
     * @return the persisted entity.
     */
    public PetDTO save(PetDTO petDTO) {
        log.debug("Request to save Pet : {}", petDTO);
        Pet pet = petMapper.toEntity(petDTO);
        pet = petRepository.save(pet);
        petSearchRepository.index(pet);
        return petMapper.toDto(pet);
    }

    /**
     * Update a pet.
     *
     * @param petDTO the entity to save.
     * @return the persisted entity.
     */
    public PetDTO update(PetDTO petDTO) {
        log.debug("Request to update Pet : {}", petDTO);
        Pet pet = petMapper.toEntity(petDTO);
        pet = petRepository.save(pet);
        petSearchRepository.index(pet);
        return petMapper.toDto(pet);
    }

    /**
     * Partially update a pet.
     *
     * @param petDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PetDTO> partialUpdate(PetDTO petDTO) {
        log.debug("Request to partially update Pet : {}", petDTO);

        return petRepository
            .findById(petDTO.getId())
            .map(existingPet -> {
                petMapper.partialUpdate(existingPet, petDTO);

                return existingPet;
            })
            .map(petRepository::save)
            .map(savedPet -> {
                petSearchRepository.index(savedPet);
                return savedPet;
            })
            .map(petMapper::toDto);
    }

    /**
     * Get all the pets.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PetDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Pets");
        return petRepository.findAll(pageable).map(petMapper::toDto);
    }

    /**
     * Get all the pets with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<PetDTO> findAllWithEagerRelationships(Pageable pageable) {
        return petRepository.findAllWithEagerRelationships(pageable).map(petMapper::toDto);
    }

    /**
     * Get one pet by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PetDTO> findOne(Long id) {
        log.debug("Request to get Pet : {}", id);
        return petRepository.findOneWithEagerRelationships(id).map(petMapper::toDto);
    }

    /**
     * Delete the pet by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Pet : {}", id);
        petRepository.deleteById(id);
        petSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the pet corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PetDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Pets for query {}", query);
        return petSearchRepository.search(query, pageable).map(petMapper::toDto);
    }
}
