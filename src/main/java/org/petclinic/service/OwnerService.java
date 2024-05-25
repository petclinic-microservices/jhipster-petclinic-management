package org.petclinic.service;

import java.util.Optional;
import org.petclinic.domain.Owner;
import org.petclinic.repository.OwnerRepository;
import org.petclinic.repository.search.OwnerSearchRepository;
import org.petclinic.service.dto.OwnerDTO;
import org.petclinic.service.mapper.OwnerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.petclinic.domain.Owner}.
 */
@Service
@Transactional
public class OwnerService {

    private final Logger log = LoggerFactory.getLogger(OwnerService.class);

    private final OwnerRepository ownerRepository;

    private final OwnerMapper ownerMapper;

    private final OwnerSearchRepository ownerSearchRepository;

    public OwnerService(OwnerRepository ownerRepository, OwnerMapper ownerMapper, OwnerSearchRepository ownerSearchRepository) {
        this.ownerRepository = ownerRepository;
        this.ownerMapper = ownerMapper;
        this.ownerSearchRepository = ownerSearchRepository;
    }

    /**
     * Save a owner.
     *
     * @param ownerDTO the entity to save.
     * @return the persisted entity.
     */
    public OwnerDTO save(OwnerDTO ownerDTO) {
        log.debug("Request to save Owner : {}", ownerDTO);
        Owner owner = ownerMapper.toEntity(ownerDTO);
        owner = ownerRepository.save(owner);
        ownerSearchRepository.index(owner);
        return ownerMapper.toDto(owner);
    }

    /**
     * Update a owner.
     *
     * @param ownerDTO the entity to save.
     * @return the persisted entity.
     */
    public OwnerDTO update(OwnerDTO ownerDTO) {
        log.debug("Request to update Owner : {}", ownerDTO);
        Owner owner = ownerMapper.toEntity(ownerDTO);
        owner = ownerRepository.save(owner);
        ownerSearchRepository.index(owner);
        return ownerMapper.toDto(owner);
    }

    /**
     * Partially update a owner.
     *
     * @param ownerDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<OwnerDTO> partialUpdate(OwnerDTO ownerDTO) {
        log.debug("Request to partially update Owner : {}", ownerDTO);

        return ownerRepository
            .findById(ownerDTO.getId())
            .map(existingOwner -> {
                ownerMapper.partialUpdate(existingOwner, ownerDTO);

                return existingOwner;
            })
            .map(ownerRepository::save)
            .map(savedOwner -> {
                ownerSearchRepository.index(savedOwner);
                return savedOwner;
            })
            .map(ownerMapper::toDto);
    }

    /**
     * Get all the owners.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<OwnerDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Owners");
        return ownerRepository.findAll(pageable).map(ownerMapper::toDto);
    }

    /**
     * Get one owner by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<OwnerDTO> findOne(Long id) {
        log.debug("Request to get Owner : {}", id);
        return ownerRepository.findById(id).map(ownerMapper::toDto);
    }

    /**
     * Delete the owner by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Owner : {}", id);
        ownerRepository.deleteById(id);
        ownerSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the owner corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<OwnerDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Owners for query {}", query);
        return ownerSearchRepository.search(query, pageable).map(ownerMapper::toDto);
    }
}
