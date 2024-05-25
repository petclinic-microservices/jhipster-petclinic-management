package org.petclinic.service;

import java.util.Optional;
import org.petclinic.domain.Visit;
import org.petclinic.repository.VisitRepository;
import org.petclinic.repository.search.VisitSearchRepository;
import org.petclinic.service.dto.VisitDTO;
import org.petclinic.service.mapper.VisitMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.petclinic.domain.Visit}.
 */
@Service
@Transactional
public class VisitService {

    private final Logger log = LoggerFactory.getLogger(VisitService.class);

    private final VisitRepository visitRepository;

    private final VisitMapper visitMapper;

    private final VisitSearchRepository visitSearchRepository;

    public VisitService(VisitRepository visitRepository, VisitMapper visitMapper, VisitSearchRepository visitSearchRepository) {
        this.visitRepository = visitRepository;
        this.visitMapper = visitMapper;
        this.visitSearchRepository = visitSearchRepository;
    }

    /**
     * Save a visit.
     *
     * @param visitDTO the entity to save.
     * @return the persisted entity.
     */
    public VisitDTO save(VisitDTO visitDTO) {
        log.debug("Request to save Visit : {}", visitDTO);
        Visit visit = visitMapper.toEntity(visitDTO);
        visit = visitRepository.save(visit);
        visitSearchRepository.index(visit);
        return visitMapper.toDto(visit);
    }

    /**
     * Update a visit.
     *
     * @param visitDTO the entity to save.
     * @return the persisted entity.
     */
    public VisitDTO update(VisitDTO visitDTO) {
        log.debug("Request to update Visit : {}", visitDTO);
        Visit visit = visitMapper.toEntity(visitDTO);
        visit = visitRepository.save(visit);
        visitSearchRepository.index(visit);
        return visitMapper.toDto(visit);
    }

    /**
     * Partially update a visit.
     *
     * @param visitDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<VisitDTO> partialUpdate(VisitDTO visitDTO) {
        log.debug("Request to partially update Visit : {}", visitDTO);

        return visitRepository
            .findById(visitDTO.getId())
            .map(existingVisit -> {
                visitMapper.partialUpdate(existingVisit, visitDTO);

                return existingVisit;
            })
            .map(visitRepository::save)
            .map(savedVisit -> {
                visitSearchRepository.index(savedVisit);
                return savedVisit;
            })
            .map(visitMapper::toDto);
    }

    /**
     * Get all the visits.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<VisitDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Visits");
        return visitRepository.findAll(pageable).map(visitMapper::toDto);
    }

    /**
     * Get all the visits with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<VisitDTO> findAllWithEagerRelationships(Pageable pageable) {
        return visitRepository.findAllWithEagerRelationships(pageable).map(visitMapper::toDto);
    }

    /**
     * Get one visit by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<VisitDTO> findOne(Long id) {
        log.debug("Request to get Visit : {}", id);
        return visitRepository.findOneWithEagerRelationships(id).map(visitMapper::toDto);
    }

    /**
     * Delete the visit by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Visit : {}", id);
        visitRepository.deleteById(id);
        visitSearchRepository.deleteFromIndexById(id);
    }

    /**
     * Search for the visit corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<VisitDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Visits for query {}", query);
        return visitSearchRepository.search(query, pageable).map(visitMapper::toDto);
    }
}
