package org.petclinic.service;

import java.util.Optional;
import org.petclinic.domain.PetType;
import org.petclinic.repository.PetTypeRepository;
import org.petclinic.repository.search.PetTypeSearchRepository;
import org.petclinic.service.dto.PetTypeDTO;
import org.petclinic.service.mapper.PetTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.petclinic.domain.PetType}.
 */
@Service
@Transactional
public class PetTypeService {

    private final Logger log = LoggerFactory.getLogger(PetTypeService.class);

    private final PetTypeRepository petTypeRepository;

    private final PetTypeMapper petTypeMapper;

    private final PetTypeSearchRepository petTypeSearchRepository;

    public PetTypeService(
        PetTypeRepository petTypeRepository,
        PetTypeMapper petTypeMapper,
        PetTypeSearchRepository petTypeSearchRepository
    ) {
        this.petTypeRepository = petTypeRepository;
        this.petTypeMapper = petTypeMapper;
        this.petTypeSearchRepository = petTypeSearchRepository;
    }

    /**
     * Save a petType.
     *
     * @param petTypeDTO the entity to save.
     * @return the persisted entity.
     */
    public PetTypeDTO save(PetTypeDTO petTypeDTO) {
        log.debug("Request to save PetType : {}", petTypeDTO);
        PetType petType = petTypeMapper.toEntity(petTypeDTO);
        petType = petTypeRepository.save(petType);
        petTypeSearchRepository.index(petType);
        return petTypeMapper.toDto(petType);
    }

    /**
     * Update a petType.
     *
     * @param petTypeDTO the entity to save.
     * @return the persisted entity.
     */
    public PetTypeDTO update(PetTypeDTO petTypeDTO) {
        log.debug("Request to update PetType : {}", petTypeDTO);
        PetType petType = petTypeMapper.toEntity(petTypeDTO);
        petType = petTypeRepository.save(petType);
        petTypeSearchRepository.index(petType);
        return petTypeMapper.toDto(petType);
    }

    /**
     * Partially update a petType.
     *
     * @param petTypeDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<PetTypeDTO> partialUpdate(PetTypeDTO petTypeDTO) {
        log.debug("Request to partially update PetType : {}", petTypeDTO);

        return petTypeRepository
            .findById(petTypeDTO.getId())
            .map(existingPetType -> {
                petTypeMapper.partialUpdate(existingPetType, petTypeDTO);

                return existingPetType;
            })
            .map(petTypeRepository::save)
            .map(savedPetType -> {
                petTypeSearchRepository.index(savedPetType);
                return savedPetType;
            })
            .map(petTypeMapper::toDto);
    }

    /**
     * Get all the petTypes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PetTypeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all PetTypes");
        return petTypeRepository.findAll(pageable).map(petTypeMapper::toDto);
    }

    /**
     * Get one petType by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<PetTypeDTO> findOne(Long id) {
        log.debug("Request to get PetType : {}", id);
        return petTypeRepository.findById(id).map(petTypeMapper::toDto);
    }

    /**
     * Delete the petType by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete PetType : {}", id);
        petTypeRepository.deleteById(id);
        petTypeSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the petType corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<PetTypeDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of PetTypes for query {}", query);
        return petTypeSearchRepository.search(query, pageable).map(petTypeMapper::toDto);
    }
}
