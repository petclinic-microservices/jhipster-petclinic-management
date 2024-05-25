package org.petclinic.service;

import java.util.Optional;
import org.petclinic.domain.Specialty;
import org.petclinic.repository.SpecialtyRepository;
import org.petclinic.repository.search.SpecialtySearchRepository;
import org.petclinic.service.dto.SpecialtyDTO;
import org.petclinic.service.mapper.SpecialtyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.petclinic.domain.Specialty}.
 */
@Service
@Transactional
public class SpecialtyService {

    private final Logger log = LoggerFactory.getLogger(SpecialtyService.class);

    private final SpecialtyRepository specialtyRepository;

    private final SpecialtyMapper specialtyMapper;

    private final SpecialtySearchRepository specialtySearchRepository;

    public SpecialtyService(
        SpecialtyRepository specialtyRepository,
        SpecialtyMapper specialtyMapper,
        SpecialtySearchRepository specialtySearchRepository
    ) {
        this.specialtyRepository = specialtyRepository;
        this.specialtyMapper = specialtyMapper;
        this.specialtySearchRepository = specialtySearchRepository;
    }

    /**
     * Save a specialty.
     *
     * @param specialtyDTO the entity to save.
     * @return the persisted entity.
     */
    public SpecialtyDTO save(SpecialtyDTO specialtyDTO) {
        log.debug("Request to save Specialty : {}", specialtyDTO);
        Specialty specialty = specialtyMapper.toEntity(specialtyDTO);
        specialty = specialtyRepository.save(specialty);
        specialtySearchRepository.index(specialty);
        return specialtyMapper.toDto(specialty);
    }

    /**
     * Update a specialty.
     *
     * @param specialtyDTO the entity to save.
     * @return the persisted entity.
     */
    public SpecialtyDTO update(SpecialtyDTO specialtyDTO) {
        log.debug("Request to update Specialty : {}", specialtyDTO);
        Specialty specialty = specialtyMapper.toEntity(specialtyDTO);
        specialty = specialtyRepository.save(specialty);
        specialtySearchRepository.index(specialty);
        return specialtyMapper.toDto(specialty);
    }

    /**
     * Partially update a specialty.
     *
     * @param specialtyDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SpecialtyDTO> partialUpdate(SpecialtyDTO specialtyDTO) {
        log.debug("Request to partially update Specialty : {}", specialtyDTO);

        return specialtyRepository
            .findById(specialtyDTO.getId())
            .map(existingSpecialty -> {
                specialtyMapper.partialUpdate(existingSpecialty, specialtyDTO);

                return existingSpecialty;
            })
            .map(specialtyRepository::save)
            .map(savedSpecialty -> {
                specialtySearchRepository.index(savedSpecialty);
                return savedSpecialty;
            })
            .map(specialtyMapper::toDto);
    }

    /**
     * Get all the specialties.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<SpecialtyDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Specialties");
        return specialtyRepository.findAll(pageable).map(specialtyMapper::toDto);
    }

    /**
     * Get one specialty by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SpecialtyDTO> findOne(Long id) {
        log.debug("Request to get Specialty : {}", id);
        return specialtyRepository.findById(id).map(specialtyMapper::toDto);
    }

    /**
     * Delete the specialty by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Specialty : {}", id);
        specialtyRepository.deleteById(id);
        specialtySearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the specialty corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<SpecialtyDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Specialties for query {}", query);
        return specialtySearchRepository.search(query, pageable).map(specialtyMapper::toDto);
    }
}
